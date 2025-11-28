package com.noovoweb.validator.ksp

/**
 * Centralized repository of validation patterns and utilities.
 *
 * All regex patterns and validation helpers used by the code generator.
 */
object ValidationPatterns {
    // === String Patterns ===

    /**
     * Email validation pattern.
     * Matches standard email format: username@domain.tld
     */
    const val EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    /**
     * URL validation pattern.
     * Matches HTTP/HTTPS URLs.
     */
    const val URL = "^https?://[^\\s/$.?#].[^\\s]*$"

    /**
     * UUID validation pattern.
     * Matches standard UUID format: 8-4-4-4-12 hex digits.
     */
    const val UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"

    /**
     * Alpha pattern - only letters.
     */
    const val ALPHA = "^[a-zA-Z]+$"

    /**
     * Alphanumeric pattern - letters and numbers only.
     */
    const val ALPHANUMERIC = "^[a-zA-Z0-9]+$"

    /**
     * ASCII pattern - only ASCII characters.
     */
    const val ASCII = "^[\\x00-\\x7F]+$"

    // === Network Patterns ===

    /**
     * IPv4 address pattern.
     * Matches valid IPv4 addresses (0.0.0.0 to 255.255.255.255).
     *
     * Simple and safe pattern with no ReDoS risk.
     */
    const val IPV4 = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"

    /**
     * IPv6 address pattern - SIMPLIFIED for safety.
     *
     * NOTE: This is a simplified pattern that catches most IPv6 addresses.
     * For production use, consider using InetAddress.getByName() instead
     * which provides proper validation without ReDoS risk.
     *
     * Matches: Standard IPv6 (2001:0db8:85a3::8a2e:0370:7334)
     * May not catch all edge cases but is safe from ReDoS.
     */
    const val IPV6 = "^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$"

    /**
     * MAC address pattern.
     * Matches MAC addresses with colon or hyphen separators.
     */
    const val MAC_ADDRESS = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

    // === Date/Time Patterns ===

    /**
     * ISO date pattern (YYYY-MM-DD).
     */
    const val ISO_DATE = "^\\d{4}-\\d{2}-\\d{2}$"

    /**
     * ISO datetime pattern.
     * Matches ISO 8601 datetime format with optional timezone.
     */
    const val ISO_DATETIME = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?$"

    // === Validation Helpers ===

