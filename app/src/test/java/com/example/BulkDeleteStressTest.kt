package com.example
 
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bookmarks.BookmarkNormalizer
import com.example.data.Website
import com.example.data.WebsiteDao
import com.example.data.WebsiteDatabase
import com.example.data.WebsiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
 
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BulkDeleteStressTest {
 
     private lateinit var db: WebsiteDatabase
     private lateinit var dao: WebsiteDao
     private lateinit var repository: WebsiteRepository
 
     @Before
     fun setup() {
         val context = ApplicationProvider.getApplicationContext<Context>()
         db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
             .allowMainThreadQueries()
             .build()
         dao = db.websiteDao()
         repository = WebsiteRepository(dao)
     }
 
     @After
     fun teardown() {
         db.close()
     }
 
     @Test
     fun testDelete10000Items_atOnce_doesNotCrashWithTooManySqlVariables() = runBlocking {
         // Insert 10,000 items in chunks of 500
         val totalItems = 10_000
         val insertedIds = mutableListOf<Long>()
         (1..totalItems).chunked(500).forEach { chunk ->
             val websites = chunk.map { i ->
                 val url = "https://example.com/site_$i"
                 Website(
                     id = 0,
                     url = url,
                     title = "Bookmark $i",
                     category = "General",
                     dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                     sourcePath = null,
                     createdAtMs = 1710000000000L + i
                 )
             }
             val ids = dao.insertAll(websites)
             insertedIds.addAll(ids)
         }
 
         assertEquals(totalItems, insertedIds.size)
         assertEquals(totalItems, dao.getAllWebsitesList().size)
 
         // Delete all 10,000 items at once via repository
         repository.deleteWebsites(insertedIds)
 
         // Verify all items deleted cleanly
         val remaining = dao.getAllWebsitesList()
         assertEquals(0, remaining.size)
     }
 
     @Test
     fun testPartialBulkDelete_deletesTargetedSubsetOnly() = runBlocking {
         val totalItems = 3_000
         val insertedIds = mutableListOf<Long>()
         (1..totalItems).chunked(500).forEach { chunk ->
             val websites = chunk.map { i ->
                 val url = "https://test.org/item_$i"
                 Website(
                     id = 0,
                     url = url,
                     title = "Item $i",
                     category = "Tech",
                     dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                     sourcePath = null,
                     createdAtMs = 1710000000000L + i
                 )
             }
             insertedIds.addAll(dao.insertAll(websites))
         }
 
         // Delete the first 1,800 items
         val toDelete = insertedIds.take(1_800)
         val toKeep = insertedIds.drop(1_800).toSet()
 
         repository.deleteWebsites(toDelete)
 
         val remaining = dao.getAllWebsitesList()
         assertEquals(1_200, remaining.size)
         assertTrue(remaining.all { it.id in toKeep })
     }
 
     @Test
     fun testEmptyListBulkDelete_doesNotThrow() = runBlocking {
         repository.deleteWebsites(emptyList())
         assertEquals(0, dao.getAllWebsitesList().size)
     }
 
     @Test
     fun testDelete1000000Ids_chunkingLogicScalesWithoutOomOrCrash() = runBlocking {
         // Create a simulated list of 1,000,000 IDs (8 MB of RAM in memory)
         // To verify that chunking 1,000,000 IDs by 500 processes 2,000 chunks smoothly
         val oneMillionIds = LongArray(1_000_000) { it.toLong() + 1 }.toList()
 
         assertEquals(1_000_000, oneMillionIds.size)
 
         // Deleting on an empty database should safely execute all 2,000 chunks without SQLite error or crash
         val start = System.currentTimeMillis()
         repository.deleteWebsites(oneMillionIds)
         val duration = System.currentTimeMillis() - start
 
         // Verify database remains consistent and healthy
         assertEquals(0, dao.getAllWebsitesList().size)
         println("Successfully processed 1,000,000 ID chunked deletion in ${duration}ms")
     }
 }
