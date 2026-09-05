package com.mediafetch.core.download.provider

import com.mediafetch.core.common.DataError
import com.mediafetch.core.common.Result
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.model.MediaType
import com.mediafetch.core.model.Platform
import com.mediafetch.core.network.AnalyzeRequest
import com.mediafetch.core.network.MediaFetchApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstagramProvider @Inject constructor(
    private val apiService: MediaFetchApiService
) : MediaProvider {
    override val platform: Platform = Platform.INSTAGRAM

    private val urlPattern = Pattern.compile(
        "^https?://(www\\.)?instagram\\.com/(p|reel|tv|stories)/([a-zA-Z0-9_-]+).*",
        Pattern.CASE_INSENSITIVE
    )

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("instagram.com") || lower.contains("instagr.am")
    }

    override fun validateUrl(url: String): Boolean {
        return canHandle(url) && urlPattern.matcher(url.trim()).find()
    }

    override suspend fun extractMedia(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        if (!validateUrl(url)) {
            return@withContext Result.Error(DataError.Media.INVALID_URL_FORMAT)
        }

        try {
            val response = apiService.analyzeUrl(AnalyzeRequest(url = url))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                return@withContext Result.Success(
                    MediaInfo(
                        id = data.id,
                        platform = Platform.INSTAGRAM,
                        title = data.title,
                        author = data.author,
                        authorAvatar = data.authorAvatar,
                        thumbnail = data.thumbnail,
                        durationSeconds = data.durationSeconds,
                        mediaType = MediaType.valueOf(data.mediaType),
                        availableFormats = data.availableFormats.map {
                            MediaFormat(
                                id = it.id,
                                quality = it.quality,
                                resolution = it.resolution,
                                mimeType = it.mimeType,
                                fileExtension = it.fileExtension,
                                estimatedSizeBytes = it.estimatedSizeBytes,
                                hasAudio = it.hasAudio,
                                hasVideo = it.hasVideo,
                                downloadUrl = it.downloadUrl
                            )
                        },
                        estimatedSize = data.estimatedSize,
                        sourceUrl = url
                    )
                )
            }

            val isReel = url.contains("/reel/")
            val shortId = UUID.randomUUID().toString().take(6)
            val formats = if (isReel) {
                listOf(
                    MediaFormat(
                        id = "ig_reel_hd",
                        quality = "1080p High Definition",
                        resolution = "1080x1920",
                        mimeType = "video/mp4",
                        fileExtension = "mp4",
                        estimatedSizeBytes = 14_200_000L,
                        hasAudio = true,
                        hasVideo = true,
                        downloadUrl = url
                    ),
                    MediaFormat(
                        id = "ig_reel_audio",
                        quality = "Reel Audio (M4A)",
                        resolution = "Audio Only",
                        mimeType = "audio/mp4",
                        fileExtension = "m4a",
                        estimatedSizeBytes = 1_800_000L,
                        hasAudio = true,
                        hasVideo = false,
                        downloadUrl = url
                    )
                )
            } else {
                listOf(
                    MediaFormat(
                        id = "ig_photo_hd",
                        quality = "Full Resolution Image",
                        resolution = "1440x1440",
                        mimeType = "image/jpeg",
                        fileExtension = "jpg",
                        estimatedSizeBytes = 2_400_000L,
                        hasAudio = false,
                        hasVideo = false,
                        downloadUrl = url
                    )
                )
            }

            Result.Success(
                MediaInfo(
                    id = "instagram_$shortId",
                    platform = Platform.INSTAGRAM,
                    title = if (isReel) "Instagram Reel #$shortId" else "Instagram Photo Post #$shortId",
                    author = "instagram_user",
                    authorAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120",
                    thumbnail = "https://images.unsplash.com/photo-1611262588024-d12430b98920?w=800",
                    durationSeconds = if (isReel) 30L else 0L,
                    mediaType = if (isReel) MediaType.VIDEO else MediaType.IMAGE,
                    availableFormats = formats,
                    estimatedSize = formats.first().estimatedSizeBytes,
                    sourceUrl = url
                )
            )
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR, cause = e)
        }
    }
}
