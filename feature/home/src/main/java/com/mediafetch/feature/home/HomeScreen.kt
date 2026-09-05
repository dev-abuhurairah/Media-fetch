package com.mediafetch.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mediafetch.core.common.Formatters
import com.mediafetch.core.model.Platform
import com.mediafetch.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAnalyze: (String) -> Unit,
    onNavigateToDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("🔥 All", "YouTube", "TikTok", "Instagram", "Facebook", "Music")

    // Curated trending & popular public media for 1-tap download showcase
    val trendingMediaList = remember {
        listOf(
            TrendingMedia(
                id = "yt_1",
                title = "Blender Open Movie Project 4K - Big Buck Bunny",
                author = "Blender Animation Studio",
                duration = "09:56",
                platform = Platform.YOUTUBE,
                thumbnailUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
                views = "18M views"
            ),
            TrendingMedia(
                id = "tt_1",
                title = "Viral Shuffle Dance Freestyle & Beatdrop Choreo",
                author = "@dance_vibes",
                duration = "00:45",
                platform = Platform.TIKTOK,
                thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.tiktok.com/@tiktok/video/7106594312292453678",
                views = "4.2M views"
            ),
            TrendingMedia(
                id = "ig_1",
                title = "Cinematic Drone Voyage Across the Italian Dolomites 4K",
                author = "@alpine_wanderer",
                duration = "01:15",
                platform = Platform.INSTAGRAM,
                thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.instagram.com/reel/C8_123abc/",
                views = "980K views"
            ),
            TrendingMedia(
                id = "fb_1",
                title = "Handcrafted Woodworking & Acoustic Guitar Build",
                author = "Master Craftsman",
                duration = "08:12",
                platform = Platform.FACEBOOK,
                thumbnailUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.facebook.com/watch/?v=1234567890",
                views = "2.1M views"
            ),
            TrendingMedia(
                id = "yt_2",
                title = "Chillhop Lo-Fi Beats for Coding and Relaxation",
                author = "Lo-Fi Records",
                duration = "04:18",
                platform = Platform.YOUTUBE,
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.youtube.com/watch?v=5qap5aO4i9A",
                views = "32M views"
            ),
            TrendingMedia(
                id = "tt_2",
                title = "Mind-Blowing Space & Physics Facts in 60 Seconds",
                author = "@science_daily",
                duration = "00:58",
                platform = Platform.TIKTOK,
                thumbnailUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=600&auto=format&fit=crop&q=80",
                videoUrl = "https://www.tiktok.com/@sciencedaily/video/71234567890",
                views = "7.5M views"
            )
        )
    }

    val filteredList = remember(selectedCategory, trendingMediaList) {
        when (selectedCategory) {
            "YouTube" -> trendingMediaList.filter { it.platform == Platform.YOUTUBE }
            "TikTok" -> trendingMediaList.filter { it.platform == Platform.TIKTOK }
            "Instagram" -> trendingMediaList.filter { it.platform == Platform.INSTAGRAM }
            "Facebook" -> trendingMediaList.filter { it.platform == Platform.FACEBOOK }
            "Music" -> trendingMediaList.filter {
                it.title.contains("Beat", ignoreCase = true) ||
                        it.title.contains("Lo-Fi", ignoreCase = true) ||
                        it.title.contains("Guitar", ignoreCase = true)
            }
            else -> trendingMediaList
        }
    }

    // Trigger clipboard check on screen composition
    LaunchedEffect(Unit) {
        viewModel.checkClipboardOnResume()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(SnapYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MediaFetch",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SnapYellow)
                                )
                            }
                            Text(
                                text = "Snap-Fast Media Downloader",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDownloads) {
                        BadgedBox(
                            badge = {
                                if (uiState.stats.activeCount > 0) {
                                    Badge(
                                        containerColor = SnapYellow,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            "${uiState.stats.activeCount}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Downloads",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
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
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Privacy-conscious smart clipboard banner
            item {
                AnimatedVisibility(
                    visible = uiState.clipboardUrl != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.clipboardUrl?.let { url ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SnapYellow.copy(alpha = 0.12f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SnapYellow.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SnapYellow.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = SnapYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Link detected in clipboard",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${uiState.clipboardPlatform.displayName} URL ready to analyze",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.useClipboardUrl()
                                        onNavigateToAnalyze(url)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SnapYellow,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Analyze", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.dismissClipboardBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SnapTube Search & Paste Pill Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SnapSearchBar(
                        query = uiState.urlInput,
                        onQueryChange = { viewModel.onUrlChanged(it) },
                        onSearch = {
                            if (uiState.isUrlValid) {
                                onNavigateToAnalyze(uiState.urlInput.trim())
                            }
                        },
                        onPaste = {
                            viewModel.checkClipboardOnResume()
                            viewModel.useClipboardUrl()
                        },
                        onClear = { viewModel.clearInput() },
                        detectedPlatform = uiState.detectedPlatform,
                        placeholder = "Search or paste video URL..."
                    )

                    // Instant Analyze Button if a valid URL is typed or pasted
                    AnimatedVisibility(
                        visible = uiState.isUrlValid,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Button(
                            onClick = { onNavigateToAnalyze(uiState.urlInput.trim()) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SnapYellow,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyze & Download (${uiState.detectedPlatform.displayName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Category Filter Pills (SnapTube-style)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isCatSelected = if (cat.contains("All")) selectedCategory == "All" else selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isCatSelected) SnapYellow else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isCatSelected) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            onClick = {
                                selectedCategory = if (cat.contains("All")) "All" else cat
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCatSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isCatSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Platform Quick Shortcut Circles (The SnapTube signature grid)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PlatformCircleShortcut(
                            name = "YouTube",
                            icon = Icons.Default.PlayArrow,
                            iconColor = Color.White,
                            backgroundColor = PlatformYouTube,
                            onClick = { selectedCategory = "YouTube" },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformCircleShortcut(
                            name = "TikTok",
                            icon = Icons.Default.MusicVideo,
                            iconColor = Color.White,
                            backgroundColor = Color(0xFF1E222B),
                            onClick = { selectedCategory = "TikTok" },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformCircleShortcut(
                            name = "Instagram",
                            icon = Icons.Default.CameraAlt,
                            iconColor = Color.White,
                            backgroundColor = PlatformInstagram,
                            onClick = { selectedCategory = "Instagram" },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformCircleShortcut(
                            name = "Facebook",
                            icon = Icons.Default.ThumbUp,
                            iconColor = Color.White,
                            backgroundColor = PlatformFacebook,
                            onClick = { selectedCategory = "Facebook" },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformCircleShortcut(
                            name = "Music",
                            icon = Icons.Default.Headphones,
                            iconColor = Color.Black,
                            backgroundColor = SnapYellow,
                            onClick = { selectedCategory = "Music" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Download Statistics Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active",
                        value = "${uiState.stats.activeCount}",
                        icon = Icons.Outlined.Downloading,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = "${uiState.stats.completedCount}",
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Saved",
                        value = Formatters.formatBytes(uiState.stats.totalDownloadedBytes),
                        icon = Icons.Outlined.Storage,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }

            // Featured & Trending Showcase with 1-Tap Golden Download Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = SnapYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedCategory == "All") "Trending & Popular" else "$selectedCategory Media",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (selectedCategory != "All") {
                        TextButton(onClick = { selectedCategory = "All" }) {
                            Text("Show All", color = SnapYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Trending media list with SnapTube-style circular yellow download buttons
            items(filteredList) { media ->
                SnapTubeVideoCard(
                    media = media,
                    onDownloadClick = { videoUrl -> onNavigateToAnalyze(videoUrl) }
                )
            }

            // Recent Activity Section
            if (uiState.recentDownloads.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Downloads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToDownloads) {
                            Text("See All", color = SnapYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(uiState.recentDownloads) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.mediaInfo.thumbnail,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.mediaInfo.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PlatformBadge(platform = item.mediaInfo.platform)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${Formatters.formatBytes(item.downloadedBytes)} • ${item.state.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
