package com.example.bookmarks

import org.jsoup.parser.Parser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale

data class ParsedBookmark(
    val url: String,
    val title: String,
    val addDateMs: Long,
    val folderPath: List<String>
)

data class ParseResult(
    val bookmarks: List<ParsedBookmark>,
    val warnings: List<String>,
    val malformedTagCount: Int,
    val hasNetscapeHeader: Boolean = false
)

object BookmarkParser {
    private const val MAX_TAG_SIZE = 1024 * 1024 // 1 MB cap

    fun detectCharset(previewBytes: ByteArray, length: Int): java.nio.charset.Charset {
        if (length <= 0) return StandardCharsets.UTF_8

        // 1. Check UTF BOMs
        if (length >= 3 && previewBytes[0] == 0xEF.toByte() && previewBytes[1] == 0xBB.toByte() && previewBytes[2] == 0xBF.toByte()) {
            return StandardCharsets.UTF_8
        }
        if (length >= 2) {
            if (previewBytes[0] == 0xFE.toByte() && previewBytes[1] == 0xFF.toByte()) return StandardCharsets.UTF_16BE
            if (previewBytes[0] == 0xFF.toByte() && previewBytes[1] == 0xFE.toByte()) return StandardCharsets.UTF_16LE
        }

        // 2. Scan for <META ... charset=...> in ASCII preview
        val previewAscii = String(previewBytes, 0, length, StandardCharsets.ISO_8859_1)
        val metaCharsetRegex = Regex("""(?i)<meta\s+[^>]*content=["'][^"']*charset=([a-zA-Z0-9_\-]+)""")
        val html5CharsetRegex = Regex("""(?i)<meta\s+[^>]*charset=["']?([a-zA-Z0-9_\-]+)""")

        val match = metaCharsetRegex.find(previewAscii) ?: html5CharsetRegex.find(previewAscii)
        if (match != null) {
            val charsetName = match.groupValues[1].trim()
            try {
                return java.nio.charset.Charset.forName(charsetName)
            } catch (_: Exception) {
                return java.nio.charset.Charset.forName("windows-1252")
            }
        }

        // 3. If no META tag found, verify if valid UTF-8
        if (isValidUtf8(previewBytes, length)) {
            return StandardCharsets.UTF_8
        }

        // 4. Fallback to Windows-1252 for non-UTF-8 legacy files
        return try {
            java.nio.charset.Charset.forName("windows-1252")
        } catch (_: Exception) {
            StandardCharsets.ISO_8859_1
        }
    }

    private fun isValidUtf8(bytes: ByteArray, length: Int): Boolean {
        return try {
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
            val bb = java.nio.ByteBuffer.wrap(bytes, 0, length)
            val cb = java.nio.CharBuffer.allocate(length)
            val result = decoder.decode(bb, cb, false)
            !result.isError
        } catch (_: Exception) {
            false
        }
    }

