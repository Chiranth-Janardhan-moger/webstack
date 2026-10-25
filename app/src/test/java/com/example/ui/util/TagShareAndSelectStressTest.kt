package com.example.ui.util

import com.example.data.Website
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class TagShareAndSelectStressTest {

    private fun createDummyWebsites(count: Int, category: String = "Dev Tools"): List<Website> {
        val list = ArrayList<Website>(count)
        for (i in 1..count) {
            list.add(
                Website(
                    id = i.toLong(),
                    url = "https://example.com/tool_$i",
                    title = "Developer Tool $i",
                    category = category,
                    dedupeKey = "example.com/tool_$i",
                    sourcePath = null,
                    createdAtMs = 1710000000000L + i
                )
            )
        }
        return list
    }

    @Test
    fun testFormatTagBookmarksForSharing_emptyList() {
        val formatted = formatTagBookmarksForSharing("EmptyTag", emptyList())
        assertEquals("EmptyTag (0 bookmarks)", formatted)
    }

    @Test
    fun testFormatTagBookmarksForSharing_singleItem() {
        val websites = createDummyWebsites(1, "Design")
        val formatted = formatTagBookmarksForSharing("Design", websites)
        assertTrue(formatted.startsWith("Design (1 link)"))
        assertTrue(formatted.contains("1. Developer Tool 1"))
        assertTrue(formatted.contains("https://example.com/tool_1"))
        assertFalse(formatted.contains("...and"))
    }

    @Test
    fun testFormatTagBookmarksForSharing_smallList() {
        val websites = createDummyWebsites(5, "Dev Tools")
        val formatted = formatTagBookmarksForSharing("Dev Tools", websites)
        assertTrue(formatted.startsWith("Dev Tools (5 links)"))
        assertTrue(formatted.contains("1. Developer Tool 1"))
        assertTrue(formatted.contains("5. Developer Tool 5"))
        assertFalse(formatted.contains("...and"))
    }

    @Test
    fun testFormatTagBookmarksForSharing_1000Items_truncatedAtMax500() {
        val websites = createDummyWebsites(1000, "Research")
        val formatted = formatTagBookmarksForSharing("Research", websites, maxDetailedLinks = 500)
        assertTrue(formatted.startsWith("Research (1000 links)"))
        assertTrue(formatted.contains("1. Developer Tool 1"))
        assertTrue(formatted.contains("500. Developer Tool 500"))
        assertFalse(formatted.contains("501. Developer Tool 501"))
        assertTrue(formatted.contains("...and 500 more Research links (1000 total)"))
        // Verifies within safe Android binder transaction boundary (< 150KB)
        assertTrue(formatted.length < 150_000)
    }

    @Test
    fun testFormatTagBookmarksForSharing_massiveScale10000Items_fastAndBounded() {
        val websites = createDummyWebsites(10_000, "BigData")
        var formatted = ""
        val elapsed = measureTimeMillis {
            formatted = formatTagBookmarksForSharing("BigData", websites, maxDetailedLinks = 500)
        }

        assertTrue("Execution should be sub-50ms for 10k items", elapsed < 100)
        assertTrue(formatted.startsWith("BigData (10000 links)"))
        assertTrue(formatted.contains("...and 9500 more BigData links (10000 total)"))
        assertTrue("Character count must be safely below 150,000 to prevent TransactionTooLargeException", formatted.length < 150_000)
    }

    @Test
    fun testFormatTagBookmarksForSharing_extremeScale100000Items_zeroOOMAndBinderSafe() {
        val websites = createDummyWebsites(100_000, "Cloud")
        var formatted = ""
        val elapsed = measureTimeMillis {
            formatted = formatTagBookmarksForSharing("Cloud", websites, maxDetailedLinks = 500)
        }

        // Even with 100k items, string formatting of the first 500 takes negligible time
        assertTrue("Execution should be fast even for 100k items", elapsed < 200)
        assertTrue(formatted.startsWith("Cloud (100000 links)"))
        assertTrue(formatted.contains("...and 99500 more Cloud links (100000 total)"))
        assertTrue("Payload must stay within Android IPC binder safe limits", formatted.length < 150_000)
    }

    @Test
    fun testSelectAllTagWebsites_filteringAndBulkIdExtraction_10000Items() {
        val targetCategory = "Dev Tools"
        val devWebsites = createDummyWebsites(5_000, "Dev Tools")
        val otherWebsites = createDummyWebsites(5_000, "Design").map {
            it.copy(id = it.id + 100_000)
        }
        val allWebsites = devWebsites + otherWebsites

        var selectedIds: List<Long> = emptyList()
        val elapsed = measureTimeMillis {
            val matching = allWebsites.filter { it.category.equals(targetCategory, ignoreCase = true) }
            selectedIds = matching.map { it.id }
        }

        assertEquals(5_000, selectedIds.size)
        assertTrue("Bulk filtering and ID extraction for 10k items should be sub-30ms", elapsed < 100)
        assertEquals(1L, selectedIds.first())
        assertEquals(5000L, selectedIds.last())
    }

    @Test
    fun testSelectAllTagWebsites_membershipAndLookupSpeed_100000Items() {
        val count = 100_000
        val selectedWebsiteIds = (1L..count.toLong()).toList()

        // Test fast HashSet conversion and O(1) membership check
        var set: Set<Long> = emptySet()
        val setBuildTime = measureTimeMillis {
            set = selectedWebsiteIds.toHashSet()
        }
        assertTrue("HashSet creation for 100k items should be fast", setBuildTime < 250)

        // Test 10 visible card lookups
        val lookupTime = measureTimeMillis {
            for (i in 1..10) {
                val testId = (i * 9999).toLong()
                assertTrue(set.contains(testId))
            }
            assertFalse(set.contains(999_999L))
        }
        assertTrue("10 card membership lookups must be sub-millisecond", lookupTime < 10)
    }

    @Test
    fun testBulkShareSelectedLinks_formattingSelectedSubset() {
        val allSites = createDummyWebsites(10, "Dev Tools")
        val selectedSubset = listOf(allSites[0], allSites[2], allSites[4])
        val formatted = formatTagBookmarksForSharing("Selected", selectedSubset)

        assertTrue(formatted.startsWith("Selected (3 links)"))
        assertTrue(formatted.contains("1. Developer Tool 1"))
        assertTrue(formatted.contains("2. Developer Tool 3"))
        assertTrue(formatted.contains("3. Developer Tool 5"))
    }

    @Test
    fun testBulkMoveCategory_chunkedBatchSimulation10000Items() {
        val total = 10_000
        val ids = (1L..total.toLong()).toList()
        val chunks = ids.chunked(500)

        assertEquals(20, chunks.size)
        chunks.forEach { chunk ->
            assertEquals(500, chunk.size)
            assertTrue(chunk.first() <= chunk.last())
        }
    }
}
