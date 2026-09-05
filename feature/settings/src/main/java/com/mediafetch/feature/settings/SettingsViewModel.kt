package com.mediafetch.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediafetch.core.database.DownloadDao
import com.mediafetch.core.database.MediaLibraryDao
import com.mediafetch.core.download.StorageHelper
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.security.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isWifiOnly: Boolean = false,
    val maxConcurrentDownloads: Int = 3,
    val warnMobileData: Boolean = true,
    val themeMode: String = "SYSTEM",
    val isDynamicColorEnabled: Boolean = true,
    val isClipboardDetectionEnabled: Boolean = true,
    val isAnalyticsOptedOut: Boolean = true,
    val librarySizeBytes: Long = 0L,
    val cacheSizeBytes: Long = 0L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityPreferences: SecurityPreferences,
    private val downloadDao: DownloadDao,
    private val mediaLibraryDao: MediaLibraryDao,
    private val storageHelper: StorageHelper
) : ViewModel() {

    private val _cacheSize = MutableStateFlow(0L)

    init {
        refreshCacheSize()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        securityPreferences.isWifiOnly,
        securityPreferences.maxConcurrentDownloads,
        securityPreferences.warnMobileData,
        securityPreferences.themeMode,
        securityPreferences.isDynamicColorEnabled,
        securityPreferences.isClipboardDetectionEnabled,
        securityPreferences.isAnalyticsOptedOut,
        mediaLibraryDao.getTotalLibrarySizeBytes(),
        _cacheSize
    ) { wifi, maxConcurrent, warnMobile, theme, dynamicColor, clipboard, analytics, librarySize, cacheSize ->
        SettingsUiState(
            isWifiOnly = wifi,
            maxConcurrentDownloads = maxConcurrent,
            warnMobileData = warnMobile,
            themeMode = theme,
            isDynamicColorEnabled = dynamicColor,
            isClipboardDetectionEnabled = clipboard,
            isAnalyticsOptedOut = analytics,
            librarySizeBytes = librarySize ?: 0L,
            cacheSizeBytes = cacheSize
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch { securityPreferences.setWifiOnly(enabled) }
    }

    fun setMaxConcurrentDownloads(limit: Int) {
        viewModelScope.launch { securityPreferences.setMaxConcurrentDownloads(limit) }
    }

    fun setWarnMobileData(warn: Boolean) {
        viewModelScope.launch { securityPreferences.setWarnMobileData(warn) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { securityPreferences.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { securityPreferences.setDynamicColor(enabled) }
    }

    fun setClipboardDetection(enabled: Boolean) {
        viewModelScope.launch { securityPreferences.setClipboardDetection(enabled) }
    }

    fun setAnalyticsOptOut(optOut: Boolean) {
        viewModelScope.launch { securityPreferences.setAnalyticsOptOut(optOut) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            downloadDao.clearByStates(
                listOf(DownloadState.COMPLETED, DownloadState.CANCELLED, DownloadState.FAILED)
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            val cacheDir = storageHelper.getAppCacheDir()
            cacheDir.listFiles()?.forEach { it.delete() }
            refreshCacheSize()
        }
    }

    private fun refreshCacheSize() {
        val cacheDir = storageHelper.getAppCacheDir()
        val total = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        _cacheSize.value = total
    }
}
