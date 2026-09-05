package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadState {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
