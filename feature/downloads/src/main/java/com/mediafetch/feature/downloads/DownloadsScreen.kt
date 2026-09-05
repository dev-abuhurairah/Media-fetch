package com.mediafetch.feature.downloads

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mediafetch.core.common.Formatters
import com.mediafetch.core.model.DownloadItem
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.ui.EmptyStateView
import com.mediafetch.core.ui.MediaFetchCard
import com.mediafetch.core.ui.PlatformBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (uiState.selectedTab == 1 && uiState.historyDownloads.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearFinishedHistory() }) {
                            Text("Clear Finished")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Text(
                            text = "Active (${uiState.activeDownloads.size})",
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Text(
                            text = "History (${uiState.historyDownloads.size})",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (uiState.selectedTab) {
                0 -> {
                    if (uiState.activeDownloads.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.CloudDownload,
                            title = "No active downloads",
                            description = "Items currently downloading will appear here with live progress."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.activeDownloads, key = { it.id }) { item ->
                                ActiveDownloadCard(
                                    item = item,
                                    onPause = { viewModel.pauseDownload(item.id) },
                                    onResume = { viewModel.resumeDownload(item.id) },
                                    onCancel = { viewModel.cancelDownload(item.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (uiState.historyDownloads.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.History,
                            title = "No download history",
                            description = "Your completed, paused, and cancelled downloads will appear here."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.historyDownloads, key = { it.id }) { item ->
                                HistoryDownloadCard(
                                    item = item,
                                    onOpen = { openMedia(context, item.localUri) },
                                    onShare = { shareMedia(context, item.localUri, item.mediaInfo.title) },
                                    onRetry = { viewModel.retryDownload(item.id) },
                                    onDelete = { viewModel.deleteHistoryItem(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    MediaFetchCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.mediaInfo.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.mediaInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = item.mediaInfo.platform)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.selectedFormat.quality,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.state == DownloadState.DOWNLOADING) {
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                }
            } else if (item.state == DownloadState.PAUSED) {
                IconButton(onClick = onResume) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                }
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { item.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val percent = (item.progress * 100).toInt()
            val speed = Formatters.formatSpeed(item.speedBytesPerSec)
            val downloaded = Formatters.formatBytes(item.downloadedBytes)
            val total = Formatters.formatBytes(item.totalBytes)
            val eta = Formatters.formatRemainingTime(item.remainingSeconds)

            Text(
                text = "$percent% • $downloaded of $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (item.state == DownloadState.DOWNLOADING) "$speed • $eta" else item.state.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HistoryDownloadCard(
    item: DownloadItem,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    MediaFetchCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.mediaInfo.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.mediaInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = item.mediaInfo.platform)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${Formatters.formatBytes(item.downloadedBytes)} • ${item.selectedFormat.fileExtension.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val statusColor = when (item.state) {
                DownloadState.COMPLETED -> Color(0xFF10B981)
                DownloadState.FAILED -> Color(0xFFEF4444)
                DownloadState.CANCELLED -> Color(0xFF6B7280)
                else -> MaterialTheme.colorScheme.primary
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = item.state.name,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.state == DownloadState.COMPLETED) {
                TextButton(onClick = onOpen) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open")
                }
                TextButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            } else if (item.state == DownloadState.FAILED || item.state == DownloadState.CANCELLED) {
                TextButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun openMedia(context: Context, uriString: String?) {
    if (uriString == null) return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(uriString), "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}

private fun shareMedia(context: Context, uriString: String?, title: String) {
    if (uriString == null) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString))
        putExtra(Intent.EXTRA_TEXT, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Share Media")) }
}
