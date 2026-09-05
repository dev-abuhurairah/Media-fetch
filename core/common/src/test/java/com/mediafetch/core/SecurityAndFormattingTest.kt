package com.mediafetch.core

import com.mediafetch.core.common.Formatters
import com.mediafetch.core.security.FilenameSanitizer
import com.mediafetch.core.security.UrlValidator
import org.junit.Assert.*
import org.junit.Test

class SecurityAndFormattingTest {

    @Test
    fun testUrlValidatorWithValidUrls() {
        val tiktokResult = UrlValidator.validate("https://www.tiktok.com/@user/video/123456789")
        assertTrue(tiktokResult.isValid)

        val igResult = UrlValidator.validate("https://www.instagram.com/reel/C8_123abc/")
        assertTrue(igResult.isValid)

        val ytResult = UrlValidator.validate("https://youtu.be/dQw4w9WgXcQ")
        assertTrue(ytResult.isValid)

        val fbResult = UrlValidator.validate("https://www.facebook.com/watch/?v=123456")
        assertTrue(fbResult.isValid)
    }

    @Test
    fun testUrlValidatorBlocksSsrfAndLocalhost() {
        val localhostResult = UrlValidator.validate("http://localhost:8080/secret")
        assertFalse(localhostResult.isValid)
        assertTrue(localhostResult.errorMessage!!.contains("Local or private network"))

        val privateIpResult = UrlValidator.validate("http://192.168.1.1/admin")
        assertFalse(privateIpResult.isValid)

        val loopbackResult = UrlValidator.validate("http://127.0.0.1:3000")
        assertFalse(loopbackResult.isValid)
    }

    @Test
    fun testUrlValidatorRejectsMalformed() {
        val emptyResult = UrlValidator.validate("")
        assertFalse(emptyResult.isValid)

        val invalidProtocol = UrlValidator.validate("ftp://example.com/file.mp4")
        assertFalse(invalidProtocol.isValid)
    }

    @Test
    fun testFilenameSanitizerPreventsPathTraversal() {
        val maliciousName = "../../etc/passwd"
        val sanitized = FilenameSanitizer.sanitize(maliciousName, "fallback", "mp4")

        assertFalse(sanitized.contains(".."))
        assertFalse(sanitized.contains("/"))
        assertTrue(sanitized.endsWith(".mp4"))
    }

    @Test
    fun testFilenameSanitizerStripsIllegalCharacters() {
        val illegalName = "Cool Video: Episode 1 <HD> *Special*?|"
        val sanitized = FilenameSanitizer.sanitize(illegalName, "fallback", "mp4")

        assertFalse(sanitized.contains(":"))
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
        assertFalse(sanitized.contains("*"))
        assertFalse(sanitized.contains("?"))
        assertFalse(sanitized.contains("|"))
    }

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
