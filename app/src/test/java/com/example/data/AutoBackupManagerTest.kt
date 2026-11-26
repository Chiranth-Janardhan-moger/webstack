package com.example.data

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AutoBackupManagerTest {

    private lateinit var context: Context
    private lateinit var tempRoot: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        Shadows.shadowOf(context as Application).grantPermissions(Manifest.permission.MANAGE_DOCUMENTS)
        tempRoot = File(context.cacheDir, "autobackup_test_${System.nanoTime()}").apply { mkdirs() }
        FakeTreeDocumentsProvider.rootDir = tempRoot
        val info = ProviderInfo().apply {
            authority = FakeTreeDocumentsProvider.AUTHORITY
            exported = true
            grantUriPermissions = true
            readPermission = Manifest.permission.MANAGE_DOCUMENTS
            writePermission = Manifest.permission.MANAGE_DOCUMENTS
        }
        Robolectric.buildContentProvider(FakeTreeDocumentsProvider::class.java).create(info)
    }

    @After
    fun teardown() {
        tempRoot.deleteRecursively()
    }

    @Test
    fun testFormatFolderDisplayName_parsesPrimaryAndSubfolders() {
        val docsUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FBackups")
        assertEquals("Documents/Backups", AutoBackupManager.formatFolderDisplayName(docsUri))

        val rootUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A")
        assertEquals("Internal Storage", AutoBackupManager.formatFolderDisplayName(rootUri))
    }

    @Test
    fun testWriteBackupToDirectory_createsAndOverwritesSingleFileCleanly() = runBlocking {
        val treeUri = DocumentsContract.buildTreeDocumentUri(
            FakeTreeDocumentsProvider.AUTHORITY,
            "root"
        )

        val firstPayload = """{"version":1,"appName":"WebStack","websites":[{"url":"https://first.com"}]}"""
        val firstResult = AutoBackupManager.writeBackupToDirectory(context, treeUri, firstPayload)
        assertTrue("First write should succeed: ${firstResult.exceptionOrNull()}", firstResult.isSuccess)

        val expectedFile = File(tempRoot, AutoBackupManager.AUTO_BACKUP_FILENAME)
        assertTrue(expectedFile.exists())
        assertEquals(firstPayload, expectedFile.readText(Charsets.UTF_8))

        val secondPayload = """{"version":1,"appName":"WebStack","websites":[]}"""
        val secondResult = AutoBackupManager.writeBackupToDirectory(context, treeUri, secondPayload)
        assertTrue("Second write should succeed: ${secondResult.exceptionOrNull()}", secondResult.isSuccess)

        // Must overwrite in place rather than writing webstack_autobackup (1).webstack
        val files = tempRoot.listFiles() ?: emptyArray()
        assertEquals(1, files.size)
        assertEquals(AutoBackupManager.AUTO_BACKUP_FILENAME, files[0].name)
        assertEquals(secondPayload, expectedFile.readText(Charsets.UTF_8))
    }

    class FakeTreeDocumentsProvider : DocumentsProvider() {
        companion object {
            const val AUTHORITY = "com.example.test.documents"
            lateinit var rootDir: File
        }

        override fun onCreate(): Boolean = true

        override fun isChildDocument(parentDocumentId: String, documentId: String): Boolean {
            return parentDocumentId == "root" && (documentId == "root" || documentId.startsWith("child:"))
        }

        override fun queryRoots(projection: Array<out String>?): Cursor {
            return MatrixCursor(projection ?: arrayOf(DocumentsContract.Root.COLUMN_ROOT_ID))
        }

        override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
            val cols = projection ?: arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )
            val cursor = MatrixCursor(cols)
            val file = resolveFile(documentId)
            val mime = if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else "application/octet-stream"
            cursor.addRow(cols.map {
                when (it) {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID -> documentId
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME -> file.name
                    DocumentsContract.Document.COLUMN_MIME_TYPE -> mime
                    else -> null
                }
            })
            return cursor
        }

        override fun queryChildDocuments(
            parentDocumentId: String,
            projection: Array<out String>?,
            sortOrder: String?
        ): Cursor {
            val cols = projection ?: arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            )
            val cursor = MatrixCursor(cols)
            val parent = resolveFile(parentDocumentId)
            parent.listFiles()?.forEach { child ->
                val childDocId = "child:${child.name}"
                cursor.addRow(cols.map {
                    when (it) {
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID -> childDocId
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME -> child.name
                        else -> null
                    }
                })
            }
            return cursor
        }

        override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
            val parent = resolveFile(parentDocumentId)
            val target = File(parent, displayName)
            if (!target.exists()) {
                target.createNewFile()
            }
            return "child:$displayName"
        }

        override fun openDocument(
            documentId: String,
            mode: String,
            signal: CancellationSignal?
        ): ParcelFileDescriptor {
            val file = resolveFile(documentId)
            val parsedMode = ParcelFileDescriptor.parseMode(mode)
            return ParcelFileDescriptor.open(file, parsedMode)
        }

        private fun resolveFile(docId: String): File {
            return if (docId == "root") {
                rootDir
            } else {
                File(rootDir, docId.removePrefix("child:"))
            }
        }
    }
}
