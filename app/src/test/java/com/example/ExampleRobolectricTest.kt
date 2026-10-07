package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType
import com.example.engine.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TubeForge", appName)
    }

    @Test
    fun `test youtube video id extraction`() {
        val standardUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(standardUrl))

        val shortUrl = "https://youtu.be/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(shortUrl))

        val directId = "dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(directId))

        val shortsUrl = "https://www.youtube.com/shorts/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(shortsUrl))
    }

    @Test
    fun `test batch url parsing`() {
        val batchInput = """
            https://www.youtube.com/watch?v=11111111111
            https://www.youtube.com/watch?v=22222222222, https://youtu.be/33333333333
        """.trimIndent()

        val parsed = YouTubeUrlParser.parseBatchUrls(batchInput)
        assertEquals(3, parsed.size)
    }

    @Test
    fun `test media format classification`() {
        assertEquals(MediaType.VIDEO, MediaFormat.MP4.mediaType)
        assertEquals(MediaType.VIDEO, MediaFormat.WEBM.mediaType)
        assertEquals(MediaType.AUDIO, MediaFormat.MP3.mediaType)
        assertEquals(MediaType.AUDIO, MediaFormat.FLAC.mediaType)
        assertEquals(MediaType.AUDIO, MediaFormat.WAV.mediaType)
    }
}
