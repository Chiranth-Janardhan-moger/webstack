package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.robolectric.annotation.GraphicsMode
import com.example.ui.util.extractUrlFromText
import com.example.ui.util.getClipboardUrl
import com.example.ui.util.formatMiddleTruncatedDomain

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WebStack", appName)
  }

  @Test
  fun `measure badge centering`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val density = 3.0f // 1080p phone density
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      textSize = 10f * density
      typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
      color = android.graphics.Color.WHITE
      textAlign = android.graphics.Paint.Align.CENTER
    }
    val bounds = android.graphics.Rect()
    paint.getTextBounds("2", 0, 1, bounds)
    
    val circleSize = (18f * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(circleSize, circleSize, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val circlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.DKGRAY
    }
    canvas.drawCircle(circleSize / 2f, circleSize / 2f, circleSize / 2f, circlePaint)
    
    // Mathematical dead-center baseline formula
    val baselineY = circleSize / 2f - (bounds.top + bounds.bottom) / 2f
    canvas.drawText("2", circleSize / 2f, baselineY, paint)
    
    var minTextY = circleSize
    var maxTextY = 0
    for (y in 0 until circleSize) {
      for (x in 0 until circleSize) {
        val pixel = bitmap.getPixel(x, y)
        if (android.graphics.Color.red(pixel) > 200 && android.graphics.Color.green(pixel) > 200 && android.graphics.Color.blue(pixel) > 200) {
          if (y < minTextY) minTextY = y
          if (y > maxTextY) maxTextY = y
        }
      }
    }
    
    val topGap = minTextY
    val bottomGap = circleSize - 1 - maxTextY
    assertEquals("Top and bottom gap must be equal for dead-center alignment", topGap, bottomGap)
  }

  @Test
  fun `extractUrlFromText extracts valid URLs from diverse text formats`() {
    assertEquals("https://linear.app", extractUrlFromText("https://linear.app"))
    assertEquals("https://linear.app", extractUrlFromText("  https://linear.app \n"))
    assertEquals("https://github.com/foo/bar", extractUrlFromText("Check out https://github.com/foo/bar!"))
    assertEquals("www.google.com", extractUrlFromText("www.google.com"))
    assertEquals("linear.app", extractUrlFromText("linear.app"))
    assertEquals("https://example.com/test", extractUrlFromText("https://example.com/test."))
    org.junit.Assert.assertNull(extractUrlFromText("hello world"))
    org.junit.Assert.assertNull(extractUrlFromText("1.0.1"))
    org.junit.Assert.assertNull(extractUrlFromText(""))
  }

  @Test
  fun `getClipboardUrl extracts URL from plain text and raw URI clips`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    // Test plain text clip
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", "https://news.ycombinator.com"))
    assertEquals("https://news.ycombinator.com", getClipboardUrl(context))

    // Test raw URI clip
    clipboard.setPrimaryClip(android.content.ClipData.newRawUri("URI", android.net.Uri.parse("https://github.com")))
    assertEquals("https://github.com", getClipboardUrl(context))

    // Test non-URL text clip
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("text", "just some notes"))
    org.junit.Assert.assertNull(getClipboardUrl(context))
  }

  @Test
  fun `formatMiddleTruncatedDomain shortens long domains preserving prefix and suffix`() {
    assertEquals("github.com", formatMiddleTruncatedDomain("github.com", 24))
    assertEquals("myplacement...vercel.app", formatMiddleTruncatedDomain("myplacementjourneyatbmsitcjm.vercel.app", 24))
    assertEquals("linear.app", formatMiddleTruncatedDomain("linear.app", 24))
    assertEquals("ab", formatMiddleTruncatedDomain("abc", 2))
    // Test with default 32 maxLength
    assertEquals("myplacementjour...cjm.vercel.app", formatMiddleTruncatedDomain("myplacementjourneyatbmsitcjm.vercel.app"))
    assertEquals(32, formatMiddleTruncatedDomain("myplacementjourneyatbmsitcjm.vercel.app").length)
  }
}
