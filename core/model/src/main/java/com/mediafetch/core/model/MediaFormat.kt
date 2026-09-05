package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaFormat(
    val id: String,
    val quality: String, // e.g. "1080p", "720p", "480p", "320kbps"
    val resolution: String = "", // e.g. "1920x1080"
    val mimeType: String = "video/mp4",
    val fileExtension: String = "mp4",
    val estimatedSizeBytes: Long = 0L,
    val hasAudio: Boolean = true,
    val hasVideo: Boolean = true,
    val downloadUrl: String? = null
)
