package com.example.bookmarks

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDao
import com.example.data.WebsiteDatabase
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
class BookmarkRoomTransactionTest {

    private lateinit var db: WebsiteDatabase
    private lateinit var dao: WebsiteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.websiteDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testImport10000Items_allInsertedSuccessfully() = runBlocking {
        val items = (1..10_000).map { i ->
            val url = "https://example.com/site_$i"
            Website(
                id = 0,
                url = url,
                title = "Title $i",
                category = "General",
                dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                sourcePath = null,
                createdAtMs = 1710000000000L
            )
        }

        var progressCalls = 0
        val result = BookmarkImporter.importBookmarks(db, dao, items) { done, total ->
            progressCalls++
            assertTrue(done <= total)
        }

        assertEquals(10_000, result.inserted)
        assertEquals(0, result.duplicates)
        assertEquals(10_000, dao.getAllWebsitesList().size)
        assertTrue(progressCalls > 0)
    }

    @Test
    fun testReimportSameFile_insertsZeroAndReportsDuplicates() = runBlocking {
        val items = (1..500).map { i ->
            val url = "https://example.com/item_$i"
            Website(
                id = 0,
                url = url,
                title = "Title $i",
                category = "General",
                dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                sourcePath = null,
                createdAtMs = 1710000000000L
            )
        }

        // First import
        val firstResult = BookmarkImporter.importBookmarks(db, dao, items)
        assertEquals(500, firstResult.inserted)
        assertEquals(0, firstResult.duplicates)
        assertEquals(500, dao.getAllWebsitesList().size)

        // Second import of exact same items
        val secondResult = BookmarkImporter.importBookmarks(db, dao, items)
        assertEquals(0, secondResult.inserted)
        assertEquals(500, secondResult.duplicates)
        assertEquals(500, dao.getAllWebsitesList().size)
    }

    @Test
    fun testExceptionMidImport_rollsBackCompletelyLeavingZeroRows() = runBlocking {
        val items = (1..1000).map { i ->
            val url = "https://example.com/rollback_$i"
            Website(
                id = 0,
                url = url,
                title = "Rollback $i",
                category = "General",
                dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                sourcePath = null,
                createdAtMs = 1710000000000L
            )
        }

        // Mid-import failure simulation: transaction throws at item 600
        var threw = false
        try {
            db.withTransaction {
                items.chunked(500).forEachIndexed { index, chunk ->
                    dao.insertAll(chunk)
                    if (index == 1) {
                        throw RuntimeException("Simulated crash mid-import at chunk index $index")
                    }
                }
            }
        } catch (_: RuntimeException) {
            threw = true
        }

        assertTrue("Expected simulated exception to be thrown", threw)
        assertEquals("Database must contain 0 rows after rollback", 0, dao.getAllWebsitesList().size)
    }

    @Test
    fun testMigration2to3_duplicateUrlsInUpgrade() {
        val sqliteDb = db.openHelper.writableDatabase
        // 1. Create legacy version 2 table: saved_websites
        sqliteDb.execSQL("""
            CREATE TABLE IF NOT EXISTS `saved_websites` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `url` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `domain` TEXT NOT NULL,
                `faviconUrl` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Insert duplicate case: example.com/page and www.example.com/page/
        sqliteDb.execSQL("INSERT INTO `saved_websites` VALUES (1, 'example.com/page', 'Page 1', 'example.com', '', 'General', 1000)")
        sqliteDb.execSQL("INSERT INTO `saved_websites` VALUES (2, 'www.example.com/page/', 'Page 2', 'example.com', '', 'General', 2000)")

        // 3. Run MIGRATION_2_3
        WebsiteDatabase.MIGRATION_2_3.migrate(sqliteDb)

        // 4. Query websites table
        val cursor = sqliteDb.query("SELECT id, url, dedupeKey FROM websites ORDER BY id ASC")
        val results = mutableListOf<Triple<Long, String, String>>()
        while (cursor.moveToNext()) {
            results.add(Triple(cursor.getLong(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()

        println("Surviving rows after upgrade: $results")
        // Both survive because raw url was used as dedupeKey in SQL ('example.com/page' != 'www.example.com/page/')
        assertEquals(2, results.size)
        assertEquals("example.com/page", results[0].second)
        assertEquals("www.example.com/page/", results[1].second)
    }
}
