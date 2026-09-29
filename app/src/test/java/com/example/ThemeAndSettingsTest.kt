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
}
