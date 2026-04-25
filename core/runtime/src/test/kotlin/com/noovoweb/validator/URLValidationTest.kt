package com.noovoweb.validator

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for URL validation using Java's URL class (no ReDoS risk).
 *
 * These tests ensure URL validation is:
 * - Secure (no regex, no ReDoS)
 * - Correct (proper URL parsing)
 * - Fast (no backtracking)
 */
class URLValidationTest {
    @Test
    fun `test valid HTTP URLs`() {
        val validURLs =
            listOf(
                "http://example.com",
                "http://www.example.com",
                "http://example.com/",
                "http://example.com/path",
                "http://example.com/path/to/resource",
                "http://example.com/path?query=value",
                "http://example.com/path?query=value&other=123",
                "http://example.com:8080",
                "http://example.com:8080/path",
                "http://subdomain.example.com",
                "http://sub.domain.example.com",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL: $url",
            )
        }
    }

    @Test
    fun `test valid HTTPS URLs`() {
        val validURLs =
            listOf(
                "https://example.com",
                "https://www.example.com",
                "https://example.com/",
                "https://example.com/secure/path",
                "https://example.com:443",
                "https://example.com:8443/path",
                "https://api.example.com/v1/resource",
                "https://example.com/path?param=value#fragment",
                "https://user@example.com/path",
                "https://example.com/path%20with%20spaces",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL: $url",
            )
        }
    }

    @Test
    fun `test invalid URLs - wrong protocol`() {
        val invalidURLs =
            listOf(
                "ftp://example.com", // FTP not allowed
                "file:///etc/passwd", // File protocol
                "javascript:alert(1)", // JavaScript (XSS)
                "data:text/html,<script>", // Data URL
                "mailto:user@example.com", // Mailto
                "ssh://example.com", // SSH
                "telnet://example.com", // Telnet
                "ws://example.com", // WebSocket (not HTTP/S)
                "wss://example.com", // Secure WebSocket (not HTTP/S)
            )

        invalidURLs.forEach { url ->
            assertFalse(
                ValidationPatterns.isValidURL(url),
                "Expected invalid URL (wrong protocol): $url",
            )
        }
    }

    @Test
    fun `test invalid URLs - malformed`() {
        val invalidURLs =
            listOf(
                "", // Empty
                "not-a-url", // No protocol
                "http://", // No host
                "://example.com", // No protocol
                "http:/example.com", // Missing slash
                "http:example.com", // Missing slashes
                "example.com", // No protocol
                "www.example.com", // No protocol
                "http://", // Incomplete
                "https://", // Incomplete
                "http:// example.com", // Space in URL
                "http://exam ple.com", // Space in host
            )

        invalidURLs.forEach { url ->
            assertFalse(
                ValidationPatterns.isValidURL(url),
                "Expected invalid URL (malformed): $url",
            )
        }
    }

    @Test
    fun `test URLs with special characters`() {
        // Valid URLs with properly encoded special characters
        val validURLs =
            listOf(
                "https://example.com/path%20with%20spaces",
                "https://example.com/path?query=hello%20world",
                "https://example.com/path#fragment",
                "https://example.com/path?a=1&b=2&c=3",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL: $url",
            )
        }
    }

    @Test
    fun `test URLs with ports`() {
        val validURLs =
            listOf(
                "http://example.com:80",
                "http://example.com:8080",
                "https://example.com:443",
                "https://example.com:8443",
                "http://localhost:3000",
                "http://127.0.0.1:8080",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL with port: $url",
            )
        }
    }

    @Test
    fun `test URLs with authentication`() {
        val validURLs =
            listOf(
                "http://user@example.com",
                "http://user:pass@example.com",
                "https://admin@api.example.com/resource",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL with auth: $url",
            )
        }
    }

    @Test
    fun `test localhost and IP addresses`() {
        val validURLs =
            listOf(
                "http://localhost",
                "http://localhost:8080",
                "http://127.0.0.1",
                "http://127.0.0.1:8080",
                "http://192.168.1.1",
                "http://10.0.0.1:3000",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL: $url",
            )
        }
    }

    @Test
    fun `test security - no ReDoS with malicious input`() {
        // Patterns that would cause ReDoS with regex
        val maliciousInputs =
            listOf(
                "http://" + "a".repeat(10000),
                "http://example.com/" + "x".repeat(10000),
                "http://example.com?" + "q=".repeat(1000),
                "http://" + "/".repeat(10000),
            )

        maliciousInputs.forEach { input ->
            // Should complete quickly without hanging
            val startTime = System.currentTimeMillis()
            val result = ValidationPatterns.isValidURL(input)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            // Result doesn't matter, just checking it completes fast
            assertTrue(duration < 100, "Validation should be fast (< 100ms), took ${duration}ms for input length ${input.length}")
        }
    }

    @Test
    fun `test case sensitivity`() {
        // Protocol should be case-insensitive
        assertTrue(ValidationPatterns.isValidURL("HTTP://example.com"))
        assertTrue(ValidationPatterns.isValidURL("HTTPS://example.com"))
        assertTrue(ValidationPatterns.isValidURL("HtTp://example.com"))
        assertTrue(ValidationPatterns.isValidURL("HtTpS://example.com"))
    }

    @Test
    fun `test IPv6 in URL`() {
        // URLs with IPv6 addresses
        val validURLs =
            listOf(
                "http://[::1]",
                "http://[::1]:8080",
                "http://[2001:db8::1]",
                "https://[2001:db8::1]:8443/path",
            )

        validURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid URL with IPv6: $url",
            )
        }
    }

    @Test
    fun `test edge cases`() {
        // Very long but valid URL
        val longPath = "a".repeat(1000)
        assertTrue(ValidationPatterns.isValidURL("http://example.com/$longPath"))

        // URL with many query parameters
        val manyParams = (1..100).joinToString("&") { "param$it=value$it" }
        assertTrue(ValidationPatterns.isValidURL("http://example.com/path?$manyParams"))
    }

    @Test
    fun `test whitespace handling`() {
        // URLs with whitespace should be invalid
        assertFalse(ValidationPatterns.isValidURL(" http://example.com"))
        assertFalse(ValidationPatterns.isValidURL("http://example.com "))
        assertFalse(ValidationPatterns.isValidURL(" http://example.com "))
        assertFalse(ValidationPatterns.isValidURL("http:// example.com"))
        assertFalse(ValidationPatterns.isValidURL("http://example .com"))
    }

    @Test
    fun `test real-world URLs`() {
        val realWorldURLs =
            listOf(
                "https://www.google.com",
                "https://github.com/username/repo",
                "https://api.github.com/users/username",
                "http://example.com:8080/api/v1/users?limit=10&offset=0",
                "https://docs.oracle.com/javase/8/docs/api/java/net/URL.html",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "https://stackoverflow.com/questions/12345/how-to-validate-url",
            )

        realWorldURLs.forEach { url ->
            assertTrue(
                ValidationPatterns.isValidURL(url),
                "Expected valid real-world URL: $url",
            )
        }
    }

    @Test
    fun `test potential XSS vectors are rejected`() {
        val xssVectors =
            listOf(
                "javascript:alert(1)",
                "data:text/html,<script>alert(1)</script>",
                "vbscript:msgbox(1)",
            )

        xssVectors.forEach { vector ->
            assertFalse(
                ValidationPatterns.isValidURL(vector),
                "XSS vector should be rejected: $vector",
            )
        }
    }
}
