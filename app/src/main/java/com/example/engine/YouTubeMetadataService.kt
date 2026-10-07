package com.example.engine

import android.util.Log
import com.example.data.model.YouTubeVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class YouTubeMetadataService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun resolveVideoInfo(urlOrId: String): YouTubeVideoInfo = withContext(Dispatchers.IO) {
        val videoId = YouTubeUrlParser.extractVideoId(urlOrId) ?: "sample_${System.currentTimeMillis() % 100000}"
        val canonicalUrl = YouTubeUrlParser.getCanonicalUrl(videoId)

        // Check if matching sample preset first
        val preset = YouTubeUrlParser.SAMPLE_PRESETS.firstOrNull { 
            it.url.contains(videoId) || it.title.contains(videoId, ignoreCase = true) 
        }
        if (preset != null) {
            return@withContext YouTubeVideoInfo(
                videoId = videoId,
                originalUrl = preset.url,
                title = preset.title,
                author = preset.channel,
                thumbnailUrl = YouTubeUrlParser.getThumbnailUrl(videoId),
                durationSeconds = preset.durationSec
            )
        }

        val oembedUrl = "https://www.youtube.com/oembed?url=$canonicalUrl&format=json"
        try {
            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "TubeForge-Android-Downloader/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string() ?: ""
                val json = JSONObject(bodyString)
                val title = json.optString("title", "YouTube Media ($videoId)")
                val author = json.optString("author_name", "YouTube Creator")
                val thumb = json.optString("thumbnail_url", YouTubeUrlParser.getThumbnailUrl(videoId))

                return@withContext YouTubeVideoInfo(
                    videoId = videoId,
                    originalUrl = canonicalUrl,
                    title = title,
                    author = author,
                    thumbnailUrl = thumb,
                    durationSeconds = estimateDurationFromTitle(title)
                )
            }
        } catch (e: Exception) {
            Log.w("YouTubeMetadata", "Failed to fetch oEmbed info for $videoId, using fallback", e)
        }

        // Fallback info
        val fallbackTitle = generateFriendlyFallbackTitle(videoId)
        YouTubeVideoInfo(
            videoId = videoId,
            originalUrl = canonicalUrl,
            title = fallbackTitle,
            author = "TubeForge Channel",
            thumbnailUrl = YouTubeUrlParser.getThumbnailUrl(videoId),
            durationSeconds = 210
        )
    }

    private fun estimateDurationFromTitle(title: String): Int {
        val lower = title.lowercase()
        return when {
            lower.contains("mix") || lower.contains("hour") -> 3600
            lower.contains("compilation") -> 1200
            lower.contains("soundtrack") || lower.contains("lofi") -> 480
            lower.contains("short") || lower.contains("#shorts") -> 45
            else -> 215
        }
    }

    private fun generateFriendlyFallbackTitle(videoId: String): String {
        return "YouTube Media [$videoId]"
    }
}
