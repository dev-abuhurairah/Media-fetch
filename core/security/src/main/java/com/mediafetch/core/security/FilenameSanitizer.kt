package com.mediafetch.core.security

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FilenameSanitizer {
    private const val MAX_FILENAME_LENGTH = 120
    private val ILLEGAL_CHARS = Regex("[\\\\/:*?\"<>|\\x00-\\x1F]")
    private val CONSECUTIVE_DOTS = Regex("\\.{2,}")

    fun sanitize(originalName: String?, fallbackBase: String = "mediafetch_download", extension: String = "mp4"): String {
        val safeExtension = extension.replace(".", "").trim().lowercase().ifEmpty { "mp4" }
        
        var baseName = originalName?.trim() ?: ""
        if (baseName.isEmpty()) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            return "${fallbackBase}_${timestamp}.$safeExtension"
        }

        // Remove extension if present in originalName to avoid duplication
        if (baseName.endsWith(".$safeExtension", ignoreCase = true)) {
            baseName = baseName.substring(0, baseName.length - (safeExtension.length + 1))
        }

        // Strip path traversal attempts and forbidden characters
        baseName = baseName.replace(CONSECUTIVE_DOTS, "_")
        baseName = baseName.replace(ILLEGAL_CHARS, "_")
        baseName = baseName.replace(Regex("\\s+"), " ").trim()

        // Truncate if exceeds maximum length
        if (baseName.length > MAX_FILENAME_LENGTH) {
            baseName = baseName.substring(0, MAX_FILENAME_LENGTH).trim()
        }

        if (baseName.isEmpty()) {
            baseName = fallbackBase
        }

        val timestampSuffix = System.currentTimeMillis() % 100000
        return "${baseName}_$timestampSuffix.$safeExtension"
    }
}
