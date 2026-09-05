package com.mediafetch.core.security

import java.net.URI
import java.util.regex.Pattern

object UrlValidator {
    private val URL_PATTERN = Pattern.compile(
        "^(https?)://([a-zA-Z0-9.-]+)(:[0-9]+)?(/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    private val BLOCKED_HOSTS = listOf(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "::1"
    )

    data class ValidationResult(
        val isValid: Boolean,
        val sanitizedUrl: String? = null,
        val errorMessage: String? = null
    )

    fun validate(rawUrl: String?): ValidationResult {
        if (rawUrl.isNullOrBlank()) {
            return ValidationResult(isValid = false, errorMessage = "URL cannot be empty.")
        }

        val trimmed = rawUrl.trim()
        val matcher = URL_PATTERN.matcher(trimmed)
        if (!matcher.matches()) {
            return ValidationResult(isValid = false, errorMessage = "Please enter a valid HTTP or HTTPS URL.")
        }

        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") {
                return ValidationResult(isValid = false, errorMessage = "Only HTTP and HTTPS protocols are supported.")
            }

            val host = uri.host?.lowercase() ?: return ValidationResult(isValid = false, errorMessage = "URL host is invalid.")

            // SSRF & Local network access protection
            if (BLOCKED_HOSTS.contains(host) || isPrivateOrLoopbackIp(host)) {
                return ValidationResult(isValid = false, errorMessage = "Local or private network addresses are not permitted.")
            }

            ValidationResult(isValid = true, sanitizedUrl = trimmed)
        } catch (e: Exception) {
            ValidationResult(isValid = false, errorMessage = "Malformed URL: ${e.message}")
        }
    }

    private fun isPrivateOrLoopbackIp(host: String): Boolean {
        if (host.startsWith("10.") || host.startsWith("192.168.")) return true
        if (host.startsWith("169.254.")) return true // link-local
        if (host.matches(Regex("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*"))) return true
        return false
    }
}
