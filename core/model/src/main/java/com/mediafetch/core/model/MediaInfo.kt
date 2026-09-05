package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val id: String,
    val platform: Platform,
    val title: String,
    val author: String = "Unknown Creator",
    val authorAvatar: String? = null,
    val thumbnail: String? = null,
    val durationSeconds: Long = 0L,
    val mediaType: MediaType = MediaType.VIDEO,
    val availableFormats: List<MediaFormat> = emptyList(),
    val estimatedSize: Long = 0L,
    val sourceUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUserAuthorized: Boolean = true
)
