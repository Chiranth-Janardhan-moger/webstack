package com.example.server

import com.example.data.Website
import com.example.data.WebsiteRepository
import com.example.ui.util.extractDomain
import com.example.ui.util.resolveDedupeKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

sealed class SyncResult {
    data class Success(val addedCount: Int, val totalHostCount: Int, val hostDevice: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

object HostSyncClient {

    suspend fun discoverHost(timeoutMs: Long = 1200L): String? = withContext(Dispatchers.IO) {
        // 1. First attempt UDP broadcast probe
        try {
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = timeoutMs.toInt()
            val probe = "WEBSTACK_DISCOVER_PROBE".toByteArray(Charsets.UTF_8)
            val ports = listOf(8080, 8081, 8082)
            for (p in ports) {
                try {
                    val packet = DatagramPacket(probe, probe.size, InetAddress.getByName("255.255.255.255"), p)
                    socket.send(packet)
                } catch (_: Exception) {}
            }

            val buf = ByteArray(512)
            val reply = DatagramPacket(buf, buf.size)
            socket.receive(reply)
            val msg = String(reply.data, 0, reply.length, Charsets.UTF_8).trim()
            socket.close()
            if (msg.contains("WEBSTACK_DISCOVER_ACK")) {
                val url = msg.substringAfter("WEBSTACK_DISCOVER_ACK").trim()
                if (url.isNotBlank()) return@withContext url
            }
        } catch (_: Exception) {}

        // 2. Fallback: Fast LAN subnet scan if UDP broadcast is isolated by router
        try {
            val myIp = WifiUtils.getLocalIpAddress()
            if (myIp != null && myIp.contains(".")) {
                val prefix = myIp.substringBeforeLast(".") + "."
                val ports = listOf(8080, 8081)
                for (port in ports) {
                    val found = scanSubnet(prefix, port)
                    if (found != null) return@withContext found
                }
            }
        } catch (_: Exception) {}

        null
    }

    private suspend fun scanSubnet(prefix: String, port: Int): String? = kotlinx.coroutines.coroutineScope {
        val jobs = (1..254).map { hostNum ->
            async(Dispatchers.IO) {
                val testIp = "$prefix$hostNum"
                try {
                    val url = URL("http://$testIp:$port/api/status")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 300
                        readTimeout = 300
                        requestMethod = "GET"
                    }
                    if (conn.responseCode == 200) {
                        val text = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                        conn.disconnect()
                        if (text.contains("\"status\":\"online\"")) {
                            return@async "http://$testIp:$port"
                        }
                    }
                    conn.disconnect()
                } catch (_: Exception) {}
                null
            }
        }
        jobs.awaitAll().firstOrNull { it != null }
    }

    suspend fun syncBookmarks(
        hostUrl: String,
        pin: String,
        repository: WebsiteRepository,
        onAddCategory: (String) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        val cleanHost = hostUrl.trim().replace(Regex("/+$"), "")
        if (cleanHost.isBlank()) {
            return@withContext SyncResult.Error("Host address is required")
        }
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return@withContext SyncResult.Error("PIN must be 6 digits")
        }

        try {
            // 1. Authenticate with Host PIN
            val authUrl = URL("$cleanHost/api/auth")
            val authConn = (authUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 4000
                readTimeout = 4000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            OutputStreamWriter(authConn.outputStream, Charsets.UTF_8).use { writer ->
                val body = JSONObject().apply { put("pin", pin) }.toString()
                writer.write(body)
                writer.flush()
            }

            val authCode = authConn.responseCode
            if (authCode != 200) {
                authConn.disconnect()
                return@withContext if (authCode == 401 || authCode == 403) {
                    SyncResult.Error("Invalid PIN entered")
                } else {
                    SyncResult.Error("Failed to authenticate (HTTP $authCode)")
                }
            }

            val authResponseText = BufferedReader(InputStreamReader(authConn.inputStream, Charsets.UTF_8)).use { it.readText() }
            authConn.disconnect()

            val authJson = JSONObject(authResponseText)
            val token = authJson.optString("token")
            if (token.isBlank()) {
                return@withContext SyncResult.Error("Host did not return session token")
            }

            // 2. Query Status for Device Name
            var hostDevice = "WebStack Host"
            try {
                val statusUrl = URL("$cleanHost/api/status")
                val statusConn = (statusUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("Authorization", "Bearer $token")
                }
                if (statusConn.responseCode == 200) {
                    val statusText = BufferedReader(InputStreamReader(statusConn.inputStream, Charsets.UTF_8)).use { it.readText() }
                    val statusJson = JSONObject(statusText)
                    val dev = statusJson.optString("device")
                    if (dev.isNotBlank()) hostDevice = dev
                }
                statusConn.disconnect()
            } catch (_: Exception) {}

            // 3. Fetch Bookmarks
            val bookmarksUrl = URL("$cleanHost/api/bookmarks")
            val bookmarksConn = (bookmarksUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Authorization", "Bearer $token")
            }

            val bookmarksCode = bookmarksConn.responseCode
            if (bookmarksCode != 200) {
                bookmarksConn.disconnect()
                return@withContext SyncResult.Error("Failed to fetch bookmarks (HTTP $bookmarksCode)")
            }

            val bookmarksResponseText = BufferedReader(InputStreamReader(bookmarksConn.inputStream, Charsets.UTF_8)).use { it.readText() }
            bookmarksConn.disconnect()

            val jsonArray = JSONArray(bookmarksResponseText)
            val incoming = ArrayList<Website>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val url = obj.optString("url")
                if (url.isBlank()) continue
                val title = obj.optString("title", extractDomain(url))
                val category = obj.optString("category", "Personal")
                val dedupe = resolveDedupeKey(url)
                incoming.add(
                    Website(
                        url = url,
                        title = title,
                        category = category,
                        dedupeKey = dedupe
                    )
                )
            }

            // 4. Merge into local database
            val existing = repository.getAllWebsitesList()
            val existingKeys = existing.map { it.dedupeKey }.toSet()
            val existingUrls = existing.map { it.url.trim().trimEnd('/') }.toSet()
            val toInsert = incoming.filter { it.dedupeKey !in existingKeys && it.url.trim().trimEnd('/') !in existingUrls }

            if (toInsert.isNotEmpty()) {
                repository.insertWebsites(toInsert)
            }

            // Add categories
            incoming.map { it.category }.distinct().forEach { cat ->
                onAddCategory(cat)
            }

            SyncResult.Success(
                addedCount = toInsert.size,
                totalHostCount = incoming.size,
                hostDevice = hostDevice
            )
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Failed to connect to host")
        }
    }
}
