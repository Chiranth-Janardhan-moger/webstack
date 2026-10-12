package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Website
import com.example.data.WebsiteDatabase
import com.example.data.WebsiteRepository
import com.example.ui.util.DEFAULT_CATEGORIES
import android.content.Context
import com.example.data.BackupManager
import com.example.ui.util.normalizeUrlForComparison
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

class WebsiteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WebsiteDatabase.getDatabase(application)
    private val repository: WebsiteRepository
    val websiteDao: com.example.data.WebsiteDao get() = repository.websiteDao
    private val prefs = application.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _cachedScreenshotIds = MutableStateFlow<Set<Long>>(emptySet())
    val cachedScreenshotIds: StateFlow<Set<Long>> = _cachedScreenshotIds.asStateFlow()

    init {
        repository = WebsiteRepository(database.websiteDao())
        loadCategories()
        loadCachedScreenshotIds()
    }

    private fun loadCachedScreenshotIds() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = getApplication<Application>().filesDir
                ?.listFiles { file -> file.name.startsWith("screenshot_") && file.name.endsWith(".jpg") }
                ?.mapNotNull { file ->
                    file.name.removePrefix("screenshot_").removeSuffix(".jpg").toLongOrNull()
                }?.toSet() ?: emptySet()
            _cachedScreenshotIds.value = ids
        }
    }

    fun onScreenshotSaved(id: Long) {
        _cachedScreenshotIds.value = _cachedScreenshotIds.value + id
    }

    private fun loadCategories() {
        val list = prefs.getString("saved_categories_csv", null)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        if (list.isNullOrEmpty()) {
            _categories.value = DEFAULT_CATEGORIES
            saveCategoriesToPrefs(DEFAULT_CATEGORIES)
        } else {
            _categories.value = list
        }
    }

    private fun saveCategoriesToPrefs(list: List<String>) {
        prefs.edit().putString("saved_categories_csv", list.joinToString(",")).apply()
    }

    private fun loadCachedWebsites(): List<Website>? {
        val json = prefs.getString("cached_websites_json", null) ?: return null
        return try {
            val array = JSONArray(json)
            val list = ArrayList<Website>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val url = obj.optString("url", "")
                list.add(
                    Website(
                        id = obj.optLong("id", 0L),
                        url = url,
                        title = obj.optString("title", ""),
                        category = obj.optString("category", "General"),
                        dedupeKey = obj.optString("dedupeKey", com.example.bookmarks.BookmarkNormalizer.dedupeKey(url) ?: url),
                        sourcePath = if (obj.has("sourcePath") && !obj.isNull("sourcePath")) obj.getString("sourcePath") else null,
                        createdAtMs = obj.optLong("createdAtMs", obj.optLong("createdAt", 0L))
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    private fun saveWebsitesToPrefs(websites: List<Website>) {
        try {
            val array = JSONArray()
            // Cap cold-start cache to top 50 bookmarks to prevent OOM and disk bloat on large imports
            for (site in websites.take(50)) {
                val obj = JSONObject()
                obj.put("id", site.id)
                obj.put("url", site.url)
                obj.put("title", site.title)
                obj.put("domain", site.domain)
                obj.put("faviconUrl", site.faviconUrl)
                obj.put("category", site.category)
                obj.put("createdAt", site.createdAt)
                array.put(obj)
            }
            prefs.edit().putString("cached_websites_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank() || trimmed.equals("All", ignoreCase = true)) return false
        val current = _categories.value.toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return false
        current.add(trimmed)
        _categories.value = current
        saveCategoriesToPrefs(current)
        return true
    }

    fun renameCategory(oldName: String, newName: String): Boolean {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || trimmedNew.equals("All", ignoreCase = true)) return false
        val current = _categories.value.toMutableList()
        val index = current.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (index == -1) return false
        if (current.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) }) {
            return false
        }
        current[index] = trimmedNew
        _categories.value = current
        saveCategoriesToPrefs(current)

        viewModelScope.launch {
            repository.renameCategory(oldName, trimmedNew)
        }
        return true
    }

    fun deleteCategory(name: String): Boolean {
        val current = _categories.value.toMutableList()
        val removed = current.removeAll { it.equals(name, ignoreCase = true) }
        if (removed) {
            _categories.value = current
            saveCategoriesToPrefs(current)
            viewModelScope.launch {
                repository.resetCategory(name)
            }
            return true
        }
        return false
    }

    val websitesList: StateFlow<List<Website>?> = repository.allWebsites
        .onEach { saveWebsitesToPrefs(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = loadCachedWebsites()
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    fun findDuplicateUrl(url: String, excludeId: Long? = null): Website? {
        val target = normalizeUrlForComparison(url)
        if (target.isBlank()) return null
        return websitesList.value?.firstOrNull {
            it.id != excludeId && normalizeUrlForComparison(it.url) == target
        }
    }

    fun saveWebsite(url: String, category: String? = null, onSuccess: () -> Unit) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            _saveError.value = "URL cannot be empty"
            return
        }

        val duplicate = findDuplicateUrl(trimmed)
        if (duplicate != null) {
            val titleDisplay = duplicate.title.ifBlank { duplicate.domain }
            _saveError.value = "Link already in your stack: \"$titleDisplay\""
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            val result = repository.fetchAndSave(trimmed, category)
            _isSaving.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _saveError.value = result.exceptionOrNull()?.message ?: "Failed to save website"
            }
        }
    }

    fun refreshScreenshot(id: Long) {
        try {
            java.io.File(getApplication<Application>().filesDir, "screenshot_${id}.jpg").delete()
        } catch (_: Exception) {}
        _cachedScreenshotIds.value = _cachedScreenshotIds.value - id
    }

    fun updateWebsite(website: Website, onResult: ((Boolean, String?) -> Unit)? = null) {
        val duplicate = findDuplicateUrl(website.url, excludeId = website.id)
        if (duplicate != null) {
            val titleDisplay = duplicate.title.ifBlank { duplicate.domain }
            onResult?.invoke(false, "Link already in your stack: \"$titleDisplay\"")
            return
        }
        viewModelScope.launch {
            repository.update(website)
            onResult?.invoke(true, null)
        }
    }

    fun deleteWebsite(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            refreshScreenshot(id)
        }
    }

    fun clearError() {
        _saveError.value = null
    }

    suspend fun exportBackup(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val websites = repository.getAllWebsitesList()
            val categories = _categories.value
            val json = BackupManager.exportToJson(websites, categories)
            outputStream.bufferedWriter().use { writer ->
                writer.write(json)
                writer.flush()
            }
            Result.success(websites.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(inputStream: InputStream): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val parseResult = BackupManager.parseBackup(json)
            if (parseResult.isFailure) {
                return@withContext Result.failure(parseResult.exceptionOrNull() ?: Exception("Invalid backup file"))
            }

            val backupData = parseResult.getOrThrow()

            // 1. Merge categories
            var newCategoriesCount = 0
            val existingCategories = _categories.value.toMutableList()
            backupData.categories.forEach { cat ->
                if (!existingCategories.any { it.equals(cat, ignoreCase = true) }) {
                    existingCategories.add(cat)
                    newCategoriesCount++
                }
            }
            if (newCategoriesCount > 0) {
                _categories.value = existingCategories
                saveCategoriesToPrefs(existingCategories)
            }

            // 2. Merge websites (non-destructive)
            val existingWebsites = repository.getAllWebsitesList()
            val existingUrls = existingWebsites.map { normalizeUrlForComparison(it.url) }.toSet()

            val toInsert = backupData.websites.filter {
                !existingUrls.contains(normalizeUrlForComparison(it.url))
            }

            if (toInsert.isNotEmpty()) {
                repository.insertWebsites(toInsert)
            }

            Result.success(Pair(toInsert.size, newCategoriesCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportHtmlBookmarks(outputStream: OutputStream, nowMs: Long = System.currentTimeMillis()): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            com.example.bookmarks.BookmarkExporter.exportBookmarks(outputStream, repository.websiteDao, nowMs)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun preprocessHtmlBookmarks(inputStream: InputStream): Result<com.example.bookmarks.ImportPreprocessResult> = withContext(Dispatchers.IO) {
        try {
            val taxonomyMap = try {
                getApplication<Application>().assets.open("domain_taxonomy.json").use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    com.example.bookmarks.DomainClassifier.loadTaxonomyFromJson(text)
                }
            } catch (_: Exception) {
                emptyMap()
            }
            val classifier = com.example.bookmarks.DomainClassifier(taxonomyMap)
            val result = com.example.bookmarks.BookmarkImporter.preprocess(inputStream, classifier)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commitImportBookmarks(
        bookmarks: List<com.example.bookmarks.ClassifiedBookmark>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<com.example.bookmarks.ImportResult> = withContext(Dispatchers.IO) {
        try {
            val websites = bookmarks.map { b ->
                Website(
                    url = b.url,
                    title = b.title,
                    category = b.category,
                    dedupeKey = b.dedupeKey,
                    sourcePath = b.sourcePath,
                    createdAtMs = b.createdAtMs
                )
            }
            val result = com.example.bookmarks.BookmarkImporter.importBookmarks(
                db = database,
                dao = repository.websiteDao,
                items = websites,
                onProgress = onProgress
            )

            val newCats = bookmarks.map { it.category }.distinct()
            val current = _categories.value.toMutableList()
            var addedAny = false
            for (c in newCats) {
                if (c.isNotBlank() && !current.any { it.equals(c, ignoreCase = true) }) {
                    current.add(c)
                    addedAny = true
                }
            }
            if (addedAny) {
                _categories.value = current
                saveCategoriesToPrefs(current)
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

