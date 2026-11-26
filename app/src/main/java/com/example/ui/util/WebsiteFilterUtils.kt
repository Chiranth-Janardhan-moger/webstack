package com.example.ui.util

import com.example.data.Website
import com.example.ui.model.BookmarkSortOrder

/**
 * Pure filtering and visibility helpers shared by the Home screen, capsule bar, and unit tests.
 *
 * Visibility contract:
 * - Hidden (`parkedCategories`) tags are muted/archived out of All, Favorites, and the Home capsule bar.
 * - Locked (`lockedCategories`) tags stay counted in the All capsule and keep their pill on Home,
 *   while their cards stay hidden from All, Favorites (until unlocked), and search.
 */
object WebsiteFilterUtils {

    fun countVisibleAllWebsites(
        websites: List<Website>,
        parkedCategories: List<String>
    ): Int {
        if (parkedCategories.isEmpty()) return websites.size
        val parkedLower = parkedCategories.map { it.lowercase() }.toSet()
        return websites.count { !parkedLower.contains(it.category.lowercase()) }
    }

    fun countVisibleFavorites(
        websites: List<Website>,
        favoriteIds: Set<Long>,
        parkedCategories: List<String>,
        lockedCategories: List<String>,
        unlockedCategories: Set<String>
    ): Int {
        val parkedLower = parkedCategories.map { it.lowercase() }.toSet()
        val lockedLower = lockedCategories.map { it.lowercase() }.toSet()
        val unlockedLower = unlockedCategories.map { it.lowercase() }.toSet()
        val sealedLockedLower = lockedLower - unlockedLower
        return websites.count { site ->
            val catLower = site.category.lowercase()
            favoriteIds.contains(site.id) &&
                !parkedLower.contains(catLower) &&
                !sealedLockedLower.contains(catLower)
        }
    }

    fun filterHomeWebsites(
        websites: List<Website>,
        selectedCategory: String,
        searchQuery: String,
        sortOrder: BookmarkSortOrder,
        favoriteIds: Set<Long>,
        parkedCategories: List<String>,
        lockedCategories: List<String>,
        unlockedCategories: Set<String>
    ): List<Website> {
        val sorted = when (sortOrder) {
            BookmarkSortOrder.NEWEST -> websites
            BookmarkSortOrder.OLDEST -> websites.asReversed()
        }
        val parkedLower = parkedCategories.map { it.lowercase() }.toSet()
        val lockedLower = lockedCategories.map { it.lowercase() }.toSet()
        val unlockedLower = unlockedCategories.map { it.lowercase() }.toSet()
        val sealedLockedLower = lockedLower - unlockedLower

        val categoryFiltered = when {
            selectedCategory.equals("All", ignoreCase = true) -> sorted.filter { site ->
                val catLower = site.category.lowercase()
                !lockedLower.contains(catLower) && !parkedLower.contains(catLower)
            }
            selectedCategory.equals("Favorites", ignoreCase = true) -> sorted.filter { site ->
                val catLower = site.category.lowercase()
                favoriteIds.contains(site.id) &&
                    !sealedLockedLower.contains(catLower) &&
                    !parkedLower.contains(catLower)
            }
            else -> sorted.filter { site ->
                val catLower = site.category.lowercase()
                site.category.equals(selectedCategory, ignoreCase = true) &&
                    !sealedLockedLower.contains(catLower) &&
                    !parkedLower.contains(catLower)
            }
        }

        val query = searchQuery.trim()
        if (query.isBlank()) return categoryFiltered
        return categoryFiltered.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true) ||
                it.domain.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }
}
