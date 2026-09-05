package com.mediafetch.core.network

import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaType
import com.mediafetch.core.model.Platform
import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeRequest(
    val url: String,
    val clientVersion: String = "1.0.0",
    val options: Map<String, String> = emptyMap()
)

@Serializable
data class AnalyzeResponse(
    val success: Boolean,
    val data: AnalyzedMediaDto? = null,
    val error: String? = null,
    val errorCode: String? = null
)

@Serializable
data class AnalyzedMediaDto(
    val id: String,
    val platform: String,
    val title: String,
    val author: String = "Unknown Creator",
    val authorAvatar: String? = null,
    val thumbnail: String? = null,
    val durationSeconds: Long = 0L,
    val mediaType: String = "VIDEO",
    val availableFormats: List<MediaFormatDto> = emptyList(),
    val estimatedSize: Long = 0L,
    val sourceUrl: String
)

@Serializable
data class MediaFormatDto(
    val id: String,
    val quality: String,
    val resolution: String = "",
    val mimeType: String = "video/mp4",
    val fileExtension: String = "mp4",
    val estimatedSizeBytes: Long = 0L,
    val hasAudio: Boolean = true,
    val hasVideo: Boolean = true,
    val downloadUrl: String? = null
)

@Serializable
data class DownloadRequest(
    val url: String,
    val formatId: String
)

@Serializable
data class DownloadResponse(
    val success: Boolean,
    val downloadUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val error: String? = null
)

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val platforms: Map<String, Boolean>
)
