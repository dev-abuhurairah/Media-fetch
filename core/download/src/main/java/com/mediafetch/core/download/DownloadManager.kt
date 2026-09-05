package com.mediafetch.core.download

import android.content.Context
import android.net.Uri
import com.mediafetch.core.common.DispatchersModule
import com.mediafetch.core.common.IoDispatcher
import com.mediafetch.core.database.DownloadDao
import com.mediafetch.core.database.DownloadEntity
import com.mediafetch.core.database.MediaLibraryDao
import com.mediafetch.core.database.MediaLibraryEntity
import com.mediafetch.core.database.toDomain
import com.mediafetch.core.database.toEntity
import com.mediafetch.core.model.DownloadItem
import com.mediafetch.core.model.DownloadState
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.security.FilenameSanitizer
import com.mediafetch.core.security.SecurityPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val mediaLibraryDao: MediaLibraryDao,
    private val storageHelper: StorageHelper,
    private val securityPreferences: SecurityPreferences,
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val managerScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads().map { list ->
        list.map { it.toDomain() }
    }

    val activeDownloads: Flow<List<DownloadItem>> = downloadDao.getDownloadsByState(
        listOf(DownloadState.DOWNLOADING, DownloadState.QUEUED)
    ).map { list -> list.map { it.toDomain() } }

    val completedDownloads: Flow<List<DownloadItem>> = downloadDao.getDownloadsByState(
        listOf(DownloadState.COMPLETED)
    ).map { list -> list.map { it.toDomain() } }

    suspend fun enqueueDownload(mediaInfo: MediaInfo, format: MediaFormat): String {
        val downloadId = UUID.randomUUID().toString()
        val safeFileName = FilenameSanitizer.sanitize(
            originalName = mediaInfo.title,
            fallbackBase = "media_${mediaInfo.platform.name.lowercase()}",
            extension = format.fileExtension
        )

        val downloadItem = DownloadItem(
            id = downloadId,
            mediaInfo = mediaInfo,
            selectedFormat = format,
            state = DownloadState.QUEUED,
            totalBytes = format.estimatedSizeBytes,
            localFilePath = safeFileName
        )

        downloadDao.insertOrUpdate(downloadItem.toEntity())
        processQueue()
        return downloadId
    }

    private fun processQueue() {
        managerScope.launch {
            val maxConcurrent = securityPreferences.maxConcurrentDownloads.first()
            val runningCount = activeJobs.size
            if (runningCount >= maxConcurrent) return@launch

            val queuedEntities = downloadDao.getDownloadsByState(listOf(DownloadState.QUEUED)).first()
            for (entity in queuedEntities) {
                if (activeJobs.size >= maxConcurrent) break
                if (!activeJobs.containsKey(entity.id)) {
                    startDownloadJob(entity.toDomain())
                }
            }
        }
    }

    private fun startDownloadJob(item: DownloadItem) {
        val job = managerScope.launch {
            executeDownload(item)
        }
        activeJobs[item.id] = job
        job.invokeOnCompletion {
            activeJobs.remove(item.id)
            processQueue()
        }
    }

    private suspend fun executeDownload(item: DownloadItem) = withContext(ioDispatcher) {
        val targetUrl = item.selectedFormat.downloadUrl ?: item.mediaInfo.sourceUrl
        val tempFile = File(storageHelper.getAppCacheDir(), "${item.id}.tmp")

        var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L

        // Update to DOWNLOADING state
        downloadDao.updateProgress(
            id = item.id,
            state = DownloadState.DOWNLOADING,
            progress = if (item.totalBytes > 0) downloadedBytes.toFloat() / item.totalBytes else 0f,
            downloaded = downloadedBytes,
            total = item.totalBytes,
            speed = 0L,
            eta = 0L,
            updatedAt = System.currentTimeMillis()
        )

        try {
            val requestBuilder = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "*/*")
            if (downloadedBytes > 0) {
                requestBuilder.header("Range", "bytes=$downloadedBytes-")
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful && response.code != 206) {
                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.FAILED,
                    progress = 0f,
                    downloaded = downloadedBytes,
                    total = item.totalBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
                return@withContext
            }

            val responseBody = response.body ?: throw IllegalStateException("Empty response body")
            val contentType = response.header("Content-Type").orEmpty()
            if (contentType.contains("text/html", ignoreCase = true) && !item.selectedFormat.mimeType.contains("html", ignoreCase = true)) {
                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.FAILED,
                    progress = 0f,
                    downloaded = downloadedBytes,
                    total = item.totalBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
                return@withContext
            }

            val contentLength = responseBody.contentLength()
            val totalBytes = if (response.code == 206) contentLength + downloadedBytes else contentLength
            val effectiveTotal = if (totalBytes > 0) totalBytes else item.totalBytes

            val append = (response.code == 206 && downloadedBytes > 0)
            val output = FileOutputStream(tempFile, append)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            val inputStream = responseBody.byteStream()
            var lastUpdateTime = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L
            var currentSpeed = 0L

            inputStream.use { input ->
                output.use { out ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        bytesSinceLastUpdate += bytesRead

                        val now = System.currentTimeMillis()
                        val delta = now - lastUpdateTime
                        if (delta >= 600) { // Update throttled to ~600ms for UI performance
                            currentSpeed = (bytesSinceLastUpdate * 1000) / delta
                            val remainingBytes = (effectiveTotal - downloadedBytes).coerceAtLeast(0L)
                            val remainingSeconds = if (currentSpeed > 0) remainingBytes / currentSpeed else 0L
                            val progress = if (effectiveTotal > 0) (downloadedBytes.toFloat() / effectiveTotal).coerceIn(0f, 1f) else 0f

                            downloadDao.updateProgress(
                                id = item.id,
                                state = DownloadState.DOWNLOADING,
                                progress = progress,
                                downloaded = downloadedBytes,
                                total = effectiveTotal,
                                speed = currentSpeed,
                                eta = remainingSeconds,
                                updatedAt = now
                            )

                            lastUpdateTime = now
                            bytesSinceLastUpdate = 0L
                        }
                    }
                }
            }

            // Transfer from temporary cache into MediaStore Scoped Storage
            val destination = storageHelper.createMediaDestination(
                fileName = item.localFilePath ?: "media_${System.currentTimeMillis()}.${item.selectedFormat.fileExtension}",
                mimeType = item.selectedFormat.mimeType,
                mediaType = item.mediaInfo.mediaType
            )

            if (destination != null) {
                destination.outputStream.use { destOut ->
                    tempFile.inputStream().use { tempIn ->
                        tempIn.copyTo(destOut)
                    }
                }
                storageHelper.markDownloadComplete(destination.uri)
                tempFile.delete()

                // Register into in-app Media Library
                mediaLibraryDao.insert(
                    MediaLibraryEntity(
                        id = item.id,
                        title = item.mediaInfo.title,
                        platform = item.mediaInfo.platform,
                        mediaType = item.mediaInfo.mediaType,
                        author = item.mediaInfo.author,
                        fileUri = destination.uri.toString(),
                        filePath = item.localFilePath ?: "",
                        fileSizeBytes = downloadedBytes,
                        durationSeconds = item.mediaInfo.durationSeconds,
                        thumbnailUri = item.mediaInfo.thumbnail,
                        mimeType = item.selectedFormat.mimeType
                    )
                )

                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.COMPLETED,
                    progress = 1.0f,
                    downloaded = downloadedBytes,
                    total = downloadedBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.FAILED,
                    progress = 0f,
                    downloaded = downloadedBytes,
                    total = downloadedBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            }

        } catch (e: Exception) {
            val isPaused = !activeJobs.containsKey(item.id)
            if (isPaused) {
                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.PAUSED,
                    progress = if (item.totalBytes > 0) downloadedBytes.toFloat() / item.totalBytes else 0f,
                    downloaded = downloadedBytes,
                    total = item.totalBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                downloadDao.updateProgress(
                    id = item.id,
                    state = DownloadState.FAILED,
                    progress = 0f,
                    downloaded = downloadedBytes,
                    total = item.totalBytes,
                    speed = 0L,
                    eta = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun pauseDownload(id: String) {
        val job = activeJobs.remove(id)
        job?.cancel()
        val entity = downloadDao.getDownloadById(id)
        if (entity != null) {
            downloadDao.update(entity.copy(state = DownloadState.PAUSED, speedBytesPerSec = 0L, remainingSeconds = 0L))
        }
        processQueue()
    }

    suspend fun resumeDownload(id: String) {
        val entity = downloadDao.getDownloadById(id) ?: return
        downloadDao.update(entity.copy(state = DownloadState.QUEUED))
        processQueue()
    }

    suspend fun cancelDownload(id: String) {
        val job = activeJobs.remove(id)
        job?.cancel()
        val tempFile = File(storageHelper.getAppCacheDir(), "$id.tmp")
        if (tempFile.exists()) tempFile.delete()
        downloadDao.deleteById(id)
        processQueue()
    }

    suspend fun retryDownload(id: String) {
        val entity = downloadDao.getDownloadById(id) ?: return
        downloadDao.update(entity.copy(state = DownloadState.QUEUED, progress = 0f, downloadedBytes = 0L))
        processQueue()
    }
}
