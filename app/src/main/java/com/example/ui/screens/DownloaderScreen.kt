package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType
import com.example.data.model.QualityPreset
import com.example.engine.YouTubeUrlParser
import com.example.ui.components.DownloadPermissionBanner
import com.example.ui.theme.SpeedAccent
import com.example.ui.viewmodel.TubeForgeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloaderScreen(
    viewModel: TubeForgeViewModel,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlInput by viewModel.urlInput.collectAsState()
    val isBatchMode by viewModel.isBatchMode.collectAsState()
    val batchText by viewModel.batchText.collectAsState()
    val batchEntries by viewModel.batchEntries.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val isTurboEnabled by viewModel.isTurboEnabled.collectAsState()
    val resolvedPreview by viewModel.resolvedPreview.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val hasPermission by viewModel.hasNotificationPermission.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Banner (if notification permission not yet granted on Android 13+)
            item {
                DownloadPermissionBanner(
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission
                )
            }

            // Hero Banner Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_downloader_banner),
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "ULTRA HD & HI-RES AUDIO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "YouTube Fast Downloader",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Convert to MP4, WebM, MP3 320k, FLAC & batch queue",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Mode Switcher (Single Video vs Batch Downloader)
            item {
                TabRow(
                    selectedTabIndex = if (isBatchMode) 1 else 0,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = !isBatchMode,
                        onClick = { viewModel.setBatchMode(false) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Single Media", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("tab_single_mode")
                    )
                    Tab(
                        selected = isBatchMode,
                        onClick = { viewModel.setBatchMode(true) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PlaylistPlay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Batch Queue", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("tab_batch_mode")
                    )
                }
            }

            // Single Mode Input & Preview
            if (!isBatchMode) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "YouTube URL or Video ID",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { viewModel.setUrlInput(it) },
                                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("url_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                trailingIcon = {
                                    Row {
                                        if (urlInput.isNotEmpty()) {
                                            IconButton(
                                                onClick = { viewModel.setUrlInput("") },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                                if (!clip.isNullOrBlank()) {
                                                    viewModel.setUrlInput(clip)
                                                }
                                            },
                                            modifier = Modifier.minimumInteractiveComponentSize().testTag("paste_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = "Paste",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Try sample links:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                YouTubeUrlParser.SAMPLE_PRESETS.take(4).forEach { sample ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clickable { viewModel.selectSamplePreset(sample) }
                                            .testTag("sample_${sample.category}")
                                    ) {
                                        Text(
                                            text = "${if (sample.type == "AUDIO") "🎵" else "🎬"} ${sample.title.take(18)}...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            if (isResolving) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Resolving video metadata...", style = MaterialTheme.typography.bodySmall)
                                }
                            } else if (resolvedPreview != null) {
                                val info = resolvedPreview!!
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth().testTag("resolved_preview_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 80.dp, height = 54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black)
                                        ) {
                                            AsyncImage(
                                                model = info.thumbnailUrl,
                                                contentDescription = info.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.matchParentSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = info.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${info.author} • ${info.durationSeconds}s",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Batch Mode Input
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Batch URLs (One per line or comma)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = { viewModel.loadSampleBatch() },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_load_sample_batch")
                                ) {
                                    Text("Load 5 Samples", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = batchText,
                                onValueChange = { viewModel.setBatchText(it) },
                                placeholder = { Text("https://www.youtube.com/watch?v=AAA\nhttps://www.youtube.com/watch?v=BBB\nhttps://youtu.be/CCC") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                                    .testTag("batch_text_field"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            if (batchEntries.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Items Detected: ${batchEntries.size}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row {
                                        Text(
                                            text = "Select All",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                                            modifier = Modifier
                                                .clickable { viewModel.selectAllBatchItems(true) }
                                                .padding(6.dp)
                                        )
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                            modifier = Modifier
                                                .clickable { viewModel.selectAllBatchItems(false) }
                                                .padding(6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                batchEntries.forEachIndexed { idx, entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable { viewModel.toggleBatchItem(idx) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = entry.isSelected,
                                            onCheckedChange = { viewModel.toggleBatchItem(idx) },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Format & Conversion Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Format & Quality Profile",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Video vs Audio Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedMediaType == MediaType.VIDEO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setMediaType(MediaType.VIDEO) }
                                    .testTag("type_video_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = if (selectedMediaType == MediaType.VIDEO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Video Download",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedMediaType == MediaType.VIDEO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedMediaType == MediaType.AUDIO) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setMediaType(MediaType.AUDIO) }
                                    .testTag("type_audio_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = if (selectedMediaType == MediaType.AUDIO) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Music / Audio",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedMediaType == MediaType.AUDIO) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Formats
                        Text(
                            text = "Container / Encoding Format:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val formats = if (selectedMediaType == MediaType.VIDEO) {
                            listOf(MediaFormat.MP4, MediaFormat.WEBM, MediaFormat.MKV, MediaFormat.AVI)
                        } else {
                            listOf(MediaFormat.MP3, MediaFormat.FLAC, MediaFormat.AAC, MediaFormat.WAV, MediaFormat.OGG)
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formats.forEach { fmt ->
                                FilterChip(
                                    selected = selectedFormat == fmt,
                                    onClick = { viewModel.setFormat(fmt) },
                                    label = { Text(fmt.name, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (selectedMediaType == MediaType.VIDEO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("format_${fmt.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quality Presets
                        Text(
                            text = if (selectedMediaType == MediaType.VIDEO) "Resolution:" else "Bitrate / Quality:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val qualities = if (selectedMediaType == MediaType.VIDEO) {
                            listOf(QualityPreset.RES_1080P, QualityPreset.RES_720P, QualityPreset.RES_480P)
                        } else {
                            listOf(QualityPreset.AUDIO_320K, QualityPreset.AUDIO_256K, QualityPreset.AUDIO_192K, QualityPreset.AUDIO_FLAC)
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            qualities.forEach { q ->
                                FilterChip(
                                    selected = selectedQuality == q,
                                    onClick = { viewModel.setQuality(q) },
                                    label = { Text(q.label, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("quality_${q.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Turbo switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = SpeedAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Multi-Thread Turbo Streams",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Accelerates download & conversion processing",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isTurboEnabled,
                                onCheckedChange = { viewModel.toggleTurbo(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SpeedAccent, checkedTrackColor = SpeedAccent.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("turbo_switch")
                            )
                        }
                    }
                }
            }

            // Primary Button
            item {
                Button(
                    onClick = {
                        if (isBatchMode) {
                            viewModel.startBatchDownload()
                        } else {
                            viewModel.startSingleDownload()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_download_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMediaType == MediaType.VIDEO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBatchMode) {
                                val count = batchEntries.count { it.isSelected }
                                "Start Batch Download ($count Items)"
                            } else {
                                "Download ${selectedFormat.name} (${selectedQuality.label})"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
