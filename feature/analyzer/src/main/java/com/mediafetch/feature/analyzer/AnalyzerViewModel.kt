package com.mediafetch.feature.analyzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediafetch.core.common.Result
import com.mediafetch.core.common.toUserFacingMessage
import com.mediafetch.core.download.DownloadManager
import com.mediafetch.core.download.provider.MediaProviderRegistry
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.network.NetworkMonitor
import com.mediafetch.core.security.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AnalyzerUiState {
    data object Idle : AnalyzerUiState
    data object Loading : AnalyzerUiState
    data class Success(
        val mediaInfo: MediaInfo,
        val selectedFormat: MediaFormat,
        val showMobileDataWarning: Boolean = false,
        val isEnqueuing: Boolean = false
    ) : AnalyzerUiState
    data class Error(val message: String) : AnalyzerUiState
    data class Enqueued(val downloadId: String) : AnalyzerUiState
}

@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    private val providerRegistry: MediaProviderRegistry,
    private val downloadManager: DownloadManager,
    private val networkMonitor: NetworkMonitor,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    fun analyzeUrl(url: String) {
        _uiState.value = AnalyzerUiState.Loading
        viewModelScope.launch {
            val result = providerRegistry.extractMedia(url)
            when (result) {
                is Result.Success -> {
                    val media = result.data
                    val defaultFormat = media.availableFormats.firstOrNull() ?: MediaFormat(
                        id = "default",
                        quality = "Standard",
                        downloadUrl = url
                    )
                    
                    val isWifi = networkMonitor.isWifiConnected()
                    val warnMobile = securityPreferences.warnMobileData.first()
                    val showWarning = !isWifi && warnMobile

                    _uiState.value = AnalyzerUiState.Success(
                        mediaInfo = media,
                        selectedFormat = defaultFormat,
                        showMobileDataWarning = showWarning
                    )
                }
                is Result.Error -> {
                    val message = result.error.toUserFacingMessage()
                    _uiState.value = AnalyzerUiState.Error(message)
                }
                is Result.Loading -> {
                    _uiState.value = AnalyzerUiState.Loading
                }
            }
        }
    }

    fun selectFormat(format: MediaFormat) {
        val current = _uiState.value as? AnalyzerUiState.Success ?: return
        _uiState.value = current.copy(selectedFormat = format)
    }

    fun startDownload() {
        val current = _uiState.value as? AnalyzerUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isEnqueuing = true)
            val downloadId = downloadManager.enqueueDownload(
                mediaInfo = current.mediaInfo,
                format = current.selectedFormat
            )
            _uiState.value = AnalyzerUiState.Enqueued(downloadId)
        }
    }

    fun dismissWarning() {
        val current = _uiState.value as? AnalyzerUiState.Success ?: return
        _uiState.value = current.copy(showMobileDataWarning = false)
    }

    fun reset() {
        _uiState.value = AnalyzerUiState.Idle
    }
}
