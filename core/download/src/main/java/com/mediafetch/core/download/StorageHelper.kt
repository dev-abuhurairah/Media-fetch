package com.mediafetch.core.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mediafetch.core.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class Destination(
        val uri: Uri,
        val outputStream: OutputStream
    )

    fun createMediaDestination(
        fileName: String,
        mimeType: String,
        mediaType: MediaType
    ): Destination? {
        val resolver = context.contentResolver
        val collection: Uri = when (mediaType) {
            MediaType.VIDEO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            MediaType.IMAGE, MediaType.CAROUSEL -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            MediaType.AUDIO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        }

        val relativePath = when (mediaType) {
            MediaType.VIDEO -> "${Environment.DIRECTORY_MOVIES}/MediaFetch"
            MediaType.IMAGE, MediaType.CAROUSEL -> "${Environment.DIRECTORY_PICTURES}/MediaFetch"
            MediaType.AUDIO -> "${Environment.DIRECTORY_MUSIC}/MediaFetch"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val itemUri = resolver.insert(collection, contentValues) ?: return null
        val outputStream = resolver.openOutputStream(itemUri, "w") ?: return null

        return Destination(uri = itemUri, outputStream = outputStream)
    }

    fun markDownloadComplete(itemUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            context.contentResolver.update(itemUri, contentValues, null, null)
        }
    }

    fun deleteMedia(itemUri: Uri): Boolean {
        return try {
            context.contentResolver.delete(itemUri, null, null) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getAppCacheDir(): File = context.cacheDir
}
