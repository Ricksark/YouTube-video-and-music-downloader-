package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('QUEUED', 'DOWNLOADING', 'CONVERTING', 'PAUSED') ORDER BY createdAt ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC, createdAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DownloadEntity>)

    @Update
    suspend fun update(item: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress, downloadedSizeBytes = :downloaded, downloadSpeedBytesPerSec = :speed, etaSeconds = :eta, currentPhase = :phase WHERE id = :id")
    suspend fun updateProgress(
        id: String,
        status: String,
        progress: Float,
        downloaded: Long,
        speed: Long,
        eta: Int,
        phase: String
    )

    @Query("UPDATE downloads SET status = 'COMPLETED', progress = 1.0, currentPhase = 'Completed', completedAt = :completedAt, filePath = :filePath WHERE id = :id")
    suspend fun markCompleted(id: String, filePath: String, completedAt: Long)

    @Query("UPDATE downloads SET status = 'FAILED', currentPhase = 'Failed', errorMessage = :error WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads WHERE status IN ('CANCELLED', 'FAILED')")
    suspend fun clearFailedAndCancelled()
}
