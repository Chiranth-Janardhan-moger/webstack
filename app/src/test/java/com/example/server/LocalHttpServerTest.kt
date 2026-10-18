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
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalHttpServerTest {

    private lateinit var db: WebsiteDatabase
    private lateinit var dao: WebsiteDao
    private lateinit var repository: WebsiteRepository
    private lateinit var server: LocalHttpServer
    private var testPort = 18880

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.websiteDao()
        repository = WebsiteRepository(dao)

        server = LocalHttpServer(
            context = context,
            repository = repository,
            getCategories = { listOf("Personal", "Work", "Tech") }
        )
    }

    @After
    fun teardown() {
        server.stop()
        db.close()
    }

    @Test
    fun testServerLifecycle_startsAndStopsCleanly() = runBlocking {
        server.start(testPort)
        delay(150)

        val status = server.status.value
        assertTrue("Server should be running", status.isRunning)
        assertTrue("Port should be set", status.port >= testPort)
        assertEquals(6, status.pin.length)

        server.stop()
        delay(100)
        assertFalse("Server should be stopped", server.status.value.isRunning)
    }

    @Test
    fun testStatusEndpoint_returnsOnlineStatus() = runBlocking {
        server.start(testPort)
        delay(150)
        val port = server.status.value.port

        val url = URL("http://127.0.0.1:$port/api/status")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 3000
        conn.readTimeout = 3000

        assertEquals(200, conn.responseCode)
        val response = conn.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)
        assertEquals("online", json.getString("status"))
        conn.disconnect()
    }

    @Test
    fun testAuthEndpoint_invalidAndValidPin() = runBlocking {
        server.start(testPort)
        delay(150)
        val port = server.status.value.port
        val correctPin = server.status.value.pin

        // 1. Invalid PIN should return 403
        val badConn = URL("http://127.0.0.1:$port/api/auth").openConnection() as HttpURLConnection
        badConn.requestMethod = "POST"
        badConn.doOutput = true
        badConn.setRequestProperty("Content-Type", "application/json")
        badConn.outputStream.use { it.write("""{"pin":"000000"}""".toByteArray()) }
        assertEquals(403, badConn.responseCode)
        badConn.disconnect()

        // 2. Valid PIN should return 200 and session token
        val goodConn = URL("http://127.0.0.1:$port/api/auth").openConnection() as HttpURLConnection
        goodConn.requestMethod = "POST"
        goodConn.doOutput = true
        goodConn.setRequestProperty("Content-Type", "application/json")
        goodConn.outputStream.use { it.write("""{"pin":"$correctPin"}""".toByteArray()) }
        assertEquals(200, goodConn.responseCode)
        val resp = JSONObject(goodConn.inputStream.bufferedReader().use { it.readText() })
        assertTrue(resp.getBoolean("success"))
        assertNotNull(resp.getString("token"))
        assertTrue(resp.getString("token").isNotBlank())
        goodConn.disconnect()
    }

    @Test
    fun testBookmarksEndpoint_unauthorizedWithoutToken() = runBlocking {
        server.start(testPort)
        delay(150)
        val port = server.status.value.port

        val conn = URL("http://127.0.0.1:$port/api/bookmarks").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        assertEquals(401, conn.responseCode)
        conn.disconnect()
    }

    @Test
    fun testBookmarksEndpoint_authorizedReturnsSeededItems() = runBlocking {
        // Seed database
        dao.insertWebsite(
            Website(
                id = 0,
                url = "https://kotlinlang.org",
                title = "Kotlin Programming",
                category = "Work",
                dedupeKey = "kotlinlang.org",
                sourcePath = null,
                createdAtMs = 1710000000000L
            )
        )

        server.start(testPort)
        delay(150)
        val port = server.status.value.port
        val pin = server.status.value.pin

        // Authenticate
        val authConn = URL("http://127.0.0.1:$port/api/auth").openConnection() as HttpURLConnection
        authConn.requestMethod = "POST"
        authConn.doOutput = true
        authConn.setRequestProperty("Content-Type", "application/json")
        authConn.outputStream.use { it.write("""{"pin":"$pin"}""".toByteArray()) }
        val token = JSONObject(authConn.inputStream.bufferedReader().use { it.readText() }).getString("token")
        authConn.disconnect()

        // Fetch bookmarks
        val conn = URL("http://127.0.0.1:$port/api/bookmarks").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        assertEquals(200, conn.responseCode)
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(body)
        assertEquals(1, array.length())
        assertEquals("Kotlin Programming", array.getJSONObject(0).getString("title"))
        assertEquals("https://kotlinlang.org", array.getJSONObject(0).getString("url"))
        conn.disconnect()
    }

    @Test
    fun testCategoriesEndpoint_authorized() = runBlocking {
        server.start(testPort)
        delay(150)
        val port = server.status.value.port
        val pin = server.status.value.pin

        // Authenticate
        val authConn = URL("http://127.0.0.1:$port/api/auth").openConnection() as HttpURLConnection
        authConn.requestMethod = "POST"
        authConn.doOutput = true
        authConn.setRequestProperty("Content-Type", "application/json")
        authConn.outputStream.use { it.write("""{"pin":"$pin"}""".toByteArray()) }
        val token = JSONObject(authConn.inputStream.bufferedReader().use { it.readText() }).getString("token")
        authConn.disconnect()

        val conn = URL("http://127.0.0.1:$port/api/categories").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        assertEquals(200, conn.responseCode)
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(body)
        assertEquals(3, array.length())
        assertEquals("Personal", array.getString(0))
        conn.disconnect()
    }

    @Test
    fun testServerWithCustomPinAndPort_usesProvidedValues() = runBlocking {
        val customPort = 18895
        val customPin = "987654"
        server.start(preferredPort = customPort, customPin = customPin)
        delay(150)

        val status = server.status.value
        assertTrue("Server should be running", status.isRunning)
        assertEquals(customPort, status.port)
        assertEquals(customPin, status.pin)

        // Verify auth works with custom PIN
        val url = URL("http://127.0.0.1:$customPort/api/auth")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.bufferedWriter().use { it.write("""{"pin":"$customPin"}""") }

        assertEquals(200, conn.responseCode)
        val response = conn.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)
        assertTrue(json.getBoolean("success"))
        assertNotNull(json.getString("token"))
        conn.disconnect()
    }

    @Test
    fun testOfflineExpiryHoursAndServiceWorkerRoute() = runBlocking {
        val customPort = 18896
        val customPin = "112233"
        server.start(preferredPort = customPort, customPin = customPin)
        delay(150)

        // 1. Verify /api/status returns offlineExpiryHours
        val statusUrl = URL("http://127.0.0.1:$customPort/api/status")
        val statusConn = statusUrl.openConnection() as HttpURLConnection
        assertEquals(200, statusConn.responseCode)
        val statusBody = JSONObject(statusConn.inputStream.bufferedReader().use { it.readText() })
        assertTrue(statusBody.has("offlineExpiryHours"))
        assertEquals(24, statusBody.getInt("offlineExpiryHours"))
        statusConn.disconnect()

        // 2. Verify /api/auth returns offlineExpiryHours
        val authUrl = URL("http://127.0.0.1:$customPort/api/auth")
        val authConn = authUrl.openConnection() as HttpURLConnection
        authConn.requestMethod = "POST"
        authConn.doOutput = true
        authConn.setRequestProperty("Content-Type", "application/json")
        authConn.outputStream.bufferedWriter().use { it.write("""{"pin":"$customPin"}""") }
        assertEquals(200, authConn.responseCode)
        val authBody = JSONObject(authConn.inputStream.bufferedReader().use { it.readText() })
        assertEquals(24, authBody.getInt("offlineExpiryHours"))
        authConn.disconnect()

        // 3. Verify /sw.js serves javascript with 200 OK
        val swUrl = URL("http://127.0.0.1:$customPort/sw.js")
        val swConn = swUrl.openConnection() as HttpURLConnection
        assertEquals(200, swConn.responseCode)
        assertTrue(swConn.contentType.contains("javascript"))
        val swContent = swConn.inputStream.bufferedReader().use { it.readText() }
        assertTrue(swContent.contains("CACHE_NAME"))
        swConn.disconnect()
    }

    @Test
    fun testServerWithInvalidCustomPin_fallsBackToGeneratedSixDigitPin() = runBlocking {
        val customPort = 18897
        val invalidPin = "1234a"
        server.start(preferredPort = customPort, customPin = invalidPin)
        delay(150)

        val status = server.status.value
        assertTrue("Server should be running", status.isRunning)
        assertEquals(customPort, status.port)
        assertNotEquals(invalidPin, status.pin)
        assertEquals(6, status.pin.length)
        assertTrue(status.pin.all { it.isDigit() })
    }

    @Test
    fun testServerRestart_bindsSamePortImmediatelyWithoutFailure() = runBlocking {
        val customPort = 18898
        server.start(preferredPort = customPort)
        delay(150)
        assertTrue(server.status.value.isRunning)
        assertEquals(customPort, server.status.value.port)

        // Stop and immediately restart on the exact same port
        server.stop()
        server.start(preferredPort = customPort)
        delay(150)
        assertTrue("Server should be running after immediate restart", server.status.value.isRunning)
        assertEquals(customPort, server.status.value.port)
    }

    @Test
    fun testCreateBookmark_triggersAsyncTitleFetch() = runBlocking {
        val customPort = 18899
        server.start(preferredPort = customPort)
        delay(150)
        val pin = server.status.value.pin

        // Authenticate
        val authConn = URL("http://127.0.0.1:$customPort/api/auth").openConnection() as HttpURLConnection
        authConn.requestMethod = "POST"
        authConn.doOutput = true
        authConn.setRequestProperty("Content-Type", "application/json")
        authConn.outputStream.use { it.write("""{"pin":"$pin"}""".toByteArray()) }
        val token = JSONObject(authConn.inputStream.bufferedReader().use { it.readText() }).getString("token")
        authConn.disconnect()

        // Create bookmark
        val createConn = URL("http://127.0.0.1:$customPort/api/bookmarks").openConnection() as HttpURLConnection
        createConn.requestMethod = "POST"
        createConn.doOutput = true
        createConn.setRequestProperty("Authorization", "Bearer $token")
        createConn.setRequestProperty("Content-Type", "application/json")
        createConn.outputStream.use { it.write("""{"url":"https://github.com","category":"Tech"}""".toByteArray()) }
        assertEquals(201, createConn.responseCode)
        val createdBody = JSONObject(createConn.inputStream.bufferedReader().use { it.readText() })
        val bookmarkId = createdBody.getLong("id")
        createConn.disconnect()

        // Wait briefly for async title fetcher to run and update Room
        delay(600)
        val site = dao.getAllWebsitesList().firstOrNull { it.id == bookmarkId }
        assertNotNull(site)
        assertTrue("Title should be populated and not blank", site!!.title.isNotBlank())
    }
}
