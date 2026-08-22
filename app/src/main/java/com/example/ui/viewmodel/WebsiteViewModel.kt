package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Website
import com.example.data.WebsiteDatabase
import com.example.data.WebsiteRepository
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WebsiteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WebsiteRepository
    private val prefs = application.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)

    private val defaultCategories = listOf("Personal", "Design", "Tools", "Work", "Reading")

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        val database = WebsiteDatabase.getDatabase(application)
        repository = WebsiteRepository(database.websiteDao())
        loadCategories()
    }

    private fun loadCategories() {
        val saved = prefs.getString("saved_categories_csv", null)
        if (saved.isNullOrBlank()) {
            _categories.value = defaultCategories
            saveCategoriesToPrefs(defaultCategories)
        } else {
            val list = saved.split(",").map { it.trim() }.filter { it.isNotBlank() }
            _categories.value = if (list.isNotEmpty()) list else defaultCategories
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
            val file = java.io.File(getApplication<Application>().filesDir, "screenshot_${id}.jpg")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateWebsite(website: Website) {
        viewModelScope.launch {
            repository.update(website)
        }
    }

    fun deleteWebsite(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            try {
                val file = java.io.File(getApplication<Application>().filesDir, "screenshot_${id}.jpg")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _saveError.value = null
    }
}

class WebsiteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebsiteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WebsiteViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
