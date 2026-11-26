package com.example.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URLDecoder

object AutoBackupManager {
    const val AUTO_BACKUP_FILENAME = "webstack_autobackup.webstack"
    const val AUTO_BACKUP_MIME_TYPE = "application/octet-stream"

    /**
     * Converts a SAF tree URI into a clean user-facing directory label (e.g., "Documents/Backups").
     */
    fun formatFolderDisplayName(treeUri: Uri): String {
        val rawDocId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: treeUri.lastPathSegment
            ?: return "Selected Folder"

        val decoded = runCatching { URLDecoder.decode(rawDocId, "UTF-8") }.getOrDefault(rawDocId)
        val colonIndex = decoded.indexOf(':')
        if (colonIndex == -1) {
            return decoded.ifBlank { "Selected Folder" }
        }

        val volume = decoded.substring(0, colonIndex)
        val relativePath = decoded.substring(colonIndex + 1).trim('/')

        return when {
            relativePath.isNotBlank() && volume.equals("primary", ignoreCase = true) -> relativePath
            relativePath.isNotBlank() -> "$volume/$relativePath"
            volume.equals("primary", ignoreCase = true) -> "Internal Storage"
            else -> volume.ifBlank { "Selected Folder" }
        }
    }

    /**
     * Verifies this app still holds persisted write permission for [treeUri].
     */
    fun hasPersistedWritePermission(context: Context, treeUri: Uri): Boolean {
        return try {
            context.contentResolver.persistedUriPermissions.any { perm ->
                perm.uri == treeUri && perm.isWritePermission
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Serializes [jsonContent] cleanly and overwrites [AUTO_BACKUP_FILENAME] inside [treeUri].
     * Finding/creating the target document and opening with "wt" keeps one deterministic file
     * for local backups and external directory sync tools.
     */
    suspend fun writeBackupToDirectory(
        context: Context,
        treeUri: Uri,
        jsonContent: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payloadBytes = jsonContent.toByteArray(Charsets.UTF_8)
            val resolver = context.contentResolver
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)

            var targetDocUri: Uri? = findExistingBackupFile(resolver, treeUri, childrenUri)

            if (targetDocUri == null) {
                targetDocUri = DocumentsContract.createDocument(
                    resolver,
                    parentDocUri,
                    AUTO_BACKUP_MIME_TYPE,
                    AUTO_BACKUP_FILENAME
                )
            }

            if (targetDocUri == null) {
                return@withContext Result.failure(IOException("Could not create backup file in folder"))
            }

            val out = resolver.openOutputStream(targetDocUri, "wt")
                ?: return@withContext Result.failure(IOException("Could not open backup file for writing"))

            out.use { stream ->
                stream.write(payloadBytes)
                stream.flush()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findExistingBackupFile(
        resolver: android.content.ContentResolver,
        treeUri: Uri,
        childrenUri: Uri
    ): Uri? {
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resolver.query(childrenUri, projection, android.os.Bundle.EMPTY, null)
        } else {
            resolver.query(childrenUri, projection, null, null, null)
        }
        cursor?.use { c ->
            val idCol = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            if (idCol == -1 || nameCol == -1) return null

            while (c.moveToNext()) {
                val name = c.getString(nameCol)
                if (name == AUTO_BACKUP_FILENAME) {
                    val docId = c.getString(idCol)
                    if (!docId.isNullOrBlank()) {
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    }
                }
            }
        }
        return null
    }
}