    fun parse(input: InputStream): ParseResult {
        val bufferedInput = if (input.markSupported()) input else java.io.BufferedInputStream(input, 64 * 1024)
        val previewSize = 8192
        bufferedInput.mark(previewSize)
        val preview = ByteArray(previewSize)
        var bytesRead = 0
        while (bytesRead < previewSize) {
            val r = bufferedInput.read(preview, bytesRead, previewSize - bytesRead)
            if (r == -1) break
            bytesRead += r
        }
        bufferedInput.reset()

        val charset = detectCharset(preview, bytesRead)
        val reader = BufferedReader(InputStreamReader(bufferedInput, charset), 64 * 1024)

        // Strip leading BOM (\uFEFF) if present
        reader.mark(1)
        val firstChar = reader.read()
        if (firstChar != 0xFEFF && firstChar != -1) {
            reader.reset()
        }

        var hasNetscapeHeader = false
        val bookmarks = mutableListOf<ParsedBookmark>()
        val warnings = mutableListOf<String>()
        var malformedCount = 0

        val folderStack = ArrayDeque<String?>()
        var pendingFolder: String? = null
        var isCurrentFolderBrowserRoot = false

        var inA = false
        var currentAUrl = ""
        var currentAAddDate = 0L

        var inH3 = false

        val textBuffer = StringBuilder()

        fun emitPendingA() {
            if (inA) {
                val rawTitle = textBuffer.toString()
                val title = Parser.unescapeEntities(rawTitle, false)
                bookmarks.add(
                    ParsedBookmark(
                        url = currentAUrl,
                        title = title,
                        addDateMs = BookmarkNormalizer.normalizeToMillis(currentAAddDate),
                        folderPath = folderStack.filterNotNull()
                    )
                )
                inA = false
                textBuffer.clear()
            }
        }

        val tagBuffer = StringBuilder()

        while (true) {
            val ch = reader.read()
            if (ch == -1) break

            val c = ch.toChar()

            if (c == '<') {
                // Check if this is a comment <!--
                reader.mark(3)
                val c1 = reader.read()
                val c2 = reader.read()
                val c3 = reader.read()
                if (c1 == '!'.code && c2 == '-'.code && c3 == '-'.code) {
                    // Skip comment entirely until --> or EOF
                    var dashCount = 0
                    while (true) {
                        val commentCh = reader.read()
                        if (commentCh == -1) break
                        if (commentCh == '-'.code) {
                            dashCount++
                        } else if (commentCh == '>'.code && dashCount >= 2) {
                            break
                        } else {
                            dashCount = 0
                        }
                    }
                    continue
                } else {
                    reader.reset()
                }

                // Read tag until '>' outside quotes
                tagBuffer.clear()
                var quoteChar: Char? = null
                var tagCompleted = false

                while (true) {
                    val tagCh = reader.read()
                    if (tagCh == -1) {
                        // Truncated input during tag
                        warnings.add("Truncated tag at EOF")
                        break
                    }
                    val tc = tagCh.toChar()
                    if (quoteChar == null) {
                        if (tc == '\'' || tc == '"') {
                            quoteChar = tc
                        } else if (tc == '>') {
                            tagCompleted = true
                            break
                        }
                    } else {
                        if (tc == quoteChar) {
                            quoteChar = null
                        }
                    }

                    tagBuffer.append(tc)

                    if (tagBuffer.length > MAX_TAG_SIZE) {
                        malformedCount++
                        warnings.add("Tag exceeded 1MB limit; resyncing at next '<'")
                        // Resync: discard tag buffer and advance until next '<' or EOF
                        while (true) {
                            reader.mark(1)
                            val nextCh = reader.read()
                            if (nextCh == -1 || nextCh == '<'.code) {
                                if (nextCh == '<'.code) reader.reset()
                                break
                            }
                        }
                        break
                    }
                }

                if (!tagCompleted) {
                    continue
                }

                val tagContent = tagBuffer.toString().trim()
                if (tagContent.isEmpty()) continue

                if (tagContent.contains("NETSCAPE-BOOKMARK-FILE-1", ignoreCase = true)) {
                    hasNetscapeHeader = true
                }

                val isClosing = tagContent.startsWith("/")
                val contentWithoutSlash = if (isClosing) tagContent.substring(1).trim() else tagContent

                // Extract tag name (up to whitespace or end)
                var nameEnd = 0
                while (nameEnd < contentWithoutSlash.length && !contentWithoutSlash[nameEnd].isWhitespace()) {
                    nameEnd++
                }
                val rawTagName = contentWithoutSlash.substring(0, nameEnd)
                val tagName = rawTagName.uppercase(Locale.ROOT)
                val attrString = contentWithoutSlash.substring(nameEnd).trim()

                when {
                    !isClosing && tagName == "H3" -> {
                        inH3 = true
                        textBuffer.clear()
                        val attrs = parseAttributes(attrString)
                        val isToolbar = attrs["PERSONAL_TOOLBAR_FOLDER"].equals("true", ignoreCase = true)
                        val isUnfiled = attrs["UNFILED_BOOKMARKS_FOLDER"].equals("true", ignoreCase = true)
                        isCurrentFolderBrowserRoot = isToolbar || isUnfiled
                    }
                    isClosing && tagName == "H3" -> {
                        if (inH3) {
                            val raw = textBuffer.toString()
                            val decoded = Parser.unescapeEntities(raw, false).trim()
                            pendingFolder = if (isCurrentFolderBrowserRoot || decoded.isEmpty()) null else decoded
                            inH3 = false
                            textBuffer.clear()
                        }
                    }
                    !isClosing && tagName == "DL" -> {
                        emitPendingA()
                        folderStack.addLast(pendingFolder)
                        pendingFolder = null
                        isCurrentFolderBrowserRoot = false
                    }
                    isClosing && tagName == "DL" -> {
                        emitPendingA()
                        if (folderStack.isNotEmpty()) {
                            folderStack.removeLast()
                        }
                    }
                    !isClosing && tagName == "A" -> {
                        emitPendingA()
                        inA = true
                        textBuffer.clear()
                        val attrs = parseAttributes(attrString)
                        val href = attrs["HREF"] ?: ""
                        currentAUrl = Parser.unescapeEntities(href, false)
                        currentAAddDate = (attrs["ADD_DATE"] ?: "0").toLongOrNull() ?: 0L
                    }
                    isClosing && tagName == "A" -> {
                        emitPendingA()
                    }
                    else -> {
                        // Ignore DT, DD, P, HR, META, TITLE, H1, etc.
                    }
                }
            } else {
                // Text outside tags
                if (inH3 || inA) {
                    textBuffer.append(c)
                }
            }
        }

        // EOF reached: if A tag was left open, emit what we have
        emitPendingA()

        if (!hasNetscapeHeader) {
            warnings.add("Missing Netscape bookmark header (<!DOCTYPE NETSCAPE-Bookmark-file-1>)")
        }

        return ParseResult(
            bookmarks = bookmarks,
            warnings = warnings,
            malformedTagCount = malformedCount,
            hasNetscapeHeader = hasNetscapeHeader
        )
    }

