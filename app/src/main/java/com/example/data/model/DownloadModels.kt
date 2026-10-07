package com.example.data.model

enum class MediaType(val displayName: String) {
    VIDEO("Video"),
    AUDIO("Audio")
}

enum class MediaFormat(
    val extension: String,
    val mediaType: MediaType,
    val mimeType: String,
    val description: String
) {
    MP4("mp4", MediaType.VIDEO, "video/mp4", "MPEG-4 Video (H.264 / AAC)"),
    WEBM("webm", MediaType.VIDEO, "video/webm", "WebM Video (VP9 / Opus)"),
    MKV("mkv", MediaType.VIDEO, "video/x-matroska", "Matroska Multimedia Container"),
    AVI("avi", MediaType.VIDEO, "video/x-msvideo", "Audio Video Interleave"),
    
    MP3("mp3", MediaType.AUDIO, "audio/mpeg", "MPEG-1 Audio Layer III (320kbps max)"),
    AAC("aac", MediaType.AUDIO, "audio/aac", "Advanced Audio Coding"),
    M4A("m4a", MediaType.AUDIO, "audio/mp4", "MPEG-4 Audio Stream"),
    FLAC("flac", MediaType.AUDIO, "audio/flac", "Free Lossless Audio Codec (Studio)"),
    WAV("wav", MediaType.AUDIO, "audio/wav", "Waveform Audio File (Uncompressed)"),
    OGG("ogg", MediaType.AUDIO, "audio/ogg", "Ogg Vorbis Audio")
}

enum class QualityPreset(
    val label: String,
    val mediaType: MediaType,
    val bitrateKbps: Int,
    val estimatedMbPerMin: Double
) {
    RES_1080P("1080p Full HD", MediaType.VIDEO, 6000, 45.0),
    RES_720P("720p HD", MediaType.VIDEO, 3000, 22.5),
    RES_480P("480p SD", MediaType.VIDEO, 1500, 11.2),
    
    AUDIO_320K("320 kbps (Studio Quality)", MediaType.AUDIO, 320, 2.4),
    AUDIO_256K("256 kbps (High Quality)", MediaType.AUDIO, 256, 1.9),
    AUDIO_192K("192 kbps (Standard)", MediaType.AUDIO, 192, 1.4),
    AUDIO_128K("128 kbps (Compact)", MediaType.AUDIO, 128, 0.95),
    AUDIO_FLAC("FLAC (Lossless 24-bit)", MediaType.AUDIO, 1411, 10.5)
}

enum class DownloadStatus(val label: String) {
    QUEUED("Queued"),
    DOWNLOADING("Downloading"),
    CONVERTING("Converting Format"),
    COMPLETED("Completed"),
    PAUSED("Paused"),
    CANCELLED("Cancelled"),
    FAILED("Failed")
}

data class YouTubeVideoInfo(
    val videoId: String,
    val originalUrl: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val durationSeconds: Int = 224,
    val viewCountText: String = "1.2M views"
)
