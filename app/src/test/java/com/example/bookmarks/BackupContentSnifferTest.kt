package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets

class BackupContentSnifferTest {

    @Test
    fun testSniffJsonBackup() {
        val json = """{"version": 1, "categories": ["Dev"], "websites": [{"url": "https://example.com"}]}"""
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.JSON_BACKUP, format)
    }

    @Test
    fun testSniffJsonBackupWithLeadingWhitespace() {
        val json = "   \n\r\t  {\"websites\": []}"
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.JSON_BACKUP, format)
    }

    @Test
    fun testSniffNetscapeHtmlBookmarks() {
        val html = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n<DL><p><DT><A HREF=\"https://test.com\">Test</A></DL>"
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.HTML_BOOKMARKS, format)
    }

    @Test
    fun testSniffHtmlBookmarksWithoutNetscapeHeader() {
        val html = "<DL><p>\r\n<DT><A HREF=\"https://test.com\">Test</A>\r\n</DL>"
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.HTML_BOOKMARKS, format)
    }

    @Test
    fun testSniffUnknownContent() {
        val garbage = "Some random text file or binary data without bookmark markers"
        val bytes = garbage.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.UNKNOWN, format)
    }

    @Test
    fun testSniffEmptyBytes() {
        val bytes = ByteArray(0)
        val format = BackupContentSniffer.sniff(bytes, 0)
        assertEquals(DetectedBackupFormat.UNKNOWN, format)
    }

    @Test
    fun testSniffPlainTextUrls() {
        val text = "https://github.com\nhttps://stackoverflow.com\nhttps://developer.mozilla.org"
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        val format = BackupContentSniffer.sniff(bytes, bytes.size)
        assertEquals(DetectedBackupFormat.PLAIN_TEXT_URLS, format)
    }
}
