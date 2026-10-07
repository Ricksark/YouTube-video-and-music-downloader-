package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MediaPreviewDialog
import com.example.ui.components.TubeForgeTopBar
import com.example.ui.screens.ActiveQueueScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.TubeForgeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TubeForgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TubeForgeApp(viewModel)
            }
        }
    }
}

@Composable
fun TubeForgeApp(viewModel: TubeForgeViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val isTurbo by viewModel.isTurboEnabled.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val previewItem by viewModel.previewItem.collectAsState()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsState()
    val previewPlaybackSec by viewModel.previewPlaybackSeconds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button: return to DOWNLOADER tab if elsewhere
    BackHandler(enabled = currentTab != AppTab.DOWNLOADER) {
        viewModel.setTab(AppTab.DOWNLOADER)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TubeForgeTopBar(
                title = currentTab.title,
                activeCount = activeTasks.size,
                isTurboActive = isTurbo
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.navigationBarsPadding().testTag("bottom_navigation_bar")
            ) {
                // Downloader Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.DOWNLOADER,
                    onClick = { viewModel.setTab(AppTab.DOWNLOADER) },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloader") },
                    label = { Text("Download") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("nav_download")
                )

                // Active Queue Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.QUEUE,
                    onClick = { viewModel.setTab(AppTab.QUEUE) },
                    icon = {
                        if (activeTasks.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = CrimsonPrimary) {
                                        Text("${activeTasks.size}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Queue, contentDescription = "Queue")
                            }
                        } else {
                            Icon(Icons.Default.Queue, contentDescription = "Queue")
                        }
                    },
                    label = { Text("Queue") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("nav_queue")
                )

                // Library Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.LIBRARY,
                    onClick = { viewModel.setTab(AppTab.LIBRARY) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Library") },
                    label = { Text("Library") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("nav_library")
                )

                // Converter Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CONVERTER,
                    onClick = { viewModel.setTab(AppTab.CONVERTER) },
                    icon = { Icon(Icons.Default.Transform, contentDescription = "Converter") },
                    label = { Text("Convert") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("nav_converter")
                )

                // Settings Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.setTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DOWNLOADER -> DownloaderScreen(viewModel)
                AppTab.QUEUE -> ActiveQueueScreen(viewModel)
                AppTab.LIBRARY -> LibraryScreen(viewModel)
                AppTab.CONVERTER -> ConverterScreen(viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel)
            }

            // Preview Player Modal
            if (previewItem != null) {
                MediaPreviewDialog(
                    item = previewItem!!,
                    isPlaying = isPlayingPreview,
                    playbackSeconds = previewPlaybackSec,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekPreview(it) },
                    onClose = { viewModel.closePreview() },
                    onConvert = {
                        val item = previewItem!!
                        viewModel.closePreview()
                        viewModel.prepareConversion(item)
                    }
                )
            }
        }
    }
}
