package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class BookmarkParserTest {

    @Test
    fun testEmptyFile_returnsZeroBookmarksWithoutCrashing() {
        val stream = ByteArrayInputStream(ByteArray(0))
        val result = BookmarkParser.parse(stream)
        assertEquals(0, result.bookmarks.size)
        assertEquals(0, result.malformedTagCount)
    }

    @Test
    fun testBomAndCrlf_strippedAndParsedCorrectly() {
        val html = "\uFEFF<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n<DL><p>\r\n<DT><A HREF=\"https://example.com\">Example</A>\r\n</DL><p>\r\n"
        val stream = ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8))
        val result = BookmarkParser.parse(stream)
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://example.com", result.bookmarks[0].url)
        assertEquals("Example", result.bookmarks[0].title)
    }

    @Test
    fun testHtmlEntities_decodedAccurately() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://example.com?a=1&amp;b=2">C++ &amp; Java &lt;Guide&gt; &#39;Special&#39; &quot;Quotes&quot; &#x1F600; 日本語 عربي</A>
            </DL><p>
        """.trimIndent()
        val stream = ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8))
        val result = BookmarkParser.parse(stream)
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://example.com?a=1&b=2", result.bookmarks[0].url)
        assertEquals("C++ & Java <Guide> 'Special' \"Quotes\" \uD83D\uDE00 日本語 عربي", result.bookmarks[0].title)
    }

    @Test
    fun testCaseInsensitiveTagsAndAttributes() {
        val html = """
            <!DOCTYPE netscape-bookmark-file-1>
            <dl><p>
            <dt><h3 add_date='1700000000'>Sub Folder</h3>
            <dl><p>
            <dt><a href='https://example.org' add_date="1700000000">Example Org</a>
            </dl><p>
            </dl><p>
        """.trimIndent()
        val stream = ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8))
        val result = BookmarkParser.parse(stream)
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://example.org", result.bookmarks[0].url)
        assertEquals("Example Org", result.bookmarks[0].title)
        assertEquals(listOf("Sub Folder"), result.bookmarks[0].folderPath)
    }

    @Test
    fun testUnquotedAttributes() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF=https://example.net ADD_DATE=1700000000>Unquoted Link</A>
            </DL><p>
        """.trimIndent()
        val stream = ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8))
        val result = BookmarkParser.parse(stream)
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://example.net", result.bookmarks[0].url)
        assertEquals("Unquoted Link", result.bookmarks[0].title)
    }

    @Test
    fun testMissingClosingATag_emitsOnNextA_onDL_andOnEof() {
        // Missing </A> before next <A>
        val html1 = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://site1.com">Site 1
            <DT><A HREF="https://site2.com">Site 2
            </DL><p>
        """.trimIndent()
        val result1 = BookmarkParser.parse(ByteArrayInputStream(html1.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(2, result1.bookmarks.size)
        assertEquals("https://site1.com", result1.bookmarks[0].url)
        assertTrue(result1.bookmarks[0].title.contains("Site 1"))
        assertEquals("https://site2.com", result1.bookmarks[1].url)
        assertTrue(result1.bookmarks[1].title.contains("Site 2"))

        // Missing </A> at EOF
        val html2 = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://site3.com">Site 3
        """.trimIndent()
        val result2 = BookmarkParser.parse(ByteArrayInputStream(html2.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(1, result2.bookmarks.size)
        assertEquals("https://site3.com", result2.bookmarks[0].url)
        assertTrue(result2.bookmarks[0].title.contains("Site 3"))
    }

    @Test
    fun testBrowserRoots_flaggedAsNoise() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><H3 PERSONAL_TOOLBAR_FOLDER="true">Barre de favoris</H3>
            <DL><p>
                <DT><H3>Real Subfolder</H3>
                <DL><p>
                    <DT><A HREF="https://sub.com">Sub Link</A>
                </DL><p>
            </DL><p>
            <DT><H3 UNFILED_BOOKMARKS_FOLDER="true">Lesezeichen-Menü</H3>
            <DL><p>
                <DT><A HREF="https://unfiled.com">Unfiled Link</A>
            </DL><p>
            </DL><p>
        """.trimIndent()
        val result = BookmarkParser.parse(ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(2, result.bookmarks.size)
        // Sub Link: parent "Barre de favoris" was skipped as browser root, so folderPath contains only Real Subfolder
        assertEquals(listOf("Real Subfolder"), result.bookmarks[0].folderPath)
        // Unfiled Link: folderPath is empty because UNFILED_BOOKMARKS_FOLDER was skipped
        assertEquals(emptyList<String>(), result.bookmarks[1].folderPath)
    }

    @Test
    fun testLargeBase64Icon_ignoredWithoutOom() {
        val largeBase64 = "B".repeat(100 * 1024) // 100 KB base64
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://heavy.com" ICON="data:image/png;base64,$largeBase64">Heavy Icon Link</A>
            </DL><p>
        """.trimIndent()
        val result = BookmarkParser.parse(ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://heavy.com", result.bookmarks[0].url)
        assertEquals("Heavy Icon Link", result.bookmarks[0].title)
    }

    @Test
    fun testCorruptAndRandomGarbage_doesNotCrash() {
        val garbage = "<html><body>Some random binary or corrupt text <<<<<<>>>>> <unknown> random </body></html>"
        val result = BookmarkParser.parse(ByteArrayInputStream(garbage.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(0, result.bookmarks.size)
    }
}
