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
import com.example.ui.util.resolveDedupeKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val _parkedCategories = MutableStateFlow<List<String>>(emptyList())
    val parkedCategories: StateFlow<List<String>> = _parkedCategories.asStateFlow()

    private val _fallbackCategory = MutableStateFlow<String>("Personal")
    val fallbackCategory: StateFlow<String> = _fallbackCategory.asStateFlow()

    private val _cachedScreenshotIds = MutableStateFlow<Set<Long>>(emptySet())
    val cachedScreenshotIds: StateFlow<Set<Long>> = _cachedScreenshotIds.asStateFlow()

    private val _settingsSubScreen = MutableStateFlow(com.example.ui.sheets.SettingsSubScreen.MAIN)
    val settingsSubScreen: StateFlow<com.example.ui.sheets.SettingsSubScreen> = _settingsSubScreen.asStateFlow()

    private val _pendingPreprocessResult = MutableStateFlow<com.example.bookmarks.ImportPreprocessResult?>(null)
    val pendingPreprocessResult: StateFlow<com.example.bookmarks.ImportPreprocessResult?> = _pendingPreprocessResult.asStateFlow()

    private val _importProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val importProgress: StateFlow<Pair<Int, Int>?> = _importProgress.asStateFlow()

    private val localServer: com.example.server.LocalHttpServer by lazy {
        com.example.server.LocalHttpServer(
            context = application,
            repository = repository,
            getCategories = { _categories.value }
        )
    }
    val serverStatus: StateFlow<com.example.server.ServerStatus> get() = localServer.status

    fun startLocalServer(port: Int = 8080) {
        localServer.start(port)
    }

    fun stopLocalServer() {
        localServer.stop()
    }

    fun setSettingsSubScreen(subScreen: com.example.ui.sheets.SettingsSubScreen) {
        _settingsSubScreen.value = subScreen
    }

    fun setPendingPreprocessResult(result: com.example.bookmarks.ImportPreprocessResult?) {
        _pendingPreprocessResult.value = result
    }

    fun clearPendingPreprocessResult() {
        _pendingPreprocessResult.value = null
    }

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
        val ensuredList = if (list.isNullOrEmpty()) {
            DEFAULT_CATEGORIES
        } else if (!list.any { it.equals("Personal", ignoreCase = true) }) {
            listOf("Personal") + list
        } else {
            list
        }
        _categories.value = ensuredList
        saveCategoriesToPrefs(ensuredList)

        val parkedList = prefs.getString("parked_categories_csv", null)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.filterNot { it.equals("Personal", ignoreCase = true) } ?: emptyList()
        _parkedCategories.value = parkedList
        if (parkedList.size != (prefs.getString("parked_categories_csv", null)?.split(",")?.size ?: 0)) {
            saveParkedCategoriesToPrefs(parkedList)
        }

        val fallback = prefs.getString("fallback_deleted_category", "Personal")?.trim()?.takeIf { it.isNotBlank() } ?: "Personal"
        _fallbackCategory.value = fallback
    }

    fun setFallbackCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            _fallbackCategory.value = trimmed
            prefs.edit().putString("fallback_deleted_category", trimmed).apply()
        }
    }

    private fun saveCategoriesToPrefs(list: List<String>) {
        prefs.edit().putString("saved_categories_csv", list.joinToString(",")).apply()
    }

    private fun saveParkedCategoriesToPrefs(list: List<String>) {
        prefs.edit().putString("parked_categories_csv", list.joinToString(",")).apply()
    }

    private fun loadCachedWebsites(): List<Website>? {
        val json = prefs.getString("cached_websites_json", null) ?: return null
        return try {
            val array = JSONArray(json)
            val list = ArrayList<Website>(array.length())
            val seenIds = HashSet<Long>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optLong("id", 0L)
                if (id <= 0L || !seenIds.add(id)) {
                    // Invalid or duplicate ID: drop the entire cache for this launch and let Room populate
                    prefs.edit().remove("cached_websites_json").apply()
                    return null
                }
                val url = obj.optString("url", "")
                list.add(
                    Website(
                        id = id,
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
            prefs.edit().remove("cached_websites_json").apply()
            null
        }
    }

    private fun saveWebsitesToPrefs(websites: List<Website>) {
        try {
            val array = JSONArray()
            // Cap cold-start cache to top 50 bookmarks to prevent OOM and disk bloat on large imports
            val valid = websites.filter { it.id > 0L }.distinctBy { it.id }.take(50)
            for (site in valid) {
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
        val parked = _parkedCategories.value.toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) } || parked.any { it.equals(trimmed, ignoreCase = true) }) {
            return false
        }
        current.add(trimmed)
        _categories.value = current
        saveCategoriesToPrefs(current)
        return true
    }

    fun renameCategory(oldName: String, newName: String): Boolean {
        if (oldName.equals("Personal", ignoreCase = true)) return false
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || trimmedNew.equals("All", ignoreCase = true) || trimmedNew.equals("Personal", ignoreCase = true)) return false
        val current = _categories.value.toMutableList()
        val parked = _parkedCategories.value.toMutableList()
        val indexActive = current.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        val indexParked = parked.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (indexActive == -1 && indexParked == -1) return false
        if (current.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) } ||
            parked.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) }) {
            return false
        }
        if (indexActive != -1) {
            current[indexActive] = trimmedNew
            _categories.value = current
            saveCategoriesToPrefs(current)
        }
        if (indexParked != -1) {
            parked[indexParked] = trimmedNew
            _parkedCategories.value = parked
            saveParkedCategoriesToPrefs(parked)
        }

        if (oldName.equals(_fallbackCategory.value, ignoreCase = true)) {
            setFallbackCategory(trimmedNew)
        }

        viewModelScope.launch {
            repository.renameCategory(oldName, trimmedNew)
        }
        return true
    }

    fun deleteCategory(name: String, fallbackOverride: String? = null): Boolean {
        if (name.equals("Personal", ignoreCase = true)) {
            return false
        }
        val current = _categories.value.toMutableList()
        val parked = _parkedCategories.value.toMutableList()
        val removedActive = current.removeAll { it.equals(name, ignoreCase = true) }
        val removedParked = parked.removeAll { it.equals(name, ignoreCase = true) }
        if (removedActive || removedParked) {
            if (removedActive) {
                _categories.value = current
                saveCategoriesToPrefs(current)
            }
            if (removedParked) {
                _parkedCategories.value = parked
                saveParkedCategoriesToPrefs(parked)
            }

            var target = fallbackOverride?.trim()?.takeIf { it.isNotBlank() } ?: _fallbackCategory.value
            if (target.equals(name, ignoreCase = true)) {
                target = current.firstOrNull() ?: "Personal"
                setFallbackCategory(target)
            }

            viewModelScope.launch {
                repository.resetCategory(name, target)
            }
            return true
        }
        return false
    }

    fun updateCategoriesOrder(newActive: List<String>) {
        _categories.value = newActive
        saveCategoriesToPrefs(newActive)
    }

    fun parkCategory(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.equals("Personal", ignoreCase = true)) {
            return
        }
        val active = _categories.value.toMutableList()
        val parked = _parkedCategories.value.toMutableList()
        val removed = active.removeAll { it.equals(trimmed, ignoreCase = true) }
        if (removed) {
            if (!parked.any { it.equals(trimmed, ignoreCase = true) }) {
                parked.add(trimmed)
            }
            _categories.value = active
            _parkedCategories.value = parked
            saveCategoriesToPrefs(active)
            saveParkedCategoriesToPrefs(parked)
        }
    }

    fun unparkCategory(tag: String, toIndex: Int? = null) {
        val trimmed = tag.trim()
        val active = _categories.value.toMutableList()
        val parked = _parkedCategories.value.toMutableList()
        val removed = parked.removeAll { it.equals(trimmed, ignoreCase = true) }
        if (removed) {
            if (!active.any { it.equals(trimmed, ignoreCase = true) }) {
                if (toIndex != null && toIndex in 0..active.size) {
                    active.add(toIndex, trimmed)
                } else {
                    active.add(trimmed)
                }
            }
            _categories.value = active
            _parkedCategories.value = parked
            saveCategoriesToPrefs(active)
            saveParkedCategoriesToPrefs(parked)
        }
    }

    fun reorderActiveCategories(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val active = _categories.value.toMutableList()
        if (fromIndex in active.indices && toIndex in active.indices) {
            val item = active.removeAt(fromIndex)
            active.add(toIndex, item)
            _categories.value = active
            saveCategoriesToPrefs(active)
        }
    }

    fun resetCategoriesToDefaults() {
        _categories.value = DEFAULT_CATEGORIES
        _parkedCategories.value = emptyList()
        _fallbackCategory.value = "Personal"
        saveCategoriesToPrefs(DEFAULT_CATEGORIES)
        saveParkedCategoriesToPrefs(emptyList())
        prefs.edit().putString("fallback_deleted_category", "Personal").apply()
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
        val targetKey = resolveDedupeKey(url)
        if (targetKey.isBlank()) return null
        return websitesList.value?.firstOrNull {
            it.id != excludeId && it.dedupeKey == targetKey
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
                val savedWebsite = result.getOrThrow()
                onSuccess()
                launch(Dispatchers.IO) {
                    repository.fetchTitleAsync(savedWebsite.id, savedWebsite.url, savedWebsite.domain)
                }
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

    fun deleteWebsites(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWebsites(ids)
            val filesDir = getApplication<Application>().filesDir
            ids.forEach { id ->
                try {
                    java.io.File(filesDir, "screenshot_${id}.jpg").delete()
                } catch (_: Exception) {}
            }
            val idSet = ids.toHashSet()
            _cachedScreenshotIds.update { it - idSet }
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
            val existingKeys = existingWebsites.map { it.dedupeKey }.toSet()

            val toInsert = backupData.websites.filter {
                !existingKeys.contains(it.dedupeKey)
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
            _importProgress.value = Pair(0, bookmarks.size)
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
                onProgress = { done, total ->
                    _importProgress.value = Pair(done, total)
                    onProgress(done, total)
                }
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
            val persisted = repository.getAllWebsitesList()
            saveWebsitesToPrefs(persisted)

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _importProgress.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        localServer.stop()
    }
}

