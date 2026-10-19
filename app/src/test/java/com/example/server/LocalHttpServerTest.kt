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

        // Create bookmark pointing to local server endpoint so test is 100% offline-deterministic
        val testUrl = "http://127.0.0.1:$customPort/"
        val createConn = URL("http://127.0.0.1:$customPort/api/bookmarks").openConnection() as HttpURLConnection
        createConn.requestMethod = "POST"
        createConn.doOutput = true
        createConn.setRequestProperty("Authorization", "Bearer $token")
        createConn.setRequestProperty("Content-Type", "application/json")
        createConn.outputStream.use { it.write("""{"url":"$testUrl","category":"Tech"}""".toByteArray()) }
        assertEquals(201, createConn.responseCode)
        val createdBody = JSONObject(createConn.inputStream.bufferedReader().use { it.readText() })
        val bookmarkId = createdBody.getLong("id")
        createConn.disconnect()

        // Wait with polling for async title fetcher to run and update Room
        var site: com.example.data.Website? = null
        for (i in 0 until 30) {
            delay(100)
            site = dao.getAllWebsitesList().firstOrNull { it.id == bookmarkId }
            if (site?.title?.isNotBlank() == true) break
        }
        assertNotNull(site)
        assertTrue("Title should be populated and not blank", site!!.title.isNotBlank())
    }

    @Test
    fun testManifestEndpoint_returnsValidJson() = runBlocking {
        val customPort = 18900
        server.start(preferredPort = customPort)
        delay(150)

        val manifestUrl = URL("http://127.0.0.1:$customPort/manifest.json")
        val conn = manifestUrl.openConnection() as HttpURLConnection
        assertEquals(200, conn.responseCode)
        assertTrue(conn.contentType.contains("manifest+json") || conn.contentType.contains("json"))
        val body = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        assertEquals("WebStack PC Companion", body.getString("name"))
        assertEquals("standalone", body.getString("display"))
        assertTrue(body.has("icons"))
        val icons = body.getJSONArray("icons")
        assertEquals(1, icons.length())
        assertEquals("/favicon.svg", icons.getJSONObject(0).getString("src"))
        conn.disconnect()
    }

    @Test
    fun testFaviconEndpoint_returnsValidSvg() = runBlocking {
        val customPort = 18902
        server.start(preferredPort = customPort)
        delay(150)

        // 1. /favicon.svg
        val svgUrl = URL("http://127.0.0.1:$customPort/favicon.svg")
        val svgConn = svgUrl.openConnection() as HttpURLConnection
        assertEquals(200, svgConn.responseCode)
        assertTrue(svgConn.contentType.contains("svg+xml"))
        val svgBody = svgConn.inputStream.bufferedReader().use { it.readText() }
        assertTrue(svgBody.contains("<svg") && svgBody.contains("M16 6"))
        svgConn.disconnect()

        // 2. /favicon.ico
        val icoUrl = URL("http://127.0.0.1:$customPort/favicon.ico")
        val icoConn = icoUrl.openConnection() as HttpURLConnection
        assertEquals(200, icoConn.responseCode)
        assertTrue(icoConn.contentType.contains("icon") || icoConn.contentType.contains("svg+xml") || icoConn.contentType.contains("image/"))
        icoConn.disconnect()

        // 3. /favicon.png
        val pngUrl = URL("http://127.0.0.1:$customPort/favicon.png")
        val pngConn = pngUrl.openConnection() as HttpURLConnection
        assertEquals(200, pngConn.responseCode)
        assertTrue(pngConn.contentType.contains("png") || pngConn.contentType.contains("svg+xml") || pngConn.contentType.contains("image/"))
        pngConn.disconnect()
    }

    @Test
    fun testStatusEndpoint_tokenAuthenticationAndActiveConnections() = runBlocking {
        val customPort = 18901
        server.start(preferredPort = customPort)
        delay(150)
        val pin = server.status.value.pin

        // 1. Initial status check without token: returns 200, authenticated=false, connectedClients=0
        val anonUrl = URL("http://127.0.0.1:$customPort/api/status")
        val anonConn = anonUrl.openConnection() as HttpURLConnection
        assertEquals(200, anonConn.responseCode)
        val anonBody = JSONObject(anonConn.inputStream.bufferedReader().use { it.readText() })
        assertEquals("online", anonBody.getString("status"))
        assertFalse("Should not be authenticated", anonBody.getBoolean("authenticated"))
        anonConn.disconnect()
        assertEquals(0, server.status.value.connectedClients)

        // 2. Status check with invalid token: returns 401 Unauthorized
        val badUrl = URL("http://127.0.0.1:$customPort/api/status?token=invalid_token_123")
        val badConn = badUrl.openConnection() as HttpURLConnection
        assertEquals(401, badConn.responseCode)
        badConn.disconnect()
        assertEquals(0, server.status.value.connectedClients)

        // 3. Authenticate with PIN to get valid token
        val authUrl = URL("http://127.0.0.1:$customPort/api/auth")
        val authConn = authUrl.openConnection() as HttpURLConnection
        authConn.requestMethod = "POST"
        authConn.doOutput = true
        authConn.setRequestProperty("Content-Type", "application/json")
        authConn.outputStream.use { it.write("""{"pin":"$pin"}""".toByteArray()) }
        assertEquals(200, authConn.responseCode)
        val token = JSONObject(authConn.inputStream.bufferedReader().use { it.readText() }).getString("token")
        authConn.disconnect()

        // 4. Status check with valid token via query parameter: returns 200, authenticated=true, increments connectedClients
        val goodUrl = URL("http://127.0.0.1:$customPort/api/status?token=$token")
        val goodConn = goodUrl.openConnection() as HttpURLConnection
        assertEquals(200, goodConn.responseCode)
        val goodBody = JSONObject(goodConn.inputStream.bufferedReader().use { it.readText() })
        assertEquals("online", goodBody.getString("status"))
        assertTrue("Should be authenticated", goodBody.getBoolean("authenticated"))
        goodConn.disconnect()
        assertEquals(1, server.status.value.connectedClients)

        // 5. Bookmarks endpoint with query param token: returns 200 OK
        val bmsUrl = URL("http://127.0.0.1:$customPort/api/bookmarks?token=$token")
        val bmsConn = bmsUrl.openConnection() as HttpURLConnection
        assertEquals(200, bmsConn.responseCode)
        bmsConn.disconnect()

        // 6. Categories endpoint with query param token: returns 200 OK
        val catsUrl = URL("http://127.0.0.1:$customPort/api/categories?token=$token")
        val catsConn = catsUrl.openConnection() as HttpURLConnection
        assertEquals(200, catsConn.responseCode)
        catsConn.disconnect()
    }
}
