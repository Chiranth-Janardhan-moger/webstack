package com.example.server

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDao
import com.example.data.WebsiteDatabase
import com.example.data.WebsiteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HostSyncClientTest {

    private lateinit var hostDb: WebsiteDatabase
    private lateinit var hostDao: WebsiteDao
    private lateinit var hostRepo: WebsiteRepository
    private lateinit var hostServer: LocalHttpServer

    private lateinit var clientDb: WebsiteDatabase
    private lateinit var clientDao: WebsiteDao
    private lateinit var clientRepo: WebsiteRepository

    private val testPort = 19123
    private val fixedPin = "889900"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Host Database and Server
        hostDb = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        hostDao = hostDb.websiteDao()
        hostRepo = WebsiteRepository(hostDao)

        hostServer = LocalHttpServer(
            context = context,
            repository = hostRepo,
            getCategories = { listOf("Personal", "Tech") }
        )
        hostServer.start(preferredPort = testPort, customPin = fixedPin)

        // 2. Client Database
        clientDb = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        clientDao = clientDb.websiteDao()
        clientRepo = WebsiteRepository(clientDao)
    }

    @After
    fun teardown() {
        hostServer.stop()
        hostDb.close()
        clientDb.close()
    }

    @Test
    fun testValidation_emptyHost_returnsError() = runBlocking {
        val result = HostSyncClient.syncBookmarks(
            hostUrl = "",
            pin = "123456",
            repository = clientRepo,
            onAddCategory = {}
        )
        assertTrue(result is SyncResult.Error)
        assertEquals("Host address is required", (result as SyncResult.Error).message)
    }

    @Test
    fun testValidation_invalidPinFormat_returnsError() = runBlocking {
        val resultShort = HostSyncClient.syncBookmarks(
            hostUrl = "http://127.0.0.1:$testPort",
            pin = "123",
            repository = clientRepo,
            onAddCategory = {}
        )
        assertTrue(resultShort is SyncResult.Error)
        assertEquals("PIN must be 6 digits", (resultShort as SyncResult.Error).message)

        val resultAlpha = HostSyncClient.syncBookmarks(
            hostUrl = "http://127.0.0.1:$testPort",
            pin = "12345a",
            repository = clientRepo,
            onAddCategory = {}
        )
        assertTrue(resultAlpha is SyncResult.Error)
        assertEquals("PIN must be 6 digits", (resultAlpha as SyncResult.Error).message)
    }

    @Test
    fun testAuth_wrongPin_returnsUnauthorizedError() = runBlocking {
        delay(150)
        val result = HostSyncClient.syncBookmarks(
            hostUrl = "http://127.0.0.1:$testPort",
            pin = "000000",
            repository = clientRepo,
            onAddCategory = {}
        )
        assertTrue(result is SyncResult.Error)
        assertEquals("Invalid PIN entered", (result as SyncResult.Error).message)
    }

    @Test
    fun testSyncBookmarks_successfulPullAndDeduplication() = runBlocking {
        delay(150)

        // Populate host with 3 bookmarks
        val hostBookmarks = listOf(
            Website(url = "https://kotlinlang.org", title = "Kotlin", category = "Tech", dedupeKey = com.example.ui.util.resolveDedupeKey("https://kotlinlang.org")),
            Website(url = "https://github.com", title = "GitHub", category = "Work", dedupeKey = com.example.ui.util.resolveDedupeKey("https://github.com")),
            Website(url = "https://wikipedia.org", title = "Wikipedia", category = "General", dedupeKey = com.example.ui.util.resolveDedupeKey("https://wikipedia.org"))
        )
        hostRepo.insertWebsites(hostBookmarks)

        // Client already has one of them ("https://kotlinlang.org")
        clientRepo.insertWebsites(listOf(
            Website(url = "https://kotlinlang.org", title = "Kotlin", category = "Tech", dedupeKey = com.example.ui.util.resolveDedupeKey("https://kotlinlang.org"))
        ))

        val capturedCategories = mutableListOf<String>()

        val result = HostSyncClient.syncBookmarks(
            hostUrl = "http://127.0.0.1:$testPort",
            pin = fixedPin,
            repository = clientRepo,
            onAddCategory = { capturedCategories.add(it) }
        )

        assertTrue("Expected Success, got: $result", result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.addedCount) // GitHub and Wikipedia added, Kotlin skipped
        assertEquals(3, success.totalHostCount)

        // Verify client Room DB now contains all 3
        val clientWebsites = clientRepo.getAllWebsitesList()
        assertEquals(3, clientWebsites.size)
        val urls = clientWebsites.map { it.url }.toSet()
        assertTrue(urls.contains("https://kotlinlang.org"))
        assertTrue(urls.contains("https://github.com"))
        assertTrue(urls.contains("https://wikipedia.org"))

        // Verify captured categories
        assertTrue(capturedCategories.contains("Tech"))
        assertTrue(capturedCategories.contains("Work"))
        assertTrue(capturedCategories.contains("General"))

        // Running sync a second time should add 0 new bookmarks
        val secondResult = HostSyncClient.syncBookmarks(
            hostUrl = "http://127.0.0.1:$testPort",
            pin = fixedPin,
            repository = clientRepo,
            onAddCategory = {}
        )
        assertTrue(secondResult is SyncResult.Success)
        assertEquals(0, (secondResult as SyncResult.Success).addedCount)
    }
}
