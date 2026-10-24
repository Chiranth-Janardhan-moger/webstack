package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.bookmarks.BookmarkNormalizer
import com.example.data.Website
import com.example.data.WebsiteDatabase
import com.example.ui.viewmodel.WebsiteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FavoritesFeatureStressTest {

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
    fun testBulkFavorite_10000Items_atOnce_persistsAndRestoresCleanly() {
        val vm = WebsiteViewModel(app)
        val ids = (1L..10_000L).toList()

        // 1. Bulk set favorite
        vm.setWebsitesFavorite(ids, true)
        assertEquals(10_000, vm.favoriteWebsiteIds.value.size)
        assertTrue(vm.favoriteWebsiteIds.value.contains(1L))
        assertTrue(vm.favoriteWebsiteIds.value.contains(5_000L))
        assertTrue(vm.favoriteWebsiteIds.value.contains(10_000L))

        // 2. Verify new ViewModel instance reloads all 10,000 from SharedPreferences CSV
        val vmReloaded = WebsiteViewModel(app)
        assertEquals(10_000, vmReloaded.favoriteWebsiteIds.value.size)

        // 3. Toggle when all are favorited -> all should be unfavorited
        vmReloaded.toggleWebsitesFavorite(ids)
        assertTrue(vmReloaded.favoriteWebsiteIds.value.isEmpty())

        // 4. Verify persistence after unfavorite
        val vmEmpty = WebsiteViewModel(app)
        assertTrue(vmEmpty.favoriteWebsiteIds.value.isEmpty())
    }

    @Test
    fun testMixedBulkFavoriteToggle_togglesAllToFavoritedWhenAnyAreMissing() {
        val vm = WebsiteViewModel(app)
        val groupA = (1L..50L).toList()
        val groupB = (51L..100L).toList()
        val allIds = groupA + groupB

        // Only favorite group A
        vm.setWebsitesFavorite(groupA, true)
        assertEquals(50, vm.favoriteWebsiteIds.value.size)

        // Toggle across all 100 items -> since not all are favorited, ALL 100 should become favorited!
        vm.toggleWebsitesFavorite(allIds)
        assertEquals(100, vm.favoriteWebsiteIds.value.size)
        assertTrue(vm.favoriteWebsiteIds.value.containsAll(allIds))

        // Toggle again across all 100 items -> since ALL are favorited, ALL 100 should become unfavorited!
        vm.toggleWebsitesFavorite(allIds)
        assertTrue(vm.favoriteWebsiteIds.value.isEmpty())
    }

    @Test
    fun testDeleteWebsites_evictsFavoritesFromStateAndPrefs() = runBlocking {
        val vm = WebsiteViewModel(app)
        val initialIds = (1L..500L).toList()
        vm.setWebsitesFavorite(initialIds, true)
        assertEquals(500, vm.favoriteWebsiteIds.value.size)

        // Delete half the websites
        val toDelete = (1L..250L).toList()
        vm.deleteWebsites(toDelete)

        // Wait brief tick for coroutine
        Thread.sleep(100)

        assertEquals(250, vm.favoriteWebsiteIds.value.size)
        assertFalse(vm.favoriteWebsiteIds.value.contains(1L))
        assertFalse(vm.favoriteWebsiteIds.value.contains(250L))
        assertTrue(vm.favoriteWebsiteIds.value.contains(251L))
        assertTrue(vm.favoriteWebsiteIds.value.contains(500L))

        // Verify reloaded ViewModel also has evicted them
        val vmReloaded = WebsiteViewModel(app)
        assertEquals(250, vmReloaded.favoriteWebsiteIds.value.size)
    }

    @Test
    fun testSingleDeleteWebsite_evictsFavorite() = runBlocking {
        val vm = WebsiteViewModel(app)
        vm.setFavorite(42L, true)
        assertTrue(vm.favoriteWebsiteIds.value.contains(42L))

        vm.deleteWebsite(42L)
        Thread.sleep(100)

        assertFalse(vm.favoriteWebsiteIds.value.contains(42L))
    }

    @Test
    fun testMalformedSharedPreferencesCsv_recoversGracefullyWithoutCrash() {
        val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
        // Corrupt CSV with alphanumeric, empty items, trailing commas, negative numbers, huge values
        prefs.edit().putString(
            "favorite_website_ids_csv",
            "1, bad_id, , 9999999999999999999999999999999999, 42, -99, 100, trailing,"
        ).commit()

        val vm = WebsiteViewModel(app)
        val loaded = vm.favoriteWebsiteIds.value
        // Only valid positive/parsable Long IDs should be retained
        assertTrue(loaded.contains(1L))
        assertTrue(loaded.contains(42L))
        assertTrue(loaded.contains(100L))
        assertTrue(loaded.contains(-99L)) // negative long is parsable
        assertFalse(loaded.contains(0L))
        assertEquals(4, loaded.size)
    }

    @Test
    fun testConcurrentFavoriteOperations_threadSafetyStress() = runBlocking {
        val vm = WebsiteViewModel(app)

        // 30 concurrent workers performing rapid toggle and bulk updates
        val jobs = (1..30).map { workerId ->
            async(Dispatchers.Default) {
                val base = workerId * 100L
                val ids = (base until base + 50).toList()
                for (iter in 1..20) {
                    vm.setWebsitesFavorite(ids, true)
                    vm.toggleWebsitesFavorite(ids)
                    vm.setWebsitesFavorite(ids, true)
                }
            }
        }
        jobs.awaitAll()

        // All 30 * 50 = 1500 IDs should be favorited
        assertEquals(1500, vm.favoriteWebsiteIds.value.size)
    }

    @Test
    fun testEmptyAndBoundaryInputs_doNotCrash() {
        val vm = WebsiteViewModel(app)
        // Empty list
        vm.setWebsitesFavorite(emptyList(), true)
        vm.setWebsitesFavorite(emptyList(), false)
        vm.toggleWebsitesFavorite(emptyList())

        // Duplicate IDs in list
        val duplicates = listOf(7L, 7L, 7L, 8L, 8L)
        vm.setWebsitesFavorite(duplicates, true)
        assertEquals(2, vm.favoriteWebsiteIds.value.size)

        // Idempotency: setting already favorited item to true
        vm.setFavorite(7L, true)
        assertEquals(2, vm.favoriteWebsiteIds.value.size)

        // Setting non-favorited item to false
        vm.setFavorite(999L, false)
        assertEquals(2, vm.favoriteWebsiteIds.value.size)
    }
}
