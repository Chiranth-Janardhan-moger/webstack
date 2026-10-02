package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Standard System Colors (Light & Dark)
// ==========================================

// System Backgrounds (Light)
val AppSystemBackgroundLight = Color(0xFFFFFFFF)
val AppSecondaryBackgroundLight = Color(0xFFF6F7FA)
val AppTertiaryBackgroundLight = Color(0xFFFFFFFF)
val AppGroupedBackgroundLight = Color(0xFFF6F7FA)
val AppSecondaryGroupedBackgroundLight = Color(0xFFFFFFFF)

// System Backgrounds (Dark)
val AppSystemBackgroundDark = Color(0xFF000000)
val AppSecondaryBackgroundDark = Color(0xFF1C1C1E)
val AppTertiaryBackgroundDark = Color(0xFF2C2C2E)
val AppGroupedBackgroundDark = Color(0xFF000000)
val AppSecondaryGroupedBackgroundDark = Color(0xFF1C1C1E)

// System Labels & Foreground (Light)
val AppLabelLight = Color(0xFF000000)
val AppSecondaryLabelLight = Color(0x99000000) // ~60%
val AppTertiaryLabelLight = Color(0x4D000000)  // ~30%
val AppQuaternaryLabelLight = Color(0x29000000) // ~16%

// System Labels & Foreground (Dark)
val AppLabelDark = Color(0xFFFFFFFF)
val AppSecondaryLabelDark = Color(0x99FFFFFF) // ~60%
val AppTertiaryLabelDark = Color(0x4DFFFFFF)  // ~30%
val AppQuaternaryLabelDark = Color(0x29FFFFFF) // ~16%

// System Separators & Hairlines
val AppSeparatorLight = Color(0x14000000) // ~8% softer hairline
val AppSeparatorDark = Color(0x38FFFFFF)   // ~22%

// System Fills / Capsule Backgrounds
val AppFillLight = Color(0x1F787880) // 12%
val AppSecondaryFillLight = Color(0x14787880) // 8%
val AppTertiaryFillLight = Color(0x0D787880) // 5%

val AppFillDark = Color(0x33787880) // 20%
val AppSecondaryFillDark = Color(0x24787880) // 14%
val AppTertiaryFillDark = Color(0x18787880) // 9%

// Accent Colors
val AppBlue = Color(0xFF007AFF)
val AppBlueDark = Color(0xFF0A84FF)
val AppIndigo = Color(0xFF5856D6)
val AppPurple = Color(0xFFAF52DE)
val AppPink = Color(0xFFFF2D55)
val AppRed = Color(0xFFFF3B30)
val AppRedDark = Color(0xFFFF453A)
val AppOrange = Color(0xFFFF9500)
val AppYellow = Color(0xFFFFCC00)
val AppGreen = Color(0xFF34C759)
val AppGreenDark = Color(0xFF30D158)
val AppTeal = Color(0xFF30B0C7)
val AppCyan = Color(0xFF32ADE6)
val AppGray = Color(0xFF8E8E93)

// Liquid Glass Specular Effects
val GlassHighlightLight = Color(0x66FFFFFF)
val GlassHighlightDark = Color(0x1AFFFFFF)

/**
 * Extended semantic color palette provided through CompositionLocal
 */
@Immutable
data class AppColors(
    val systemBackground: Color,
    val secondaryBackground: Color,
    val tertiaryBackground: Color,
    val groupedBackground: Color,
    val secondaryGroupedBackground: Color,
    val surface: Color = secondaryGroupedBackground,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val quaternaryLabel: Color,
    val separator: Color,
    val fill: Color,
    val secondaryFill: Color,
    val tertiaryFill: Color,
    val accent: Color,
    val destructive: Color,
    val success: Color,
    val glassHighlight: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        systemBackground = AppSystemBackgroundLight,
        secondaryBackground = AppSecondaryBackgroundLight,
        tertiaryBackground = AppTertiaryBackgroundLight,
        groupedBackground = AppGroupedBackgroundLight,
        secondaryGroupedBackground = AppSecondaryGroupedBackgroundLight,
        surface = AppSecondaryGroupedBackgroundLight,
        label = AppLabelLight,
        secondaryLabel = AppSecondaryLabelLight,
        tertiaryLabel = AppTertiaryLabelLight,
        quaternaryLabel = AppQuaternaryLabelLight,
        separator = AppSeparatorLight,
        fill = AppFillLight,
        secondaryFill = AppSecondaryFillLight,
        tertiaryFill = AppTertiaryFillLight,
        accent = AppBlue,
        destructive = AppRed,
        success = AppGreen,
        glassHighlight = GlassHighlightLight,
        isDark = false
    )
}
