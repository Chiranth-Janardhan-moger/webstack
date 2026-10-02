package com.example.ui.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

val DEFAULT_CATEGORIES = listOf("Personal", "Design", "Tools", "Work", "Reading")

fun getCategoryAccentColor(category: String, isDark: Boolean): Color {
    val clean = category.trim().lowercase()
    if (clean.isEmpty() || clean == "all" || clean == "uncategorized") {
        return if (isDark) AppGray else AppGray
    }

    // Explicit curated mappings for common standard categories
    when (clean) {
        "personal" -> return AppPurple
        "design" -> return AppIndigo
        "tools", "dev", "development", "coding", "code" -> return if (isDark) AppBlueDark else AppBlue
        "reading", "books", "news", "articles", "article" -> return AppOrange
        "work", "business", "office" -> return AppTeal
        "social", "chat", "media" -> return AppPink
        "finance", "money", "crypto" -> return if (isDark) AppGreenDark else AppGreen
        "entertainment", "video", "youtube", "music" -> return if (isDark) AppRedDark else AppRed
        "learning", "education", "study" -> return AppYellow
        "travel", "places" -> return AppCyan
    }

    val palette = if (isDark) DYNAMIC_PALETTE_DARK else DYNAMIC_PALETTE_LIGHT
    val hash = kotlin.math.abs(clean.hashCode())
    return palette[hash % palette.size]
}

private val DYNAMIC_PALETTE_LIGHT = listOf(
    AppIndigo,
    AppBlue,
    AppTeal,
    AppCyan,
    AppGreen,
    AppOrange,
    AppPink,
    AppPurple,
    AppRed,
    AppYellow
)

private val DYNAMIC_PALETTE_DARK = listOf(
    AppIndigo,
    AppBlueDark,
    AppTeal,
    AppCyan,
    AppGreenDark,
    AppOrange,
    AppPink,
    AppPurple,
    AppRedDark,
    AppYellow
)
