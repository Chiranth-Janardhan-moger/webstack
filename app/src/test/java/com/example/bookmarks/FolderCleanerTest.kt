package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FolderCleanerTest {

    @Test
    fun testNoiseFolders_areRejected() {
        val noiseList = listOf(
            "Bookmarks",
            "Bookmarks bar",
            "bookmarks toolbar",
            "Bookmark menu",
            "Other bookmarks",
            "mobile bookmarks",
            "Favourites",
            "Favorites bar",
            "unfiled bookmarks",
            "imported",
            "imported from chrome",
            "new folder",
            "new folder (1)",
            "untitled",
            "untitled folder",
            "untitled folder 2",
            "temp",
            "temporary",
            "tmp",
            "stuff",
            "misc",
            "miscellaneous",
            "links",
            "link",
            "asdf",
            "asdffff"
        )
        for (noise in noiseList) {
            assertNull("Expected '$noise' to be recognized as noise", FolderCleaner.cleanFolderName(noise))
        }
    }

    @Test
    fun testOrderPrefixStripping() {
        assertEquals("Tech", FolderCleaner.cleanFolderName("01_Tech"))
        assertEquals("Tech", FolderCleaner.cleanFolderName("1. Tech"))
        assertEquals("Tech", FolderCleaner.cleanFolderName("01 - Tech"))
        assertEquals("Tech", FolderCleaner.cleanFolderName("1) Tech"))
        assertEquals("Development", FolderCleaner.cleanFolderName("007_Development"))
        assertEquals("Design System", FolderCleaner.cleanFolderName("2. Design System"))
    }

    @Test
    fun testRequiredNegativeCases_notCleanedOrRejected() {
        val preserved = listOf(
            "Templates",
            "Temperature Control",
            "Tempo",
            "3D Printing",
            "5-minute meals",
            "2024 Taxes",
            "1.5 GHz CPUs"
        )
        for (f in preserved) {
            val cleaned = FolderCleaner.cleanFolderName(f)
            assertNotNull("Folder '$f' must not be rejected", cleaned)
            assertEquals("Folder '$f' must be preserved as-is", f, cleaned)
        }
    }

    @Test
    fun testCanonicalDisplayNames_casingFrequencyAndTies() {
        // Frequency winner
        val list1 = listOf("Tech", "tech", "Tech")
        val map1 = FolderCleaner.canonicalDisplayNames(list1)
        assertEquals("Tech", map1["tech"])

        // Tied frequency: first seen wins
        val list2 = listOf("mobile", "Mobile")
        val map2 = FolderCleaner.canonicalDisplayNames(list2)
        assertEquals("mobile", map2["mobile"])

        val list3 = listOf("Mobile", "mobile")
        val map3 = FolderCleaner.canonicalDisplayNames(list3)
        assertEquals("Mobile", map3["mobile"])
    }
}
