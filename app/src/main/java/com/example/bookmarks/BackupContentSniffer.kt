package com.example.bookmarks

import java.nio.charset.StandardCharsets
import java.util.Locale

enum class DetectedBackupFormat {
    HTML_BOOKMARKS,
    JSON_BACKUP,
    PLAIN_TEXT_URLS,
    UNKNOWN
}

object BackupContentSniffer {

    /**
     * Inspects the initial bytes of a backup or bookmark stream to determine
     * whether it contains HTML Netscape bookmarks or JSON application backup data.
     */
    fun sniff(bytes: ByteArray, length: Int): DetectedBackupFormat {
        if (length <= 0) return DetectedBackupFormat.UNKNOWN

        val preview = String(bytes, 0, length, StandardCharsets.ISO_8859_1).trimStart()

        // 1. JSON Backup check
        if (preview.startsWith("{")) {
            val lower = preview.lowercase(Locale.ROOT)
            if (lower.contains("\"websites\"") || lower.contains("\"categories\"") || lower.contains("\"version\"")) {
                return DetectedBackupFormat.JSON_BACKUP
            }
        }

        // 2. HTML Netscape bookmarks check
        val upper = preview.uppercase(Locale.ROOT)
        if (upper.contains("<!DOCTYPE NETSCAPE") ||
            upper.contains("<H3") ||
            upper.contains("<A HREF") ||
            upper.contains("<DL>") ||
            upper.contains("<DL ") ||
            upper.contains("<DL\n") ||
            upper.contains("<DL\r") ||
            upper.contains("<TITLE>BOOKMARKS</TITLE>") ||
            upper.contains("<TITLE>FAVORITES</TITLE>")) {
            return DetectedBackupFormat.HTML_BOOKMARKS
        }

        // 3. Plain text URLs check
        val lowerPreview = preview.lowercase(Locale.ROOT)
        if (lowerPreview.contains("http://") || lowerPreview.contains("https://") || lowerPreview.contains("://")) {
            return DetectedBackupFormat.PLAIN_TEXT_URLS
        }

        return DetectedBackupFormat.UNKNOWN
    }
}
