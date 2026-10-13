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

    @Test
    fun testMissingNetscapeHeader_emitsWarningAndSetsFlag() {
        val html = """
            <html><body>
            <DL><p>
            <DT><A HREF="https://example.com">Non Netscape Bookmark</A>
            </DL><p>
            </body></html>
        """.trimIndent()
        val result = BookmarkParser.parse(ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(1, result.bookmarks.size)
        org.junit.Assert.assertFalse("Must flag false when Netscape header missing", result.hasNetscapeHeader)
        assertTrue("Must include missing header warning", result.warnings.any { it.contains("Missing Netscape bookmark header") })
    }

    @Test
    fun testValidNetscapeHeader_hasNetscapeHeaderIsTrue() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><A HREF="https://example.com">Valid Bookmark</A>
            </DL><p>
        """.trimIndent()
        val result = BookmarkParser.parse(ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(1, result.bookmarks.size)
        assertTrue("Must flag true when Netscape header present", result.hasNetscapeHeader)
        assertTrue("Must not contain missing header warning", result.warnings.none { it.contains("Missing Netscape bookmark header") })
    }

    @Test
    fun testLegacyWindows1252WithMetaTag_decodesAccurately() {
        // Construct raw bytes in windows-1252 containing non-ASCII characters:
        // Café München “Zürich” — Über & Co
        // é: 0xE9, ü: 0xFC, “: 0x93, ”: 0x94, —: 0x97, Ü: 0xDC
        val rawHtml = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=windows-1252\">\r\n" +
                "<DL><p>\r\n" +
                "<DT><A HREF=\"https://example.de\">Café München “Zürich” — Über & Co</A>\r\n" +
                "</DL><p>\r\n"
        val bytes = rawHtml.toByteArray(java.nio.charset.Charset.forName("windows-1252"))
        val result = BookmarkParser.parse(ByteArrayInputStream(bytes))
        assertEquals(1, result.bookmarks.size)
        assertEquals("Café München “Zürich” — Über & Co", result.bookmarks[0].title)
        assertTrue("Must not produce replacement characters", !result.bookmarks[0].title.contains("\uFFFD"))
    }

    @Test
    fun testLegacyWindows1252WithoutMetaTag_fallsBackToWindows1252() {
        // Raw bytes with non-ASCII single byte without META tag or BOM
        val rawHtml = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n" +
                "<DL><p>\r\n" +
                "<DT><A HREF=\"https://legacy.fr\">Café de Paris</A>\r\n" +
                "</DL><p>\r\n"
        val bytes = rawHtml.toByteArray(java.nio.charset.Charset.forName("windows-1252"))
        val result = BookmarkParser.parse(ByteArrayInputStream(bytes))
        assertEquals(1, result.bookmarks.size)
        assertEquals("Café de Paris", result.bookmarks[0].title)
        assertTrue("Must not produce replacement characters", !result.bookmarks[0].title.contains("\uFFFD"))
    }

    @Test
    fun testMetaTagWithInvalidCharset_fallsBackToWindows1252() {
        val rawHtml = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=unsupported_bogus_charset\">\r\n" +
                "<DL><p>\r\n" +
                "<DT><A HREF=\"https://example.com\">Fallback Test</A>\r\n" +
                "</DL><p>\r\n"
        val bytes = rawHtml.toByteArray(StandardCharsets.UTF_8)
        val result = BookmarkParser.parse(ByteArrayInputStream(bytes))
        assertEquals(1, result.bookmarks.size)
        assertEquals("Fallback Test", result.bookmarks[0].title)
    }

    @Test
    fun testUtf8EmojiStraddles8192Boundary_decodesAccurately() {
        // Construct stream where a 4-byte UTF-8 emoji straddles byte 8192:
        // Bytes 0..8189: ASCII prefix (8190 bytes)
        // Bytes 8190..8191: First 2 bytes of grinning face \uD83D\uDE00 (0xF0, 0x9F)
        // Bytes 8192..8193: Remaining 2 bytes (0x98, 0x80)
        // Followed by closing tags
        val prefix = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n<DL><p>\r\n<DT><A HREF=\"https://boundary.example.com\">"
        val prefixBytes = prefix.toByteArray(StandardCharsets.US_ASCII)
        val paddingLen = 8190 - prefixBytes.size
        val padding = "A".repeat(paddingLen).toByteArray(StandardCharsets.US_ASCII)

        val emojiBytes = "\uD83D\uDE00".toByteArray(StandardCharsets.UTF_8) // 4 bytes: F0 9F 98 80
        assertEquals(4, emojiBytes.size)

        val suffix = "</A>\r\n</DL><p>\r\n"
        val suffixBytes = suffix.toByteArray(StandardCharsets.US_ASCII)

        val totalBytes = prefixBytes + padding + emojiBytes + suffixBytes
        assertEquals(8190, prefixBytes.size + padding.size)
        // Emoji starts exactly at index 8190 and extends to 8194, straddling boundary 8192

        val result = BookmarkParser.parse(ByteArrayInputStream(totalBytes))
        assertEquals(1, result.bookmarks.size)
        assertEquals("https://boundary.example.com", result.bookmarks[0].url)
        assertTrue("Title must end with decoded emoji", result.bookmarks[0].title.endsWith("\uD83D\uDE00"))
        assertTrue("Title must not contain replacement characters", !result.bookmarks[0].title.contains("\uFFFD"))
    }

    @Test
    fun testUtf16LeAndBeBomFiles_parsedCorrectly() {
        val html = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n<DL><p>\r\n<DT><A HREF=\"https://utf16.example.com\">UTF16 Bookmark \uD83D\uDE00</A>\r\n</DL><p>\r\n"

        // UTF-16LE with BOM (0xFF, 0xFE)
        val leContent = html.toByteArray(StandardCharsets.UTF_16LE)
        val leBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + leContent
        val leResult = BookmarkParser.parse(ByteArrayInputStream(leBytes))
        assertEquals(1, leResult.bookmarks.size)
        assertEquals("https://utf16.example.com", leResult.bookmarks[0].url)
        assertEquals("UTF16 Bookmark \uD83D\uDE00", leResult.bookmarks[0].title)

        // UTF-16BE with BOM (0xFE, 0xFF)
        val beContent = html.toByteArray(StandardCharsets.UTF_16BE)
        val beBytes = byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + beContent
        val beResult = BookmarkParser.parse(ByteArrayInputStream(beBytes))
        assertEquals(1, beResult.bookmarks.size)
        assertEquals("https://utf16.example.com", beResult.bookmarks[0].url)
        assertEquals("UTF16 Bookmark \uD83D\uDE00", beResult.bookmarks[0].title)
    }

    @Test
    fun testNonAsciiOnlyAfter8Kb_parsedCorrectly() {
        val asciiPreamble = buildString {
            append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n<DL><p>\r\n")
            while (length < 8500) {
                append("<DT><A HREF=\"https://ascii.example.com/item\">ASCII Link Preamble</A>\r\n")
            }
            append("<DT><A HREF=\"https://late-non-ascii.example.com\">Café München “Zürich” — Über & Co</A>\r\n")
            append("</DL><p>\r\n")
        }
        val bytes = asciiPreamble.toByteArray(StandardCharsets.UTF_8)
        val result = BookmarkParser.parse(ByteArrayInputStream(bytes))
        assertTrue(result.bookmarks.size >= 2)
        val lateBookmark = result.bookmarks.find { it.url == "https://late-non-ascii.example.com" }
        org.junit.Assert.assertNotNull(lateBookmark)
        assertEquals("Café München “Zürich” — Über & Co", lateBookmark?.title)
        assertTrue("Must not contain replacement characters", lateBookmark?.title?.contains("\uFFFD") == false)
    }
}

