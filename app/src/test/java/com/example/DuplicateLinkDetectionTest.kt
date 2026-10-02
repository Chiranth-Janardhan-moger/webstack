package com.example

import com.example.ui.util.normalizeUrlForComparison
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DuplicateLinkDetectionTest {

    @Test
    fun testSameDomainDifferentPaths_areNotDuplicates() {
        val link1 = normalizeUrlForComparison("reading.com/book1")
        val link2 = normalizeUrlForComparison("reading.com/book2")

        assertNotEquals(link1, link2)
        assertEquals("reading.com/book1", link1)
        assertEquals("reading.com/book2", link2)
    }

    @Test
    fun testSameLinkDifferentSchemesAndWww_areDuplicates() {
        val linkPlain = normalizeUrlForComparison("reading.com/book1")
        val linkHttps = normalizeUrlForComparison("https://reading.com/book1")
        val linkHttp = normalizeUrlForComparison("http://reading.com/book1")
        val linkWww = normalizeUrlForComparison("https://www.reading.com/book1")

        assertEquals(linkPlain, linkHttps)
        assertEquals(linkHttps, linkHttp)
        assertEquals(linkHttps, linkWww)
    }

    @Test
    fun testTrailingSlashNormalization() {
        val withSlash = normalizeUrlForComparison("https://github.com/torvalds/linux/")
        val withoutSlash = normalizeUrlForComparison("https://github.com/torvalds/linux")
        assertEquals(withoutSlash, withSlash)

        val rootWithSlash = normalizeUrlForComparison("https://github.com/")
        val rootWithoutSlash = normalizeUrlForComparison("https://github.com")
        assertEquals(rootWithoutSlash, rootWithSlash)
    }

    @Test
    fun testQueryParameters_sameContentDifferentOrder_areDuplicates() {
        val url1 = normalizeUrlForComparison("https://example.com/search?page=2&q=jetpack")
        val url2 = normalizeUrlForComparison("https://example.com/search?q=jetpack&page=2")
        assertEquals(url1, url2)

        val differentQuery = normalizeUrlForComparison("https://example.com/search?q=compose&page=2")
        assertNotEquals(url1, differentQuery)
    }

    @Test
    fun testFragments_differentiateSubSections() {
        val anchor1 = normalizeUrlForComparison("https://example.com/doc#part1")
        val anchor2 = normalizeUrlForComparison("https://example.com/doc#part2")
        val anchor1Repeat = normalizeUrlForComparison("https://example.com/doc#part1")

        assertNotEquals(anchor1, anchor2)
        assertEquals(anchor1, anchor1Repeat)
    }
}
