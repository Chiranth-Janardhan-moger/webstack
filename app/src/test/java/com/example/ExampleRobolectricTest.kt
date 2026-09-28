package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.robolectric.annotation.GraphicsMode

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
}
