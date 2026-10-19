package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeUtils {
    fun generateAndroidBitmap(content: String, size: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun generateQrBitmap(content: String, size: Int = 512): ImageBitmap? {
        return generateAndroidBitmap(content, size)?.asImageBitmap()
    }

    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val hints = java.util.EnumMap<com.google.zxing.DecodeHintType, Any>(com.google.zxing.DecodeHintType::class.java).apply {
                put(com.google.zxing.DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
            }
            try {
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                QRCodeReader().decode(binaryBitmap, hints).text
            } catch (_: Exception) {
                val binaryBitmap = BinaryBitmap(com.google.zxing.common.GlobalHistogramBinarizer(source))
                QRCodeReader().decode(binaryBitmap, hints).text
            }
        } catch (_: Exception) {
            null
        }
    }

    fun parseQrUrlAndPin(rawText: String): Pair<String, String>? {
        val text = rawText.trim()
        if (!text.startsWith("http://") && !text.startsWith("https://")) return null
        return try {
            val uri = android.net.Uri.parse(text)
            val pin = uri.getQueryParameter("pin") ?: ""
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: return null
            val port = if (uri.port != -1) ":${uri.port}" else ""
            val hostUrl = "$scheme://$host$port"
            Pair(hostUrl, pin)
        } catch (_: Exception) {
            null
        }
    }
}
