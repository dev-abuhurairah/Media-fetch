package com.mediafetch.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediafetch.core.model.MediaType
import com.mediafetch.core.model.Platform

@Entity(tableName = "media_library")
data class MediaLibraryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val platform: Platform,
    val mediaType: MediaType,
    val author: String,
    val fileUri: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val durationSeconds: Long,
    val thumbnailUri: String?,
    val mimeType: String,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
