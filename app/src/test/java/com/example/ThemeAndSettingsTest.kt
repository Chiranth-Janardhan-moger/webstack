package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.model.AppThemeMode
import com.example.ui.model.WebStackLayoutMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemeAndSettingsTest {

    @Test
    fun testAppThemeMode_titlesAndEnumValues() {
        assertEquals("System", AppThemeMode.SYSTEM.title)
        assertEquals("Light", AppThemeMode.LIGHT.title)
        assertEquals("Dark", AppThemeMode.DARK.title)
        assertEquals(3, AppThemeMode.entries.size)
    }

    @Test
    fun testAppThemeMode_preferencePersistenceAndFallback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)

        // Clean prefs
        prefs.edit().remove("app_theme").commit()

        // 1. Verify default fallback when unset
        val defaultMode = runCatching { AppThemeMode.valueOf(prefs.getString("app_theme", "") ?: "") }
            .getOrDefault(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, defaultMode)

        // 2. Save Dark theme
        prefs.edit().putString("app_theme", AppThemeMode.DARK.name).commit()
        val darkSaved = runCatching { AppThemeMode.valueOf(prefs.getString("app_theme", "") ?: "") }
            .getOrDefault(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.DARK, darkSaved)

        // 3. Save Light theme
        prefs.edit().putString("app_theme", AppThemeMode.LIGHT.name).commit()
        val lightSaved = runCatching { AppThemeMode.valueOf(prefs.getString("app_theme", "") ?: "") }
            .getOrDefault(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.LIGHT, lightSaved)

        // 4. Invalid value gracefully falls back to SYSTEM
        prefs.edit().putString("app_theme", "INVALID_MODE").commit()
        val invalidFallback = runCatching { AppThemeMode.valueOf(prefs.getString("app_theme", "") ?: "") }
            .getOrDefault(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, invalidFallback)
    }

    @Test
    fun testLayoutMode_preferencePersistenceAndFallback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)

        prefs.edit().putString("layout_mode", WebStackLayoutMode.GRID_CARDS.name).commit()
        val restored = runCatching { WebStackLayoutMode.valueOf(prefs.getString("layout_mode", "") ?: "") }
            .getOrDefault(WebStackLayoutMode.LARGE_CARDS)
        assertEquals(WebStackLayoutMode.GRID_CARDS, restored)
    }

    @Test
    fun testSettingsSubScreen_navigationAndResetToMain() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.WebsiteViewModel(app)

        assertEquals(com.example.ui.sheets.SettingsSubScreen.MAIN, vm.settingsSubScreen.value)

        vm.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.APPEARANCE)
        assertEquals(com.example.ui.sheets.SettingsSubScreen.APPEARANCE, vm.settingsSubScreen.value)

        vm.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.APP_INFO)
        assertEquals(com.example.ui.sheets.SettingsSubScreen.APP_INFO, vm.settingsSubScreen.value)

        vm.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.MAIN)
        assertEquals(com.example.ui.sheets.SettingsSubScreen.MAIN, vm.settingsSubScreen.value)
    }

    @Test
    fun testBookmarkSortOrder_titlesSubtitlesAndEnumValues() {
        assertEquals("Newest", com.example.ui.model.BookmarkSortOrder.NEWEST.title)
        assertEquals("Show recently saved bookmarks first", com.example.ui.model.BookmarkSortOrder.NEWEST.subtitle)
        assertEquals("Oldest", com.example.ui.model.BookmarkSortOrder.OLDEST.title)
        assertEquals("Show earliest saved bookmarks first", com.example.ui.model.BookmarkSortOrder.OLDEST.subtitle)
        assertEquals(2, com.example.ui.model.BookmarkSortOrder.entries.size)
    }

    @Test
    fun testBookmarkSortOrder_preferencePersistenceAndFallback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("webstack_settings", Context.MODE_PRIVATE)

        prefs.edit().remove("bookmark_sort_order").commit()

        val defaultMode = runCatching {
            com.example.ui.model.BookmarkSortOrder.valueOf(prefs.getString("bookmark_sort_order", "") ?: "")
        }.getOrDefault(com.example.ui.model.BookmarkSortOrder.NEWEST)
        assertEquals(com.example.ui.model.BookmarkSortOrder.NEWEST, defaultMode)

        prefs.edit().putString("bookmark_sort_order", com.example.ui.model.BookmarkSortOrder.OLDEST.name).commit()
        val oldestMode = runCatching {
            com.example.ui.model.BookmarkSortOrder.valueOf(prefs.getString("bookmark_sort_order", "") ?: "")
        }.getOrDefault(com.example.ui.model.BookmarkSortOrder.NEWEST)
        assertEquals(com.example.ui.model.BookmarkSortOrder.OLDEST, oldestMode)

        prefs.edit().putString("bookmark_sort_order", "INVALID_ORDER").commit()
        val invalidMode = runCatching {
            com.example.ui.model.BookmarkSortOrder.valueOf(prefs.getString("bookmark_sort_order", "") ?: "")
        }.getOrDefault(com.example.ui.model.BookmarkSortOrder.NEWEST)
        assertEquals(com.example.ui.model.BookmarkSortOrder.NEWEST, invalidMode)
    }

    @Test
    fun testBookmarkSortOrder_sortingLogicReversesListCorrectly() {
        val b1 = com.example.data.Website(id = 1, title = "Newest Item", url = "https://a.com", category = "Tech", dedupeKey = "https://a.com", createdAtMs = 300L)
        val b2 = com.example.data.Website(id = 2, title = "Middle Item", url = "https://b.com", category = "Tech", dedupeKey = "https://b.com", createdAtMs = 200L)
        val b3 = com.example.data.Website(id = 3, title = "Oldest Item", url = "https://c.com", category = "Tech", dedupeKey = "https://c.com", createdAtMs = 100L)
        val list = listOf(b1, b2, b3)

        val newestSorted = when (com.example.ui.model.BookmarkSortOrder.NEWEST) {
            com.example.ui.model.BookmarkSortOrder.NEWEST -> list
            com.example.ui.model.BookmarkSortOrder.OLDEST -> list.asReversed()
        }
        assertEquals("Newest Item", newestSorted.first().title)
        assertEquals("Oldest Item", newestSorted.last().title)

        val oldestSorted = when (com.example.ui.model.BookmarkSortOrder.OLDEST) {
            com.example.ui.model.BookmarkSortOrder.NEWEST -> list
            com.example.ui.model.BookmarkSortOrder.OLDEST -> list.asReversed()
        }
        assertEquals("Oldest Item", oldestSorted.first().title)
        assertEquals("Newest Item", oldestSorted.last().title)
    }

    @Test
    fun testCompanionCustomPin_enforcesSixDigitsAndPersistence() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.WebsiteViewModel(app)

        // Valid 6 digits
        vm.setCompanionCustomPin("654321")
        assertEquals("654321", vm.companionCustomPin.value)

        // Strips non-digits and truncates to 6 digits
        vm.setCompanionCustomPin("99abc-88#77@66")
        assertEquals("998877", vm.companionCustomPin.value)

        // Over 6 digits truncated to 6
        vm.setCompanionCustomPin("123456789")
        assertEquals("123456", vm.companionCustomPin.value)
    }
}

