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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class WebsiteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WebsiteRepository
    private val prefs = application.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        val database = WebsiteDatabase.getDatabase(application)
        repository = WebsiteRepository(database.websiteDao())
        loadCategories()
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

    val websitesList: StateFlow<List<Website>> = repository.allWebsites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    fun saveWebsite(url: String, category: String? = null, onSuccess: () -> Unit) {
        if (url.trim().isBlank()) {
            _saveError.value = "URL cannot be empty"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            val result = repository.fetchAndSave(url, category)
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
    }

    fun updateWebsite(website: Website) {
        viewModelScope.launch {
            repository.update(website)
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
            val existingUrls = existingWebsites.map { it.url.trim().lowercase() }.toSet()

            val toInsert = backupData.websites.filter {
                !existingUrls.contains(it.url.trim().lowercase())
            }

            if (toInsert.isNotEmpty()) {
                repository.insertWebsites(toInsert)
            }

            Result.success(Pair(toInsert.size, newCategoriesCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
