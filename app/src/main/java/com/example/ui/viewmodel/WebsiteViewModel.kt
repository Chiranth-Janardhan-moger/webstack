package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AutoBackupManager
import com.example.data.BackupManager
import com.example.data.Website
import com.example.data.WebsiteDatabase
import com.example.data.WebsiteRepository
import com.example.ui.model.FavoriteBadgeShape
import com.example.ui.model.FavoriteColorPreset
import com.example.ui.model.FavoriteIconType
import com.example.ui.util.DEFAULT_CATEGORIES
import com.example.ui.util.resolveDedupeKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val _lockedCategories = MutableStateFlow<List<String>>(emptyList())
    val lockedCategories: StateFlow<List<String>> = _lockedCategories.asStateFlow()

    private val _unlockedCategories = MutableStateFlow<Set<String>>(emptySet())
    val unlockedCategories: StateFlow<Set<String>> = _unlockedCategories.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean("app_lock_enabled", false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(!prefs.getBoolean("app_lock_enabled", false))
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _fallbackCategory = MutableStateFlow<String>("Personal")
    val fallbackCategory: StateFlow<String> = _fallbackCategory.asStateFlow()

    private val _autoBackupEnabled = MutableStateFlow(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    private val _autoBackupFolderUri = MutableStateFlow<String?>(null)
    val autoBackupFolderUri: StateFlow<String?> = _autoBackupFolderUri.asStateFlow()

    private val _autoBackupFolderName = MutableStateFlow<String>("")
    val autoBackupFolderName: StateFlow<String> = _autoBackupFolderName.asStateFlow()

    private val _autoBackupLastTimestamp = MutableStateFlow(0L)
    val autoBackupLastTimestamp: StateFlow<Long> = _autoBackupLastTimestamp.asStateFlow()

    private val _autoBackupError = MutableStateFlow<String?>(null)
    val autoBackupError: StateFlow<String?> = _autoBackupError.asStateFlow()

    private val _isAutoBackingUp = MutableStateFlow(false)
    val isAutoBackingUp: StateFlow<Boolean> = _isAutoBackingUp.asStateFlow()

    private val autoBackupMutex = Mutex()

    private val _cachedScreenshotIds = MutableStateFlow<Set<Long>>(emptySet())
    val cachedScreenshotIds: StateFlow<Set<Long>> = _cachedScreenshotIds.asStateFlow()

    private val favoriteLock = Any()
    private val _favoriteWebsiteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteWebsiteIds: StateFlow<Set<Long>> = _favoriteWebsiteIds.asStateFlow()

    private val _favoriteIconType = MutableStateFlow(FavoriteIconType.HEART)
    val favoriteIconType: StateFlow<FavoriteIconType> = _favoriteIconType.asStateFlow()

    private val _favoriteBadgeShape = MutableStateFlow(FavoriteBadgeShape.PURE_FLOAT)
    val favoriteBadgeShape: StateFlow<FavoriteBadgeShape> = _favoriteBadgeShape.asStateFlow()

    private val _favoriteColorPreset = MutableStateFlow(FavoriteColorPreset.CRIMSON_PINK)
    val favoriteColorPreset: StateFlow<FavoriteColorPreset> = _favoriteColorPreset.asStateFlow()

    private val _settingsSubScreen = MutableStateFlow(com.example.ui.sheets.SettingsSubScreen.MAIN)
    val settingsSubScreen: StateFlow<com.example.ui.sheets.SettingsSubScreen> = _settingsSubScreen.asStateFlow()

    private val _pendingPreprocessResult = MutableStateFlow<com.example.bookmarks.ImportPreprocessResult?>(null)
    val pendingPreprocessResult: StateFlow<com.example.bookmarks.ImportPreprocessResult?> = _pendingPreprocessResult.asStateFlow()

    private val _importProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val importProgress: StateFlow<Pair<Int, Int>?> = _importProgress.asStateFlow()

    private val _companionPort = MutableStateFlow(prefs.getInt(PREF_COMPANION_PORT, DEFAULT_COMPANION_PORT))
    val companionPort: StateFlow<Int> = _companionPort.asStateFlow()

    private val _companionUseFixedPin = MutableStateFlow(prefs.getBoolean(PREF_COMPANION_USE_FIXED_PIN, false))
    val companionUseFixedPin: StateFlow<Boolean> = _companionUseFixedPin.asStateFlow()

    private val _companionCustomPin = MutableStateFlow(
        prefs.getString(PREF_COMPANION_CUSTOM_PIN, "")?.filter { it.isDigit() }?.take(6) ?: ""
    )
    val companionCustomPin: StateFlow<String> = _companionCustomPin.asStateFlow()

    private val _companionOfflineExpiryHours = MutableStateFlow(prefs.getInt(PREF_COMPANION_OFFLINE_EXPIRY_HOURS, DEFAULT_COMPANION_OFFLINE_EXPIRY_HOURS))
    val companionOfflineExpiryHours: StateFlow<Int> = _companionOfflineExpiryHours.asStateFlow()

    private val localServer: com.example.server.LocalHttpServer by lazy {
        com.example.server.LocalHttpServer(
            context = application,
            repository = repository,
            getCategories = { _categories.value },
            getOfflineExpiryHours = { _companionOfflineExpiryHours.value },
            getLockedCategories = { _lockedCategories.value }
        )
    }
    val serverStatus: StateFlow<com.example.server.ServerStatus> get() = localServer.status

    fun setCompanionPort(port: Int) {
        val validPort = port.coerceIn(1024, 65535)
        prefs.edit().putInt(PREF_COMPANION_PORT, validPort).apply()
        _companionPort.value = validPort
    }

    fun setCompanionUseFixedPin(useFixed: Boolean) {
        prefs.edit().putBoolean(PREF_COMPANION_USE_FIXED_PIN, useFixed).apply()
        _companionUseFixedPin.value = useFixed
    }

    fun setCompanionCustomPin(pin: String) {
        val cleanPin = pin.filter { it.isDigit() }.take(6)
        prefs.edit().putString(PREF_COMPANION_CUSTOM_PIN, cleanPin).apply()
        _companionCustomPin.value = cleanPin
    }

    fun setCompanionOfflineExpiryHours(hours: Int) {
        val validHours = if (hours in listOf(24, 168, 720, 0)) hours else DEFAULT_COMPANION_OFFLINE_EXPIRY_HOURS
        prefs.edit().putInt(PREF_COMPANION_OFFLINE_EXPIRY_HOURS, validHours).apply()
        _companionOfflineExpiryHours.value = validHours
    }

    fun startLocalServer(
        port: Int = _companionPort.value,
        customPin: String? = if (_companionUseFixedPin.value && _companionCustomPin.value.length == 6 && _companionCustomPin.value.all { it.isDigit() }) _companionCustomPin.value else null
    ) {
        localServer.start(port, customPin)
    }

    fun stopLocalServer() {
        localServer.stop()
    }

    fun revokeCompanionSession() {
        localServer.revokeSession()
    }

    fun restartLocalServer() {
        stopLocalServer()
        startLocalServer()
    }

    val websiteRepository: WebsiteRepository get() = repository

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
        loadAutoBackupPrefs()
        loadCachedScreenshotIds()
        loadFavorites()
        loadFavoriteCustomization()
        startAutoBackupObserver()
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

    private fun loadFavoriteCustomization() {
        val iconId = prefs.getString("favorite_icon_type", FavoriteIconType.HEART.id)
        val shapeId = prefs.getString("favorite_badge_shape", FavoriteBadgeShape.PURE_FLOAT.id)
        val colorId = prefs.getString("favorite_color_preset", FavoriteColorPreset.CRIMSON_PINK.id)
        _favoriteIconType.value = FavoriteIconType.fromId(iconId)
        _favoriteBadgeShape.value = FavoriteBadgeShape.fromId(shapeId)
        _favoriteColorPreset.value = FavoriteColorPreset.fromId(colorId)
    }

    fun setFavoriteIconType(icon: FavoriteIconType) {
        _favoriteIconType.value = icon
        prefs.edit().putString("favorite_icon_type", icon.id).apply()
    }

    fun setFavoriteBadgeShape(shape: FavoriteBadgeShape) {
        _favoriteBadgeShape.value = shape
        prefs.edit().putString("favorite_badge_shape", shape.id).apply()
    }

    fun setFavoriteColorPreset(color: FavoriteColorPreset) {
        _favoriteColorPreset.value = color
        prefs.edit().putString("favorite_color_preset", color.id).apply()
    }

    private fun loadFavorites() {
        val ids = prefs.getString("favorite_website_ids_csv", null)
            ?.split(",")
            ?.mapNotNull { it.trim().toLongOrNull() }
            ?.toSet() ?: emptySet()
        _favoriteWebsiteIds.value = ids
    }

    fun toggleFavorite(id: Long) {
        synchronized(favoriteLock) {
            val current = _favoriteWebsiteIds.value
            val updated = if (current.contains(id)) current - id else current + id
            _favoriteWebsiteIds.value = updated
            prefs.edit().putString("favorite_website_ids_csv", updated.joinToString(",")).apply()
        }
    }

    fun setFavorite(id: Long, isFavorite: Boolean) {
        synchronized(favoriteLock) {
            val current = _favoriteWebsiteIds.value
            val updated = if (isFavorite) current + id else current - id
            if (updated != current) {
                _favoriteWebsiteIds.value = updated
                prefs.edit().putString("favorite_website_ids_csv", updated.joinToString(",")).apply()
            }
        }
    }

    fun setWebsitesFavorite(ids: Collection<Long>, isFavorite: Boolean) {
        if (ids.isEmpty()) return
        synchronized(favoriteLock) {
            val current = _favoriteWebsiteIds.value
            val updated = if (isFavorite) current + ids else current - ids.toSet()
            if (updated != current) {
                _favoriteWebsiteIds.value = updated
                prefs.edit().putString("favorite_website_ids_csv", updated.joinToString(",")).apply()
            }
        }
    }

    fun toggleWebsitesFavorite(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        synchronized(favoriteLock) {
            val current = _favoriteWebsiteIds.value
            val allFavorited = ids.all { current.contains(it) }
            setWebsitesFavorite(ids, !allFavorited)
        }
    }

    private fun loadCategories() {
        val lockedList = prefs.getString("locked_categories_csv", null)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.filterNot { it.equals("Personal", ignoreCase = true) || it.equals("All", ignoreCase = true) }
            ?.distinctBy { it.lowercase() }
            ?: emptyList()
        _lockedCategories.value = lockedList
        val lockedSet = lockedList.map { it.lowercase() }.toSet()

        val list = prefs.getString("saved_categories_csv", null)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.filterNot { lockedSet.contains(it.lowercase()) }
        val ensuredList = if (list.isNullOrEmpty()) {
            DEFAULT_CATEGORIES.filterNot { lockedSet.contains(it.lowercase()) }
        } else if (!list.any { it.equals("Personal", ignoreCase = true) }) {
            listOf("Personal") + list
        } else {
            list
        }
        _categories.value = ensuredList
        saveCategoriesToPrefs(ensuredList)

        val parkedList = prefs.getString("parked_categories_csv", null)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.filterNot { it.equals("Personal", ignoreCase = true) || lockedSet.contains(it.lowercase()) }
            ?: emptyList()
        _parkedCategories.value = parkedList
        if (parkedList.size != (prefs.getString("parked_categories_csv", null)?.split(",")?.size ?: 0)) {
            saveParkedCategoriesToPrefs(parkedList)
        }

        val fallback = prefs.getString("fallback_deleted_category", "Personal")
            ?.trim()
            ?.takeIf { it.isNotBlank() && !lockedSet.contains(it.lowercase()) }
            ?: "Personal"
        _fallbackCategory.value = fallback
    }

    private fun loadAutoBackupPrefs() {
        val uriStr = prefs.getString("auto_backup_folder_uri", null)?.takeIf { it.isNotBlank() }
        val enabled = prefs.getBoolean("auto_backup_enabled", false) && uriStr != null
        _autoBackupFolderUri.value = uriStr
        _autoBackupFolderName.value = prefs.getString("auto_backup_folder_name", "") ?: ""
        _autoBackupLastTimestamp.value = prefs.getLong("auto_backup_last_timestamp", 0L)
        if (enabled && !AutoBackupManager.hasPersistedWritePermission(getApplication(), Uri.parse(uriStr!!))) {
            _autoBackupError.value = "Folder unavailable — tap to reselect"
        } else {
            _autoBackupError.value = null
        }
        _autoBackupEnabled.value = enabled
    }

    fun enableAutoBackupWithFolder(treeUri: Uri, persistPermission: Boolean = true) {
        val app = getApplication<Application>()
        if (persistPermission) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                app.contentResolver.takePersistableUriPermission(treeUri, flags)
            } catch (_: SecurityException) {}
        }
        val folderName = AutoBackupManager.formatFolderDisplayName(treeUri)
        val uriStr = treeUri.toString()
        prefs.edit()
            .putString("auto_backup_folder_uri", uriStr)
            .putString("auto_backup_folder_name", folderName)
            .putBoolean("auto_backup_enabled", true)
            .apply()
        _autoBackupFolderUri.value = uriStr
        _autoBackupFolderName.value = folderName
        _autoBackupError.value = null
        _autoBackupEnabled.value = true
        triggerManualAutoBackup()
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        if (enabled && _autoBackupFolderUri.value.isNullOrBlank()) {
            return
        }
        _autoBackupEnabled.value = enabled
        prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
        if (enabled) {
            _autoBackupError.value = null
            triggerManualAutoBackup()
        }
    }

    fun triggerManualAutoBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            performAutoBackupInternal()
        }
    }

    @OptIn(FlowPreview::class)
    private fun startAutoBackupObserver() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.allWebsites,
                _categories,
                _parkedCategories,
                _lockedCategories
            ) { websites, activeCats, parkedCats, lockedCats ->
                val maxCreated = websites.maxOfOrNull { it.createdAtMs } ?: 0L
                val titleHash = websites.fold(0) { acc, w -> 31 * acc + w.id.hashCode() + w.title.hashCode() + w.category.hashCode() + w.url.hashCode() }
                Triple(websites.size to maxCreated, titleHash, Triple(activeCats, parkedCats, lockedCats))
            }
                .distinctUntilChanged()
                .drop(1)
                .debounce(3000L)
                .collect {
                    if (_autoBackupEnabled.value && !_autoBackupFolderUri.value.isNullOrBlank()) {
                        performAutoBackupInternal()
                    }
                }
        }
    }

    private suspend fun performAutoBackupInternal() {
        val uriStr = _autoBackupFolderUri.value ?: return
        if (!_autoBackupEnabled.value || uriStr.isBlank()) return

        autoBackupMutex.withLock {
            if (!_autoBackupEnabled.value) return
            _isAutoBackingUp.value = true
            try {
                val websites = repository.getAllWebsitesList()
                val exportCats = (_categories.value + _lockedCategories.value).distinctBy { it.lowercase() }
                val json = BackupManager.exportToJson(websites, exportCats, _lockedCategories.value)
                val treeUri = Uri.parse(uriStr)
                val writeResult = AutoBackupManager.writeBackupToDirectory(getApplication(), treeUri, json)
                writeResult.onSuccess {
                    val now = System.currentTimeMillis()
                    prefs.edit().putLong("auto_backup_last_timestamp", now).apply()
                    getApplication<Application>().getSharedPreferences("webstack_settings", Context.MODE_PRIVATE)
                        .edit().putLong("last_backup_timestamp", now).apply()
                    _autoBackupLastTimestamp.value = now
                    _autoBackupError.value = null
                }.onFailure {
                    _autoBackupError.value = "Folder unavailable — tap to reselect"
                }
            } catch (_: Exception) {
                _autoBackupError.value = "Folder unavailable — tap to reselect"
            } finally {
                _isAutoBackingUp.value = false
            }
        }
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

    private fun saveLockedCategoriesToPrefs(list: List<String>) {
        prefs.edit().putString("locked_categories_csv", list.joinToString(",")).apply()
    }

    fun isCategoryLocked(tag: String): Boolean {
        return _lockedCategories.value.any { it.equals(tag.trim(), ignoreCase = true) }
    }

    fun isCategorySessionUnlocked(tag: String): Boolean {
        return _unlockedCategories.value.contains(tag.trim().lowercase())
    }

    fun unlockCategoryForSession(tag: String) {
        val key = tag.trim().lowercase()
        if (key.isNotBlank()) {
            _unlockedCategories.update { it + key }
        }
    }

    fun lockAllSessionCategories() {
        if (_unlockedCategories.value.isNotEmpty()) {
            _unlockedCategories.value = emptySet()
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock_enabled", enabled).apply()
        _appLockEnabled.value = enabled
        _isAppUnlocked.value = true
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        if (_appLockEnabled.value) {
            _isAppUnlocked.value = false
        }
    }

    fun lockCategory(tag: String): Boolean {
        val trimmed = tag.trim()
        if (trimmed.isBlank() || trimmed.equals("Personal", ignoreCase = true) || trimmed.equals("All", ignoreCase = true)) {
            return false
        }
        val active = _categories.value.toMutableList()
        val parked = _parkedCategories.value.toMutableList()
        val locked = _lockedCategories.value.toMutableList()
        if (locked.any { it.equals(trimmed, ignoreCase = true) }) {
            return false
        }
        val removedActive = active.removeAll { it.equals(trimmed, ignoreCase = true) }
        val removedParked = parked.removeAll { it.equals(trimmed, ignoreCase = true) }
        if (!removedActive && !removedParked) {
            return false
        }
        locked.add(trimmed)
        if (removedActive) {
            _categories.value = active
            saveCategoriesToPrefs(active)
        }
        if (removedParked) {
            _parkedCategories.value = parked
            saveParkedCategoriesToPrefs(parked)
        }
        _lockedCategories.value = locked
        saveLockedCategoriesToPrefs(locked)
        _unlockedCategories.update { it - trimmed.lowercase() }
        if (_fallbackCategory.value.equals(trimmed, ignoreCase = true)) {
            val replacement = active.firstOrNull() ?: "Personal"
            setFallbackCategory(replacement)
        }
        return true
    }

    fun unlockCategoryPermanently(tag: String) {
        val trimmed = tag.trim()
        val locked = _lockedCategories.value.toMutableList()
        val active = _categories.value.toMutableList()
        val removed = locked.removeAll { it.equals(trimmed, ignoreCase = true) }
        if (removed) {
            if (!active.any { it.equals(trimmed, ignoreCase = true) }) {
                active.add(trimmed)
            }
            _lockedCategories.value = locked
            _categories.value = active
            saveLockedCategoriesToPrefs(locked)
            saveCategoriesToPrefs(active)
            _unlockedCategories.update { it - trimmed.lowercase() }
        }
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
        val locked = _lockedCategories.value
        if (current.any { it.equals(trimmed, ignoreCase = true) } ||
            parked.any { it.equals(trimmed, ignoreCase = true) } ||
            locked.any { it.equals(trimmed, ignoreCase = true) }
        ) {
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
        val locked = _lockedCategories.value.toMutableList()
        val indexActive = current.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        val indexParked = parked.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        val indexLocked = locked.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (indexActive == -1 && indexParked == -1 && indexLocked == -1) return false
        if (current.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) } ||
            parked.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) } ||
            locked.any { it.equals(trimmedNew, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) }
        ) {
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
        if (indexLocked != -1) {
            locked[indexLocked] = trimmedNew
            _lockedCategories.value = locked
            saveLockedCategoriesToPrefs(locked)
            _unlockedCategories.update { set ->
                if (set.contains(oldName.lowercase())) set - oldName.lowercase() + trimmedNew.lowercase() else set
            }
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
        val locked = _lockedCategories.value.toMutableList()
        val removedActive = current.removeAll { it.equals(name, ignoreCase = true) }
        val removedParked = parked.removeAll { it.equals(name, ignoreCase = true) }
        val removedLocked = locked.removeAll { it.equals(name, ignoreCase = true) }
        if (removedActive || removedParked || removedLocked) {
            if (removedActive) {
                _categories.value = current
                saveCategoriesToPrefs(current)
            }
            if (removedParked) {
                _parkedCategories.value = parked
                saveParkedCategoriesToPrefs(parked)
            }
            if (removedLocked) {
                _lockedCategories.value = locked
                saveLockedCategoriesToPrefs(locked)
                _unlockedCategories.update { it - name.lowercase() }
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
        val lockedSet = _lockedCategories.value.map { it.lowercase() }.toSet()
        val restoredDefaults = DEFAULT_CATEGORIES.filterNot { lockedSet.contains(it.lowercase()) }
        _categories.value = restoredDefaults
        _parkedCategories.value = emptyList()
        _fallbackCategory.value = "Personal"
        saveCategoriesToPrefs(restoredDefaults)
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

    private fun formatDuplicateError(duplicate: Website): String {
        return if (isCategoryLocked(duplicate.category) && !isCategorySessionUnlocked(duplicate.category)) {
            "Link already saved in a locked tag"
        } else {
            val titleDisplay = duplicate.title.ifBlank { duplicate.domain }
            "Link already in your stack: \"$titleDisplay\""
        }
    }

    fun saveWebsite(url: String, category: String? = null, isFavorite: Boolean = false, onSuccess: () -> Unit) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            _saveError.value = "URL cannot be empty"
            return
        }

        val duplicate = findDuplicateUrl(trimmed)
        if (duplicate != null) {
            _saveError.value = formatDuplicateError(duplicate)
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            val dbDuplicate = withContext(Dispatchers.IO) { repository.findDuplicate(trimmed) }
            if (dbDuplicate != null) {
                _isSaving.value = false
                _saveError.value = formatDuplicateError(dbDuplicate)
                return@launch
            }
            val result = repository.fetchAndSave(trimmed, category)
            _isSaving.value = false
            if (result.isSuccess) {
                val savedWebsite = result.getOrThrow()
                if (isFavorite) {
                    setFavorite(savedWebsite.id, true)
                }
                onSuccess()
                launch(Dispatchers.IO) {
                    repository.fetchTitleAsync(savedWebsite.id, savedWebsite.url, savedWebsite.domain)
                }
            } else {
                _saveError.value = result.exceptionOrNull()?.message ?: "Failed to save website"
            }
        }
    }

    private val _activeRefreshingIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Long>()

    fun refreshScreenshot(id: Long) {
        try {
            java.io.File(getApplication<Application>().filesDir, "screenshot_${id}.jpg").delete()
        } catch (_: Exception) {}
        _cachedScreenshotIds.value = _cachedScreenshotIds.value - id
    }

    fun refreshWebsite(website: Website) {
        refreshScreenshot(website.id)
        if (com.example.ui.util.isPlaceholderTitle(website.title, website.domain, website.url)) {
            if (!_activeRefreshingIds.add(website.id)) return
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.fetchTitleAsync(
                        id = website.id,
                        url = website.url,
                        domain = website.domain,
                        fallbackToDomainOnFailure = false
                    )
                } finally {
                    _activeRefreshingIds.remove(website.id)
                }
            }
        }
    }

    fun updateWebsite(website: Website, onResult: ((Boolean, String?) -> Unit)? = null) {
        val duplicate = findDuplicateUrl(website.url, excludeId = website.id)
        if (duplicate != null) {
            onResult?.invoke(false, formatDuplicateError(duplicate))
            return
        }
        viewModelScope.launch {
            repository.update(website)
            onResult?.invoke(true, null)
        }
    }

    fun deleteWebsite(id: Long) {
        setFavorite(id, false)
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
            synchronized(favoriteLock) {
                val currentFavs = _favoriteWebsiteIds.value
                val updatedFavs = currentFavs - idSet
                if (updatedFavs != currentFavs) {
                    _favoriteWebsiteIds.value = updatedFavs
                    prefs.edit().putString("favorite_website_ids_csv", updatedFavs.joinToString(",")).apply()
                }
            }
        }
    }

    fun moveWebsitesToCategory(ids: List<Long>, newCategory: String) {
        if (ids.isEmpty() || newCategory.isBlank()) return
        val trimmed = newCategory.trim()
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWebsitesCategory(ids, trimmed)
        }
    }

    fun clearError() {
        _saveError.value = null
    }

    suspend fun exportBackup(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val websites = repository.getAllWebsitesList()
            val categories = (_categories.value + _lockedCategories.value).distinctBy { it.lowercase() }
            val json = BackupManager.exportToJson(websites, categories, _lockedCategories.value)
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

            // 1. Merge locked & active categories cleanly
            var newCategoriesCount = 0
            val lockedCats = _lockedCategories.value.toMutableList()
            val parkedCats = _parkedCategories.value.toMutableList()
            val existingCategories = _categories.value.toMutableList()
            var lockedChanged = false
            var parkedChanged = false

            backupData.lockedCategories.forEach { lockedCat ->
                if (!lockedCats.any { it.equals(lockedCat, ignoreCase = true) }) {
                    lockedCats.add(lockedCat)
                    lockedChanged = true
                    newCategoriesCount++
                }
                if (existingCategories.removeAll { it.equals(lockedCat, ignoreCase = true) }) {
                    // Moved from active into locked cleanly
                }
                if (parkedCats.removeAll { it.equals(lockedCat, ignoreCase = true) }) {
                    parkedChanged = true
                }
            }

            val lockedSet = lockedCats.map { it.lowercase() }.toSet()
            backupData.categories.forEach { cat ->
                if (!lockedSet.contains(cat.lowercase()) &&
                    !existingCategories.any { it.equals(cat, ignoreCase = true) } &&
                    !parkedCats.any { it.equals(cat, ignoreCase = true) }
                ) {
                    existingCategories.add(cat)
                    newCategoriesCount++
                }
            }

            if (lockedChanged) {
                _lockedCategories.value = lockedCats
                saveLockedCategoriesToPrefs(lockedCats)
            }
            if (parkedChanged) {
                _parkedCategories.value = parkedCats
                saveParkedCategoriesToPrefs(parkedCats)
            }
            if (newCategoriesCount > 0 || lockedChanged) {
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

    suspend fun exportPlainText(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val websites = repository.getAllWebsitesList()
            val text = BackupManager.exportToPlainText(websites)
            outputStream.bufferedWriter().use { writer ->
                writer.write(text)
                writer.flush()
            }
            Result.success(websites.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restorePlainText(inputStream: InputStream): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val parsedWebsites = BackupManager.parsePlainTextUrls(inputStream)
            if (parsedWebsites.isEmpty()) {
                return@withContext Result.failure(Exception("No valid URLs found in file"))
            }

            val existingWebsites = repository.getAllWebsitesList()
            val existingKeys = existingWebsites.map { it.dedupeKey }.toSet()

            val toInsert = parsedWebsites.filter { !existingKeys.contains(it.dedupeKey) }

            if (toInsert.isNotEmpty()) {
                repository.insertWebsites(toInsert)
            }

            Result.success(Pair(toInsert.size, 0))
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

    companion object {
        const val PREF_COMPANION_PORT = "pc_companion_port"
        const val PREF_COMPANION_USE_FIXED_PIN = "pc_companion_use_fixed_pin"
        const val PREF_COMPANION_CUSTOM_PIN = "pc_companion_custom_pin"
        const val PREF_COMPANION_OFFLINE_EXPIRY_HOURS = "pc_companion_offline_expiry_hours"
        const val DEFAULT_COMPANION_PORT = 8080
        const val DEFAULT_COMPANION_OFFLINE_EXPIRY_HOURS = 24
    }
}

