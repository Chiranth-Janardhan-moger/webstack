package com.example.server

import android.content.Context
import android.os.Build
import com.example.data.WebsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay

data class ServerStatus(
    val isRunning: Boolean = false,
    val ipAddress: String? = null,
    val port: Int = 8080,
    val pin: String = "",
    val connectedClients: Int = 0,
    val errorMessage: String? = null
)

class LocalHttpServer(
    private val context: Context,
    private val repository: WebsiteRepository,
    private val getCategories: () -> List<String>,
    private val getOfflineExpiryHours: () -> Int = { 24 }
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val secureRandom = SecureRandom()

    private val _status = MutableStateFlow(ServerStatus())
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private var activePin: String = ""
    private var activeSessionToken: String = ""
    private val activeSessions = ConcurrentHashMap<String, Long>()

    @Synchronized
    fun start(preferredPort: Int = 8080, customPin: String? = null) {
        if (_status.value.isRunning) return

        val ip = WifiUtils.getLocalIpAddress()
        if (ip == null) {
            _status.value = ServerStatus(
                isRunning = false,
                errorMessage = "No active Wi-Fi connection detected. Connect to Wi-Fi first."
            )
            return
        }

        activePin = if (!customPin.isNullOrBlank() && customPin.trim().length == 6 && customPin.trim().all { it.isDigit() }) customPin.trim() else generatePin()
        activeSessionToken = generateToken()

        serverJob = scope.launch {
            var boundSocket: ServerSocket? = null
            var boundPort = preferredPort
            val maxPort = (preferredPort + 10).coerceAtMost(65535)
            for (p in preferredPort..maxPort) {
                try {
                    val s = ServerSocket()
                    s.reuseAddress = true
                    s.bind(InetSocketAddress(p))
                    boundSocket = s
                    boundPort = p
                    break
                } catch (_: Exception) {}
            }

            if (boundSocket == null) {
                _status.value = ServerStatus(
                    isRunning = false,
                    errorMessage = "Failed to bind local port ($preferredPort-$maxPort)"
                )
                return@launch
            }

            serverSocket = boundSocket
            _status.value = ServerStatus(
                isRunning = true,
                ipAddress = ip,
                port = boundPort,
                pin = activePin,
                connectedClients = 0,
                errorMessage = null
            )

            launch {
                while (isActive) {
                    delay(3000L)
                    cleanupOldSessions()
                }
            }

            try {
                while (isActive && !boundSocket.isClosed) {
                    val clientSocket = try {
                        boundSocket.accept()
                    } catch (_: Exception) {
                        break
                    }
                    launch {
                        handleClient(clientSocket)
                    }
                }
            } finally {
                synchronized(this@LocalHttpServer) {
                    if (serverSocket == boundSocket) {
                        stop()
                    }
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
        activeSessions.clear()
        _status.value = ServerStatus(isRunning = false, connectedClients = 0)
    }

    private fun generatePin(): String {
        val n = secureRandom.nextInt(900000) + 100000
        return n.toString()
    }

    private fun generateToken(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun recordClientActivity(clientId: String) {
        activeSessions[clientId] = System.currentTimeMillis()
        cleanupOldSessions()
    }

    private fun cleanupOldSessions() {
        val cutoff = System.currentTimeMillis() - 25_000L
        activeSessions.entries.removeIf { it.value < cutoff }
        val count = activeSessions.size
        if (_status.value.isRunning && _status.value.connectedClients != count) {
            _status.value = _status.value.copy(connectedClients = count)
        }
    }

    private suspend fun handleClient(socket: Socket) {
        val remoteIp = (socket.remoteSocketAddress as? InetSocketAddress)?.address?.hostAddress ?: "client"
        try {
            socket.use { s ->
                s.soTimeout = 12000
                val input = s.getInputStream()
                val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
                val output = s.getOutputStream()

                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                val method = parts[0].uppercase()
                val rawPath = parts[1]

                // Parse headers
                val headers = mutableMapOf<String, String>()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) break
                    val colon = line!!.indexOf(':')
                    if (colon > 0) {
                        val k = line!!.substring(0, colon).trim().lowercase()
                        val v = line!!.substring(colon + 1).trim()
                        headers[k] = v
                    }
                }

                // Read body if present
                val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                val body = if (contentLength > 0 && contentLength < 1024 * 1024) {
                    val buffer = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val r = reader.read(buffer, read, contentLength - read)
                        if (r == -1) break
                        read += r
                    }
                    String(buffer, 0, read)
                } else ""

                val uriPath = if (rawPath.contains("?")) rawPath.substringBefore("?") else rawPath
                val queryParams = parseQueryParams(rawPath)

                if (method == "OPTIONS") {
                    sendCorsResponse(output)
                    return
                }

                when {
                    uriPath == "/" || uriPath == "/index.html" -> {
                        serveWebCompanion(output)
                    }
                    uriPath == "/sw.js" -> {
                        serveServiceWorker(output)
                    }
                    uriPath == "/manifest.json" -> {
                        serveManifest(output)
                    }
                    uriPath == "/favicon.ico" -> {
                        serveFaviconIco(output)
                    }
                    uriPath == "/favicon.png" -> {
                        serveFaviconPng(output)
                    }
                    uriPath == "/favicon.svg" -> {
                        serveFaviconSvg(output)
                    }
                    uriPath == "/api/status" -> {
                        val hasToken = (headers["authorization"]?.startsWith("Bearer ", ignoreCase = true) == true) || queryParams.containsKey("token")
                        val isAuth = isAuthorized(headers, queryParams)
                        if (isAuth) {
                            recordClientActivity(remoteIp)
                        }
                        if (hasToken && !isAuth) {
                            sendJsonResponse(output, 401, """{"error":"Unauthorized: Invalid or expired session token","status":"online","authenticated":false}""")
                            return
                        }
                        val json = JSONObject().apply {
                            put("status", "online")
                            put("authenticated", isAuth)
                            put("device", Build.MODEL ?: "Android Device")
                            put("bookmarksCount", repository.getAllWebsitesList().size)
                            put("offlineExpiryHours", getOfflineExpiryHours())
                        }
                        sendJsonResponse(output, 200, json.toString())
                    }
                    uriPath == "/api/auth" && method == "POST" -> {
                        handleAuth(body, output, remoteIp)
                    }
                    uriPath == "/api/disconnect" && method == "POST" -> {
                        activeSessions.remove(remoteIp)
                        cleanupOldSessions()
                        sendJsonResponse(output, 200, """{"success":true}""")
                    }
                    uriPath == "/api/categories" -> {
                        if (!isAuthorized(headers, queryParams)) {
                            sendJsonResponse(output, 401, """{"error":"Unauthorized"}""")
                            return
                        }
                        recordClientActivity(remoteIp)
                        val categories = getCategories()
                        val jsonArray = JSONArray(categories)
                        sendJsonResponse(output, 200, jsonArray.toString())
                    }
                    uriPath == "/api/bookmarks" && method == "GET" -> {
                        if (!isAuthorized(headers, queryParams)) {
                            sendJsonResponse(output, 401, """{"error":"Unauthorized"}""")
                            return
                        }
                        recordClientActivity(remoteIp)
                        val bookmarks = repository.getAllWebsitesList()
                        val array = JSONArray()
                        bookmarks.forEach { b ->
                            val obj = JSONObject().apply {
                                put("id", b.id)
                                put("title", b.title)
                                put("url", b.url)
                                put("domain", b.domain)
                                put("category", b.category)
                                put("createdAtMs", b.createdAtMs)
                            }
                            array.put(obj)
                        }
                        sendJsonResponse(output, 200, array.toString())
                    }
                    uriPath == "/api/bookmarks" && method == "POST" -> {
                        if (!isAuthorized(headers, queryParams)) {
                            sendJsonResponse(output, 401, """{"error":"Unauthorized"}""")
                            return
                        }
                        recordClientActivity(remoteIp)
                        handleCreateBookmark(body, output)
                    }
                    else -> {
                        sendJsonResponse(output, 404, """{"error":"Not Found"}""")
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun handleAuth(body: String, output: OutputStream, remoteIp: String) {
        try {
            val json = JSONObject(body)
            val submittedPin = json.optString("pin", "").trim()
            if (submittedPin.isNotBlank() && submittedPin == activePin) {
                recordClientActivity(remoteIp)
                val resp = JSONObject().apply {
                    put("token", activeSessionToken)
                    put("success", true)
                    put("offlineExpiryHours", getOfflineExpiryHours())
                }
                sendJsonResponse(output, 200, resp.toString())
            } else {
                sendJsonResponse(output, 403, """{"error":"Invalid PIN. Please check your phone screen."}""")
            }
        } catch (_: Exception) {
            sendJsonResponse(output, 400, """{"error":"Malformed JSON"}""")
        }
    }

    private suspend fun handleCreateBookmark(body: String, output: OutputStream) {
        try {
            val json = JSONObject(body)
            val rawUrl = json.optString("url", "").trim()
            val category = json.optString("category", "Personal").trim()

            if (rawUrl.isBlank()) {
                sendJsonResponse(output, 400, """{"error":"URL cannot be empty"}""")
                return
            }

            val result = repository.fetchAndSave(rawUrl, category)
            result.onSuccess { site ->
                val resp = JSONObject().apply {
                    put("success", true)
                    put("id", site.id)
                    put("title", site.title)
                    put("url", site.url)
                    put("domain", site.domain)
                    put("category", site.category)
                    put("createdAtMs", site.createdAtMs)
                }
                sendJsonResponse(output, 201, resp.toString())
                scope.launch {
                    repository.fetchTitleAsync(site.id, site.url, site.domain)
                }
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Failed to save bookmark"
                sendJsonResponse(output, 422, """{"error":${JSONObject.quote(errorMsg)}}""")
            }
        } catch (_: Exception) {
            sendJsonResponse(output, 400, """{"error":"Invalid JSON payload"}""")
        }
    }

    private fun isAuthorized(headers: Map<String, String>, queryParams: Map<String, String>): Boolean {
        val authHeader = headers["authorization"]
        if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
            val token = authHeader.substring(7).trim()
            if (token == activeSessionToken && activeSessionToken.isNotBlank()) return true
        }

        val paramToken = queryParams["token"]
        if (paramToken != null && paramToken == activeSessionToken && activeSessionToken.isNotBlank()) {
            return true
        }

        return false
    }

    private fun serveWebCompanion(output: OutputStream) {
        try {
            context.assets.open("web/index.html").use { input ->
                val buffer = ByteArray(8192)
                val byteStream = ByteArrayOutputStream()
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    byteStream.write(buffer, 0, read)
                }
                val htmlBytes = byteStream.toByteArray()

                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html; charset=utf-8\r\n" +
                        "Content-Length: ${htmlBytes.size}\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"

                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(htmlBytes)
                output.flush()
            }
        } catch (e: Exception) {
            val errorHtml = "<html><body><h1>WebStack Web Companion</h1><p>Failed to load web assets: ${e.message}</p></body></html>"
            val errorBytes = errorHtml.toByteArray(Charsets.UTF_8)
            val header = "HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Type: text/html; charset=utf-8\r\n" +
                    "Content-Length: ${errorBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(header.toByteArray(Charsets.UTF_8))
            output.write(errorBytes)
            output.flush()
        }
    }

    private fun serveServiceWorker(output: OutputStream) {
        try {
            context.assets.open("web/sw.js").use { input ->
                val buffer = ByteArray(8192)
                val byteStream = ByteArrayOutputStream()
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    byteStream.write(buffer, 0, read)
                }
                val jsBytes = byteStream.toByteArray()

                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/javascript; charset=utf-8\r\n" +
                        "Content-Length: ${jsBytes.size}\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Service-Worker-Allowed: /\r\n" +
                        "Connection: close\r\n\r\n"

                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(jsBytes)
                output.flush()
            }
        } catch (_: Exception) {
            sendJsonResponse(output, 404, """{"error":"sw.js not found"}""")
        }
    }

    private fun serveManifest(output: OutputStream) {
        val icon = JSONObject().apply {
            put("src", "/favicon.svg")
            put("sizes", "any")
            put("type", "image/svg+xml")
            put("purpose", "any maskable")
        }
        val json = JSONObject().apply {
            put("name", "WebStack PC Companion")
            put("short_name", "WebStack")
            put("description", "Local-first bookmark companion")
            put("start_url", "/")
            put("display", "standalone")
            put("background_color", "#121214")
            put("theme_color", "#121214")
            put("icons", JSONArray().put(icon))
        }
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/manifest+json; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun serveFaviconSvg(output: OutputStream) {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32"><rect width="32" height="32" rx="8" fill="#18181b"/><path d="M16 6 L7 25 L16 19 L25 25 Z" fill="#ffffff"/></svg>"""
        val bytes = svg.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: image/svg+xml; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Cache-Control: public, max-age=86400\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun serveFaviconPng(output: OutputStream) {
        serveAssetBinary(output, "web/favicon.png", "image/png")
    }

    private fun serveFaviconIco(output: OutputStream) {
        serveAssetBinary(output, "web/favicon.ico", "image/x-icon")
    }

    private fun serveAssetBinary(output: OutputStream, assetPath: String, contentType: String) {
        try {
            context.assets.open(assetPath).use { input ->
                val buffer = ByteArray(8192)
                val byteStream = ByteArrayOutputStream()
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    byteStream.write(buffer, 0, read)
                }
                val bytes = byteStream.toByteArray()
                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: $contentType\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Cache-Control: public, max-age=86400\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(bytes)
                output.flush()
            }
        } catch (_: Exception) {
            serveFaviconSvg(output)
        }
    }

    private fun sendJsonResponse(output: OutputStream, statusCode: Int, json: String) {
        val statusText = when (statusCode) {
            200 -> "OK"
            201 -> "Created"
            204 -> "No Content"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            422 -> "Unprocessable Entity"
            else -> "Error"
        }
        val bytes = json.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: *\r\n" +
                "Access-Control-Allow-Private-Network: true\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun sendCorsResponse(output: OutputStream) {
        val header = "HTTP/1.1 204 No Content\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: *\r\n" +
                "Access-Control-Allow-Private-Network: true\r\n" +
                "Access-Control-Max-Age: 86400\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    private fun parseQueryParams(path: String): Map<String, String> {
        val query = path.substringAfter("?", "")
        if (query.isBlank()) return emptyMap()
        val result = mutableMapOf<String, String>()
        for (param in query.split("&")) {
            val pair = param.split("=")
            if (pair.isNotEmpty()) {
                val key = URLDecoder.decode(pair[0], "UTF-8")
                val value = if (pair.size > 1) URLDecoder.decode(pair[1], "UTF-8") else ""
                result[key] = value
            }
        }
        return result
    }
}