    /**
     * Check if a string is valid JSON.
     *
     * Performs proper JSON structural validation:
     * - Braces/brackets are properly balanced
     * - Strings are properly quoted
     * - No trailing commas
     * - Valid number formats
     * - Valid escape sequences
     *
     * This is a lightweight implementation without external dependencies.
     */
    fun isValidJson(input: String): Boolean {
        if (input.isBlank()) return false

        val trimmed = input.trim()

        // Quick sanity check
        if (!(
                (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]")) ||
                    trimmed.startsWith("\"") || trimmed == "true" ||
                    trimmed == "false" || trimmed == "null" ||
                    trimmed.firstOrNull()?.let { it.isDigit() || it == '-' } == true
            )
        ) {
            return false
        }

        return try {
            JsonParser(trimmed).parse()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Simple JSON parser for validation.
     */
    private class JsonParser(private val json: String) {
        private var index = 0

        fun parse() {
            parseValue()
            skipWhitespace()
            if (index != json.length) {
                throw IllegalArgumentException("Extra characters after JSON")
            }
        }

        private fun skipWhitespace() {
            while (index < json.length && json[index].isWhitespace()) {
                index++
            }
        }

        private fun parseValue() {
            skipWhitespace()
            if (index >= json.length) throw IllegalArgumentException("Unexpected end")

            when (json[index]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalArgumentException("Invalid JSON value")
            }
        }

        private fun parseObject() {
            index++ // skip '{'
            skipWhitespace()

            if (index < json.length && json[index] == '}') {
                index++
                return
            }

            while (true) {
                skipWhitespace()
                if (index >= json.length) throw IllegalArgumentException("Unclosed object")

                if (json[index] != '"') throw IllegalArgumentException("Key must be string")
                parseString()

                skipWhitespace()
                if (index >= json.length || json[index] != ':') {
                    throw IllegalArgumentException("Expected ':'")
                }
                index++

                parseValue()

                skipWhitespace()
                if (index >= json.length) throw IllegalArgumentException("Unclosed object")

                when (json[index]) {
                    ',' -> {
                        index++
                        skipWhitespace()
                        if (index < json.length && json[index] == '}') {
                            throw IllegalArgumentException("Trailing comma")
                        }
                    }
                    '}' -> {
                        index++
                        return
                    }
                    else -> throw IllegalArgumentException("Expected ',' or '}'")
                }
            }
        }

        private fun parseArray() {
            index++ // skip '['
            skipWhitespace()

            if (index < json.length && json[index] == ']') {
                index++
                return
            }

            while (true) {
                parseValue()

                skipWhitespace()
                if (index >= json.length) throw IllegalArgumentException("Unclosed array")

                when (json[index]) {
                    ',' -> {
                        index++
                        skipWhitespace()
                        if (index < json.length && json[index] == ']') {
                            throw IllegalArgumentException("Trailing comma")
                        }
                    }
                    ']' -> {
                        index++
                        return
                    }
                    else -> throw IllegalArgumentException("Expected ',' or ']'")
                }
            }
        }

        private fun parseString() {
            index++ // skip '"'

            while (index < json.length) {
                when (val ch = json[index]) {
                    '"' -> {
                        index++
                        return
                    }
                    '\\' -> {
                        index++
                        if (index >= json.length) throw IllegalArgumentException("Invalid escape")
                        val escaped = json[index]
                        if (escaped !in "\"\\/ bfnrtu") {
                            throw IllegalArgumentException("Invalid escape: $escaped")
                        }
                        index++
                    }
                    in '\u0000'..'\u001F' -> {
                        throw IllegalArgumentException("Control char must be escaped")
                    }
                    else -> index++
                }
            }
            throw IllegalArgumentException("Unclosed string")
        }

        private fun parseNumber() {
            if (json[index] == '-') index++

            if (index >= json.length || !json[index].isDigit()) {
                throw IllegalArgumentException("Invalid number")
            }

            if (json[index] == '0') {
                index++
            } else {
                while (index < json.length && json[index].isDigit()) {
                    index++
                }
            }

            if (index < json.length && json[index] == '.') {
                index++
                if (index >= json.length || !json[index].isDigit()) {
                    throw IllegalArgumentException("Invalid decimal")
                }
                while (index < json.length && json[index].isDigit()) {
                    index++
                }
            }

            if (index < json.length && (json[index] == 'e' || json[index] == 'E')) {
                index++
                if (index < json.length && (json[index] == '+' || json[index] == '-')) {
                    index++
                }
                if (index >= json.length || !json[index].isDigit()) {
                    throw IllegalArgumentException("Invalid exponent")
                }
                while (index < json.length && json[index].isDigit()) {
                    index++
                }
            }
        }

        private fun parseBoolean() {
            if (json.substring(index).startsWith("true")) {
                index += 4
            } else if (json.substring(index).startsWith("false")) {
                index += 5
            } else {
                throw IllegalArgumentException("Invalid boolean")
            }
        }

        private fun parseNull() {
            if (json.substring(index).startsWith("null")) {
                index += 4
            } else {
                throw IllegalArgumentException("Invalid null")
            }
        }
    }

    /**
     * Check if a value is "accepted" (truthy).
     *
     * Accepts: true, "1", "yes", "true", "on" (case-insensitive), 1
     */
    fun isAccepted(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.lowercase() in setOf("1", "yes", "true", "on")
            is Int -> value == 1
            else -> false
        }
    }

    /**
     * Validate port number range (1-65535).
     */
    fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }

    /**
     * Check if a number is an integer (whole number).
     */
    fun isInteger(value: Number): Boolean {
        return when (value) {
            is Int, is Long, is Short, is Byte -> true
            is Float -> value % 1 == 0f
            is Double -> value % 1 == 0.0
            else -> false
        }
    }

    /**
     * Check if a number has decimal places.
     */
    fun isDecimal(value: Number): Boolean {
        return when (value) {
            is Int, is Long, is Short, is Byte -> false
            is Float -> value % 1 != 0f
            is Double -> value % 1 != 0.0
            else -> false
        }
    }
}
