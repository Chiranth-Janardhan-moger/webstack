package com.example.ui.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class FavoriteIconType(val id: String, val displayName: String) {
    HEART("heart", "Heart"),
    STAR("star", "Star"),
    BOOKMARK("bookmark", "Bookmark"),
    DIAMOND("diamond", "Diamond"),
    SPARKLE("sparkle", "Sparkle");

    fun getFilledIcon(): ImageVector = when (this) {
        HEART -> Icons.Filled.Favorite
        STAR -> Icons.Filled.Star
        BOOKMARK -> Icons.Filled.Bookmark
        DIAMOND -> Icons.Filled.Diamond
        SPARKLE -> Icons.Filled.AutoAwesome
    }

    fun getOutlinedIcon(): ImageVector = when (this) {
        HEART -> Icons.Outlined.FavoriteBorder
        STAR -> Icons.Outlined.StarBorder
        BOOKMARK -> Icons.Outlined.BookmarkBorder
        DIAMOND -> Icons.Outlined.Diamond
        SPARKLE -> Icons.Outlined.AutoAwesome
    }

    companion object {
        fun fromId(id: String?): FavoriteIconType =
            entries.firstOrNull { it.id == id } ?: HEART
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
enum class FavoriteBadgeShape(val id: String, val displayName: String) {
    PURE_FLOAT("pure_float", "Pure Float"),
    CIRCLE("circle", "Circle"),
    SQUIRCLE("squircle", "Squircle"),
    CLOVER("clover", "4-Leaf Clover"),
    FLOWER("flower", "Flower"),
    SOFT_BURST("soft_burst", "Soft Burst");

    @Composable
    fun toComposeShape(): Shape {
        val rawShape = when (this) {
            PURE_FLOAT -> CircleShape
            CIRCLE -> CircleShape
            SQUIRCLE -> RoundedCornerShape(6.dp)
            CLOVER -> MaterialShapes.Clover4Leaf.toShape()
            FLOWER -> MaterialShapes.Cookie6Sided.toShape()
            SOFT_BURST -> MaterialShapes.Cookie9Sided.toShape()
        }
        return remember(rawShape) { SafeExpressiveShape(rawShape) }
    }

    companion object {
        fun fromId(id: String?): FavoriteBadgeShape =
            when (id) {
                "pure_float" -> PURE_FLOAT
                "circle" -> CIRCLE
                "squircle" -> SQUIRCLE
                "clover" -> CLOVER
                "flower", "cookie" -> FLOWER
                "soft_burst", "pentagon" -> SOFT_BURST
                else -> entries.firstOrNull { it.id == id } ?: PURE_FLOAT
            }
    }
}

enum class FavoriteColorPreset(val id: String, val displayName: String, val color: Color) {
    CRIMSON_PINK("crimson_pink", "Crimson Pink", Color(0xFFFF2D55)),
    ROYAL_GOLD("royal_gold", "Royal Gold", Color(0xFFFFB300)),
    ELECTRIC_PURPLE("electric_purple", "Electric Purple", Color(0xFFAF52DE)),
    SKY_BLUE("sky_blue", "Sky Blue", Color(0xFF007AFF)),
    EMERALD_GREEN("emerald_green", "Emerald Green", Color(0xFF34C759)),
    SUNSET_ORANGE("sunset_orange", "Sunset Orange", Color(0xFFFF9500));

    companion object {
        fun fromId(id: String?): FavoriteColorPreset =
            when (id) {
                "crimson", "crimson_pink" -> CRIMSON_PINK
                "royal_gold", "amber" -> ROYAL_GOLD
                "purple", "electric_purple" -> ELECTRIC_PURPLE
                "blue", "sky_blue" -> SKY_BLUE
                "green", "emerald_green" -> EMERALD_GREEN
                "sunset_orange", "orange", "teal" -> SUNSET_ORANGE
                else -> entries.firstOrNull { it.id == id } ?: CRIMSON_PINK
            }
    }
}
