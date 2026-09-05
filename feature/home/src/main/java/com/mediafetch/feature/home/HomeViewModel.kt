package com.mediafetch.feature.home

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediafetch.core.common.Formatters
import com.mediafetch.core.database.DownloadDao
import com.mediafetch.core.database.MediaLibraryDao
import com.mediafetch.core.database.toDomain
import com.mediafetch.core.download.provider.MediaProviderRegistry
import com.mediafetch.core.model.DownloadItem
import com.mediafetch.core.model.Platform
import com.mediafetch.core.security.SecurityPreferences
import com.mediafetch.core.security.UrlValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadStats(
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val totalDownloadedBytes: Long = 0L
)

data class HomeUiState(
    val urlInput: String = "",
    val detectedPlatform: Platform = Platform.UNKNOWN,
    val isUrlValid: Boolean = false,
    val clipboardUrl: String? = null,
    val clipboardPlatform: Platform = Platform.UNKNOWN,
    val stats: DownloadStats = DownloadStats(),
    val recentDownloads: List<DownloadItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerRegistry: MediaProviderRegistry,
    private val downloadDao: DownloadDao,
    private val mediaLibraryDao: MediaLibraryDao,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDownloadsAndLibrary()
    }

    private fun observeDownloadsAndLibrary() {
        viewModelScope.launch {
            combine(
                downloadDao.getAllDownloads(),
                mediaLibraryDao.getTotalLibrarySizeBytes()
            ) { downloads, totalBytes ->
                val domainDownloads = downloads.map { it.toDomain() }
                val active = domainDownloads.count { it.state.name == "DOWNLOADING" || it.state.name == "QUEUED" }
                val completed = domainDownloads.count { it.state.name == "COMPLETED" }

                DownloadStats(
                    activeCount = active,
                    completedCount = completed,
                    totalDownloadedBytes = totalBytes ?: 0L
                ) to domainDownloads.take(5)
            }.collect { (stats, recents) ->
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    recentDownloads = recents
                )
            }
        }
    }

    fun onUrlChanged(newUrl: String) {
        val validation = UrlValidator.validate(newUrl)
        val platform = if (validation.isValid) providerRegistry.detectPlatform(newUrl) else Platform.UNKNOWN
        _uiState.value = _uiState.value.copy(
            urlInput = newUrl,
            isUrlValid = validation.isValid && platform != Platform.UNKNOWN,
            detectedPlatform = platform
        )
    }

    fun checkClipboardOnResume() {
        viewModelScope.launch {
            if (!securityPreferences.isClipboardDetectionEnabled.first()) return@launch

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return@launch
            if (clipboard.hasPrimaryClip() && (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true)) {
                val item = clipboard.primaryClip?.getItemAt(0)
                val text = item?.text?.toString()?.trim()

                if (!text.isNullOrBlank() && text != _uiState.value.urlInput) {
                    val validation = UrlValidator.validate(text)
                    val platform = if (validation.isValid) providerRegistry.detectPlatform(text) else Platform.UNKNOWN
                    if (validation.isValid && platform != Platform.UNKNOWN) {
                        _uiState.value = _uiState.value.copy(
                            clipboardUrl = text,
                            clipboardPlatform = platform
                        )
                    }
                }
            }
        }
    }

    fun useClipboardUrl() {
        _uiState.value.clipboardUrl?.let {
            onUrlChanged(it)
            dismissClipboardBanner()
        }
    }

    fun dismissClipboardBanner() {
        _uiState.value = _uiState.value.copy(clipboardUrl = null)
    }

    fun clearInput() {
        _uiState.value = _uiState.value.copy(
            urlInput = "",
            isUrlValid = false,
            detectedPlatform = Platform.UNKNOWN
        )
    }
}
