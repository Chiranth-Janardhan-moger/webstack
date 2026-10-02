package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Color Scheme for Material 3
private val AppLightColorScheme = lightColorScheme(
    primary = AppLabelLight,
    onPrimary = AppSystemBackgroundLight,
    secondary = AppSecondaryLabelLight,
    onSecondary = AppSystemBackgroundLight,
    background = AppGroupedBackgroundLight,
    onBackground = AppLabelLight,
    surface = AppSecondaryGroupedBackgroundLight,
    onSurface = AppLabelLight,
    surfaceVariant = AppSecondaryBackgroundLight,
    onSurfaceVariant = AppSecondaryLabelLight,
    outline = AppSeparatorLight,
    error = AppRed,
    onError = Color.White
)

// Dark Color Scheme for Material 3
private val AppDarkColorScheme = darkColorScheme(
    primary = AppLabelDark,
    onPrimary = AppSystemBackgroundDark,
    secondary = AppSecondaryLabelDark,
    onSecondary = AppSystemBackgroundDark,
    background = AppGroupedBackgroundDark,
    onBackground = AppLabelDark,
    surface = AppSecondaryGroupedBackgroundDark,
    onSurface = AppLabelDark,
    surfaceVariant = AppSecondaryBackgroundDark,
    onSurfaceVariant = AppSecondaryLabelDark,
    outline = AppSeparatorDark,
    error = AppRedDark,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
    val appColors = if (darkTheme) {
        AppColors(
            systemBackground = AppSystemBackgroundDark,
            secondaryBackground = AppSecondaryBackgroundDark,
            tertiaryBackground = AppTertiaryBackgroundDark,
            groupedBackground = AppGroupedBackgroundDark,
            secondaryGroupedBackground = AppSecondaryGroupedBackgroundDark,
            label = AppLabelDark,
            secondaryLabel = AppSecondaryLabelDark,
            tertiaryLabel = AppTertiaryLabelDark,
            quaternaryLabel = AppQuaternaryLabelDark,
            separator = AppSeparatorDark,
            fill = AppFillDark,
            secondaryFill = AppSecondaryFillDark,
            tertiaryFill = AppTertiaryFillDark,
            accent = AppBlueDark,
            destructive = AppRedDark,
            success = AppGreenDark,
            glassHighlight = GlassHighlightDark,
            isDark = true
        )
    } else {
        AppColors(
            systemBackground = AppSystemBackgroundLight,
            secondaryBackground = AppSecondaryBackgroundLight,
            tertiaryBackground = AppTertiaryBackgroundLight,
            groupedBackground = AppGroupedBackgroundLight,
            secondaryGroupedBackground = AppSecondaryGroupedBackgroundLight,
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
                val statusBarColor = (if (darkTheme) AppSystemBackgroundDark else AppSecondaryBackgroundLight).toArgb()
                val navBarColor = (if (darkTheme) AppSystemBackgroundDark else AppSecondaryBackgroundLight).toArgb()
                window.statusBarColor = statusBarColor
                window.navigationBarColor = navBarColor
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
