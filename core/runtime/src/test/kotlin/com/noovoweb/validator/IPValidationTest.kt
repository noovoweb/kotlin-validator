package com.noovoweb.validator

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for IP address validation using InetAddress (no ReDoS risk).
 * 
 * These tests ensure IPv4 and IPv6 validation is:
 * - Secure (no regex, no ReDoS)
 * - Correct (handles all valid formats)
 * - Fast (no backtracking)
 */
class IPValidationTest {

    @Test
    fun `test valid IPv4 addresses`() {
        val validIPv4 = listOf(
            "0.0.0.0",
            "127.0.0.1",
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "255.255.255.255",
            "8.8.8.8",
            "1.1.1.1"
        )

        validIPv4.forEach { ip ->
            assertTrue(
                ValidationPatterns.isValidIPv4(ip),
                "Expected valid IPv4: $ip"
            )
        }
    }

    @Test
    fun `test invalid IPv4 addresses`() {
        val invalidIPv4 = listOf(
            "256.1.1.1",           // Out of range
            "1.256.1.1",           // Out of range
            "1.1.256.1",           // Out of range
            "1.1.1.256",           // Out of range
            "999.999.999.999",     // Way out of range
            "1.1.1",               // Too few octets
            "1.1.1.1.1",           // Too many octets
            "a.b.c.d",             // Letters
            "1.2.3.4.5",           // Too many parts
            "1.2.3",               // Too few parts
            "",                    // Empty
            "localhost",           // Hostname
            "192.168.1",           // Incomplete
            "192.168.1.1.1"        // Too many octets
        )

        invalidIPv4.forEach { ip ->
            assertFalse(
                ValidationPatterns.isValidIPv4(ip),
                "Expected invalid IPv4: $ip"
            )
        }
    }

    @Test
    fun `test valid IPv6 addresses`() {
        val validIPv6 = listOf(
            // Full format
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
            "2001:0DB8:85A3:0000:0000:8A2E:0370:7334",  // Uppercase
            
            // Compressed format
            "2001:db8:85a3::8a2e:370:7334",
            "2001:db8::1",
            "::1",                                       // Loopback
            "::",                                        // All zeros
            
            // Link-local
            "fe80::1",
            "fe80::200:5aee:feaa:20a2",
            
            // Other valid formats
            "2001:db8:85a3:0:0:8a2e:370:7334",
            "2001:0db8:0000:0042:0000:8a2e:0370:7334"
            
            // Note: IPv4-mapped IPv6 (::ffff:192.0.2.1) parsed by InetAddress as IPv4
        )

        validIPv6.forEach { ip ->
            assertTrue(
                ValidationPatterns.isValidIPv6(ip),
                "Expected valid IPv6: $ip"
            )
        }
    }

    @Test
    fun `test invalid IPv6 addresses`() {
        val invalidIPv6 = listOf(
            "gggg::1",             // Invalid hex
            ":::",                 // Too many colons
            "2001:db8:85a3::8a2e:370g:7334",  // Invalid character
            "2001:db8:85a3::8a2e::7334",      // Double compression
            "2001:db8:85a3:8a2e:370:7334",    // Too few groups
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334:extra",  // Too many groups
            "",                    // Empty
            "localhost",           // Hostname
            "192.168.1.1",         // IPv4 (not IPv6)
            "::ffff:999.0.2.1",    // Invalid IPv4 part
            "12345::1"             // Group too long
        )

        invalidIPv6.forEach { ip ->
            assertFalse(
                ValidationPatterns.isValidIPv6(ip),
                "Expected invalid IPv6: $ip"
            )
        }
    }

    @Test
    fun `test isValidIP accepts both IPv4 and IPv6`() {
        val validIPs = listOf(
            // IPv4
            "192.168.1.1",
            "8.8.8.8",
            
            // IPv6
            "2001:db8::1",
            "::1",
            "fe80::1"
        )

        validIPs.forEach { ip ->
            assertTrue(
                ValidationPatterns.isValidIP(ip),
                "Expected valid IP: $ip"
            )
        }
    }

    @Test
    fun `test isValidIP rejects invalid addresses`() {
        val invalidIPs = listOf(
            "256.1.1.1",           // Invalid IPv4
            "gggg::1",             // Invalid IPv6
            "localhost",           // Hostname
            "",                    // Empty
            "not-an-ip"
        )

        invalidIPs.forEach { ip ->
            assertFalse(
                ValidationPatterns.isValidIP(ip),
                "Expected invalid IP: $ip"
            )
        }
    }

    @Test
    fun `test IPv6 is not recognized as IPv4`() {
        val ipv6Addresses = listOf(
            "2001:db8::1",
            "::1",
            "fe80::1"
        )

        ipv6Addresses.forEach { ip ->
            assertFalse(
                ValidationPatterns.isValidIPv4(ip),
                "IPv6 should not be valid IPv4: $ip"
            )
            assertTrue(
                ValidationPatterns.isValidIPv6(ip),
                "Should be valid IPv6: $ip"
            )
        }
    }

    @Test
    fun `test IPv4 is not recognized as IPv6`() {
        val ipv4Addresses = listOf(
            "192.168.1.1",
            "8.8.8.8",
            "127.0.0.1"
        )

        ipv4Addresses.forEach { ip ->
            assertTrue(
                ValidationPatterns.isValidIPv4(ip),
                "Should be valid IPv4: $ip"
            )
            assertFalse(
                ValidationPatterns.isValidIPv6(ip),
                "IPv4 should not be valid IPv6: $ip"
            )
        }
    }

    @Test
    fun `test security - no ReDoS with malicious input`() {
        // Patterns that would cause ReDoS with regex
        val maliciousInputs = listOf(
            ":" + ":".repeat(10000),
            "a" + "a".repeat(10000),
            "." + ".".repeat(10000),
            "1." + "1.".repeat(1000)
        )

        maliciousInputs.forEach { input ->
            // Should complete quickly without hanging
            val startTime = System.currentTimeMillis()
            val result = ValidationPatterns.isValidIP(input)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            assertFalse(result, "Malicious input should be invalid")
            assertTrue(duration < 100, "Validation should be fast (< 100ms), took ${duration}ms")
        }
    }

    @Test
    fun `test edge cases`() {
        // Localhost variations
        assertTrue(ValidationPatterns.isValidIPv4("127.0.0.1"))
        assertTrue(ValidationPatterns.isValidIPv6("::1"))
        
        // All zeros
        assertTrue(ValidationPatterns.isValidIPv4("0.0.0.0"))
        assertTrue(ValidationPatterns.isValidIPv6("::"))
        
        // Max values
        assertTrue(ValidationPatterns.isValidIPv4("255.255.255.255"))
        assertTrue(ValidationPatterns.isValidIPv6("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
    }

    @Test
    fun `test whitespace handling`() {
        // InetAddress.getByName() trims whitespace, but we want exact matches
        assertFalse(ValidationPatterns.isValidIPv4(" 192.168.1.1"))
        assertFalse(ValidationPatterns.isValidIPv4("192.168.1.1 "))
        assertFalse(ValidationPatterns.isValidIPv4(" 192.168.1.1 "))
        
        // These should be invalid (whitespace in input)
        assertFalse(ValidationPatterns.isValidIPv6(" ::1"))
        assertFalse(ValidationPatterns.isValidIPv6("::1 "))
    }
}
