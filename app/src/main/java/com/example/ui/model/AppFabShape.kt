package com.example.ui.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Defensive shape wrapper preventing IllegalArgumentException: "Can't get the direction of a 0-length vector"
 * when rendering complex polygons during 0-size initial layout or animations, falling back cleanly to CircleShape
 * on any unexpected outline creation failures.
 */
class SafeExpressiveShape(private val delegate: Shape) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(Rect.Zero)
        }
        return try {
            delegate.createOutline(size, layoutDirection, density)
        } catch (_: Throwable) {
            CircleShape.createOutline(size, layoutDirection, density)
        }
    }
}

class ScaledSquareShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val minDim = minOf(size.width, size.height)
        if (minDim <= 0f) return Outline.Rectangle(Rect.Zero)
        // At <=42dp preview tiles, 5.5dp radius guarantees prominent straight edges (clearly a square).
        // At 58dp FAB, 15dp radius provides standard Material 3 rounded square FAB curvature.
        val radius = if (minDim <= 42f * density.density) 5.5f * density.density else 15f * density.density
        return Outline.Rounded(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
            )
        )
    }
}

/**
 * FAB shape definitions using official Google Android Material 3 and MaterialShapes Expressive APIs.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
enum class AppFabShape(val id: String, val displayName: String, val isStandard: Boolean = false) {
    // Row 1: Circle, Square, Slanted
    CIRCLE("circle", "Circle", isStandard = true),
    ROUNDED_SQUARE("rounded_square", "Square", isStandard = true),
    SLANTED("slanted", "Slanted", isStandard = false),

    // Row 2: Pill, Arch, Ghost-ish
    PILL("pill", "Pill", isStandard = false),
    ARCH("arch", "Arch", isStandard = false),
    GHOST("ghost", "Ghost-ish", isStandard = false),

    // Row 3: Pentagon, 4-Leaf Clover, 4-Sided Cookie
    PENTAGON("pentagon", "Pentagon", isStandard = false),
    CLOVER_4("clover_4", "4-Leaf Clover", isStandard = false),
    COOKIE_4("cookie_4", "4-Sided Cookie", isStandard = false),

    // Row 4: 6-Sided Cookie, 7-Sided Cookie, 9-Sided Cookie
    COOKIE_6("cookie_6", "6-Sided Cookie", isStandard = false),
    COOKIE_7("cookie_7", "7-Sided Cookie", isStandard = false),
    COOKIE_9("cookie_9", "9-Sided Cookie", isStandard = false);

    @Composable
    fun toComposeShape(): Shape {
        val rawShape = when (this) {
            CIRCLE -> return CircleShape
            ROUNDED_SQUARE -> return scaledSquareShape
            SLANTED -> MaterialShapes.Slanted.toShape()
            PILL -> MaterialShapes.Pill.toShape()
            ARCH -> MaterialShapes.Arch.toShape()
            GHOST -> MaterialShapes.Ghostish.toShape()
            PENTAGON -> MaterialShapes.Pentagon.toShape()
            CLOVER_4 -> MaterialShapes.Clover4Leaf.toShape()
            COOKIE_4 -> MaterialShapes.Cookie4Sided.toShape()
            COOKIE_6 -> MaterialShapes.Cookie6Sided.toShape()
            COOKIE_7 -> MaterialShapes.Cookie7Sided.toShape()
            COOKIE_9 -> MaterialShapes.Cookie9Sided.toShape()
        }
        return remember(rawShape) { SafeExpressiveShape(rawShape) }
    }

    companion object {
        private val scaledSquareShape = ScaledSquareShape()

        fun fromId(id: String?): AppFabShape {
            if (id == null) return CIRCLE
            if (id == "standard_m3" || id == "square" || id == "medium" || id == "small" || id == "extra_large") {
                return ROUNDED_SQUARE
            }
            return entries.firstOrNull { it.id == id } ?: CIRCLE
        }
    }
}
