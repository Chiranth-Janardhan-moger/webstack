package com.example.ui.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Resolves thumbnail/preview image URLs for saved websites.
 * Prefers direct video thumbnails and Open Graph/oEmbed metadata images before
 * falling back to full-page screenshots.
 */
object PreviewUrlResolver {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val youtubeIdRegex = Regex("^[A-Za-z0-9_-]{11}$")

    fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    /**
     * Extracts an 11-character YouTube video ID from standard, mobile, shorts, embed, or youtu.be URLs.
     */
    fun extractYouTubeVideoId(rawUrl: String): String? {
        val uri = runCatching { URI(normalizeUrl(rawUrl)) }.getOrNull() ?: return null
        val host = uri.host?.lowercase()?.removePrefix("www.")?.removePrefix("m.") ?: return null

        val candidate = when {
            host == "youtu.be" -> uri.path?.trim('/')?.substringBefore('/')
            host == "youtube.com" || host == "music.youtube.com" || host == "youtube-nocookie.com" -> {
                val path = uri.path.orEmpty()
                when {
                    path.startsWith("/shorts/") -> path.removePrefix("/shorts/").substringBefore('/')
                    path.startsWith("/embed/") -> path.removePrefix("/embed/").substringBefore('/')
                    path.startsWith("/live/") -> path.removePrefix("/live/").substringBefore('/')
                    path.startsWith("/v/") -> path.removePrefix("/v/").substringBefore('/')
                    else -> parseQueryParam(uri.rawQuery, "v")
                }
            }
            else -> null
        }

        return candidate?.takeIf { youtubeIdRegex.matches(it) }
    }

    private fun parseQueryParam(rawQuery: String?, key: String): String? {
        if (rawQuery.isNullOrBlank()) return null
        for (pair in rawQuery.split('&')) {
            val eqIdx = pair.indexOf('=')
            val k = if (eqIdx >= 0) pair.substring(0, eqIdx) else pair
            if (k == key && eqIdx >= 0) {
                return runCatching {
                    java.net.URLDecoder.decode(pair.substring(eqIdx + 1), "UTF-8")
                }.getOrElse { pair.substring(eqIdx + 1) }
            }
        }
        return null
    }

    /**
     * Extracts a Dailymotion video ID from dailymotion.com/video/<id> or dai.ly/<id>.
     */
    fun extractDailymotionVideoId(rawUrl: String): String? {
        val uri = runCatching { URI(normalizeUrl(rawUrl)) }.getOrNull() ?: return null
        val host = uri.host?.lowercase()?.removePrefix("www.") ?: return null
        val rawSegment = when {
            host == "dai.ly" -> uri.path?.trim('/')?.substringBefore('/')
            host == "dailymotion.com" -> {
                val path = uri.path.orEmpty()
                if (path.startsWith("/video/")) path.removePrefix("/video/").substringBefore('/') else null
            }
            else -> null
        }
        return rawSegment?.substringBefore('_')?.takeIf { it.isNotBlank() && it.all { ch -> ch.isLetterOrDigit() } }
    }

    /**
     * Returns an official oEmbed endpoint for platforms that expose video/media thumbnails cleanly.
     */
    fun buildOEmbedEndpoint(rawUrl: String): String? {
        val fullUrl = normalizeUrl(rawUrl)
        val uri = runCatching { URI(fullUrl) }.getOrNull() ?: return null
        val host = uri.host?.lowercase()?.removePrefix("www.")?.removePrefix("m.") ?: return null
        val encoded = URLEncoder.encode(fullUrl, "UTF-8")

        return when {
            host == "tiktok.com" || host.endsWith(".tiktok.com") ->
                "https://www.tiktok.com/oembed?url=$encoded"
            host == "vimeo.com" || host == "player.vimeo.com" ->
                "https://vimeo.com/api/oembed.json?url=$encoded"
            host == "soundcloud.com" ->
                "https://soundcloud.com/oembed?format=json&url=$encoded"
            host == "open.spotify.com" ->
                "https://open.spotify.com/oembed?url=$encoded"
            else -> null
        }
    }

    /**
     * Extracts the best metadata thumbnail image URL (og:image, twitter:image, thumbnailUrl) from HTML.
     */
    fun extractMetadataImageFromHtml(html: String, baseUrl: String): String? {
        if (html.isBlank()) return null
        val doc = Jsoup.parse(html, baseUrl)
        val selectors = listOf(
            "meta[property=og:image:secure_url]",
            "meta[property=og:image]",
            "meta[name=og:image]",
            "meta[name=twitter:image:src]",
            "meta[name=twitter:image]",
            "meta[property=twitter:image]",
            "meta[itemprop=thumbnailUrl]",
            "link[rel=image_src]"
        )
        for (selector in selectors) {
            val el = doc.selectFirst(selector) ?: continue
            val attr = if (selector.startsWith("link")) "abs:href" else "abs:content"
            val candidate = el.attr(attr).trim().ifBlank {
                val raw = el.attr(if (selector.startsWith("link")) "href" else "content").trim()
                if (raw.isNotBlank()) runCatching { URI(baseUrl).resolve(raw).toString() }.getOrNull().orEmpty() else ""
            }
            if (candidate.startsWith("http://", ignoreCase = true) ||
                candidate.startsWith("https://", ignoreCase = true)
            ) {
                return candidate
            }
        }
        return null
    }

