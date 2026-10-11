package com.example.bookmarks

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object BookmarkNormalizer {
    private val TRACKING = Regex("^(utm_.*|fbclid|gclid|mc_eid|igshid)$", RegexOption.IGNORE_CASE)

    /**
     * Timestamps: accept seconds, millis, micros.
     */
    fun normalizeToMillis(raw: Long): Long = when {
        raw <= 0L -> 0L
        raw < 100_000_000_000L -> raw * 1000          // seconds
        raw < 100_000_000_000_000L -> raw             // millis
        else -> raw / 1000                            // micros
    }

    /**
     * URL: toHttpUrlOrNull rejects javascript:, chrome:, about:, place:, file:, data:.
     * Returns deduplication key, or null if scheme is unsupported.
     */
    fun dedupeKey(raw: String): String? {
        val url = raw.trim().toHttpUrlOrNull() ?: return null   // null => skip, count UNSUPPORTED_SCHEME
        val b = url.newBuilder().fragment(null)
        url.queryParameterNames.filter { TRACKING.matches(it) }.forEach { b.removeAllQueryParameters(it) }
        val u = b.build()
        val host = u.host.removePrefix("www.")
        val path = if (u.encodedPath.length > 1) u.encodedPath.trimEnd('/').ifEmpty { "/" } else u.encodedPath
        val port = if (u.port != HttpUrl.defaultPort(u.scheme)) ":${u.port}" else ""
        val query = u.encodedQuery?.let { "?$it" } ?: ""
        return "${u.scheme}://$host$port$path$query"            // http and https stay distinct
    }
}
