package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val mediaType: String = MediaType.VIDEO.name,
    val targetFormat: String = MediaFormat.MP4.name,
    val quality: String = "1080p Full HD",
    val totalSizeBytes: Long = 0L,
    val downloadedSizeBytes: Long = 0L,
    val status: String = DownloadStatus.QUEUED.name,
    val progress: Float = 0.0f,
    val downloadSpeedBytesPerSec: Long = 0L,
    val etaSeconds: Int = 0,
    val currentPhase: String = "Queued",
    val filePath: String? = null,
    val durationSeconds: Int = 0,
    val batchId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)
