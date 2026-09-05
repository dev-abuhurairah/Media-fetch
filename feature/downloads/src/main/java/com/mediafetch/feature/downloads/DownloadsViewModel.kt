package com.mediafetch.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediafetch.core.database.DownloadDao
import com.mediafetch.core.database.toDomain
import com.mediafetch.core.download.DownloadManager
import com.mediafetch.core.model.DownloadItem
import com.mediafetch.core.model.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val activeDownloads: List<DownloadItem> = emptyList(),
    val historyDownloads: List<DownloadItem> = emptyList(),
    val selectedTab: Int = 0 // 0 = Active, 1 = History
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadDao: DownloadDao
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<DownloadsUiState> = downloadDao.getAllDownloads().map { list ->
        val domainList = list.map { it.toDomain() }
        val active = domainList.filter { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED || it.state == DownloadState.PAUSED }
        val history = domainList.filter { it.state == DownloadState.COMPLETED || it.state == DownloadState.FAILED || it.state == DownloadState.CANCELLED }

        DownloadsUiState(
            activeDownloads = active,
            historyDownloads = history,
            selectedTab = _selectedTab.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadsUiState()
    )

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            downloadManager.pauseDownload(id)
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            downloadManager.resumeDownload(id)
        }
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch {
            downloadManager.cancelDownload(id)
        }
    }

    fun retryDownload(id: String) {
        viewModelScope.launch {
            downloadManager.retryDownload(id)
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            downloadDao.deleteById(id)
        }
    }

    fun clearFinishedHistory() {
        viewModelScope.launch {
            downloadDao.clearByStates(listOf(DownloadState.COMPLETED, DownloadState.CANCELLED))
        }
    }
}
