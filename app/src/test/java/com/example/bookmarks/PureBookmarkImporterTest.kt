package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class PureBookmarkImporterTest {

    private val classifier = DomainClassifier(
        mapOf(
            "github.com" to "Development",
            "reddit.com" to "Social"
        )
    )

    @Test
    fun testPreprocess_filteringAndClustering() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <TITLE>Bookmarks</TITLE>
            <H1>Bookmarks</H1>
            <DL><p>
                <DT><H3>Work</H3>
                <DL><p>
                    <DT><A HREF="https://github.com/repo1" ADD_DATE="1710000000">Repo 1</A>
                    <DT><A HREF="https://github.com/repo1?utm_campaign=xyz" ADD_DATE="1710000000">Repo 1 Dupe</A>
                    <DT><A HREF="javascript:alert(1)">Bad Scheme</A>
                    <DT><A HREF="chrome://flags">Chrome URL</A>
                </DL><p>
                <DT><A HREF="https://reddit.com/r/android" ADD_DATE="1710000000">Reddit</A>
                <DT><A HREF="https://www.google.com/search">Google WWW</A>
                <DT><A HREF="https://google.com/maps">Google Non-WWW</A>
                <DT><A HREF="https://google.com/news">Google Cluster 3</A>
            </DL><p>
        """.trimIndent()

        val result = BookmarkImporter.preprocess(
            ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)),
            classifier
        )

        assertEquals(5, result.classifiedBookmarks.size)
        assertEquals(1, result.internalDuplicatesCount)
        assertEquals(2, result.unsupportedSchemeCount)
        assertTrue(result.warnings.isEmpty())

        // Verify clusters
        assertNotNull(result.clusters)
        assertTrue(result.clusters.size >= 3)

        // Verify ordering: descending by link count
        for (i in 0 until result.clusters.size - 1) {
            assertTrue(result.clusters[i].linkCount >= result.clusters[i + 1].linkCount)
        }

        val googleCluster = result.clusters.find { it.displayName == "Google" }
        assertNotNull(googleCluster)
        assertEquals(3, googleCluster!!.linkCount)
        assertEquals(listOf("google.com"), googleCluster.sampleDomains)
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
    fun testExecuteBatchInsert_chunksAndProgress() = kotlinx.coroutines.runBlocking {
        val items = (1..1200).map { i ->
            com.example.data.Website(
                id = 0,
                url = "https://example.com/site_$i",
                title = "Title $i",
                category = "General",
                dedupeKey = "https://example.com/site_$i",
                sourcePath = null,
                createdAtMs = 0L
            )
        }

        val progress = mutableListOf<Pair<Int, Int>>()
        val result = BookmarkImporter.executeBatchInsert(
            items = items,
            insertChunk = { chunk -> chunk.map { 1L } },
            onProgress = { done, total -> progress.add(Pair(done, total)) }
        )

        assertEquals(1200, result.inserted)
        assertEquals(0, result.duplicates)
        assertEquals(3, progress.size)
        assertEquals(Pair(500, 1200), progress[0])
        assertEquals(Pair(1000, 1200), progress[1])
        assertEquals(Pair(1200, 1200), progress[2])
    }

    @Test
    fun testExecuteBatchInsert_withDuplicates() = kotlinx.coroutines.runBlocking {
        val items = (1..10).map { i ->
            com.example.data.Website(
                id = 0,
                url = "https://example.com/site_$i",
                title = "Title $i",
                category = "General",
                dedupeKey = "https://example.com/site_$i",
                sourcePath = null,
                createdAtMs = 0L
            )
        }

        // Half inserted (1L), half duplicate (-1L)
        val result = BookmarkImporter.executeBatchInsert(
            items = items,
            insertChunk = { chunk -> chunk.mapIndexed { idx, _ -> if (idx % 2 == 0) 1L else -1L } }
        )

        assertEquals(5, result.inserted)
        assertEquals(5, result.duplicates)
    }

    @Test
    fun testBuildClusters_mixedSourcesAndEmptyHost() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>MultiSource</H3>
                <DL><p>
                    <DT><A HREF="https://site1.org/1">A</A>
                    <DT><A HREF="https://site1.org/2">B</A>
                    <DT><A HREF="https://site2.org/1">C</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val res = BookmarkImporter.preprocess(
            ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)),
            classifier
        )
        assertNotNull(res.clusters)
        assertEquals(1, res.clusters.size)
        assertEquals(CategorySource.FOLDER, res.clusters[0].source)
        assertEquals(3, res.clusters[0].linkCount)
    }

    @Test
    fun testBuildClusters_allHostAndSourceVariants() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>FolderCluster</H3>
                <DL><p>
                    <DT><A HREF="https://site.org/1">Folder Link</A>
                </DL><p>
                <DT><A HREF="https://github.com/1">Domain Rule Link</A>
                <DT><A HREF="https://3dprint.net/1">Cluster Link 1</A>
                <DT><A HREF="https://3dprint.net/2">Cluster Link 2</A>
                <DT><A HREF="https://3dprint.net/3">Cluster Link 3</A>
                <DT><A HREF="https://rare-single-link.org/1">Uncategorized Link</A>
            </DL><p>
        """.trimIndent()
        val res = BookmarkImporter.preprocess(ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)), classifier)
        val sources = res.clusters.map { it.source }.toSet()
        assertTrue(sources.contains(CategorySource.FOLDER))
        assertTrue(sources.contains(CategorySource.DOMAIN_RULE))
        assertTrue(sources.contains(CategorySource.CLUSTER))
        assertTrue(sources.contains(CategorySource.UNCATEGORIZED))
    }
}
