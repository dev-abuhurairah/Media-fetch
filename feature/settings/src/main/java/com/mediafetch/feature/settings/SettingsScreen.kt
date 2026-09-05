package com.mediafetch.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediafetch.core.common.Formatters
import com.mediafetch.core.ui.MediaFetchCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showConcurrencyDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Downloads Category
            item {
                SettingsSectionHeader(title = "Downloads")
                Spacer(modifier = Modifier.height(8.dp))
                MediaFetchCard {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Wifi,
                        title = "Download on Wi-Fi only",
                        subtitle = "Avoid downloading over cellular data",
                        checked = uiState.isWifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsSwitchItem(
                        icon = Icons.Outlined.WarningAmber,
                        title = "Warn on mobile data",
                        subtitle = "Alert before starting downloads on cellular",
                        checked = uiState.warnMobileData,
                        onCheckedChange = { viewModel.setWarnMobileData(it) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsClickableItem(
                        icon = Icons.Outlined.Layers,
                        title = "Concurrent download limit",
                        value = "${uiState.maxConcurrentDownloads} files",
                        onClick = { showConcurrencyDialog = true }
                    )
                }
            }

            // Appearance Category
            item {
                SettingsSectionHeader(title = "Appearance")
                Spacer(modifier = Modifier.height(8.dp))
                MediaFetchCard {
                    SettingsClickableItem(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        value = when (uiState.themeMode) {
                            "DARK" -> "Dark mode"
                            "LIGHT" -> "Light mode"
                            else -> "System default"
                        },
                        onClick = { showThemeDialog = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsSwitchItem(
                        icon = Icons.Outlined.ColorLens,
                        title = "Dynamic Colors",
                        subtitle = "Use Material You dynamic theming on Android 12+",
                        checked = uiState.isDynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }
            }

            // Privacy & Security Category
            item {
                SettingsSectionHeader(title = "Privacy & Security")
                Spacer(modifier = Modifier.height(8.dp))
                MediaFetchCard {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.ContentPaste,
                        title = "Smart clipboard detection",
                        subtitle = "Show analyze prompt when a media link is copied",
                        checked = uiState.isClipboardDetectionEnabled,
                        onCheckedChange = { viewModel.setClipboardDetection(it) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Security,
                        title = "Opt-out of analytics",
                        subtitle = "Disable anonymous usage diagnostics",
                        checked = uiState.isAnalyticsOptedOut,
                        onCheckedChange = { viewModel.setAnalyticsOptOut(it) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsClickableItem(
                        icon = Icons.Outlined.DeleteSweep,
                        title = "Clear download history",
                        value = "Clear logs",
                        onClick = { viewModel.clearHistory() }
                    )
                }
            }

            // Storage Category
            item {
                SettingsSectionHeader(title = "Storage")
                Spacer(modifier = Modifier.height(8.dp))
                MediaFetchCard {
                    SettingsClickableItem(
                        icon = Icons.Outlined.VideoLibrary,
                        title = "Library storage used",
                        value = Formatters.formatBytes(uiState.librarySizeBytes),
                        onClick = {}
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsClickableItem(
                        icon = Icons.Outlined.CleaningServices,
                        title = "Clear temporary cache",
                        value = Formatters.formatBytes(uiState.cacheSizeBytes),
                        onClick = { viewModel.clearCache() }
                    )
                }
            }

            // About Category
            item {
                SettingsSectionHeader(title = "About")
                Spacer(modifier = Modifier.height(8.dp))
                MediaFetchCard {
                    SettingsClickableItem(
                        icon = Icons.Outlined.Info,
                        title = "MediaFetch Version",
                        value = "v1.0.0 (Production)",
                        onClick = {}
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsClickableItem(
                        icon = Icons.Outlined.Policy,
                        title = "Privacy Policy",
                        value = "Read",
                        onClick = { showPrivacyDialog = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    SettingsClickableItem(
                        icon = Icons.Outlined.Gavel,
                        title = "Terms of Service & Compliance",
                        value = "Read",
                        onClick = { showTermsDialog = true }
                    )
                }
            }
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    listOf(
                        "SYSTEM" to "System default",
                        "LIGHT" to "Light",
                        "DARK" to "Dark"
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Concurrency Limit Dialog
    if (showConcurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showConcurrencyDialog = false },
            title = { Text("Concurrent Downloads") },
            text = {
                Column {
                    (1..5).forEach { count ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMaxConcurrentDownloads(count)
                                    showConcurrencyDialog = false
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = uiState.maxConcurrentDownloads == count,
                                onClick = {
                                    viewModel.setMaxConcurrentDownloads(count)
                                    showConcurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "$count concurrent download${if (count > 1) "s" else ""}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConcurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    text = "MediaFetch operates on a strict privacy-first principle. " +
                            "We do not collect personal identifiers, passwords, authentication cookies, private messages, or unprompted browsing history. " +
                            "Clipboard inspections are performed strictly upon user return to the app and only to detect valid public media URLs. " +
                            "All downloaded media is saved locally on your device in standard media collections.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("Acknowledge") }
            }
        )
    }

    // Terms & Compliance Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service & Compliance") },
            text = {
                Text(
                    text = "MediaFetch is designed exclusively for downloading legitimately available public and user-authorized media. " +
                            "The app does not bypass digital rights management (DRM), private content protections, or authentication barriers. " +
                            "Users are solely responsible for ensuring compliance with copyright laws, platform terms of service, and fair use guidelines in their respective jurisdictions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) { Text("I Agree") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
