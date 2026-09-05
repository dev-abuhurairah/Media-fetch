package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType {
    VIDEO,
    IMAGE,
    CAROUSEL,
    AUDIO
}
