package com.example.engine

import java.util.regex.Pattern

object YouTubeUrlParser {

    private val VIDEO_ID_REGEX = Pattern.compile(
        "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
    )

    fun extractVideoId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isEmpty()) return null

        // If it's already an 11-character alphanumeric YouTube ID
        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        val matcher = VIDEO_ID_REGEX.matcher(trimmed)
        return if (matcher.matches()) {
            val id = matcher.group(1)
            if (id != null && id.length == 11) id else null
        } else {
            null
        }
    }

    fun parseBatchUrls(text: String): List<String> {
        val rawList = text.split(Regex("[\n\r,]+"))
        return rawList
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun getThumbnailUrl(videoId: String): String {
        return "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
    }

    fun getCanonicalUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }

    data class SampleMediaPreset(
        val title: String,
        val channel: String,
        val url: String,
        val durationSec: Int,
        val type: String,
        val category: String
    )

    val SAMPLE_PRESETS = listOf(
        SampleMediaPreset(
            title = "Midnight City Synthwave (Copyright Free)",
            channel = "RetroWave Records",
            url = "https://www.youtube.com/watch?v=UB4FzJ8p-4w",
            durationSec = 245,
            type = "AUDIO",
            category = "Music"
        ),
        SampleMediaPreset(
            title = "Cinematic 4K Drone Footage - Nordic Fjords",
            channel = "Nature Horizons",
            url = "https://www.youtube.com/watch?v=1La4QzGeaaQ",
            durationSec = 180,
            type = "VIDEO",
            category = "Video"
        ),
        SampleMediaPreset(
            title = "Lofi Hip Hop Chill Beats to Study & Relax",
            channel = "Cozy Cafe Sounds",
            url = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
            durationSec = 310,
            type = "AUDIO",
            category = "Music"
        ),
        SampleMediaPreset(
            title = "Tokyo Neon Night Walk (Binaural Audio 60fps)",
            channel = "City Walkers 4K",
            url = "https://www.youtube.com/watch?v=gQliI_X_wZ0",
            durationSec = 420,
            type = "VIDEO",
            category = "Video"
        ),
        SampleMediaPreset(
            title = "Epic Orchestral Fantasy Soundtrack",
            channel = "Mythic Sounds Studio",
            url = "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
            durationSec = 215,
            type = "AUDIO",
            category = "Music"
        )
    )
}
