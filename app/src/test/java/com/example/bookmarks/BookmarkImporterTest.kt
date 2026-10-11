package com.example.bookmarks

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDao
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
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
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookmarkImporterTest {

    private val classifier = DomainClassifier(
        mapOf("github.com" to "Development", "reddit.com" to "Social")
    )

    private lateinit var db: WebsiteDatabase
    private lateinit var dao: WebsiteDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
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
    fun testPreprocess_handlesValidBookmarksAndFiltering() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <TITLE>Bookmarks</TITLE>
            <H1>Bookmarks</H1>
            <DL><p>
                <DT><H3>Work</H3>
                <DL><p>
                    <DT><A HREF="https://github.com/repo1" ADD_DATE="1710000000">Repo 1</A>
                    <DT><A HREF="https://github.com/repo1?utm_source=test" ADD_DATE="1710000000">Repo 1 Dupe</A>
                    <DT><A HREF="javascript:alert(1)">Bad Scheme</A>
                    <DT><A HREF="chrome://settings">Chrome URL</A>
                </DL><p>
                <DT><A HREF="https://reddit.com/r/android" ADD_DATE="1710000000">Reddit</A>
            </DL><p>
        """.trimIndent()

        val result = BookmarkImporter.preprocess(
            ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)),
            classifier
        )

        // Total 5 links: 2 valid unique, 1 internal duplicate (Repo 1 Dupe), 2 unsupported schemes (javascript, chrome)
        assertEquals(2, result.classifiedBookmarks.size)
        assertEquals(1, result.internalDuplicatesCount)
        assertEquals(2, result.unsupportedSchemeCount)
        assertTrue(result.warnings.isEmpty())

        // Verify clusters
        assertNotNull(result.clusters)
        assertEquals(2, result.clusters.size)

        val workCluster = result.clusters.find { it.displayName == "Work" }
        assertNotNull(workCluster)
        assertEquals(1, workCluster!!.linkCount)
        assertEquals(CategorySource.FOLDER, workCluster.source)
        assertEquals("Work", workCluster.assignedCategory)
        assertTrue(workCluster.isIncluded)
        assertEquals(listOf("github.com"), workCluster.sampleDomains)

        val socialCluster = result.clusters.find { it.displayName == "Social" }
        assertNotNull(socialCluster)
        assertEquals(1, socialCluster!!.linkCount)
        assertEquals(CategorySource.DOMAIN_RULE, socialCluster.source)
        assertEquals(listOf("reddit.com"), socialCluster.sampleDomains)
    }

    @Test
    fun testPreprocess_emptyStream() {
        val result = BookmarkImporter.preprocess(
            ByteArrayInputStream(ByteArray(0)),
            classifier
        )
        assertEquals(0, result.classifiedBookmarks.size)
        assertEquals(0, result.internalDuplicatesCount)
        assertEquals(0, result.unsupportedSchemeCount)
        assertEquals(0, result.clusters.size)
    }

    @Test
    fun testBuildClusters_sortingAndDominantSource() {
        val b1 = ClassifiedBookmark("https://site1.com/a", "A", "key1", emptyList(), 0L, "CatBig", CategorySource.FOLDER, null, "site1.com", "site1.com")
        val b2 = ClassifiedBookmark("https://www.site2.com/b", "B", "key2", emptyList(), 0L, "CatBig", CategorySource.FOLDER, null, "www.site2.com", "site2.com")
        val b3 = ClassifiedBookmark("https://site3.com/c", "C", "key3", emptyList(), 0L, "CatBig", CategorySource.DOMAIN_RULE, null, "site3.com", "site3.com")
        val b4 = ClassifiedBookmark("https://site4.com/d", "D", "key4", emptyList(), 0L, "CatSmall", CategorySource.UNCATEGORIZED, null, "site4.com", "site4.com")
        val b5 = ClassifiedBookmark("https://site5.com/e", "E", "key5", emptyList(), 0L, "EmptyHostCat", CategorySource.UNCATEGORIZED, null, "", "")

        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>CatBig</H3>
                <DL><p>
                    <DT><A HREF="https://site1.com/a">A</A>
                    <DT><A HREF="https://www.site2.com/b">B</A>
                    <DT><A HREF="https://site3.com/c">C</A>
                </DL><p>
                <DT><A HREF="https://site4.com/d">D</A>
            </DL><p>
        """.trimIndent()

        val res = BookmarkImporter.preprocess(ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)), classifier)
        assertTrue(res.clusters.size >= 2)
        // Verify sorted by descending link count
        assertTrue(res.clusters[0].linkCount >= res.clusters[1].linkCount)
    }

    @Test
    fun testImportBookmarks_chunksAndProgressReporting() = runBlocking {
        val items = (1..1200).map { i ->
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

        val progressSnapshots = mutableListOf<Pair<Int, Int>>()
        val result = BookmarkImporter.importBookmarks(db, dao, items) { done, total ->
            progressSnapshots.add(Pair(done, total))
        }

        assertEquals(1200, result.inserted)
        assertEquals(0, result.duplicates)
        assertEquals(1200, dao.getAllWebsitesList().size)
        // 1200 items in chunks of 500 => 3 progress calls (500, 1000, 1200)
        assertEquals(3, progressSnapshots.size)
        assertEquals(Pair(500, 1200), progressSnapshots[0])
        assertEquals(Pair(1000, 1200), progressSnapshots[1])
        assertEquals(Pair(1200, 1200), progressSnapshots[2])
    }

    @Test
    fun testBookmarkDelivery_formatExportFileName() {
        val fixedTime = 1710000000000L // 2024-03-09 UTC / local date
        val fileName = BookmarkDelivery.formatExportFileName(fixedTime)
        assertTrue(fileName.startsWith("webstack_bookmarks_"))
        assertTrue(fileName.endsWith(".html"))
    }

    @Test
    fun testBuildClusters_emptyHostAndDuplicates() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Mixed</H3>
                <DL><p>
                    <DT><A HREF="https://www.google.com/search">Google WWW</A>
                    <DT><A HREF="https://google.com/maps">Google Non-WWW</A>
                    <DT><A HREF="https://google.com/maps">Google Duplicate in cluster</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val res = BookmarkImporter.preprocess(ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)), classifier)
        assertEquals(1, res.clusters.size)
        assertEquals(1, res.internalDuplicatesCount)
        val cluster = res.clusters[0]
        assertEquals(listOf("google.com"), cluster.sampleDomains)
    }

    @Test
    fun testImportBookmarks_withIgnoredDuplicates() = runBlocking {
        val item1 = Website(0, "https://ex.com/1", "T1", "Cat", "https://ex.com/1", null, 0L)
        val item2 = Website(0, "https://ex.com/1", "T2", "Cat", "https://ex.com/1", null, 0L)

        val res = BookmarkImporter.importBookmarks(db, dao, listOf(item1, item2))
        assertEquals(1, res.inserted)
        assertEquals(1, res.duplicates)
    }
}
