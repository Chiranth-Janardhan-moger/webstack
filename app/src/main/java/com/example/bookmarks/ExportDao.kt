package com.example.bookmarks

import com.example.data.Website

interface ExportDao {
    suspend fun categoriesSorted(): List<String>
    suspend fun byCategory(category: String): List<Website>
}
