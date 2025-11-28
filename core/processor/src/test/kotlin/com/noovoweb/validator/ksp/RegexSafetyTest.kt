package com.noovoweb.validator.ksp

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Security tests for ReDoS protection in regex pattern validation.
 *
 * These tests ensure that dangerous patterns are caught at compile-time
 * and that the protections cannot be bypassed.
 */
class RegexSafetyTest {
    @Test
    fun `test nested quantifiers are rejected`() {
        // Nested quantifiers are the most dangerous ReDoS patterns
        val dangerousPatterns =
            listOf(
                "(a+)+",
                "(a*)*",
                "(a+)*",
                "(.*)+",
                "(.+)*",
                "([a-z]+)+",
                "(\\d+)+",
                "(a+){2,}",
                "(a*){2,}",
            )

        dangerousPatterns.forEach { pattern ->
            val exception =
                assertThrows<IllegalArgumentException> {
                    RegexSafety.validatePattern(pattern)
                }
            assertTrue(
                exception.message!!.contains("SECURITY ERROR"),
                "Expected security error for pattern: $pattern",
            )
            assertTrue(
                exception.message!!.contains("nested quantifiers"),
                "Expected 'nested quantifiers' in error message for: $pattern",
            )
        }
    }

    @Test
    fun `test multiple wildcards are rejected`() {
        val dangerousPatterns =
            listOf(
                ".*.*",
                ".+.+",
                ".*.*.*",
                ".+.+.+",
            )

        dangerousPatterns.forEach { pattern ->
            val exception =
                assertThrows<IllegalArgumentException> {
                    RegexSafety.validatePattern(pattern)
                }
            assertTrue(
                exception.message!!.contains("SECURITY ERROR"),
                "Expected security error for pattern: $pattern",
            )
        }
    }

    @Test
    fun `test alternation with quantifiers are rejected`() {
        val dangerousPatterns =
            listOf(
                "(a|ab)+",
                "(foo|foobar)*",
                "(x|xy)+",
                "(test|testing)+",
            )

        dangerousPatterns.forEach { pattern ->
            val exception =
                assertThrows<IllegalArgumentException> {
                    RegexSafety.validatePattern(pattern)
                }
            assertTrue(
                exception.message!!.contains("SECURITY ERROR"),
                "Expected security error for pattern: $pattern",
            )
        }
    }

    @Test
    fun `test safe patterns are accepted`() {
        val safePatterns =
            listOf(
                "^[a-z]+$", // Simple character class
                "^\\d{5}$", // Fixed length
                "^[A-Z][a-z]+$", // Anchored
                "^user_.*$", // Single wildcard
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", // Email
                "^https?://[^\\s/$.?#].[^\\s]*$", // URL
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", // UUID
            )

        safePatterns.forEach { pattern ->
            // Should not throw
            val warning = RegexSafety.validatePattern(pattern)
            // Some patterns may have warnings (like .* in URL) but should not be rejected
        }
    }

    @Test
    fun `test warning patterns produce warnings`() {
        val patternsWithWarnings =
            listOf(
                "^.*$", // Single .* should warn
                "^.+test$", // Single .+ should warn
                "^test\\d+end$", // \\d+ should warn
            )

        patternsWithWarnings.forEach { pattern ->
            val warning = RegexSafety.validatePattern(pattern)
            // Should not throw but should return warning
            assertTrue(
                warning != null && warning.contains("WARNING"),
                "Expected warning for pattern: $pattern",
            )
        }
    }

    @Test
    fun `test very long patterns produce warnings`() {
        val longPattern = "a".repeat(600) + "$"
        val warning = RegexSafety.validatePattern(longPattern)

        assertTrue(
            warning != null && warning.contains("Very long regex pattern"),
            "Expected warning for very long pattern",
        )
    }

    @Test
    fun `test invalid regex patterns are rejected`() {
        val invalidPatterns =
            listOf(
                "[a-z", // Unclosed bracket
                "(abc", // Unclosed paren
                "(?P<>test)", // Invalid group
                "*test", // Invalid quantifier position
            )

        invalidPatterns.forEach { pattern ->
            assertThrows<IllegalArgumentException> {
                RegexSafety.validatePatternCompiles(pattern)
            }
        }
    }

    @Test
    fun `test max input length constant is reasonable`() {
        // Ensure the max length is set to a reasonable value
        assertEquals(10_000, RegexSafety.MAX_PATTERN_INPUT_LENGTH)

        // Should be:
        // - Large enough for legitimate use cases
        // - Small enough to prevent memory exhaustion
        // - Not too small to be annoying
        assertTrue(RegexSafety.MAX_PATTERN_INPUT_LENGTH in 1_000..100_000)
    }

    @Test
    fun `test error message contains helpful information`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                RegexSafety.validatePattern("(a+)+")
            }

        val message = exception.message!!

        // Should contain all helpful info
        assertTrue(message.contains("SECURITY ERROR"))
        assertTrue(message.contains("ReDoS"))
        assertTrue(message.contains("nested quantifiers"))
        assertTrue(message.contains("Solutions:"))
        assertTrue(message.contains("owasp.org"))
    }

    @Test
    fun `test real-world attack patterns are blocked`() {
        // These are actual ReDoS patterns found in the wild
        val realWorldAttacks =
            listOf(
                "(a+)+b", // Classic ReDoS
                "(a*)*b", // Classic ReDoS variant
                "(a|a)*b", // Alternation variant
                "(a|ab)+", // Prefix overlap
                "([a-zA-Z]+)*c", // Character class
                "(\\d+)+(\\d+)+", // Multiple nested
                "^(a+)+$", // Anchored nested
                "(x+x+)+y", // Exponential backtracking
            )

        realWorldAttacks.forEach { pattern ->
            assertThrows<IllegalArgumentException>(
                "Pattern '$pattern' should be blocked",
            ) {
                RegexSafety.validatePattern(pattern)
            }
        }
    }

    @Test
    fun `test edge cases are handled`() {
        // Empty pattern
        assertNull(RegexSafety.validatePattern(""))

        // Very simple patterns
        assertNull(RegexSafety.validatePattern("a"))
        assertNull(RegexSafety.validatePattern("^a$"))
        assertNull(RegexSafety.validatePattern("[a-z]"))
    }

    @Test
    fun `test documentation examples are safe`() {
        // All patterns shown in documentation as "safe" should pass (without throwing errors)
        val docSafeExamples =
            listOf(
                "^[a-z]+$",
                "^[A-Z0-9_]+$",
                "^\\d{5}$",
                "^[A-Z]{2}\\d{6}$",
                "^user_.*$",
                // Note: Patterns with multiple .+ or (pattern)+ may trigger warnings/errors
            )

        docSafeExamples.forEach { pattern ->
            // Should not throw exceptions
            val warning = RegexSafety.validatePattern(pattern)
            RegexSafety.validatePatternCompiles(pattern)
            // Warnings are OK, errors are not
        }
    }

    @Test
    fun `test documentation dangerous examples are blocked`() {
        // All patterns shown in documentation as "dangerous" should be blocked
        val docDangerousExamples =
            listOf(
                "(a+)+",
                "(a*)*",
                "([a-z]+)+",
                ".*.*",
                ".+.+",
                "(a|ab)+",
                "(foo|foobar)+",
            )

        docDangerousExamples.forEach { pattern ->
            assertThrows<IllegalArgumentException>(
                "Documentation example '$pattern' should be blocked",
            ) {
                RegexSafety.validatePattern(pattern)
            }
        }
    }
}
