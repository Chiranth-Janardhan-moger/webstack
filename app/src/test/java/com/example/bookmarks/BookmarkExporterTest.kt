package com.example.bookmarks

import com.example.data.Website
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class BookmarkExporterTest {

    private class FakeExportDao(
        private val categories: List<String>,
        private val sitesByCategory: Map<String, List<Website>>
    ) : ExportDao {
        override suspend fun categoriesSorted(): List<String> = categories
        override suspend fun byCategory(category: String): List<Website> =
            sitesByCategory[category] ?: emptyList()
    }

    @Test
    fun testEmptyDatabase_producesValidBalancedHtml() = runBlocking {
        val dao = FakeExportDao(emptyList(), emptyMap())
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)

        val html = out.toString(StandardCharsets.UTF_8.name())
        assertTrue(html.startsWith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue(html.contains("<DL><p>"))
        assertTrue(html.trimEnd().endsWith("</DL><p>"))

        // DL tag balance
        val opens = "<DL><p>".toRegex().findAll(html).count()
        val closes = "</DL><p>".toRegex().findAll(html).count()
        assertEquals(opens, closes)
    }

    @Test
    fun testEscapingSpecialCharactersAndNewlines() = runBlocking {
        val cat = "Dev & <Tools> '\""
        val site = Website(
            id = 1,
            url = "https://example.com?a=1&b=2&name=\"test\"",
            title = "Special <Title> & 'Quotes' \"Double\"\nNext Line",
            category = cat,
            dedupeKey = "https://example.com?a=1&b=2&name=\"test\"",
            createdAtMs = 1710000000000L
        )
        val dao = FakeExportDao(listOf(cat), mapOf(cat to listOf(site)))
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)

        val html = out.toString(StandardCharsets.UTF_8.name())

        // Check category escaped
        assertTrue(html.contains("Dev &amp; &lt;Tools&gt; &#39;&quot;"))

        // Check title escaped and newline replaced by space
        assertTrue(html.contains("Special &lt;Title&gt; &amp; &#39;Quotes&#39; &quot;Double&quot; Next Line"))

        // Check URL escaped
        assertTrue(html.contains("https://example.com?a=1&amp;b=2&amp;name=&quot;test&quot;"))

        // Check ADD_DATE is 10 digits
        assertTrue(html.contains("ADD_DATE=\"1710000000\""))
    }

    @Test
    fun testUncategorizedLinksGoToRootAfterFolders() = runBlocking {
        val dao = FakeExportDao(
            categories = listOf("Dev", "Uncategorized"),
            sitesByCategory = mapOf(
                "Dev" to listOf(
                    Website(1, "https://dev.com", "Dev Link", "Dev", "https://dev.com", null, 1710000000000L)
                ),
                "Uncategorized" to listOf(
                    Website(2, "https://uncat.com", "Uncat Link", "Uncategorized", "https://uncat.com", null, 1710000000000L)
                )
            )
        )
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)

        val html = out.toString(StandardCharsets.UTF_8.name())

        val devFolderIdx = html.indexOf("<H3 ADD_DATE=\"1710000000\" LAST_MODIFIED=\"1710000000\">Dev</H3>")
        val devCloseIdx = html.indexOf("</DL><p>", devFolderIdx)
        val uncatLinkIdx = html.indexOf("https://uncat.com")

        assertTrue("Dev folder must appear in output", devFolderIdx != -1)
        assertTrue("Dev folder must close before root uncategorized link", devCloseIdx < uncatLinkIdx)
    }
}
