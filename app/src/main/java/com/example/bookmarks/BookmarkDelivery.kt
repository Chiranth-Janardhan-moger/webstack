package com.example.bookmarks

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BookmarkDelivery {

    fun formatExportFileName(nowMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return "webstack_bookmarks_${sdf.format(Date(nowMs))}.html"
    }

    /**
     * Option A (SAF): Opens stream with "wt" mode to truncate existing contents,
     * preventing trailing garbage when overwriting longer files.
     */
    suspend fun exportToSaf(
        contentResolver: ContentResolver,
        uri: Uri,
        dao: ExportDao,
        nowMs: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val out = contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Failed to open output stream for uri: $uri")
        out.use { stream ->
            BookmarkExporter.exportBookmarks(stream, dao, nowMs)
        }
    }

    /**
     * Option B (Share sheet): Writes export file to cacheDir/exports/, deletes old exports,
     * and constructs an ACTION_SEND Intent with FileProvider URI.
     */
    suspend fun createShareIntent(
        context: Context,
        dao: ExportDao,
        nowMs: Long = System.currentTimeMillis()
    ): Intent = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports")
        if (exportDir.exists()) {
            exportDir.listFiles()?.forEach { it.delete() }
        } else {
            exportDir.mkdirs()
        }

        val fileName = formatExportFileName(nowMs)
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { out ->
            BookmarkExporter.exportBookmarks(out, dao, nowMs)
        }

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("", contentUri)
        }

        Intent.createChooser(sendIntent, "Export Bookmarks")
    }
}