    private fun parseAttributes(input: String): Map<String, String> {
        val result = HashMap<String, String>()
        var i = 0
        val len = input.length

        while (i < len) {
            // Skip whitespace
            while (i < len && input[i].isWhitespace()) i++
            if (i >= len) break

            // Read attribute name
            val nameStart = i
            while (i < len && input[i] != '=' && !input[i].isWhitespace() && input[i] != '>' && input[i] != '/') {
                i++
            }
            val attrName = input.substring(nameStart, i).trim().uppercase(Locale.ROOT)
            if (attrName.isEmpty()) {
                i++
                continue
            }

            // Skip whitespace before '='
            while (i < len && input[i].isWhitespace()) i++

            if (i < len && input[i] == '=') {
                i++ // Skip '='
                // Skip whitespace after '='
                while (i < len && input[i].isWhitespace()) i++
                if (i >= len) {
                    result[attrName] = ""
                    break
                }

                val quote = input[i]
                if (quote == '"' || quote == '\'') {
                    i++ // Skip quote
                    val valStart = i
                    while (i < len && input[i] != quote) {
                        i++
                    }
                    val attrValue = input.substring(valStart, i)
                    if (i < len && input[i] == quote) i++ // Skip closing quote

                    // Ignore large ICON / ICON_URI attributes to save memory
                    if (attrName != "ICON" && attrName != "ICON_URI") {
                        result[attrName] = attrValue
                    }
                } else {
                    // Unquoted value
                    val valStart = i
                    while (i < len && !input[i].isWhitespace() && input[i] != '>') {
                        i++
                    }
                    val attrValue = input.substring(valStart, i)
                    if (attrName != "ICON" && attrName != "ICON_URI") {
                        result[attrName] = attrValue
                    }
                }
            } else {
                // Boolean attribute without value
                result[attrName] = "true"
            }
        }

        return result
    }
}
