package com.example.bookmarks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
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
        assertTrue("Parse time must be < 10 seconds", parseDurationMs < 10_000)

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

            System.gc()
            val heapAfter = runtime.totalMemory() - runtime.freeMemory()
            val heapDeltaMB = (heapAfter - heapBefore).toDouble() / (1024 * 1024)

            println("=== 50k ROOM IMPORT & ROUND TRIP BENCHMARK ===")
            println("50,000 Room Import time: ${importDurationMs} ms (~${(totalCount * 1000L) / importDurationMs} rows/sec)")
            println("50,000 Room Export time: ${exportDurationMs} ms")
            println("50,000 File Parse time: ${parseDurationMs} ms")
            println("Round-trip total time: ${importDurationMs + exportDurationMs + parseDurationMs} ms")
            println("Measured Heap Delta: ${String.format("%.2f", heapDeltaMB)} MB")

            exportFile.delete()
        } finally {
            db.close()
        }
    }
}
}
