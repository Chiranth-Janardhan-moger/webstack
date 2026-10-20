package com.example.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrCodeUtilsTest {

    @Test
    fun testQrCodeGeneration_validUrl_returnsValidBitmap() {
        val bitmap = QrCodeUtils.generateQrBitmap("http://192.168.1.50:8080/?pin=998877", 256)
        assertNotNull("Generated QR bitmap should not be null", bitmap)
        assertEquals(256, bitmap!!.width)
        assertEquals(256, bitmap.height)
    }

    @Test
    fun testQrCodeGeneration_blankOrEmpty_returnsNull() {
        assertNull(QrCodeUtils.generateQrBitmap("", 256))
        assertNull(QrCodeUtils.generateQrBitmap("   ", 256))
    }

    @Test
    fun testGenerateAndroidBitmap_validPayload_createsCorrectDimensions() {
        val testPayload = "http://192.168.1.155:8080/?pin=445566"
        val androidBitmap = QrCodeUtils.generateAndroidBitmap(testPayload, 300)
        assertNotNull(androidBitmap)
        assertEquals(300, androidBitmap!!.width)
        assertEquals(300, androidBitmap.height)
    }

    @Test
    fun testGenerateAndroidBitmap_blankPayload_returnsNull() {
        assertNull(QrCodeUtils.generateAndroidBitmap("", 256))
        assertNull(QrCodeUtils.generateAndroidBitmap("  ", 256))
    }
}
