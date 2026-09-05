package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DownloadItem(
    val id: String,
    val mediaInfo: MediaInfo,
    val selectedFormat: MediaFormat,
    val state: DownloadState = DownloadState.QUEUED,
    val progress: Float = 0f, // 0.0f to 1.0f
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val remainingSeconds: Long = 0L,
    val localUri: String? = null,
    val localFilePath: String? = null,
    val errorReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
