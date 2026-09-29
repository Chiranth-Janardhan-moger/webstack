package com.example

import com.example.data.BackupManager
import com.example.data.Website
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupManagerTest {

    @Test
    fun testExportToJson_createsValidWebStackBackupJson() {
        val websites = listOf(
            Website(
                id = 1,
                url = "https://github.com",
                title = "GitHub",
                domain = "github.com",
                category = "Tech",
                createdAt = 1727610000000L
            ),
            Website(
                id = 2,
                url = "https://linear.app",
                title = "Linear",
                domain = "linear.app",
                category = "Tools",
                createdAt = 1727615000000L
            )
        )
        val categories = listOf("Personal", "Tech", "Tools")

        val jsonString = BackupManager.exportToJson(websites, categories)
        val json = JSONObject(jsonString)

        assertEquals(1, json.getInt("version"))
        assertEquals("WebStack", json.getString("appName"))
        assertTrue(json.getLong("exportedAt") > 0)

        // Categories
        val categoriesArray = json.getJSONArray("categories")
        assertEquals(3, categoriesArray.length())
        assertEquals("Personal", categoriesArray.getString(0))
        assertEquals("Tech", categoriesArray.getString(1))
        assertEquals("Tools", categoriesArray.getString(2))

        // Websites
        val websitesArray = json.getJSONArray("websites")
        assertEquals(2, websitesArray.length())

        val firstSite = websitesArray.getJSONObject(0)
        assertEquals("https://github.com", firstSite.getString("url"))
        assertEquals("GitHub", firstSite.getString("title"))
        assertEquals("github.com", firstSite.getString("domain"))
        assertEquals("Tech", firstSite.getString("category"))
        assertEquals(1727610000000L, firstSite.getLong("createdAt"))

        // Verify zero speculative or unused fields
        assertFalse(firstSite.has("faviconUrl"))
        assertFalse(firstSite.has("notes"))
        assertFalse(firstSite.has("clickCount"))
    }

    @Test
    fun testParseBackup_validJson_returnsSuccessWithCorrectData() {
        val jsonString = """
            {
              "version": 1,
              "appName": "WebStack",
              "exportedAt": 1727616000000,
              "categories": ["Personal", "Tech", "Reading"],
              "websites": [
                {
                  "url": "https://github.com",
                  "title": "GitHub",
                  "domain": "github.com",
                  "category": "Tech",
                  "createdAt": 1727610000000
                }
              ]
            }
        """.trimIndent()

        val result = BackupManager.parseBackup(jsonString)
        assertTrue(result.isSuccess)

        val backupData = result.getOrThrow()
        assertEquals(1, backupData.version)
        assertEquals(1727616000000L, backupData.exportedAt)
        assertEquals(listOf("Personal", "Tech", "Reading"), backupData.categories)
        assertEquals(1, backupData.websites.size)

        val website = backupData.websites[0]
        assertEquals("https://github.com", website.url)
        assertEquals("GitHub", website.title)
        assertEquals("github.com", website.domain)
        assertEquals("Tech", website.category)
        assertEquals(1727610000000L, website.createdAt)
        assertEquals("", website.faviconUrl)
        assertEquals(0L, website.id) // Room auto-increment should assign fresh ID
    }

    @Test
    fun testParseBackup_withMissingOptionalFields_appliesSafeDefaults() {
        val minimalJson = """
            {
              "websites": [
                {
                  "url": "https://www.figma.com",
                  "title": "Figma"
                }
              ]
            }
        """.trimIndent()

        val result = BackupManager.parseBackup(minimalJson)
        assertTrue(result.isSuccess)

        val backupData = result.getOrThrow()
        assertEquals(1, backupData.websites.size)

        val site = backupData.websites[0]
        assertEquals("https://www.figma.com", site.url)
        assertEquals("Figma", site.title)
        assertEquals("figma.com", site.domain) // Auto-extracted domain
        assertEquals("Personal", site.category) // Defaulted category
        assertTrue(site.createdAt > 0)
    }

    @Test
    fun testParseBackup_corruptedJson_returnsFailure() {
        val corruptedJson = "not a valid json text"
        val result = BackupManager.parseBackup(corruptedJson)
        assertTrue(result.isFailure)
    }

    @Test
    fun testParseBackup_emptyUrls_areIgnored() {
        val jsonWithBlanks = """
            {
              "websites": [
                { "url": "" },
                { "url": "   " },
                { "url": "https://valid.com", "title": "Valid" }
              ]
            }
        """.trimIndent()

        val result = BackupManager.parseBackup(jsonWithBlanks)
        assertTrue(result.isSuccess)

        val backupData = result.getOrThrow()
        assertEquals(1, backupData.websites.size)
        assertEquals("https://valid.com", backupData.websites[0].url)
    }
}