    /**
     * Returns synchronous fast-path thumbnail + fallback URLs when the platform exposes a direct
     * thumbnail URL without requiring a network lookup, or null if async metadata lookup should run.
     */
    fun fastThumbnailChain(rawUrl: String, refreshToken: Long = 0L): List<String>? {
        val ytId = extractYouTubeVideoId(rawUrl)
        if (ytId != null) {
            val ts = if (refreshToken > 0L) "?t=$refreshToken" else ""
            return buildList {
                add("https://img.youtube.com/vi/$ytId/maxresdefault.jpg$ts")
                add("https://img.youtube.com/vi/$ytId/hqdefault.jpg$ts")
                addAll(screenshotFallbacks(rawUrl, refreshToken))
            }
        }

        val dmId = extractDailymotionVideoId(rawUrl)
        if (dmId != null) {
            val ts = if (refreshToken > 0L) "?t=$refreshToken" else ""
            return buildList {
                add("https://www.dailymotion.com/thumbnail/video/$dmId$ts")
                addAll(screenshotFallbacks(rawUrl, refreshToken))
            }
        }

        return null
    }

    fun screenshotFallbacks(rawUrl: String, refreshToken: Long = 0L): List<String> {
        val fullUrl = normalizeUrl(rawUrl)
        val encodedUrl = runCatching { URLEncoder.encode(fullUrl, "UTF-8") }.getOrDefault(fullUrl)
        val ts = if (refreshToken > 0L) "&t=$refreshToken" else ""
        val thumTarget = if (refreshToken > 0L) {
            val sep = if (fullUrl.contains("?")) "&" else "?"
            "$fullUrl${sep}t=$refreshToken"
        } else {
            fullUrl
        }
        return listOf(
            "https://api.microlink.io/?url=$encodedUrl&screenshot=true&embed=screenshot.url$ts",
            "https://image.thum.io/get/noanimate/width/800/crop/600/$thumTarget"
        )
    }

    /**
     * Resolves an ordered list of candidate preview URLs:
     * 1. oEmbed thumbnail_url (TikTok, Vimeo, SoundCloud, Spotify)
     * 2. Direct HTML Open Graph / Twitter image (og:image / twitter:image)
     * 3. Microlink metadata image (embed=image.url)
     * 4. Full-page screenshot fallbacks (Microlink screenshot -> thum.io)
     */
    suspend fun resolvePreviewChain(rawUrl: String, refreshToken: Long = 0L): List<String> {
        fastThumbnailChain(rawUrl, refreshToken)?.let { return it }

        val fullUrl = normalizeUrl(rawUrl)
        val encodedUrl = runCatching { URLEncoder.encode(fullUrl, "UTF-8") }.getOrDefault(fullUrl)
        val ts = if (refreshToken > 0L) "&t=$refreshToken" else ""

        val metadataUrl = withContext(Dispatchers.IO) {
            fetchOEmbedThumbnail(fullUrl) ?: fetchHtmlOgImage(fullUrl)
        }

        return buildList {
            if (!metadataUrl.isNullOrBlank()) {
                add(metadataUrl)
            }
            add("https://api.microlink.io/?url=$encodedUrl&embed=image.url$ts")
            addAll(screenshotFallbacks(fullUrl, refreshToken))
        }.distinct()
    }

    private fun fetchOEmbedThumbnail(fullUrl: String): String? {
        val endpoint = buildOEmbedEndpoint(fullUrl) ?: return null
        return runCatching {
            val request = Request.Builder()
                .url(endpoint)
                .header("User-Agent", "Mozilla/5.0 (Android) WebStack/2.1")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                val thumb = JSONObject(body).optString("thumbnail_url").trim()
                thumb.takeIf { it.startsWith("http://", true) || it.startsWith("https://", true) }
            }
        }.getOrNull()
    }

    private fun fetchHtmlOgImage(fullUrl: String): String? {
        return runCatching {
            val request = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val contentType = response.header("Content-Type").orEmpty()
                if (contentType.isNotBlank() && !contentType.contains("html", ignoreCase = true)) {
                    return@use null
                }
                // Read at most 256 KB so large pages don't waste memory; meta tags live in <head>.
                val source = response.body?.source() ?: return@use null
                source.request(262_144L)
                val html = source.buffer.clone().readUtf8()
                val finalUrl = response.request.url.toString()
                extractMetadataImageFromHtml(html, finalUrl)
            }
        }.getOrNull()
    }
}
