package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookmarkNormalizerTest {

    @Test
    fun testNormalizeToMillis_secondsMillisMicrosZeroNegative() {
        // Zero and negative
        assertEquals(0L, BookmarkNormalizer.normalizeToMillis(0L))
        assertEquals(0L, BookmarkNormalizer.normalizeToMillis(-100L))

        // Seconds (< 100_000_000_000L)
        val sec = 1710000000L
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(sec))

        // Millis (< 100_000_000_000_000L)
        val ms = 1710000000000L
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(ms))

        // Micros (>= 100_000_000_000_000L)
        val micros = 1710000000000000L
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(micros))
    }

    @Test
    fun testDedupeKey_supportedAndUnsupportedSchemes() {
        // Supported
        assertEquals("http://example.com/", BookmarkNormalizer.dedupeKey("http://example.com"))
        assertEquals("https://example.com/", BookmarkNormalizer.dedupeKey("https://example.com"))

        // Unsupported schemes return null
        assertNull(BookmarkNormalizer.dedupeKey("javascript:alert(1)"))
        assertNull(BookmarkNormalizer.dedupeKey("chrome://settings"))
        assertNull(BookmarkNormalizer.dedupeKey("about:blank"))
        assertNull(BookmarkNormalizer.dedupeKey("place:type=6&sort=14"))
        assertNull(BookmarkNormalizer.dedupeKey("file:///sdcard/doc.pdf"))
        assertNull(BookmarkNormalizer.dedupeKey("data:text/html,hello"))
        assertNull(BookmarkNormalizer.dedupeKey("ftp://example.com"))
        assertNull(BookmarkNormalizer.dedupeKey("invalid-url"))
    }

    @Test
    fun testDedupeKey_stripsWwwAndFragmentsAndTrackingParameters() {
        val base = "https://example.com/path"
        val withWww = "https://www.example.com/path"
        val withFragment = "https://example.com/path#section-1"
        val withTracking = "https://example.com/path?utm_source=twitter&utm_medium=social&fbclid=123&gclid=456&mc_eid=789&igshid=abc"
        val withSlash = "https://example.com/path/"

        val keyBase = BookmarkNormalizer.dedupeKey(base)
        assertEquals(keyBase, BookmarkNormalizer.dedupeKey(withWww))
        assertEquals(keyBase, BookmarkNormalizer.dedupeKey(withFragment))
        assertEquals(keyBase, BookmarkNormalizer.dedupeKey(withTracking))
        assertEquals(keyBase, BookmarkNormalizer.dedupeKey(withSlash))
    }

    @Test
    fun testDedupeKey_preservesLegitimateQueryParametersAndDifferentPaths() {
        val url1 = "https://example.com/search?q=kotlin"
        val url2 = "https://example.com/search?q=android"
        assertNotEquals(BookmarkNormalizer.dedupeKey(url1), BookmarkNormalizer.dedupeKey(url2))

        val path1 = "https://example.com/docs/a"
        val path2 = "https://example.com/docs/b"
        assertNotEquals(BookmarkNormalizer.dedupeKey(path1), BookmarkNormalizer.dedupeKey(path2))
    }

    @Test
    fun testDedupeKey_httpAndHttpsStayDistinct() {
        val http = BookmarkNormalizer.dedupeKey("http://example.com/page")
        val https = BookmarkNormalizer.dedupeKey("https://example.com/page")
        assertNotEquals(http, https)
        assertEquals("http://example.com/page", http)
        assertEquals("https://example.com/page", https)
    }

    @Test
    fun testDedupeKey_defaultAndCustomPorts() {
        // Default ports omitted
        assertEquals("http://example.com/", BookmarkNormalizer.dedupeKey("http://example.com:80/"))
        assertEquals("https://example.com/", BookmarkNormalizer.dedupeKey("https://example.com:443/"))

        // Custom ports retained
        assertEquals("http://example.com:8080/", BookmarkNormalizer.dedupeKey("http://example.com:8080/"))
        assertEquals("https://example.com:8443/", BookmarkNormalizer.dedupeKey("https://example.com:8443/"))
    }
}
