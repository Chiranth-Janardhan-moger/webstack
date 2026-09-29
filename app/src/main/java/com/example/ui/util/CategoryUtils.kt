package com.example.ui.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

val DEFAULT_CATEGORIES = listOf("Personal", "Design", "Tools", "Work", "Reading")

fun getCategoryAccentColor(category: String, isDark: Boolean): Color {
    val clean = category.trim().lowercase()
    if (clean.isEmpty() || clean == "all" || clean == "uncategorized") {
        return if (isDark) AppleGray else AppleGray
    }

    // Explicit curated mappings for common standard categories
    when (clean) {
        "personal" -> return ApplePurple
        "design" -> return AppleIndigo
        "tools", "dev", "development", "coding", "code" -> return if (isDark) AppleBlueDark else AppleBlue
        "reading", "books", "news", "articles", "article" -> return AppleOrange
        "work", "business", "office" -> return AppleTeal
        "social", "chat", "media" -> return ApplePink
        "finance", "money", "crypto" -> return if (isDark) AppleGreenDark else AppleGreen
        "entertainment", "video", "youtube", "music" -> return if (isDark) AppleRedDark else AppleRed
        "learning", "education", "study" -> return AppleYellow
        "travel", "places" -> return AppleCyan
    }

    // Deterministic vibrant Apple system color palette hashing for ANY custom tag
    val dynamicPalette = listOf(
        AppleIndigo,
        if (isDark) AppleBlueDark else AppleBlue,
        AppleTeal,
        AppleCyan,
        if (isDark) AppleGreenDark else AppleGreen,
        AppleOrange,
        ApplePink,
        ApplePurple,
        if (isDark) AppleRedDark else AppleRed,
        AppleYellow
    )

    val hash = kotlin.math.abs(clean.hashCode())
    return dynamicPalette[hash % dynamicPalette.size]
}
