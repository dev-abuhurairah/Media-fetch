package com.mediafetch.core.download.provider

import com.mediafetch.core.common.DataError
import com.mediafetch.core.common.Result
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.model.Platform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaProviderRegistry @Inject constructor(
    private val tikTokProvider: TikTokProvider,
    private val instagramProvider: InstagramProvider,
    private val youTubeProvider: YouTubeProvider,
    private val facebookProvider: FacebookProvider
) {
    private val providers: List<MediaProvider> by lazy {
        listOf(
            tikTokProvider,
            instagramProvider,
            youTubeProvider,
            facebookProvider
        )
    }

    fun findProvider(url: String): MediaProvider? {
        return providers.firstOrNull { it.canHandle(url) }
    }

    suspend fun extractMedia(url: String): Result<MediaInfo> {
        val provider = findProvider(url)
            ?: return Result.Error(DataError.Media.UNSUPPORTED_URL, "This link isn't supported.")

        if (!provider.validateUrl(url)) {
            return Result.Error(DataError.Media.INVALID_URL_FORMAT, "The link format is invalid for ${provider.platform.displayName}.")
        }

        return provider.extractMedia(url)
    }

    fun detectPlatform(url: String): Platform {
        return findProvider(url)?.platform ?: Platform.UNKNOWN
    }
}
