package com.mediafetch.core.common

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object Formatters {
    private val decimalFormat = DecimalFormat("#,##0.#")

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return "${decimalFormat.format(value)} ${units[digitGroups]}"
    }

    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 KB/s"
        return "${formatBytes(bytesPerSec)}/s"
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
        }
    }

    fun formatRemainingTime(seconds: Long): String {
        if (seconds <= 0) return "Calculating..."
        if (seconds < 60) return "${seconds}s left"
        val minutes = seconds / 60
        val remainingSecs = seconds % 60
        return if (minutes < 60) {
            "${minutes}m ${remainingSecs}s left"
        } else {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            "${hours}h ${remMinutes}m left"
        }
    }

    fun formatDate(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }
}
