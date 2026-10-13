package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.bookmarks.BookmarkNormalizer
import com.example.bookmarks.CategorySource
import com.example.bookmarks.ClassifiedBookmark
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebsiteViewModelCacheRegressionTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        resetDatabaseSingleton()
        app.deleteDatabase("webstack_database")
    }

    @After
    fun teardown() {
        resetDatabaseSingleton()
        app.deleteDatabase("webstack_database")
    }

    private fun resetDatabaseSingleton() {
        try {
            val field = WebsiteDatabase::class.java.getDeclaredField("INSTANCE")
            field.isAccessible = true
            field.set(null, null)
        } catch (_: Exception) {}
    }

    @Test
    fun testCommitImportBookmarks_200Items_allCachedIdsPositiveAndUnique_cappedAt50() = runBlocking {
        val vm = WebsiteViewModel(app)
        val items = (1..200).map { i ->
            ClassifiedBookmark(
                url = "https://regression-cache.org/item_$i",
                title = "Item $i",
                dedupeKey = BookmarkNormalizer.dedupeKey("https://regression-cache.org/item_$i")!!,
                folderPath = listOf("Work"),
                createdAtMs = 1710000000000L + i,
                category = "Work",
                source = CategorySource.FOLDER,
                sourcePath = "Work",
                host = "regression-cache.org",
                registrableDomain = "regression-cache.org"
            )
        }

        val result = vm.commitImportBookmarks(items)
        assertTrue("Import must succeed", result.isSuccess)
        assertEquals(200, result.getOrThrow().inserted)

        ShadowLooper.idleMainLooper()

        val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("cached_websites_json", null)
        assertTrue("cached_websites_json must not be null or blank", !cachedJson.isNullOrBlank())

        val array = JSONArray(cachedJson)
        assertTrue("Cache must have at most 50 entries", array.length() <= 50)
        assertEquals(50, array.length())

        val seenIds = mutableSetOf<Long>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getLong("id")
            assertTrue("Every cached id must be > 0 (got $id)", id > 0L)
            assertTrue("Every cached id must be unique (duplicate: $id)", seenIds.add(id))
        }
    }

    @Test
    fun testCorruptCacheWithZeroIds_dropsCacheAndPopulatesFromRoom() {
        // 1. Seed room database directly with 2 real items
        val db = WebsiteDatabase.getDatabase(app)
        val dao = db.websiteDao()
        runBlocking {
            val site1 = Website(id = 0, url = "https://valid1.com", title = "Valid 1", category = "General", dedupeKey = "https://valid1.com", createdAtMs = 1000L)
            val site2 = Website(id = 0, url = "https://valid2.com", title = "Valid 2", category = "General", dedupeKey = "https://valid2.com", createdAtMs = 2000L)
            dao.insertAll(listOf(site1, site2))

            val roomItems = dao.getAllWebsitesList()
            assertEquals(2, roomItems.size)
            assertTrue(roomItems.all { it.id > 0L })
        }

        // 2. Seed SharedPreferences cache with two corrupt entries that both have id = 0
        val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
        val corruptArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", 0L)
                put("url", "https://corrupt1.com")
                put("title", "Corrupt 1")
                put("category", "General")
                put("createdAt", 100L)
            })
            put(JSONObject().apply {
                put("id", 0L)
                put("url", "https://corrupt2.com")
                put("title", "Corrupt 2")
                put("category", "General")
                put("createdAt", 200L)
            })
        }
        prefs.edit().putString("cached_websites_json", corruptArray.toString()).commit()

        // 3. Create ViewModel: loadCachedWebsites must detect id <= 0, drop the corrupt cache, and not crash
        val vm = WebsiteViewModel(app)

        // Wait for Room flow to emit the 2 valid items
        var fullWebsites: List<Website>? = null
        for (i in 0 until 50) {
            ShadowLooper.idleMainLooper()
            val current = vm.websitesList.value
            if (current != null && current.size == 2) {
                fullWebsites = current
                break
            }
            Thread.sleep(50)
        }

        assertEquals("List must end up equal to Room contents", 2, fullWebsites?.size)
        assertEquals("Valid 2", fullWebsites?.get(0)?.title)
        assertEquals("Valid 1", fullWebsites?.get(1)?.title)

        // Verify the corrupt cache was removed from prefs
        val remainingCache = prefs.getString("cached_websites_json", null)
        if (remainingCache != null) {
            val refreshedArray = JSONArray(remainingCache)
            for (i in 0 until refreshedArray.length()) {
                assertTrue("Refreshed cache must have valid id > 0", refreshedArray.getJSONObject(i).getLong("id") > 0L)
            }
        }
    }
}
