package com.mediafetch.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testFormattersBytes() {
        assertEquals("0 B", Formatters.formatBytes(0))
        assertEquals("1 KB", Formatters.formatBytes(1024))
        assertEquals("1.5 MB", Formatters.formatBytes((1.5 * 1024 * 1024).toLong()))
        assertEquals("2 GB", Formatters.formatBytes(2L * 1024 * 1024 * 1024))
    }

    @Test
    fun testFormattersDuration() {
        assertEquals("00:00", Formatters.formatDuration(0))
        assertEquals("00:45", Formatters.formatDuration(45))
        assertEquals("02:30", Formatters.formatDuration(150))
        assertEquals("01:05:00", Formatters.formatDuration(3900))
    }
}