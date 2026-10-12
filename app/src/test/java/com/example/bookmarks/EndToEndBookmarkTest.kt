package com.example.bookmarks

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EndToEndBookmarkTest {

    private lateinit var db: WebsiteDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun loadTaxonomy(): Map<String, String> {
        val assetFile = File(System.getProperty("user.dir"), "app/src/main/assets/domain_taxonomy.json")
        val jsonStr = if (assetFile.exists()) {
            assetFile.readText(StandardCharsets.UTF_8)
        } else {
            """{
                "github.com": "Development",
                "gitlab.com": "Development",
                "stackoverflow.com": "Development",
                "developer.android.com": "Development",
                "npmjs.com": "Development",
                "dev.to": "Development",
                "news.ycombinator.com": "Development",
                "medium.com": "Reading",
                "substack.com": "Reading",
                "arxiv.org": "Reading",
                "wikipedia.org": "Reading",
                "youtube.com": "Media",
                "vimeo.com": "Media",
                "spotify.com": "Media",
                "netflix.com": "Media",
                "reddit.com": "Social",
                "x.com": "Social",
                "twitter.com": "Social",
                "linkedin.com": "Social"
            }"""
        }
        return DomainClassifier.loadTaxonomyFromJson(jsonStr)
    }

    @Test
    fun testCompleteEndToEndScenario() {
        runBlocking {
            val userDir = File(System.getProperty("user.dir") ?: ".")
            val projectRoot = if (File(userDir, "testdata").exists()) userDir else userDir.parentFile ?: userDir
            val htmlFile = File(projectRoot, "testdata/e2e/bookmarks.html")
            val jsonFile = File(projectRoot, "testdata/e2e/expected.json")

            assertTrue("testdata/e2e/bookmarks.html must exist at ${htmlFile.absolutePath}", htmlFile.exists())
            assertTrue("testdata/e2e/expected.json must exist at ${jsonFile.absolutePath}", jsonFile.exists())

        val expectedJson = JSONObject(jsonFile.readText(StandardCharsets.UTF_8))
        val expectedTotalFound = expectedJson.getInt("totalFound")
        val expectedUnsupported = expectedJson.getInt("unsupportedCount")
        val expectedInternalDupes = expectedJson.getInt("internalDuplicatesCount")
        val expectedUniqueInserted = expectedJson.getInt("uniqueInsertedCount")
        val expectedLinksObj = expectedJson.getJSONObject("links")

        // 1. IMPORT through real parser, classifier, and cluster model
        val classifier = DomainClassifier(loadTaxonomy())
        val preprocessResult = FileInputStream(htmlFile).use {
            BookmarkImporter.preprocess(it, classifier)
        }

        assertEquals("Unsupported count must match expected.json",
            expectedUnsupported, preprocessResult.unsupportedSchemeCount)
        assertEquals("Internal duplicates count must match expected.json",
            expectedInternalDupes, preprocessResult.internalDuplicatesCount)
        assertEquals("Unique links count must match expected.json",
            expectedUniqueInserted, preprocessResult.classifiedBookmarks.size)

        // Verify the category of EVERY link against expected.json answer key
        for (b in preprocessResult.classifiedBookmarks) {
            val expectedItem = expectedLinksObj.optJSONObject(b.url)
            assertNotNull("URL '${b.url}' must be present in expected.json answer key", expectedItem)
            val expectedCategory = expectedItem!!.getString("category")
            assertEquals("Category mismatch for URL '${b.url}'", expectedCategory, b.category)
        }

        // 2. APPROVAL SHEET LOGIC
        // At most 7 top clusters + remainder in other
        val clusters = preprocessResult.clusters
        val top7 = clusters.take(7)
        val other = clusters.drop(7)
        assertTrue(top7.size <= 7)

        // Changing one cluster's category reassigns all its links
        val clusterToReassign = clusters.first()
        val reassignedBookmarks = preprocessResult.classifiedBookmarks.map {
            if (it.category == clusterToReassign.id) it.copy(category = "ReassignedCategory") else it
        }
        val reassignedCount = reassignedBookmarks.count { it.category == "ReassignedCategory" }
        assertEquals(clusterToReassign.linkCount, reassignedCount)

        // Unticking a cluster excludes its links
        val clusterToExclude = clusters.last()
        val excludedBookmarks = preprocessResult.classifiedBookmarks.filter { it.category != clusterToExclude.id }
        assertEquals(preprocessResult.classifiedBookmarks.size - clusterToExclude.linkCount, excludedBookmarks.size)

        // 3. PERSISTENCE INTO ROOM & RE-IMPORT
        val websitesToInsert = preprocessResult.classifiedBookmarks.map { b ->
            Website(
                url = b.url,
                title = b.title,
                category = b.category,
                dedupeKey = b.dedupeKey,
                sourcePath = b.sourcePath,
                createdAtMs = b.createdAtMs
            )
        }
        val insertResult = BookmarkImporter.importBookmarks(db, db.websiteDao(), websitesToInsert)
        assertEquals(expectedUniqueInserted, insertResult.inserted)
        assertEquals(0, insertResult.duplicates)

        // Re-import the same file: 0 rows inserted, N duplicates reported
        val secondImportResult = BookmarkImporter.importBookmarks(db, db.websiteDao(), websitesToInsert)
        assertEquals(0, secondImportResult.inserted)
        assertEquals(expectedUniqueInserted, secondImportResult.duplicates)

        // 4. EXPORT TO HTML (SAF & Share paths)
        val exportFile = File(projectRoot, "testdata/e2e/exported_test.html")
        // Write longer dummy content to test that export truncates completely
        exportFile.writeText("X".repeat(500_000))
        val initialLongLength = exportFile.length()

        FileOutputStream(exportFile).use { out ->
            BookmarkExporter.exportBookmarks(out, db.websiteDao(), 1710000000000L)
        }
        assertTrue("Exported file must truncate previous larger file without leftover bytes",
            exportFile.length() < initialLongLength)

        // Run python validator on exported file
        val scriptFile = File(projectRoot, "tools/validate_netscape.py")
        val proc = ProcessBuilder("python", scriptFile.absolutePath, exportFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        val validatorOutput = proc.inputStream.bufferedReader().readText()
        val exitCode = proc.waitFor()
        assertEquals("Validator must pass on exported file. Output: $validatorOutput", 0, exitCode)

        // 5. ROUND TRIP: import exported file into fresh empty DB
        val freshDb = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val roundTripPreprocess = FileInputStream(exportFile).use {
                BookmarkImporter.preprocess(it, classifier)
            }
            val roundTripWebsites = roundTripPreprocess.classifiedBookmarks.map { b ->
                Website(
                    url = b.url,
                    title = b.title,
                    category = b.category,
                    dedupeKey = b.dedupeKey,
                    sourcePath = b.sourcePath,
                    createdAtMs = b.createdAtMs
                )
            }
            val rtInsertResult = BookmarkImporter.importBookmarks(freshDb, freshDb.websiteDao(), roundTripWebsites)
            assertEquals("Round trip inserted count must match initial", expectedUniqueInserted, rtInsertResult.inserted)

            // Multiset equality: (url, title, category) (export normalizes whitespace \n\r\t to space)
            val db1Sites = db.websiteDao().getAllWebsitesList().map {
                Triple(it.url, it.title.replace(Regex("[\n\r\t]"), " "), it.category)
            }.toSet()
            val db2Sites = freshDb.websiteDao().getAllWebsitesList().map { Triple(it.url, it.title, it.category) }.toSet()
            assertEquals("Round trip multiset of (url, title, category) must match exactly", db1Sites, db2Sites)
        } finally {
            freshDb.close()
            exportFile.delete()
        }
    }

    @Test
    fun testExportToFreshDbEquality() {
        runBlocking {
            val db1 = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            val db2 = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            val exportFile = File(context.cacheDir, "export_fresh_db_equality.html")
            try {
                // Populate db1 with diverse categories and root uncategorized links
                val testItems = listOf(
                    Website(0, "https://github.com/kotlin", "Kotlin Lang", "Development", "https://github.com/kotlin", "Development", 1710000000000L),
                    Website(0, "https://developer.android.com/jetpack", "Jetpack Docs", "Development", "https://developer.android.com/jetpack", "Development", 1710000001000L),
                    Website(0, "https://medium.com/@dev/story", "Android Story", "Reading", "https://medium.com/@dev/story", "Reading", 1710000002000L),
                    Website(0, "https://youtube.com/watch?v=123", "Compose Tutorial", "Media", "https://youtube.com/watch?v=123", "Media", 1710000003000L),
                    Website(0, "https://reddit.com/r/androiddev", "Android Dev Reddit", "Social", "https://reddit.com/r/androiddev", "Social", 1710000004000L),
                    Website(0, "https://customsite.org/page", "Custom Site", "Custom Folder", "https://customsite.org/page", "Custom Folder", 1710000005000L),
                    Website(0, "https://uncategorized-example.com/item", "Root Uncat", "Uncategorized", "https://uncategorized-example.com/item", null, 1710000006000L)
                )

                BookmarkImporter.importBookmarks(db1, db1.websiteDao(), testItems)
                assertEquals(testItems.size, db1.websiteDao().getAllWebsitesList().size)

                // Export db1 to HTML
                FileOutputStream(exportFile).use { out ->
                    BookmarkExporter.exportBookmarks(out, db1.websiteDao(), 1710000000000L)
                }
                assertTrue(exportFile.length() > 0)

                // Parse and import into fresh db2
                val classifier = DomainClassifier.loadFromAssets(context)
                val preprocessed = FileInputStream(exportFile).use {
                    BookmarkImporter.preprocess(it, classifier)
                }
                val db2Items = preprocessed.classifiedBookmarks.map { b ->
                    Website(
                        url = b.url,
                        title = b.title,
                        category = b.category,
                        dedupeKey = b.dedupeKey,
                        sourcePath = b.sourcePath,
                        createdAtMs = b.createdAtMs
                    )
                }
                val db2ImportResult = BookmarkImporter.importBookmarks(db2, db2.websiteDao(), db2Items)
                assertEquals(testItems.size, db2ImportResult.inserted)

                // Verify row equality between db1 and db2 ignoring local auto-increment id
                val list1 = db1.websiteDao().getAllWebsitesList().sortedBy { it.url }
                val list2 = db2.websiteDao().getAllWebsitesList().sortedBy { it.url }

                assertEquals("Total row count must match between DB1 and fresh DB2", list1.size, list2.size)
                for (i in list1.indices) {
                    val row1 = list1[i]
                    val row2 = list2[i]
                    assertEquals("URL must match at index $i", row1.url, row2.url)
                    assertEquals("Title must match at index $i", row1.title, row2.title)
                    assertEquals("Category must match at index $i", row1.category, row2.category)
                    assertEquals("DedupeKey must match at index $i", row1.dedupeKey, row2.dedupeKey)
                }
            } finally {
                db1.close()
                db2.close()
                exportFile.delete()
            }
        }
    }
}
}
