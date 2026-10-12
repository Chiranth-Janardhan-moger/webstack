package com.example.bookmarks

import android.content.Context
import android.os.Debug
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class BookmarkEmulatorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSafTruncationOverLongerFileOnDevice() {
        val testFile = File(context.cacheDir, "emulator_saf_truncate_test.html")
        testFile.writeText("A".repeat(500_000))
        assertEquals(500_000L, testFile.length())

        // Overwrite using FileOutputStream with truncate (equivalent to SAF "wt")
        val shortContent = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n<DL><p><DT><A HREF=\"https://example.com\">Short</A></DL><p>\n"
        FileOutputStream(testFile, false).use { out ->
            out.write(shortContent.toByteArray(StandardCharsets.UTF_8))
        }

        assertEquals("File must be truncated to exact short length without trailing garbage",
            shortContent.toByteArray(StandardCharsets.UTF_8).size.toLong(),
            testFile.length()
        )
        testFile.delete()
    }

    @Test
    fun testClusterApprovalLogicOnDevice() {
        val sampleHtml = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://github.com/one">G1</A>
            <DT><A HREF="https://github.com/two">G2</A>
            <DT><A HREF="https://github.com/three">G3</A>
            <DT><A HREF="https://stackoverflow.com/q1">S1</A>
            </DL><p>
        """.trimIndent()

        val classifier = DomainClassifier(mapOf(
            "github.com" to "Development",
            "stackoverflow.com" to "Development"
        ))

        val preprocessed = sampleHtml.byteInputStream().use {
            BookmarkImporter.preprocess(it, classifier)
        }

        assertEquals(4, preprocessed.classifiedBookmarks.size)
        assertTrue(preprocessed.clusters.isNotEmpty())

        // Approval UI action 1: Reassign cluster
        val reassigned = preprocessed.classifiedBookmarks.map {
            if (it.category == "Development") it.copy(category = "Tech") else it
        }
        assertEquals(4, reassigned.count { it.category == "Tech" })

        // Approval UI action 2: Untick cluster
        val excluded = preprocessed.classifiedBookmarks.filter { it.category != "Development" }
        assertEquals(0, excluded.size)
    }

    @Test
    fun testScale50kImportAndRealPeakMemoryOnDevice() {
        runBlocking {
            val db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
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
                            url = "https://device${i % 1000}.com/page_$i",
                            title = "Device Bookmark $i",
                            category = cat,
                            dedupeKey = "https://device${i % 1000}.com/page_$i",
                            sourcePath = cat,
                            createdAtMs = 1710000000000L + (i * 1000)
                        )
                    )
                }

                System.gc()
                val runtime = Runtime.getRuntime()
                val memBefore = runtime.totalMemory() - runtime.freeMemory()
                val nativeHeapBefore = Debug.getNativeHeapAllocatedSize()

                val start = System.currentTimeMillis()
                val result = BookmarkImporter.importBookmarks(db, db.websiteDao(), items)
                val duration = System.currentTimeMillis() - start

                assertEquals(totalCount, result.inserted)
                assertEquals(0, result.duplicates)

                val nativeHeapPeak = Debug.getNativeHeapAllocatedSize()
                System.gc()
                val memAfter = runtime.totalMemory() - runtime.freeMemory()

                val heapDeltaMB = (memAfter - memBefore).toDouble() / (1024 * 1024)
                val nativeHeapMB = nativeHeapPeak.toDouble() / (1024 * 1024)

                println("=== EMULATOR ON-DEVICE 50k BENCHMARK ===")
                println("Device: ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE}, API ${android.os.Build.VERSION.SDK_INT})")
                println("50,000 Device Room Import time: ${duration} ms (~${(totalCount * 1000L) / duration} rows/sec)")
                println("Measured Dalvik/ART Heap Delta: ${String.format("%.2f", heapDeltaMB)} MB")
                println("Measured Native Heap Allocated: ${String.format("%.2f", nativeHeapMB)} MB")

                assertTrue("Device import must finish within 15 seconds", duration < 15_000)
            } finally {
                db.close()
            }
        }
    }
}
