package com.mediafetch.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Platform(
    val displayName: String,
    val brandColorHex: Long,
    val domains: List<String>
) {
    TIKTOK(
        displayName = "TikTok",
        brandColorHex = 0xFF000000,
        domains = listOf("tiktok.com", "vm.tiktok.com", "vt.tiktok.com")
    ),
    INSTAGRAM(
        displayName = "Instagram",
        brandColorHex = 0xFFE1306C,
        domains = listOf("instagram.com", "instagr.am")
    ),
    YOUTUBE(
        displayName = "YouTube",
        brandColorHex = 0xFFFF0000,
        domains = listOf("youtube.com", "youtu.be", "m.youtube.com")
    ),
    FACEBOOK(
        displayName = "Facebook",
        brandColorHex = 0xFF1877F2,
        domains = listOf("facebook.com", "fb.watch", "m.facebook.com", "fb.com")
    ),
    UNKNOWN(
        displayName = "Universal Web",
        brandColorHex = 0xFF6200EE,
        domains = emptyList()
    );

    companion object {
        fun fromUrl(url: String): Platform {
            val lowercaseUrl = url.lowercase()
            return entries.firstOrNull { platform ->
                platform.domains.any { domain -> lowercaseUrl.contains(domain) }
            } ?: UNKNOWN
        }
    }
}
