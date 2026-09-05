package com.mediafetch.core.download.provider

import com.mediafetch.core.common.Result
import com.mediafetch.core.model.MediaInfo
import com.mediafetch.core.model.Platform

interface MediaProvider {
    val platform: Platform
    
    fun canHandle(url: String): Boolean
    
    fun validateUrl(url: String): Boolean
    
    suspend fun extractMedia(url: String): Result<MediaInfo>
}
