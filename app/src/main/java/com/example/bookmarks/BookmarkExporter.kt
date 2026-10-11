package com.example.bookmarks

import com.example.data.Website
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets

object BookmarkExporter {
    private const val HEADER = """<!DOCTYPE NETSCAPE-Bookmark-file-1>
<!-- This is an automatically generated file.
     It will be read and overwritten.
     DO NOT EDIT! -->
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>Bookmarks</TITLE>
<H1>Bookmarks</H1>
<DL><p>
"""

    fun String.escapeHtml(): String = buildString(length + 16) {
        for (c in this@escapeHtml) when {
            c == '&'  -> append("&amp;")
            c == '<'  -> append("&lt;")
            c == '>'  -> append("&gt;")
            c == '"'  -> append("&quot;")
            c == '\'' -> append("&#39;")
            c == '\n' || c == '\r' || c == '\t' -> append(' ')
            c < ' '   -> {}                                           // drop other control chars
            else      -> append(c)
        }
    }

    suspend fun exportBookmarks(out: OutputStream, dao: ExportDao, nowMs: Long) = withContext(Dispatchers.IO) {
        BufferedWriter(OutputStreamWriter(out, StandardCharsets.UTF_8), 64 * 1024).use { w ->
            w.write(HEADER)
            val nowS = nowMs / 1000
            for (cat in dao.categoriesSorted().filter { it != "Uncategorized" }) {     // one query per category => streaming
                w.write("    <DT><H3 ADD_DATE=\"$nowS\" LAST_MODIFIED=\"$nowS\">${cat.escapeHtml()}</H3>\n    <DL><p>\n")
                for (s in dao.byCategory(cat)) w.writeLink(s, nowS, indent = "        ")
                w.write("    </DL><p>\n")
            }
            for (s in dao.byCategory("Uncategorized")) w.writeLink(s, nowS, indent = "    ")
            w.write("</DL><p>\n")
        }
    }

    private fun Writer.writeLink(s: Website, nowS: Long, indent: String) {
        val addS = if (s.createdAtMs > 0) s.createdAtMs / 1000 else nowS
        val title = s.title.ifBlank { s.url }.escapeHtml()
        write("$indent<DT><A HREF=\"${s.url.escapeHtml()}\" ADD_DATE=\"$addS\">$title</A>\n")
    }
}
