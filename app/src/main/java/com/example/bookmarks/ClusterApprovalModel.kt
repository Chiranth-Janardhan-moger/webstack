package com.example.bookmarks

data class BookmarkCluster(
    val id: String,
    val displayName: String,
    val linkCount: Int,
    val source: CategorySource,
    val sampleDomains: List<String>,
    var assignedCategory: String,
    var isIncluded: Boolean = true
)
