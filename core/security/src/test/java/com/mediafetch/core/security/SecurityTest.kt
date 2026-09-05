package com.mediafetch.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityTest {

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
}