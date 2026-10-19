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
    fun testQrCodeRoundTrip_encodeAndDecode() {
        val testPayload = "http://192.168.1.155:8080/?pin=445566"
        val androidBitmap = QrCodeUtils.generateAndroidBitmap(testPayload, 256)
        assertNotNull(androidBitmap)
        val decoded = QrCodeUtils.decodeQrBitmap(androidBitmap!!)
        assertEquals(testPayload, decoded)
    }

    @Test
    fun testParseQrUrlAndPin_validUrlWithPin() {
        val parsed = QrCodeUtils.parseQrUrlAndPin("http://192.168.1.200:8080/?pin=654321")
        assertNotNull(parsed)
        assertEquals("http://192.168.1.200:8080", parsed!!.first)
        assertEquals("654321", parsed.second)
    }

    @Test
    fun testParseQrUrlAndPin_validUrlWithoutPin() {
        val parsed = QrCodeUtils.parseQrUrlAndPin("http://10.0.0.5:8081/index.html")
        assertNotNull(parsed)
        assertEquals("http://10.0.0.5:8081", parsed!!.first)
        assertEquals("", parsed.second)
    }

    @Test
    fun testParseQrUrlAndPin_invalidString_returnsNull() {
        assertNull(QrCodeUtils.parseQrUrlAndPin("not a url at all"))
        assertNull(QrCodeUtils.parseQrUrlAndPin(""))
        assertNull(QrCodeUtils.parseQrUrlAndPin("ftp://ftp.example.com"))
    }

    @Test
    fun testDecodeQrBitmap_solidColor_returnsNull() {
        val blankBitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val decoded = QrCodeUtils.decodeQrBitmap(blankBitmap)
        assertNull(decoded)
    }
}
