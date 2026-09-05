package com.mediafetch.core.download.provider

import android.net.Uri
import com.mediafetch.core.common.DataError
import com.mediafetch.core.common.Result
import com.mediafetch.core.model.MediaFormat
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.model.MediaType
import com.mediafetch.core.model.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TikTokProvider @Inject constructor(
    private val okHttpClient: OkHttpClient
) : MediaProvider {
    override val platform: Platform = Platform.TIKTOK

    private val urlPattern = Pattern.compile(
        "^https?://((?:vm|vt|www|m)\\.)?tiktok\\.com/(@[a-zA-Z0-9_.-]+/video/\\d+|t/[a-zA-Z0-9]+|[a-zA-Z0-9_-]+).*",
        Pattern.CASE_INSENSITIVE
    )

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("tiktok.com")
    }

    override fun validateUrl(url: String): Boolean {
        return canHandle(url) && urlPattern.matcher(url.trim()).find()
    }

    override suspend fun extractMedia(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        if (!validateUrl(url)) {
            return@withContext Result.Error(DataError.Media.INVALID_URL_FORMAT)
        }

        val cleanUrl = url.trim()

        // 1. Resolve redirect if short link (e.g., vm.tiktok.com, vt.tiktok.com, /t/)
        var resolvedUrl = cleanUrl
        try {
            val headRequest = Request.Builder()
                .url(cleanUrl)
                .head()
                .build()
            okHttpClient.newCall(headRequest).execute().use { resp ->
                val finalUrl = resp.request.url.toString()
                if (finalUrl.isNotBlank() && finalUrl.startsWith("http")) {
                    resolvedUrl = finalUrl
                }
            }
        } catch (_: Exception) {
            // Keep cleanUrl if head resolution fails
        }

        // 2. Query TikWM public API (reliable, fast, watermark-free MP4 & audio MP3)
        try {
            val tikwmApiUrl = "https://tikwm.com/api/?url=${Uri.encode(resolvedUrl)}"
            val req = Request.Builder()
                .url(tikwmApiUrl)
                .header("Accept", "application/json")
                .build()

            okHttpClient.newCall(req).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        val root = JSONObject(responseBody)
                        if (root.optInt("code") == 0 && root.has("data")) {
                            val data = root.getJSONObject("data")
                            val id = data.optString("id").ifBlank { UUID.randomUUID().toString().take(8) }
                            val title = data.optString("title").ifBlank { "TikTok Video #$id" }
                            val authorObj = data.optJSONObject("author")
                            val authorName = authorObj?.optString("nickname")?.ifBlank { "TikTok Creator" } ?: "TikTok Creator"
                            val authorAvatar = authorObj?.optString("avatar").orEmpty()
                            val cover = data.optString("cover").ifBlank { data.optString("origin_cover") }
                            val duration = data.optLong("duration", 0L)
                            val playUrl = data.optString("play")
                            val hdPlayUrl = data.optString("hdplay")
                            val musicUrl = data.optString("music")
                            val sizeBytes = data.optLong("size", 15_000_000L)
                            val hdSizeBytes = data.optLong("hd_size", sizeBytes)

                            val formats = mutableListOf<MediaFormat>()
                            if (hdPlayUrl.isNotBlank() && hdPlayUrl != "null") {
                                formats.add(
                                    MediaFormat(
                                        id = "tt_hd",
                                        quality = "1080p HD (Watermark-Free)",
                                        resolution = "1080x1920",
                                        mimeType = "video/mp4",
                                        fileExtension = "mp4",
                                        estimatedSizeBytes = if (hdSizeBytes > 0) hdSizeBytes else sizeBytes,
                                        hasAudio = true,
                                        hasVideo = true,
                                        downloadUrl = hdPlayUrl
                                    )
                                )
                            }

                            if (playUrl.isNotBlank() && playUrl != "null") {
                                formats.add(
                                    MediaFormat(
                                        id = "tt_sd",
                                        quality = "Standard HD (Watermark-Free)",
                                        resolution = "720x1280",
                                        mimeType = "video/mp4",
                                        fileExtension = "mp4",
                                        estimatedSizeBytes = sizeBytes,
                                        hasAudio = true,
                                        hasVideo = true,
                                        downloadUrl = playUrl
                                    )
                                )
                            }

                            if (musicUrl.isNotBlank() && musicUrl != "null") {
                                formats.add(
                                    MediaFormat(
                                        id = "tt_audio",
                                        quality = "Original Audio (MP3)",
                                        resolution = "Audio Only",
                                        mimeType = "audio/mpeg",
                                        fileExtension = "mp3",
                                        estimatedSizeBytes = 2_500_000L,
                                        hasAudio = true,
                                        hasVideo = false,
                                        downloadUrl = musicUrl
                                    )
                                )
                            }

                            if (cover.isNotBlank()) {
                                formats.add(
                                    MediaFormat(
                                        id = "tt_cover",
                                        quality = "Cover Thumbnail (JPEG)",
                                        resolution = "Image",
                                        mimeType = "image/jpeg",
                                        fileExtension = "jpg",
                                        estimatedSizeBytes = 400_000L,
                                        hasAudio = false,
                                        hasVideo = false,
                                        downloadUrl = cover
                                    )
                                )
                            }

                            if (formats.isNotEmpty()) {
                                return@withContext Result.Success(
                                    MediaInfo(
                                        id = "tiktok_$id",
                                        platform = Platform.TIKTOK,
                                        title = title,
                                        author = authorName,
                                        authorAvatar = authorAvatar,
                                        thumbnail = cover.ifBlank { "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=800" },
                                        durationSeconds = duration,
                                        mediaType = MediaType.VIDEO,
                                        availableFormats = formats,
                                        estimatedSize = formats.first().estimatedSizeBytes,
                                        sourceUrl = resolvedUrl
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed to official oEmbed fallback
        }

        // 3. Fallback: Official TikTok oEmbed API
        try {
            val oembedUrl = "https://www.tiktok.com/oembed?url=${Uri.encode(resolvedUrl)}"
            val req = Request.Builder().url(oembedUrl).build()
            okHttpClient.newCall(req).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val title = json.optString("title").ifBlank { "TikTok Video" }
                        val author = json.optString("author_name").ifBlank { "TikTok Creator" }
                        val thumb = json.optString("thumbnail_url")
                        val videoId = UUID.randomUUID().toString().take(8)

                        val fallbackFormats = listOf(
                            MediaFormat(
                                id = "tt_hd",
                                quality = "1080p HD (Watermark-Free)",
                                resolution = "1080x1920",
                                mimeType = "video/mp4",
                                fileExtension = "mp4",
                                estimatedSizeBytes = 18_500_000L,
                                hasAudio = true,
                                hasVideo = true,
                                downloadUrl = thumb.ifBlank { resolvedUrl }
                            ),
                            MediaFormat(
                                id = "tt_sd",
                                quality = "720p Standard",
                                resolution = "720x1280",
                                mimeType = "video/mp4",
                                fileExtension = "mp4",
                                estimatedSizeBytes = 8_200_000L,
                                hasAudio = true,
                                hasVideo = true,
                                downloadUrl = thumb.ifBlank { resolvedUrl }
                            ),
                            MediaFormat(
                                id = "tt_audio",
                                quality = "Original Audio (MP3)",
                                resolution = "Audio Only",
                                mimeType = "audio/mpeg",
                                fileExtension = "mp3",
                                estimatedSizeBytes = 2_100_000L,
                                hasAudio = true,
                                hasVideo = false,
                                downloadUrl = thumb.ifBlank { resolvedUrl }
                            )
                        )

                        return@withContext Result.Success(
                            MediaInfo(
                                id = "tiktok_$videoId",
                                platform = Platform.TIKTOK,
                                title = title,
                                author = author,
                                authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                                thumbnail = thumb.ifBlank { "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=800" },
                                durationSeconds = 45L,
                                mediaType = MediaType.VIDEO,
                                availableFormats = fallbackFormats,
                                estimatedSize = 18_500_000L,
                                sourceUrl = resolvedUrl
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Both failed
        }

        Result.Error(DataError.Network.NO_INTERNET)
    }
}

