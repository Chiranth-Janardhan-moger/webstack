package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppFabShapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStandardShapes_onlyCircleAndRoundedSquare() {
        val standard = AppFabShape.entries.filter { it.isStandard }
        assertEquals("Standard shapes must have exactly 2 shapes", 2, standard.size)
        assertTrue("Standard shapes must include CIRCLE", standard.contains(AppFabShape.CIRCLE))
        assertTrue("Standard shapes must include ROUNDED_SQUARE", standard.contains(AppFabShape.ROUNDED_SQUARE))
    }

    @Test
    fun testExpressiveShapes_includesGoogleMaterialShapes() {
        val expressive = AppFabShape.entries.filter { !it.isStandard }
        assertEquals("Expressive shapes must have exactly 10 shapes", 10, expressive.size)
        assertTrue("Expressive must include GHOST", expressive.contains(AppFabShape.GHOST))
        assertTrue("Expressive must include PILL", expressive.contains(AppFabShape.PILL))
        assertTrue("Expressive must include ARCH", expressive.contains(AppFabShape.ARCH))
        assertTrue("Expressive must include COOKIE_4", expressive.contains(AppFabShape.COOKIE_4))
        assertTrue("Expressive must include COOKIE_6", expressive.contains(AppFabShape.COOKIE_6))
        assertTrue("Expressive must include COOKIE_7", expressive.contains(AppFabShape.COOKIE_7))
        assertTrue("Expressive must include COOKIE_9", expressive.contains(AppFabShape.COOKIE_9))
        assertTrue("Expressive must include CLOVER_4", expressive.contains(AppFabShape.CLOVER_4))
        assertTrue("Expressive must include PENTAGON", expressive.contains(AppFabShape.PENTAGON))
        assertTrue("Expressive must include SLANTED", expressive.contains(AppFabShape.SLANTED))
    }

    @Test
    fun testShapesOrder_exactSequence() {
        val expected = listOf(
            AppFabShape.CIRCLE,
            AppFabShape.ROUNDED_SQUARE,
            AppFabShape.SLANTED,
            AppFabShape.PILL,
            AppFabShape.ARCH,
            AppFabShape.GHOST,
            AppFabShape.PENTAGON,
            AppFabShape.CLOVER_4,
            AppFabShape.COOKIE_4,
            AppFabShape.COOKIE_6,
            AppFabShape.COOKIE_7,
            AppFabShape.COOKIE_9
        )
        assertEquals("Shapes must match the exact 12-shape sequence", expected, AppFabShape.entries)
        assertEquals("Square display name must be Square", "Square", AppFabShape.ROUNDED_SQUARE.displayName)
    }

    @Test
    fun testRemovedShapes_areNotPresent() {
        val allIds = AppFabShape.entries.map { it.id }.toSet()
        assertEquals("Total shapes count must be 12", 12, allIds.size)
        assertFalse("gem must be removed", allIds.contains("gem"))
        assertFalse("heart must be removed", allIds.contains("heart"))
        assertFalse("fan must be removed", allIds.contains("fan"))
        assertFalse("diamond must be removed", allIds.contains("diamond"))
        assertFalse("boom must be removed", allIds.contains("boom"))
        assertFalse("oval must be removed", allIds.contains("oval"))
        assertFalse("semi_circle must be removed", allIds.contains("semi_circle"))
        assertFalse("triangle must be removed", allIds.contains("triangle"))
        assertFalse("arrow must be removed", allIds.contains("arrow"))
        assertFalse("sunny must be removed", allIds.contains("sunny"))
        assertFalse("very_sunny must be removed", allIds.contains("very_sunny"))
        assertFalse("burst must be removed", allIds.contains("burst"))
        assertFalse("soft_burst must be removed", allIds.contains("soft_burst"))
        assertFalse("clover_8 must be removed", allIds.contains("clover_8"))
        assertFalse("cookie_12 must be removed", allIds.contains("cookie_12"))
        assertFalse("flower must be removed", allIds.contains("flower"))
        assertFalse("puffy must be removed", allIds.contains("puffy"))
        assertFalse("puffy_diamond must be removed", allIds.contains("puffy_diamond"))
    }

    @Test
    fun testAllShapes_createValidOutlineWithoutExceptions() {
        val density = Density(density = 2.75f, fontScale = 1f)
        val size = Size(120f, 120f)
        val layoutDirection = LayoutDirection.Ltr

        composeTestRule.setContent {
            for (shape in AppFabShape.entries) {
                val composeShape = shape.toComposeShape()
                val outline = composeShape.createOutline(size, layoutDirection, density)
                assertNotNull("Outline for shape ${shape.displayName} should not be null", outline)
                
                // Test zero size during animations/initial layout
                val zeroOutline = composeShape.createOutline(Size.Zero, layoutDirection, density)
                assertNotNull("Zero outline for ${shape.displayName} should not be null", zeroOutline)
            }
        }
    }

    @Test
    fun testShape_serializationAndFallback() {
        for (shape in AppFabShape.entries) {
            val serialized = shape.name
            val deserialized = AppFabShape.valueOf(serialized)
            assertEquals(shape, deserialized)
        }

        // Backward compatibility fallbacks
        assertEquals(AppFabShape.ROUNDED_SQUARE, AppFabShape.fromId("standard_m3"))
        assertEquals(AppFabShape.ROUNDED_SQUARE, AppFabShape.fromId("square"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("gem"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("heart"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("triangle"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("non_existent_shape"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("sunny"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("very_sunny"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("burst"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("soft_burst"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("clover_8"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("cookie_12"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("flower"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("puffy"))
        assertEquals(AppFabShape.CIRCLE, AppFabShape.fromId("puffy_diamond"))
    }

    @Test
    fun testAllShapes_renderInFloatingActionButton() {
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                for (shape in AppFabShape.entries) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {},
                        shape = shape.toComposeShape(),
                        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
