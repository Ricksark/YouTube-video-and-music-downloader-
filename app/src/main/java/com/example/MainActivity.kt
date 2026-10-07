package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import com.example.ui.components.MediaPreviewDialog
import com.example.ui.components.TubeForgeTopBar
import com.example.ui.screens.ActiveQueueScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.TubeForgeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TubeForgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
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

    // Android 13+ (TIRAMISU) Notification Permission Requester
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermission(isGranted)
        if (isGranted) {
            viewModel.showSnackbar("Download alerts enabled successfully!")
        } else {
            viewModel.showSnackbar("Alerts disabled. You can enable them anytime in Settings.")
        }
    }

    val requestNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.updateNotificationPermission(true)
            viewModel.showSnackbar("Download notifications ready")
        }
    }

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
                tonalElevation = 3.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar")
            ) {
                // Downloader Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.DOWNLOADER,
                    onClick = { viewModel.setTab(AppTab.DOWNLOADER) },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloader") },
                    label = { Text("Download") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
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
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
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
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
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
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
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
                        selectedIconColor = MaterialTheme.colorScheme.onTertiary,
                        indicatorColor = MaterialTheme.colorScheme.tertiary
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
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
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
                AppTab.DOWNLOADER -> DownloaderScreen(
                    viewModel = viewModel,
                    onRequestPermission = requestNotificationPermission
                )
                AppTab.QUEUE -> ActiveQueueScreen(viewModel = viewModel)
                AppTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                AppTab.CONVERTER -> ConverterScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onRequestPermission = requestNotificationPermission
                )
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
                    },
                    onExportToDownloads = {
                        viewModel.exportItemToPublicDownloads(previewItem!!)
                    }
                )
            }
        }
    }
}
