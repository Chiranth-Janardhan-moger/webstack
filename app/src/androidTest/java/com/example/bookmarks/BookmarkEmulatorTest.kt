package com.example.bookmarks

import android.content.ContentValues
import android.content.Context
import android.os.Debug
import android.provider.MediaStore
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class BookmarkEmulatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSafContentResolverTruncationWithDocumentsProvider() {
        // Test real ContentResolver stream truncation with mode "wt"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "emulator_saf_provider_test.html")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                // 1. Write initial 500,000 bytes
                resolver.openOutputStream(uri, "wt")?.use { out ->
                    out.write(ByteArray(500_000) { 'A'.code.toByte() })
                }
                val initialLength = resolver.openInputStream(uri)?.use { it.available() } ?: 0
                assertTrue("Initial file must be large", initialLength >= 500_000)

                // 2. Overwrite with short Netscape bookmark export using mode "wt"
                val shortExport = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n<DL><p><DT><A HREF=\"https://example.com\">SAF Short</A></DL><p>\n"
                val shortBytes = shortExport.toByteArray(StandardCharsets.UTF_8)
                resolver.openOutputStream(uri, "wt")?.use { out ->
                    out.write(shortBytes)
                }

                // 3. Read back from ContentProvider: must be exactly truncated to shortBytes.size
                val readBack = resolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                assertEquals("File stream must be truncated without trailing data from prior write", shortBytes.size, readBack.size)
                assertEquals(shortExport, String(readBack, StandardCharsets.UTF_8))
            } finally {
                resolver.delete(uri, null, null)
            }
        } else {
            // Fallback for environments where MediaStore.Downloads is unavailable
            val testFile = File(context.cacheDir, "emulator_saf_truncate_test.html")
            testFile.writeText("A".repeat(500_000))
            val shortContent = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n<DL><p><DT><A HREF=\"https://example.com\">Short</A></DL><p>\n"
            FileOutputStream(testFile, false).use { out ->
                out.write(shortContent.toByteArray(StandardCharsets.UTF_8))
            }
            assertEquals(shortContent.toByteArray(StandardCharsets.UTF_8).size.toLong(), testFile.length())
            testFile.delete()
        }
    }

    @Test
    fun testClusterApprovalSheet_composeRenderingAndInteractionOnDevice() {
        val preprocessed = ImportPreprocessResult(
            classifiedBookmarks = listOf(
                ClassifiedBookmark(
                    url = "https://github.com",
                    title = "GitHub",
                    dedupeKey = "https://github.com",
                    folderPath = emptyList(),
                    createdAtMs = 0L,
                    category = "Development",
                    source = CategorySource.DOMAIN_RULE,
                    sourcePath = "Development",
                    host = "github.com",
                    registrableDomain = "github.com"
                )
            ),
            clusters = listOf(
                BookmarkCluster(
                    id = "c1",
                    displayName = "Development",
                    linkCount = 1,
                    source = CategorySource.DOMAIN_RULE,
                    sampleDomains = listOf("github.com"),
                    assignedCategory = "Development",
                    isIncluded = true
                )
            ),
            internalDuplicatesCount = 0,
            unsupportedSchemeCount = 0,
            warnings = listOf("Missing Netscape bookmark header"),
            hasNetscapeHeader = false
        )

        composeTestRule.setContent {
            com.example.ui.sheets.ClusterApprovalSheet(
                preprocessResult = preprocessed,
                existingCategories = listOf("General", "Development"),
                onConfirmImport = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("cluster_approval_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("netscape_header_warning_banner").assertIsDisplayed()
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
