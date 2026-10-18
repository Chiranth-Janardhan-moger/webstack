package com.example.bookmarks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.management.ManagementFactory
import java.lang.management.MemoryType
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookmarkScaleBenchmarkTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testScale50kBookmarks_exportAndParseWithinBudget() {
        runBlocking {
            val totalCount = 50_000
            val categories = listOf("Development", "Media", "Shopping", "Reading", "Social", "Work", "Personal")
            val sites = ArrayList<Website>(totalCount)

            for (i in 1..totalCount) {
                val cat = categories[i % categories.size]
                sites.add(
                    Website(
                        id = i.toLong(),
                        url = "https://scale-example${i % 500}.org/path/resource_$i",
                        title = "Scale Benchmark Resource #$i [Title]",
                        category = cat,
                        dedupeKey = "https://scale-example${i % 500}.org/path/resource_$i",
                        sourcePath = cat,
                        createdAtMs = 1710000000000L + (i * 1000)
                    )
                )
            }

            val grouped = sites.groupBy { it.category }
            val dao = object : ExportDao {
                override suspend fun categoriesSorted(): List<String> = categories.sorted()
                override suspend fun byCategory(category: String): List<Website> = grouped[category] ?: emptyList()
            }

            val testFile = File(context.cacheDir, "scale_50k_bookmarks.html")

            // 1. Export 50,000 links
            val exportStart = System.currentTimeMillis()
            BufferedOutputStream(FileOutputStream(testFile), 64 * 1024).use { out ->
                BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
            }
            val exportDurationMs = System.currentTimeMillis() - exportStart

            val fileSizeBytes = testFile.length()
            println("=== 50k SCALE BENCHMARK ===")
            println("Exported 50,000 links in: ${exportDurationMs} ms")
            println("File size bytes: $fileSizeBytes (${fileSizeBytes / (1024 * 1024)} MB)")
            assertTrue("File size must exceed 3 MB", fileSizeBytes > 3 * 1024 * 1024)

            // 2. Stream Parse 50,000 links
            val parseStart = System.currentTimeMillis()
            val parseResult = BufferedInputStream(FileInputStream(testFile), 64 * 1024).use { input ->
                BookmarkParser.parse(input)
            }
            val parseDurationMs = System.currentTimeMillis() - parseStart

            println("Parsed 50,000 links in: ${parseDurationMs} ms")
            println("Parsed count: ${parseResult.bookmarks.size}")
            println("Parser warnings: ${parseResult.warnings.size}")

            assertEquals(totalCount, parseResult.bookmarks.size)
            assertTrue("Export time must be reasonable (< 15 seconds)", exportDurationMs < 15_000)
            assertTrue("Parse time must be reasonable (< 15 seconds)", parseDurationMs < 15_000)

            testFile.delete()
        }
    }

    @Test
    fun testScale30MBFileWithEmbeddedBase64Icons_noOomAndResilient() {
        val testFile = File(context.cacheDir, "scale_30mb_icons.html")

        // Construct 30 MB HTML file with base64 embedded icons
        // Target 31 MB (~32,500,000 bytes)
        // Each entry has ~50 KB icon => ~600 links gives ~30 MB
        val iconSize = 50 * 1024
        val iconData = "B".repeat(iconSize)
        val linkCount = 600

        BufferedOutputStream(FileOutputStream(testFile), 64 * 1024).use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n")
                writer.write("<TITLE>30MB Scale Bookmarks</TITLE>\r\n")
                writer.write("<H1>Bookmarks</H1>\r\n")
                writer.write("<DL><p>\r\n")
                writer.write("    <DT><H3 ADD_DATE=\"1710000000\">Large Icons Folder</H3>\r\n")
                writer.write("    <DL><p>\r\n")

                for (i in 1..linkCount) {
                    writer.write("        <DT><A HREF=\"https://large-icon$i.example.com/item\" ADD_DATE=\"1710000000\" ICON=\"data:image/png;base64,$iconData\">Item #$i with Large Base64 Favicon</A>\r\n")
                }

                writer.write("    </DL><p>\r\n")
                writer.write("</DL><p>\r\n")
            }
        }

        val fileSizeBytes = testFile.length()
        val fileSizeMB = fileSizeBytes.toDouble() / (1024 * 1024)
        println("=== 30 MB EMBEDDED ICONS BENCHMARK ===")
        println("Generated file size: ${fileSizeBytes} bytes (${String.format("%.2f", fileSizeMB)} MB)")
        assertTrue("File must be at least 28 MB", fileSizeBytes >= 28 * 1024 * 1024)

        // Parse with streaming parser - verify memory bounded and no OOM
        val runtime = Runtime.getRuntime()
        System.gc()
        val memBefore = runtime.totalMemory() - runtime.freeMemory()

        val parseStart = System.currentTimeMillis()
        val parseResult = BufferedInputStream(FileInputStream(testFile), 64 * 1024).use { input ->
            BookmarkParser.parse(input)
        }
        val parseDurationMs = System.currentTimeMillis() - parseStart

        System.gc()
        val memAfter = runtime.totalMemory() - runtime.freeMemory()

        println("Parsed 30 MB file in: ${parseDurationMs} ms")
        println("Bookmarks extracted: ${parseResult.bookmarks.size}")
        println("Parser warnings: ${parseResult.warnings.size}")
        println("Memory diff: ${(memAfter - memBefore) / (1024 * 1024)} MB")

        assertEquals("All 600 links must be parsed", linkCount, parseResult.bookmarks.size)
        assertTrue("Parse time must be < 15 seconds", parseDurationMs < 15_000)

        testFile.delete()
    }

    @Test
    fun testScale50kRoomImport_andRoundTrip() {
        runBlocking {
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val totalCount = 50_000
            val categories = listOf("Development", "Media", "Shopping", "Reading", "Social", "Work", "Personal")
            val items = ArrayList<Website>(totalCount)
            for (i in 1..totalCount) {
                val cat = categories[i % categories.size]
                items.add(
                    Website(
                        id = 0,
                        url = "https://example${i % 1000}.com/page_$i",
                        title = "Bookmark $i",
                        category = cat,
                        dedupeKey = "https://example${i % 1000}.com/page_$i",
                        sourcePath = cat,
                        createdAtMs = 1710000000000L + (i * 1000)
                    )
                )
            }

            // Heap measurement before import
            val runtime = Runtime.getRuntime()
            System.gc()
            val heapBefore = runtime.totalMemory() - runtime.freeMemory()
            val memoryPools = try {
                ManagementFactory.getMemoryPoolMXBeans().filter { it.type == MemoryType.HEAP }
            } catch (_: Exception) {
                emptyList()
            }
            memoryPools.forEach { try { it.resetPeakUsage() } catch (_: Exception) {} }

            // 1. Bulk Database Import (Room SQLite chunked in 500s within transaction)
            val importStart = System.currentTimeMillis()
            val importResult = BookmarkImporter.importBookmarks(db, db.websiteDao(), items)
            val importDurationMs = System.currentTimeMillis() - importStart

            assertEquals(totalCount, importResult.inserted)
            assertEquals(0, importResult.duplicates)
            assertEquals(totalCount, db.websiteDao().getAllWebsitesList().size)

            // 2. Export Room database to HTML file
            val exportFile = File(context.cacheDir, "scale_50k_room_export.html")
            val exportStart = System.currentTimeMillis()
            BufferedOutputStream(FileOutputStream(exportFile), 64 * 1024).use { out ->
                BookmarkExporter.exportBookmarks(out, db.websiteDao(), 1710000000000L)
            }
            val exportDurationMs = System.currentTimeMillis() - exportStart

            // 3. Parse exported file back
            val parseStart = System.currentTimeMillis()
            val parseResult = BufferedInputStream(FileInputStream(exportFile), 64 * 1024).use { input ->
                BookmarkParser.parse(input)
            }
            val parseDurationMs = System.currentTimeMillis() - parseStart

            assertEquals(totalCount, parseResult.bookmarks.size)

            val peakHeapBytes = memoryPools.sumOf { it.peakUsage?.used ?: 0L }
            val peakHeapMB = peakHeapBytes.toDouble() / (1024 * 1024)

            System.gc()
            val heapAfter = runtime.totalMemory() - runtime.freeMemory()
            val heapDeltaMB = (heapAfter - heapBefore).toDouble() / (1024 * 1024)

            println("=== 50k ROOM IMPORT & ROUND TRIP BENCHMARK ===")
            println("50,000 Room Import time: ${importDurationMs} ms (~${(totalCount * 1000L) / importDurationMs} rows/sec)")
            println("50,000 Room Export time: ${exportDurationMs} ms")
            println("50,000 File Parse time: ${parseDurationMs} ms")
            println("Round-trip total time: ${importDurationMs + exportDurationMs + parseDurationMs} ms")
            println("Measured Retained Heap Delta: ${String.format("%.2f", heapDeltaMB)} MB")
            println("Measured Peak Heap Allocation: ${String.format("%.2f", peakHeapMB)} MB")

            exportFile.delete()
            } finally {
                db.close()
            }
        }
    }

    @Test
    fun testScale50kRealViewModelImportPath() {
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val viewModel = com.example.ui.viewmodel.WebsiteViewModel(app)

            val totalCount = 50_000
            val categories = listOf("Development", "Media", "Shopping", "Reading", "Social", "Work", "Personal")
            val bookmarks = ArrayList<ClassifiedBookmark>(totalCount)

            for (i in 1..totalCount) {
                val cat = categories[i % categories.size]
                val url = "https://vmexample${i % 1000}.com/page_$i"
                bookmarks.add(
                    ClassifiedBookmark(
                        url = url,
                        title = "VM Bookmark $i",
                        dedupeKey = url,
                        folderPath = listOf(cat),
                        createdAtMs = 1710000000000L + (i * 1000),
                        category = cat,
                        source = CategorySource.FOLDER,
                        sourcePath = cat,
                        host = "vmexample${i % 1000}.com",
                        registrableDomain = "vmexample${i % 1000}.com"
                    )
                )
            }

            val runtime = Runtime.getRuntime()
            System.gc()
            val heapBefore = runtime.totalMemory() - runtime.freeMemory()

            val start = System.currentTimeMillis()
            val result = viewModel.commitImportBookmarks(bookmarks)
            val duration = System.currentTimeMillis() - start

            assertTrue("ViewModel import must succeed", result.isSuccess)
            val importRes = result.getOrThrow()
            assertEquals(totalCount, importRes.inserted)

            System.gc()
            val heapAfter = runtime.totalMemory() - runtime.freeMemory()
            val heapDeltaMB = (heapAfter - heapBefore).toDouble() / (1024 * 1024)

            // Verify SharedPreferences is capped to exactly 50 items and didn't serialize all 50k
            val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
            val cachedJson = prefs.getString("cached_websites_json", null)
            val cachedCount = if (!cachedJson.isNullOrBlank()) org.json.JSONArray(cachedJson).length() else 0
            assertEquals("SharedPreferences cache must be capped at exactly 50 items", 50, cachedCount)

            println("=== 50k REAL VIEWMODEL IMPORT BENCHMARK ===")
            println("50,000 ViewModel commitImportBookmarks time: ${duration} ms (~${(totalCount * 1000L) / duration} rows/sec)")
            println("Cached items in SharedPreferences: $cachedCount (safely capped to exactly 50)")
            println("Heap Delta: ${String.format("%.2f", heapDeltaMB)} MB")
        }
    }

    @Test
    fun testSharedPreferencesCap_import200Items_restartsAndPreservesAll200() {
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()

            // Ensure clean database state
            resetDatabaseSingleton()
            app.deleteDatabase("webstack_database")

            // 1. Initial ViewModel run: import 200 items through ViewModel
            val vm1 = com.example.ui.viewmodel.WebsiteViewModel(app)
            val totalCount = 200
            val classified = (1..totalCount).map { i ->
                ClassifiedBookmark(
                    url = "https://example200.com/page_$i",
                    title = "Bookmark 200 #$i",
                    dedupeKey = "https://example200.com/page_$i",
                    folderPath = listOf("Work"),
                    createdAtMs = 1710000000000L + i,
                    category = "Work",
                    source = CategorySource.FOLDER,
                    sourcePath = "Work",
                    host = "example200.com",
                    registrableDomain = "example200.com"
                )
            }

            val importResult = vm1.commitImportBookmarks(classified)
            assertTrue("ViewModel import must succeed", importResult.isSuccess)
            assertEquals(200, importResult.getOrThrow().inserted)

            org.robolectric.shadows.ShadowLooper.idleMainLooper()

            // Verify SharedPreferences cache is capped to exactly 50 items
            val cachedJson = prefs.getString("cached_websites_json", null)
            assertTrue("Cache JSON must exist in SharedPreferences", !cachedJson.isNullOrBlank())
            val cachedArray = org.json.JSONArray(cachedJson)
            assertEquals("SharedPreferences cache must be capped at exactly 50 items", 50, cachedArray.length())

            // 2. Simulate complete app restart / process death:
            // Close database singleton to simulate fresh process
            resetDatabaseSingleton()

            // 3. New ViewModel instance created on app cold start
            val restartedVm = com.example.ui.viewmodel.WebsiteViewModel(app)

            // Cold start initial value (from SharedPreferences) contains the capped 50 items
            // providing instant UI display without disk stall
            val coldStartInitialValue = restartedVm.websitesList.value
            assertEquals(
                "Cold-start initial value from SharedPreferences cache must have capped 50 items",
                50,
                coldStartInitialValue?.size
            )

            org.robolectric.shadows.ShadowLooper.idleMainLooper()

            // As Room asynchronous query flow emits on cold-start, websitesList receives all 200 items
            var fullWebsitesAfterRestart: List<Website>? = null
            for (i in 0 until 50) {
                org.robolectric.shadows.ShadowLooper.idleMainLooper()
                val current = restartedVm.websitesList.value
                if (current != null && current.size == 200) {
                    fullWebsitesAfterRestart = current
                    break
                }
                Thread.sleep(50)
            }

            assertEquals(
                "ViewModel's cold-start websitesList must contain all 200 items after restart",
                200,
                fullWebsitesAfterRestart?.size
            )

            // Confirm Room database also contains all 200 rows intact
            assertEquals(200, restartedVm.websiteDao.getAllWebsitesList().size)

            // Clean up
            resetDatabaseSingleton()
            app.deleteDatabase("webstack_database")
        }
    }

    private fun resetDatabaseSingleton() {
        try {
            val field = com.example.data.WebsiteDatabase::class.java.getDeclaredField("INSTANCE")
            field.isAccessible = true
            val db = field.get(null) as? com.example.data.WebsiteDatabase
            db?.close()
            field.set(null, null)
        } catch (_: Exception) {}
    }
}
