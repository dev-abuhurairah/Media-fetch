package com.mediafetch.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mediafetch.core.database.DownloadDao
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.security.SecurityPreferences
import com.mediafetch.core.security.UrlValidator
import com.mediafetch.core.ui.MediaFetchTheme
import com.mediafetch.core.ui.ModernFloatingBottomBar
import com.mediafetch.core.ui.NavigationItem
import com.mediafetch.feature.analyzer.AnalyzerBottomSheet
import com.mediafetch.feature.analyzer.AnalyzerViewModel
import com.mediafetch.feature.downloads.DownloadsScreen
import com.mediafetch.feature.downloads.DownloadsViewModel
import com.mediafetch.feature.home.HomeScreen
import com.mediafetch.feature.home.HomeViewModel
import com.mediafetch.feature.library.LibraryScreen
import com.mediafetch.feature.library.LibraryViewModel
import com.mediafetch.feature.settings.SettingsScreen
import com.mediafetch.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Downloads : Screen("downloads", "Downloads", Icons.Outlined.Download, Icons.Filled.Download)
    data object Library : Screen("library", "Library", Icons.Outlined.VideoLibrary, Icons.Filled.VideoLibrary)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var securityPreferences: SecurityPreferences

    @Inject
    lateinit var downloadDao: DownloadDao

    private var sharedUrlToAnalyze by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeMode by securityPreferences.themeMode.collectAsState(initial = "SYSTEM")
            val isDynamicColor by securityPreferences.isDynamicColorEnabled.collectAsState(initial = false)

            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MediaFetchTheme(
                darkTheme = isDark,
                dynamicColor = isDynamicColor
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val allDownloads by downloadDao.getAllDownloads().collectAsState(initial = emptyList())
                val activeDownloadsCount = remember(allDownloads) {
                    allDownloads.count { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED }
                }

                val navItems = remember(activeDownloadsCount) {
                    listOf(
                        NavigationItem(
                            route = Screen.Home.route,
                            title = Screen.Home.title,
                            icon = Screen.Home.icon,
                            selectedIcon = Screen.Home.selectedIcon
                        ),
                        NavigationItem(
                            route = Screen.Downloads.route,
                            title = Screen.Downloads.title,
                            icon = Screen.Downloads.icon,
                            selectedIcon = Screen.Downloads.selectedIcon,
                            badgeCount = activeDownloadsCount
                        ),
                        NavigationItem(
                            route = Screen.Library.route,
                            title = Screen.Library.title,
                            icon = Screen.Library.icon,
                            selectedIcon = Screen.Library.selectedIcon
                        ),
                        NavigationItem(
                            route = Screen.Settings.route,
                            title = Screen.Settings.title,
                            icon = Screen.Settings.icon,
                            selectedIcon = Screen.Settings.selectedIcon
                        )
                    )
                }

                Scaffold(
                    bottomBar = {
                        ModernFloatingBottomBar(
                            items = navItems,
                            currentRoute = currentRoute,
                            onItemClick = { item ->
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        composable(Screen.Home.route) {
                            val homeViewModel: HomeViewModel = hiltViewModel()
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToAnalyze = { url -> sharedUrlToAnalyze = url },
                                onNavigateToDownloads = {
                                    navController.navigate(Screen.Downloads.route)
                                }
                            )
                        }

                        composable(Screen.Downloads.route) {
                            val downloadsViewModel: DownloadsViewModel = hiltViewModel()
                            DownloadsScreen(viewModel = downloadsViewModel)
                        }

                        composable(Screen.Library.route) {
                            val libraryViewModel: LibraryViewModel = hiltViewModel()
                            LibraryScreen(viewModel = libraryViewModel)
                        }

                        composable(Screen.Settings.route) {
                            val settingsViewModel: SettingsViewModel = hiltViewModel()
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }

                    // Shared URL Analyzer Bottom Sheet
                    sharedUrlToAnalyze?.let { targetUrl ->
                        val analyzerViewModel: AnalyzerViewModel = hiltViewModel()
                        AnalyzerBottomSheet(
                            url = targetUrl,
                            viewModel = analyzerViewModel,
                            onDismiss = {
                                sharedUrlToAnalyze = null
                                analyzerViewModel.reset()
                            },
                            onDownloadStarted = {
                                sharedUrlToAnalyze = null
                                navController.navigate(Screen.Downloads.route)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val extractedUrl = extractFirstUrl(sharedText)
                if (extractedUrl != null && UrlValidator.validate(extractedUrl).isValid) {
                    sharedUrlToAnalyze = extractedUrl
                }
            }
        } else if (Intent.ACTION_VIEW == intent.action) {
            val data = intent.data
            val urlParam = data?.getQueryParameter("url")
            if (!urlParam.isNullOrBlank() && UrlValidator.validate(urlParam).isValid) {
                sharedUrlToAnalyze = urlParam
            }
        }
    }

    private fun extractFirstUrl(text: String): String? {
        val pattern = Pattern.compile("https?://[a-zA-Z0-9.-]+(?:/[^\\s]*)?")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(0) else null
    }
}
