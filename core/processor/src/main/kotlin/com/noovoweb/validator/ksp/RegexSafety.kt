package com.noovoweb.validator.ksp

/**
 * Security utility for detecting dangerous regex patterns that could cause ReDoS attacks.
 *
 * ReDoS (Regular Expression Denial of Service) occurs when regex engines experience
 * catastrophic backtracking on specially crafted inputs.
 */
object RegexSafety {
    /**
     * Maximum recommended input length for pattern validation.
     * Inputs longer than this should be rejected before regex matching.
     */
    const val MAX_PATTERN_INPUT_LENGTH = 10_000

    /**
     * Patterns known to cause catastrophic backtracking.
     * These involve nested quantifiers or overlapping alternatives.
     */
    private val DANGEROUS_PATTERNS =
        listOf(
            // Nested quantifiers - the most dangerous
            Regex("""\([^)]*[+*]\)[+*]"""), // (a+)+ or (a*)* patterns
            Regex("""\([^)]*[+*]\)\{"""), // (a+){n,m} patterns
            // Multiple overlapping quantifiers
            Regex("""\.\*.*\.\*"""), // .*.* patterns
            Regex("""\.\+.*\.\+"""), // .+.+ patterns
            // Alternation with quantifiers
            Regex("""\([^)]*\|[^)]*\)[+*]"""), // (a|b)+ can be dangerous
            // Excessive backtracking potential
            Regex("""(\w\+){3,}"""), // Multiple consecutive \w+
        )

    /**
     * Warning patterns that are potentially slow but not immediately catastrophic.
     */
    private val WARNING_PATTERNS =
        listOf(
            Regex("""\.\*"""), // .* is generally slow
            Regex("""\.\+"""), // .+ is generally slow
            Regex("""\w\*"""), // \w* can be slow
            Regex("""\\d\+"""), // \d+ can be slow on long inputs
        )

    /**
     * Validate that a regex pattern is safe to use.
     *
     * @param pattern The regex pattern to validate
     * @throws IllegalArgumentException if pattern is dangerous
     * @return Warning message if pattern is potentially slow, null otherwise
     */
    fun validatePattern(pattern: String): String? {
        // Check for dangerous patterns
        for (dangerousPattern in DANGEROUS_PATTERNS) {
            if (dangerousPattern.containsMatchIn(pattern)) {
                throw IllegalArgumentException(
                    """
                    |SECURITY ERROR: Potentially dangerous regex pattern detected!
                    |Pattern: $pattern
                    |
                    |This pattern contains nested quantifiers or overlapping alternatives
                    |that can cause catastrophic backtracking (ReDoS attack).
                    |
                    |Common dangerous patterns:
                    |  - (a+)+    nested quantifiers
                    |  - (a*)*    nested quantifiers  
                    |  - (a|b)+   alternation with quantifiers
                    |  - .*.*     multiple .* patterns
                    |
                    |Solutions:
                    |  1. Simplify your regex pattern
                    |  2. Use possessive quantifiers if supported
                    |  3. Use multiple simpler validations instead
                    |  4. Consider using @MaxLength before @Pattern
                    |
                    |See: https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS
                    """.trimMargin(),
                )
            }
        }

        // Check for warning patterns
        for (warningPattern in WARNING_PATTERNS) {
            if (warningPattern.containsMatchIn(pattern)) {
                return """
                    |WARNING: Regex pattern may be slow on long inputs.
                    |Pattern: $pattern
                    |
                    |Consider using @MaxLength annotation before @Pattern to limit input size.
                    |Recommended: @MaxLength(1000) @Pattern("$pattern")
                    """.trimMargin()
            }
        }

        // Check pattern length - very long patterns are suspicious
        if (pattern.length > 500) {
            return """
                |WARNING: Very long regex pattern (${pattern.length} characters).
                |Long patterns can be slow to compile and execute.
                |Consider simplifying or breaking into multiple validations.
                """.trimMargin()
        }

        return null
    }

    /**
     * Test if a pattern compiles successfully.
     *
     * @param pattern The regex pattern to test
     * @throws IllegalArgumentException if pattern doesn't compile
     */
    fun validatePatternCompiles(pattern: String) {
        try {
            Regex(pattern)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Invalid regex pattern: $pattern\nError: ${e.message}",
                e,
            )
        }
    }
}
