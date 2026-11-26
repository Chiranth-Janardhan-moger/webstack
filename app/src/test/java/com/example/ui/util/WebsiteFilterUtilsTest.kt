package com.example.ui.util

import com.example.data.Website
import com.example.ui.model.BookmarkSortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebsiteFilterUtilsTest {

    private val sites = listOf(
        Website(id = 1L, url = "https://work.com", title = "Work Tool", category = "Work", dedupeKey = "https://work.com/"),
        Website(id = 2L, url = "https://hidden.com", title = "Archived Doc", category = "Archive", dedupeKey = "https://hidden.com/"),
        Website(id = 3L, url = "https://secret.com", title = "Private Vault", category = "Vault", dedupeKey = "https://secret.com/")
    )

    @Test
    fun hiddenTags_areMutedOutOfAllFavoritesAndCounts_whileLockedCountsInAll() {
        val parked = listOf("Archive")
        val locked = listOf("Vault")
        val favorites = setOf(1L, 2L, 3L)

        // All capsule excludes Hidden (Archive = 1) but keeps Locked (Vault = 1) counted -> Work + Vault = 2
        assertEquals(2, WebsiteFilterUtils.countVisibleAllWebsites(sites, parked))

        // Favorites count excludes both Hidden (2L) and sealed Locked (3L) -> only 1L
        assertEquals(
            1,
            WebsiteFilterUtils.countVisibleFavorites(
                websites = sites,
                favoriteIds = favorites,
                parkedCategories = parked,
                lockedCategories = locked,
                unlockedCategories = emptySet()
            )
        )

        // All feed excludes both Locked and Hidden -> only Work (1L)
        val allFeed = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "All",
            searchQuery = "",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = favorites,
            parkedCategories = parked,
            lockedCategories = locked,
            unlockedCategories = emptySet()
        )
        assertEquals(listOf(1L), allFeed.map { it.id })

        // Search under All excludes Hidden & Locked bookmarks
        val searchFeed = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "All",
            searchQuery = "com",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = favorites,
            parkedCategories = parked,
            lockedCategories = locked,
            unlockedCategories = emptySet()
        )
        assertEquals(listOf(1L), searchFeed.map { it.id })

        // Favorites feed excludes Hidden (2L) even though Locked (3L) becomes visible if session-unlocked
        val favSealed = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "Favorites",
            searchQuery = "",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = favorites,
            parkedCategories = parked,
            lockedCategories = locked,
            unlockedCategories = emptySet()
        )
        assertEquals(listOf(1L), favSealed.map { it.id })

        val favLockedUnlocked = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "Favorites",
            searchQuery = "",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = favorites,
            parkedCategories = parked,
            lockedCategories = locked,
            unlockedCategories = setOf("vault")
        )
        assertEquals(listOf(1L, 3L), favLockedUnlocked.map { it.id })
        assertFalse(favLockedUnlocked.any { it.id == 2L })
    }

    @Test
    fun unlockedLockedTag_showsCardsWhenSelectedDirectly() {
        val sealedFeed = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "Vault",
            searchQuery = "",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = emptySet(),
            parkedCategories = emptyList(),
            lockedCategories = listOf("Vault"),
            unlockedCategories = emptySet()
        )
        assertTrue(sealedFeed.isEmpty())

        val unlockedFeed = WebsiteFilterUtils.filterHomeWebsites(
            websites = sites,
            selectedCategory = "Vault",
            searchQuery = "",
            sortOrder = BookmarkSortOrder.NEWEST,
            favoriteIds = emptySet(),
            parkedCategories = emptyList(),
            lockedCategories = listOf("Vault"),
            unlockedCategories = setOf("vault")
        )
        assertEquals(listOf(3L), unlockedFeed.map { it.id })
    }
}
