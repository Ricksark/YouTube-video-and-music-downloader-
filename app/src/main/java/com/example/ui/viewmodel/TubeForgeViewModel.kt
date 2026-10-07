package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DownloadEntity
import com.example.data.database.TubeForgeDatabase
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType
import com.example.data.model.QualityPreset
import com.example.data.model.YouTubeVideoInfo
import com.example.engine.DownloadManager
import com.example.engine.FormatConverterEngine
import com.example.engine.LiveTaskProgress
import com.example.engine.YouTubeMetadataService
import com.example.engine.YouTubeUrlParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class AppTab(val title: String) {
    DOWNLOADER("Download"),
    QUEUE("Active Queue"),
    LIBRARY("Library"),
    CONVERTER("Converter"),
    SETTINGS("Settings")
}

data class BatchEntry(
    val url: String,
    val videoId: String,
    val title: String,
    val isSelected: Boolean = true
)

class TubeForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TubeForgeDatabase.getDatabase(application)
    private val downloadDao = db.downloadDao()
    private val metadataService = YouTubeMetadataService()
    val downloadManager = DownloadManager(application, downloadDao, viewModelScope)
    private val converterEngine = FormatConverterEngine(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.DOWNLOADER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Downloader Form State
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isBatchMode = MutableStateFlow(false)
    val isBatchMode: StateFlow<Boolean> = _isBatchMode.asStateFlow()

    private val _batchText = MutableStateFlow("")
    val batchText: StateFlow<String> = _batchText.asStateFlow()

    private val _batchEntries = MutableStateFlow<List<BatchEntry>>(emptyList())
    val batchEntries: StateFlow<List<BatchEntry>> = _batchEntries.asStateFlow()

    private val _selectedMediaType = MutableStateFlow(MediaType.VIDEO)
    val selectedMediaType: StateFlow<MediaType> = _selectedMediaType.asStateFlow()

    private val _selectedFormat = MutableStateFlow(MediaFormat.MP4)
    val selectedFormat: StateFlow<MediaFormat> = _selectedFormat.asStateFlow()

    private val _selectedQuality = MutableStateFlow(QualityPreset.RES_1080P)
    val selectedQuality: StateFlow<QualityPreset> = _selectedQuality.asStateFlow()

    private val _isTurboEnabled = MutableStateFlow(true)
    val isTurboEnabled: StateFlow<Boolean> = _isTurboEnabled.asStateFlow()

    private val _resolvedPreview = MutableStateFlow<YouTubeVideoInfo?>(null)
    val resolvedPreview: StateFlow<YouTubeVideoInfo?> = _resolvedPreview.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Active Queue State
    val activeTasks: StateFlow<List<DownloadEntity>> = downloadDao.getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveProgressMap: StateFlow<Map<String, LiveTaskProgress>> = downloadManager.liveProgressMap

    // Library State
    private val _libraryFilter = MutableStateFlow("ALL") // ALL, VIDEO, AUDIO
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val completedTasks: StateFlow<List<DownloadEntity>> = combine(
        downloadDao.getCompletedDownloads(),
        _libraryFilter,
        _searchQuery
    ) { list, filter, query ->
        list.filter { item ->
            val matchesFilter = when (filter) {
                "VIDEO" -> item.mediaType == MediaType.VIDEO.name
                "AUDIO" -> item.mediaType == MediaType.AUDIO.name
                else -> true
            }
            val matchesQuery = query.isBlank() || 
                item.title.contains(query, ignoreCase = true) || 
                item.author.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Media Preview Player
    private val _previewItem = MutableStateFlow<DownloadEntity?>(null)
    val previewItem: StateFlow<DownloadEntity?> = _previewItem.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    private val _previewPlaybackSeconds = MutableStateFlow(0)
    val previewPlaybackSeconds: StateFlow<Int> = _previewPlaybackSeconds.asStateFlow()

    private var playbackJob: Job? = null

    // Standalone Converter State
    private val _converterSource = MutableStateFlow<DownloadEntity?>(null)
    val converterSource: StateFlow<DownloadEntity?> = _converterSource.asStateFlow()

    private val _converterTargetFormat = MutableStateFlow(MediaFormat.MP3)
    val converterTargetFormat: StateFlow<MediaFormat> = _converterTargetFormat.asStateFlow()

    private val _converterBitrateKbps = MutableStateFlow(320)
    val converterBitrateKbps: StateFlow<Int> = _converterBitrateKbps.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _conversionProgress = MutableStateFlow(0.0f)
    val conversionProgress: StateFlow<Float> = _conversionProgress.asStateFlow()

    private val _conversionPhase = MutableStateFlow("Idle")
    val conversionPhase: StateFlow<String> = _conversionPhase.asStateFlow()

    private val _convertedFileResult = MutableStateFlow<File?>(null)
    val convertedFileResult: StateFlow<File?> = _convertedFileResult.asStateFlow()

    // Settings State
    private val _maxConcurrency = MutableStateFlow(3)
    val maxConcurrency: StateFlow<Int> = _maxConcurrency.asStateFlow()

    private val _storageUsedBytes = MutableStateFlow(0L)
    val storageUsedBytes: StateFlow<Long> = _storageUsedBytes.asStateFlow()

    init {
        // Load initial preview with first sample preset
        val sample = YouTubeUrlParser.SAMPLE_PRESETS[0]
        _urlInput.value = sample.url
        resolveUrl(sample.url)
        refreshStorageUsage()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setUrlInput(url: String) {
        _urlInput.value = url
        resolveUrl(url)
    }

    fun setBatchMode(isBatch: Boolean) {
        _isBatchMode.value = isBatch
    }

    fun setBatchText(text: String) {
        _batchText.value = text
        val urls = YouTubeUrlParser.parseBatchUrls(text)
        _batchEntries.value = urls.mapIndexed { idx, url ->
            val vid = YouTubeUrlParser.extractVideoId(url) ?: "item_$idx"
            BatchEntry(
                url = url,
                videoId = vid,
                title = "Batch Media #$idx ($vid)",
                isSelected = true
            )
        }
    }

    fun loadSampleBatch() {
        val sampleUrls = YouTubeUrlParser.SAMPLE_PRESETS.joinToString("\n") { it.url }
        setBatchText(sampleUrls)
        _batchEntries.value = YouTubeUrlParser.SAMPLE_PRESETS.map {
            val vid = YouTubeUrlParser.extractVideoId(it.url) ?: "sample"
            BatchEntry(
                url = it.url,
                videoId = vid,
                title = it.title,
                isSelected = true
            )
        }
    }

    fun toggleBatchItem(index: Int) {
        val current = _batchEntries.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(isSelected = !item.isSelected)
            _batchEntries.value = current
        }
    }

    fun selectAllBatchItems(select: Boolean) {
        _batchEntries.value = _batchEntries.value.map { it.copy(isSelected = select) }
    }

    fun setMediaType(type: MediaType) {
        _selectedMediaType.value = type
        if (type == MediaType.AUDIO) {
            _selectedFormat.value = MediaFormat.MP3
            _selectedQuality.value = QualityPreset.AUDIO_320K
        } else {
            _selectedFormat.value = MediaFormat.MP4
            _selectedQuality.value = QualityPreset.RES_1080P
        }
    }

    fun setFormat(format: MediaFormat) {
        _selectedFormat.value = format
    }

    fun setQuality(preset: QualityPreset) {
        _selectedQuality.value = preset
    }

    fun toggleTurbo(enabled: Boolean) {
        _isTurboEnabled.value = enabled
        downloadManager.isTurboSpeedEnabled = enabled
    }

    fun selectSamplePreset(preset: YouTubeUrlParser.SampleMediaPreset) {
        _urlInput.value = preset.url
        if (preset.type == "AUDIO") {
            setMediaType(MediaType.AUDIO)
        } else {
            setMediaType(MediaType.VIDEO)
        }
        resolveUrl(preset.url)
    }

    fun resolveUrl(url: String) {
        val videoId = YouTubeUrlParser.extractVideoId(url)
        if (videoId == null) {
            _resolvedPreview.value = null
            return
        }

        viewModelScope.launch {
            _isResolving.value = true
            try {
                val info = metadataService.resolveVideoInfo(url)
                _resolvedPreview.value = info
            } catch (_: Exception) {
                _resolvedPreview.value = YouTubeVideoInfo(
                    videoId = videoId,
                    originalUrl = url,
                    title = "YouTube Video ($videoId)",
                    author = "YouTube Creator",
                    thumbnailUrl = YouTubeUrlParser.getThumbnailUrl(videoId)
                )
            } finally {
                _isResolving.value = false
            }
        }
    }

    fun startSingleDownload() {
        val url = _urlInput.value.trim()
        val videoId = YouTubeUrlParser.extractVideoId(url) ?: "vid_${System.currentTimeMillis() % 100000}"
        val preview = _resolvedPreview.value
        val title = preview?.title ?: "YouTube Media ($videoId)"
        val author = preview?.author ?: "YouTube Channel"
        val thumb = preview?.thumbnailUrl ?: YouTubeUrlParser.getThumbnailUrl(videoId)
        val format = _selectedFormat.value
        val mediaType = _selectedMediaType.value
        val quality = _selectedQuality.value

        val estimatedSizeBytes = calculateEstimatedSize(quality, preview?.durationSeconds ?: 210)

        val entity = DownloadEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            author = author,
            thumbnailUrl = thumb,
            videoUrl = url,
            mediaType = mediaType.name,
            targetFormat = format.name,
            quality = quality.label,
            totalSizeBytes = estimatedSizeBytes,
            downloadedSizeBytes = 0L,
            status = DownloadStatus.QUEUED.name,
            progress = 0f,
            currentPhase = "Queued for download",
            durationSeconds = preview?.durationSeconds ?: 210
        )

        downloadManager.enqueueDownload(entity)
        showSnackbar("Added \"$title\" to download queue!")
        _currentTab.value = AppTab.QUEUE
    }

    fun startBatchDownload() {
        val selected = _batchEntries.value.filter { it.isSelected }
        if (selected.isEmpty()) {
            showSnackbar("No items selected in batch")
            return
        }

        val format = _selectedFormat.value
        val mediaType = _selectedMediaType.value
        val quality = _selectedQuality.value
        val batchId = UUID.randomUUID().toString().take(8)

        val entities = selected.mapIndexed { idx, entry ->
            val estSize = calculateEstimatedSize(quality, 210)
            DownloadEntity(
                id = UUID.randomUUID().toString(),
                title = entry.title,
                author = "YouTube Channel",
                thumbnailUrl = YouTubeUrlParser.getThumbnailUrl(entry.videoId),
                videoUrl = entry.url,
                mediaType = mediaType.name,
                targetFormat = format.name,
                quality = quality.label,
                totalSizeBytes = estSize,
                downloadedSizeBytes = 0L,
                status = DownloadStatus.QUEUED.name,
                progress = 0f,
                currentPhase = "Queued in Batch #$batchId",
                batchId = batchId,
                durationSeconds = 210
            )
        }

        downloadManager.enqueueBatch(entities)
        showSnackbar("Enqueued ${entities.size} items in batch!")
        _currentTab.value = AppTab.QUEUE
    }

    private fun calculateEstimatedSize(preset: QualityPreset, durationSec: Int): Long {
        val minutes = durationSec / 60.0
        val mb = preset.estimatedMbPerMin * minutes
        return (mb * 1024 * 1024).toLong().coerceAtLeast(2 * 1024 * 1024)
    }

    // Media Preview Player
    fun openPreview(item: DownloadEntity) {
        _previewItem.value = item
        _isPlayingPreview.value = true
        _previewPlaybackSeconds.value = 0
        startPlaybackSimulation(item.durationSeconds.coerceAtLeast(60))
    }

    fun closePreview() {
        playbackJob?.cancel()
        _isPlayingPreview.value = false
        _previewItem.value = null
    }

    fun togglePlayPause() {
        if (_isPlayingPreview.value) {
            playbackJob?.cancel()
            _isPlayingPreview.value = false
        } else {
            _isPlayingPreview.value = true
            val total = _previewItem.value?.durationSeconds ?: 120
            startPlaybackSimulation(total)
        }
    }

    fun seekPreview(fraction: Float) {
        val total = _previewItem.value?.durationSeconds ?: 120
        _previewPlaybackSeconds.value = (fraction * total).toInt()
    }

    private fun startPlaybackSimulation(totalSec: Int) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_isPlayingPreview.value && _previewPlaybackSeconds.value < totalSec) {
                delay(1000)
                _previewPlaybackSeconds.value += 1
            }
            if (_previewPlaybackSeconds.value >= totalSec) {
                _isPlayingPreview.value = false
            }
        }
    }

    // Converter actions
    fun prepareConversion(item: DownloadEntity) {
        _converterSource.value = item
        // Default target opposite or mp3
        if (item.mediaType == MediaType.VIDEO.name) {
            _converterTargetFormat.value = MediaFormat.MP3
        } else {
            _converterTargetFormat.value = MediaFormat.FLAC
        }
        _currentTab.value = AppTab.CONVERTER
    }

    fun setConverterTargetFormat(format: MediaFormat) {
        _converterTargetFormat.value = format
    }

    fun setConverterBitrate(kbps: Int) {
        _converterBitrateKbps.value = kbps
    }

    fun executeConversion() {
        val source = _converterSource.value ?: return
        val file = if (source.filePath != null) File(source.filePath) else File(getApplication<Application>().filesDir, "temp.media")

        viewModelScope.launch {
            _isConverting.value = true
            _conversionProgress.value = 0f
            _conversionPhase.value = "Starting transcoder engine..."
            try {
                val outputFile = converterEngine.convertMedia(
                    sourceFile = file,
                    sourceTitle = source.title,
                    targetFormat = _converterTargetFormat.value,
                    bitrateKbps = _converterBitrateKbps.value
                ) { prog, phase ->
                    _conversionProgress.value = prog
                    _conversionPhase.value = phase
                }
                _convertedFileResult.value = outputFile
                showSnackbar("Successfully converted to ${_converterTargetFormat.value.name}!")
                refreshStorageUsage()
            } catch (e: Exception) {
                _conversionPhase.value = "Failed: ${e.message}"
                showSnackbar("Conversion error: ${e.message}")
            } finally {
                _isConverting.value = false
            }
        }
    }

    // Settings actions
    fun setMaxConcurrency(count: Int) {
        _maxConcurrency.value = count
        downloadManager.maxConcurrentDownloads = count
    }

    fun clearCompletedDownloads() {
        downloadManager.clearCompleted()
        showSnackbar("Cleared completed items from list")
    }

    fun refreshStorageUsage() {
        viewModelScope.launch {
            val rootDir = getApplication<Application>().filesDir
            var total = 0L
            rootDir.walkTopDown().forEach { f ->
                if (f.isFile) total += f.length()
            }
            _storageUsedBytes.value = total
        }
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
