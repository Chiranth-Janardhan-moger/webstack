package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Apple Standard System Colors (Light & Dark)
// ==========================================

// System Backgrounds (Light)
val AppleSystemBackgroundLight = Color(0xFFFFFFFF)
val AppleSecondaryBackgroundLight = Color(0xFFF2F2F7)
val AppleTertiaryBackgroundLight = Color(0xFFFFFFFF)
val AppleGroupedBackgroundLight = Color(0xFFF2F2F7)
val AppleSecondaryGroupedBackgroundLight = Color(0xFFFFFFFF)

// System Backgrounds (Dark)
val AppleSystemBackgroundDark = Color(0xFF000000)
val AppleSecondaryBackgroundDark = Color(0xFF1C1C1E)
val AppleTertiaryBackgroundDark = Color(0xFF2C2C2E)
val AppleGroupedBackgroundDark = Color(0xFF000000)
val AppleSecondaryGroupedBackgroundDark = Color(0xFF1C1C1E)

// System Labels & Foreground (Light)
val AppleLabelLight = Color(0xFF000000)
val AppleSecondaryLabelLight = Color(0x99000000) // ~60%
val AppleTertiaryLabelLight = Color(0x4D000000)  // ~30%
val AppleQuaternaryLabelLight = Color(0x29000000) // ~16%

// System Labels & Foreground (Dark)
val AppleLabelDark = Color(0xFFFFFFFF)
val AppleSecondaryLabelDark = Color(0x99FFFFFF) // ~60%
val AppleTertiaryLabelDark = Color(0x4DFFFFFF)  // ~30%
val AppleQuaternaryLabelDark = Color(0x29FFFFFF) // ~16%

// System Separators & Hairlines
val AppleSeparatorLight = Color(0x1F000000) // ~12%
val AppleOpaqueSeparatorLight = Color(0xFFC6C6C8)
val AppleSeparatorDark = Color(0x38FFFFFF)   // ~22%
val AppleOpaqueSeparatorDark = Color(0xFF38383A)

// System Fills / Capsule Backgrounds
val AppleFillLight = Color(0x1F787880) // 12%
val AppleSecondaryFillLight = Color(0x14787880) // 8%
val AppleTertiaryFillLight = Color(0x0D787880) // 5%

val AppleFillDark = Color(0x33787880) // 20%
val AppleSecondaryFillDark = Color(0x24787880) // 14%
val AppleTertiaryFillDark = Color(0x18787880) // 9%

// Apple Accent Colors
val AppleBlue = Color(0xFF007AFF)
val AppleBlueDark = Color(0xFF0A84FF)
val AppleIndigo = Color(0xFF5856D6)
val ApplePurple = Color(0xFFAF52DE)
val ApplePink = Color(0xFFFF2D55)
val AppleRed = Color(0xFFFF3B30)
val AppleRedDark = Color(0xFFFF453A)
val AppleOrange = Color(0xFFFF9500)
val AppleYellow = Color(0xFFFFCC00)
val AppleGreen = Color(0xFF34C759)
val AppleGreenDark = Color(0xFF30D158)
val AppleTeal = Color(0xFF30B0C7)
val AppleCyan = Color(0xFF32ADE6)
val AppleGray = Color(0xFF8E8E93)
val AppleGray2 = Color(0xFFAEAEC2)
val AppleGray3 = Color(0xFFC7C7CC)
val AppleGray4 = Color(0xFFD1D1D6)
val AppleGray5 = Color(0xFFE5E5EA)
val AppleGray6 = Color(0xFFF2F2F7)

// Liquid Glass & Specular Effects
val GlassSurfaceLight = Color(0xCCFFFFFF)
val GlassSurfaceDark = Color(0xD91C1C1E)
val GlassBorderLight = Color(0x1F000000)
val GlassBorderDark = Color(0x33FFFFFF)
val GlassHighlightLight = Color(0x66FFFFFF)
val GlassHighlightDark = Color(0x1AFFFFFF)

// Backward compatibility tokens
val PureWhite = AppleSystemBackgroundLight
val PitchBlack = AppleSystemBackgroundDark
val Slate900 = Color(0xFF0F172A)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val SoftShadow = Color(0x0A000000)

/**
 * Extended semantic Apple color palette provided through CompositionLocal
 */
@Immutable
data class AppleColors(
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
    val glassSurface: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val isDark: Boolean
)

val LocalAppleColors = staticCompositionLocalOf {
    AppleColors(
        systemBackground = AppleSystemBackgroundLight,
        secondaryBackground = AppleSecondaryBackgroundLight,
        tertiaryBackground = AppleTertiaryBackgroundLight,
        groupedBackground = AppleGroupedBackgroundLight,
        secondaryGroupedBackground = AppleSecondaryGroupedBackgroundLight,
        surface = AppleSecondaryGroupedBackgroundLight,
        label = AppleLabelLight,
        secondaryLabel = AppleSecondaryLabelLight,
        tertiaryLabel = AppleTertiaryLabelLight,
        quaternaryLabel = AppleQuaternaryLabelLight,
        separator = AppleSeparatorLight,
        fill = AppleFillLight,
        secondaryFill = AppleSecondaryFillLight,
        tertiaryFill = AppleTertiaryFillLight,
        accent = AppleBlue,
        destructive = AppleRed,
        success = AppleGreen,
        glassSurface = GlassSurfaceLight,
        glassBorder = GlassBorderLight,
        glassHighlight = GlassHighlightLight,
        isDark = false
    )
}


