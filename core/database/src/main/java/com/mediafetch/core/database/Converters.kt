package com.mediafetch.core.database

import androidx.room.TypeConverter
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.model.MediaType
import com.mediafetch.core.model.Platform
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPlatform(value: Platform): String = value.name

    @TypeConverter
    fun toPlatform(value: String): Platform = runCatching { Platform.valueOf(value) }.getOrDefault(Platform.UNKNOWN)

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = runCatching { MediaType.valueOf(value) }.getOrDefault(MediaType.VIDEO)

    @TypeConverter
    fun fromDownloadState(value: DownloadState): String = value.name

    @TypeConverter
    fun toDownloadState(value: String): DownloadState = runCatching { DownloadState.valueOf(value) }.getOrDefault(DownloadState.QUEUED)

    @TypeConverter
    fun fromMediaInfo(value: MediaInfo): String = json.encodeToString(value)

    @TypeConverter
    fun toMediaInfo(value: String): MediaInfo = json.decodeFromString(value)

    @TypeConverter
    fun fromMediaFormat(value: MediaFormat): String = json.encodeToString(value)

    @TypeConverter
    fun toMediaFormat(value: String): MediaFormat = json.decodeFromString(value)
}
