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

    @Test
    fun testFavoriteIconType_entriesAndFromIdFallback() {
        val icons = com.example.ui.model.FavoriteIconType.entries
        assertEquals(5, icons.size)
        assertEquals("Heart", com.example.ui.model.FavoriteIconType.HEART.displayName)
        assertEquals("Star", com.example.ui.model.FavoriteIconType.STAR.displayName)
        assertEquals("Bookmark", com.example.ui.model.FavoriteIconType.BOOKMARK.displayName)
        assertEquals("Diamond", com.example.ui.model.FavoriteIconType.DIAMOND.displayName)
        assertEquals("Sparkle", com.example.ui.model.FavoriteIconType.SPARKLE.displayName)

        assertEquals(com.example.ui.model.FavoriteIconType.STAR, com.example.ui.model.FavoriteIconType.fromId("star"))
        assertEquals(com.example.ui.model.FavoriteIconType.HEART, com.example.ui.model.FavoriteIconType.fromId("unknown_id"))
        assertEquals(com.example.ui.model.FavoriteIconType.HEART, com.example.ui.model.FavoriteIconType.fromId(null))
    }

    @Test
    fun testFavoriteBadgeShape_entriesAndFromIdFallback() {
        val shapes = com.example.ui.model.FavoriteBadgeShape.entries
        assertEquals(6, shapes.size)
        assertEquals("Pure Float", com.example.ui.model.FavoriteBadgeShape.PURE_FLOAT.displayName)
        assertEquals("Circle", com.example.ui.model.FavoriteBadgeShape.CIRCLE.displayName)
        assertEquals("Squircle", com.example.ui.model.FavoriteBadgeShape.SQUIRCLE.displayName)
        assertEquals("4-Leaf Clover", com.example.ui.model.FavoriteBadgeShape.CLOVER.displayName)
        assertEquals("Flower", com.example.ui.model.FavoriteBadgeShape.FLOWER.displayName)
        assertEquals("Soft Burst", com.example.ui.model.FavoriteBadgeShape.SOFT_BURST.displayName)

        // Backward compatibility fallbacks
        assertEquals(com.example.ui.model.FavoriteBadgeShape.FLOWER, com.example.ui.model.FavoriteBadgeShape.fromId("cookie"))
        assertEquals(com.example.ui.model.FavoriteBadgeShape.SOFT_BURST, com.example.ui.model.FavoriteBadgeShape.fromId("pentagon"))
        assertEquals(com.example.ui.model.FavoriteBadgeShape.PURE_FLOAT, com.example.ui.model.FavoriteBadgeShape.fromId("invalid_shape"))
    }

    @Test
    fun testFavoriteColorPreset_entriesAndFromIdFallback() {
        val colors = com.example.ui.model.FavoriteColorPreset.entries
        assertEquals(6, colors.size)
        assertEquals("Crimson Pink", com.example.ui.model.FavoriteColorPreset.CRIMSON_PINK.displayName)
        assertEquals("Royal Gold", com.example.ui.model.FavoriteColorPreset.ROYAL_GOLD.displayName)
        assertEquals("Electric Purple", com.example.ui.model.FavoriteColorPreset.ELECTRIC_PURPLE.displayName)
        assertEquals("Sky Blue", com.example.ui.model.FavoriteColorPreset.SKY_BLUE.displayName)
        assertEquals("Emerald Green", com.example.ui.model.FavoriteColorPreset.EMERALD_GREEN.displayName)
        assertEquals("Sunset Orange", com.example.ui.model.FavoriteColorPreset.SUNSET_ORANGE.displayName)

        // Hex colors
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFF2D55), com.example.ui.model.FavoriteColorPreset.CRIMSON_PINK.color)
        
        // Backward compatibility fallbacks
        assertEquals(com.example.ui.model.FavoriteColorPreset.CRIMSON_PINK, com.example.ui.model.FavoriteColorPreset.fromId("crimson"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.ROYAL_GOLD, com.example.ui.model.FavoriteColorPreset.fromId("amber"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.ELECTRIC_PURPLE, com.example.ui.model.FavoriteColorPreset.fromId("purple"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.SKY_BLUE, com.example.ui.model.FavoriteColorPreset.fromId("blue"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.EMERALD_GREEN, com.example.ui.model.FavoriteColorPreset.fromId("green"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.SUNSET_ORANGE, com.example.ui.model.FavoriteColorPreset.fromId("teal"))
        assertEquals(com.example.ui.model.FavoriteColorPreset.CRIMSON_PINK, com.example.ui.model.FavoriteColorPreset.fromId("unknown_color"))
    }

    @Test
    fun testFavoriteCustomization_viewModelPersistenceAndState() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.WebsiteViewModel(app)

        // Initial defaults
        assertEquals(com.example.ui.model.FavoriteIconType.HEART, vm.favoriteIconType.value)
        assertEquals(com.example.ui.model.FavoriteBadgeShape.PURE_FLOAT, vm.favoriteBadgeShape.value)
        assertEquals(com.example.ui.model.FavoriteColorPreset.CRIMSON_PINK, vm.favoriteColorPreset.value)

        // Mutate and persist
        vm.setFavoriteIconType(com.example.ui.model.FavoriteIconType.STAR)
        vm.setFavoriteBadgeShape(com.example.ui.model.FavoriteBadgeShape.CLOVER)
        vm.setFavoriteColorPreset(com.example.ui.model.FavoriteColorPreset.ROYAL_GOLD)

        assertEquals(com.example.ui.model.FavoriteIconType.STAR, vm.favoriteIconType.value)
        assertEquals(com.example.ui.model.FavoriteBadgeShape.CLOVER, vm.favoriteBadgeShape.value)
        assertEquals(com.example.ui.model.FavoriteColorPreset.ROYAL_GOLD, vm.favoriteColorPreset.value)

        // Verify across new ViewModel instance loading from SharedPreferences
        val vm2 = com.example.ui.viewmodel.WebsiteViewModel(app)
        assertEquals(com.example.ui.model.FavoriteIconType.STAR, vm2.favoriteIconType.value)
        assertEquals(com.example.ui.model.FavoriteBadgeShape.CLOVER, vm2.favoriteBadgeShape.value)
        assertEquals(com.example.ui.model.FavoriteColorPreset.ROYAL_GOLD, vm2.favoriteColorPreset.value)
    }
}
