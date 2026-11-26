package com.example.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewUrlResolverTest {

    @Test
    fun `extractYouTubeVideoId handles watch, short, shorts, mobile, and embed URLs`() {
        assertEquals("dQw4w9WgXcQ", PreviewUrlResolver.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", PreviewUrlResolver.extractYouTubeVideoId("https://m.youtube.com/watch?v=dQw4w9WgXcQ&t=42s"))
        assertEquals("dQw4w9WgXcQ", PreviewUrlResolver.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ?si=abc"))
        assertEquals("dQw4w9WgXcQ", PreviewUrlResolver.extractYouTubeVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", PreviewUrlResolver.extractYouTubeVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ"))
        assertNull(PreviewUrlResolver.extractYouTubeVideoId("https://www.youtube.com/@channel"))
        assertNull(PreviewUrlResolver.extractYouTubeVideoId("https://vimeo.com/123456"))
    }

    @Test
    fun `fastThumbnailChain returns direct YouTube thumbnails before screenshot fallbacks`() {
        val chain = PreviewUrlResolver.fastThumbnailChain("https://youtu.be/dQw4w9WgXcQ")
        assertNotNull(chain)
        assertEquals("https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg", chain!![0])
        assertEquals("https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg", chain[1])
        assertTrue(chain[2].contains("screenshot=true"))
        assertTrue(chain[3].contains("image.thum.io"))
    }

    @Test
    fun `extractDailymotionVideoId handles full and short URLs`() {
        assertEquals("x8abcde", PreviewUrlResolver.extractDailymotionVideoId("https://www.dailymotion.com/video/x8abcde_some-title"))
        assertEquals("x8abcde", PreviewUrlResolver.extractDailymotionVideoId("https://dai.ly/x8abcde"))
        val chain = PreviewUrlResolver.fastThumbnailChain("https://dai.ly/x8abcde")
        assertEquals("https://www.dailymotion.com/thumbnail/video/x8abcde", chain?.firstOrNull())
    }

    @Test
    fun `buildOEmbedEndpoint recognizes TikTok, Vimeo, SoundCloud, and Spotify`() {
        assertTrue(PreviewUrlResolver.buildOEmbedEndpoint("https://www.tiktok.com/@user/video/123")!!.startsWith("https://www.tiktok.com/oembed?url="))
        assertTrue(PreviewUrlResolver.buildOEmbedEndpoint("https://vimeo.com/76979871")!!.startsWith("https://vimeo.com/api/oembed.json?url="))
        assertTrue(PreviewUrlResolver.buildOEmbedEndpoint("https://soundcloud.com/artist/track")!!.startsWith("https://soundcloud.com/oembed?format=json&url="))
        assertTrue(PreviewUrlResolver.buildOEmbedEndpoint("https://open.spotify.com/track/123")!!.startsWith("https://open.spotify.com/oembed?url="))
        assertNull(PreviewUrlResolver.buildOEmbedEndpoint("https://example.com/article"))
    }

    @Test
    fun `extractMetadataImageFromHtml prefers og image and resolves relative URLs`() {
        val html = """
            <html><head>
                <meta property="og:image" content="/assets/thumb.jpg" />
                <meta name="twitter:image" content="https://cdn.example.com/tw.jpg" />
            </head></html>
        """.trimIndent()
        assertEquals(
            "https://example.com/assets/thumb.jpg",
            PreviewUrlResolver.extractMetadataImageFromHtml(html, "https://example.com/post/1")
        )
    }

    @Test
    fun `extractMetadataImageFromHtml falls back to twitter image when og image missing`() {
        val html = """
            <html><head>
                <meta name="twitter:image" content="https://cdn.example.com/tw.jpg" />
            </head></html>
        """.trimIndent()
        assertEquals(
            "https://cdn.example.com/tw.jpg",
            PreviewUrlResolver.extractMetadataImageFromHtml(html, "https://example.com/post/1")
        )
    }
}
