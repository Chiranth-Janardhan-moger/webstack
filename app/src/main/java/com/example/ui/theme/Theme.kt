package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Apple Light Color Scheme for Material 3
private val AppleLightColorScheme = lightColorScheme(
    primary = AppleLabelLight,
    onPrimary = AppleSystemBackgroundLight,
    secondary = AppleSecondaryLabelLight,
    onSecondary = AppleSystemBackgroundLight,
    background = AppleGroupedBackgroundLight,
    onBackground = AppleLabelLight,
    surface = AppleSecondaryGroupedBackgroundLight,
    onSurface = AppleLabelLight,
    surfaceVariant = AppleSecondaryBackgroundLight,
    onSurfaceVariant = AppleSecondaryLabelLight,
    outline = AppleSeparatorLight,
    error = AppleRed,
    onError = Color.White
)

// Apple Dark Color Scheme for Material 3
private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleLabelDark,
    onPrimary = AppleSystemBackgroundDark,
    secondary = AppleSecondaryLabelDark,
    onSecondary = AppleSystemBackgroundDark,
    background = AppleGroupedBackgroundDark,
    onBackground = AppleLabelDark,
    surface = AppleSecondaryGroupedBackgroundDark,
    onSurface = AppleLabelDark,
    surfaceVariant = AppleSecondaryBackgroundDark,
    onSurfaceVariant = AppleSecondaryLabelDark,
    outline = AppleSeparatorDark,
    error = AppleRedDark,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AppleDarkColorScheme else AppleLightColorScheme
    val appleColors = if (darkTheme) {
        AppleColors(
            systemBackground = AppleSystemBackgroundDark,
            secondaryBackground = AppleSecondaryBackgroundDark,
            tertiaryBackground = AppleTertiaryBackgroundDark,
            groupedBackground = AppleGroupedBackgroundDark,
            secondaryGroupedBackground = AppleSecondaryGroupedBackgroundDark,
            label = AppleLabelDark,
            secondaryLabel = AppleSecondaryLabelDark,
            tertiaryLabel = AppleTertiaryLabelDark,
            quaternaryLabel = AppleQuaternaryLabelDark,
            separator = AppleSeparatorDark,
            fill = AppleFillDark,
            secondaryFill = AppleSecondaryFillDark,
            tertiaryFill = AppleTertiaryFillDark,
            accent = AppleBlueDark,
            destructive = AppleRedDark,
            success = AppleGreenDark,
            glassSurface = GlassSurfaceDark,
            glassBorder = GlassBorderDark,
            glassHighlight = GlassHighlightDark,
            isDark = true
        )
    } else {
        AppleColors(
            systemBackground = AppleSystemBackgroundLight,
            secondaryBackground = AppleSecondaryBackgroundLight,
            tertiaryBackground = AppleTertiaryBackgroundLight,
            groupedBackground = AppleGroupedBackgroundLight,
            secondaryGroupedBackground = AppleSecondaryGroupedBackgroundLight,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var currentContext = view.context
            while (currentContext is android.content.ContextWrapper && currentContext !is Activity) {
                currentContext = currentContext.baseContext
            }
            val activity = currentContext as? Activity
            if (activity != null) {
                val window = activity.window
                val statusBarColor = (if (darkTheme) AppleSystemBackgroundDark else AppleSecondaryBackgroundLight).toArgb()
                val navBarColor = (if (darkTheme) AppleSystemBackgroundDark else AppleSecondaryBackgroundLight).toArgb()
                window.statusBarColor = statusBarColor
                window.navigationBarColor = navBarColor
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppleColors provides appleColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


