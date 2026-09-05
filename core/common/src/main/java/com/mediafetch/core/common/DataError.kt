package com.mediafetch.core.common

sealed interface DataError {
    enum class Network : DataError {
        NO_INTERNET,
        TIMEOUT,
        SERVER_ERROR,
        PAYLOAD_TOO_LARGE,
        UNKNOWN
    }

    enum class Media : DataError {
        UNSUPPORTED_URL,
        INVALID_URL_FORMAT,
        CONTENT_UNAVAILABLE,
        PRIVATE_OR_RESTRICTED,
        AUTHENTICATION_REQUIRED,
        DRM_PROTECTED,
        EXTRACTION_FAILED,
        RATE_LIMITED
    }

    enum class Storage : DataError {
        INSUFFICIENT_STORAGE,
        PERMISSION_DENIED,
        FILE_CORRUPTED,
        PATH_TRAVERSAL_DETECTED,
        DUPLICATE_FILE
    }

    data class Custom(val code: String, val message: String) : DataError
}

fun DataError.toUserFacingMessage(): String {
    return when (this) {
        DataError.Network.NO_INTERNET -> "Check your internet connection and try again."
        DataError.Network.TIMEOUT -> "Request timed out. Please try again."
        DataError.Network.SERVER_ERROR -> "The service is temporarily unavailable. Please try again later."
        DataError.Network.PAYLOAD_TOO_LARGE -> "Media file exceeds the maximum allowed size."
        DataError.Network.UNKNOWN -> "A network error occurred. Please verify your connection."

        DataError.Media.UNSUPPORTED_URL -> "This link isn't supported."
        DataError.Media.INVALID_URL_FORMAT -> "The link format is invalid. Please check the URL and try again."
        DataError.Media.CONTENT_UNAVAILABLE -> "The requested media is not available through an authorized download method."
        DataError.Media.PRIVATE_OR_RESTRICTED -> "This media is private or restricted by the platform/creator."
        DataError.Media.AUTHENTICATION_REQUIRED -> "This content requires user account authorization."
        DataError.Media.DRM_PROTECTED -> "This media is protected by digital rights management (DRM) and cannot be downloaded."
        DataError.Media.EXTRACTION_FAILED -> "Unable to extract media information from this URL."
        DataError.Media.RATE_LIMITED -> "Too many requests. Please wait a moment before trying again."

        DataError.Storage.INSUFFICIENT_STORAGE -> "Insufficient device storage to complete this download."
        DataError.Storage.PERMISSION_DENIED -> "Storage permission is required to save this media."
        DataError.Storage.FILE_CORRUPTED -> "The downloaded file could not be verified."
        DataError.Storage.PATH_TRAVERSAL_DETECTED -> "Invalid file path requested for security reasons."
        DataError.Storage.DUPLICATE_FILE -> "A file with this name already exists in your library."

        is DataError.Custom -> message
    }
}
