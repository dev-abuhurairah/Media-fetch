package com.mediafetch.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediafetch.core.model.DownloadItem
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val mediaInfo: MediaInfo,
    val selectedFormat: MediaFormat,
    val state: DownloadState,
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val remainingSeconds: Long,
    val localUri: String?,
    val localFilePath: String?,
    val errorReason: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun DownloadEntity.toDomain(): DownloadItem {
    return DownloadItem(
        id = id,
        mediaInfo = mediaInfo,
        selectedFormat = selectedFormat,
        state = state,
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        speedBytesPerSec = speedBytesPerSec,
        remainingSeconds = remainingSeconds,
        localUri = localUri,
        localFilePath = localFilePath,
        errorReason = errorReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun DownloadItem.toEntity(): DownloadEntity {
    return DownloadEntity(
        id = id,
        mediaInfo = mediaInfo,
        selectedFormat = selectedFormat,
        state = state,
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        speedBytesPerSec = speedBytesPerSec,
        remainingSeconds = remainingSeconds,
        localUri = localUri,
        localFilePath = localFilePath,
        errorReason = errorReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
