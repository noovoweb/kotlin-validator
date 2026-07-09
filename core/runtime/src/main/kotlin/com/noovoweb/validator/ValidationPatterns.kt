package com.noovoweb.validator

import java.util.Locale

/**
 * Centralized repository of validation patterns and utilities.
 *
 * All regex patterns and validation helpers used by the code generator.
 */
public object ValidationPatterns {
    // === String Patterns ===

    /**
     * Email validation pattern.
     * Matches standard email format: username@domain.tld
     */
    public const val EMAIL: String = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    /**
     * URL validation pattern.
     * Matches HTTP/HTTPS URLs.
     *
     * NOTE: This regex is exposed for convenience but [isValidURL] should be preferred
     * for actual URL validation as it uses [java.net.URI] and avoids any ReDoS concerns.
     * The trailing quantifier is bounded ({0,2000}) here to prevent catastrophic backtracking.
     */
    public const val URL: String = "^https?://[^\\s/$.?#].[^\\s]{0,2000}$"

    /**
     * UUID validation pattern.
     * Matches standard UUID format: 8-4-4-4-12 hex digits.
     */
    public const val UUID: String = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"

    /**
     * Alpha pattern - only letters.
     */
    public const val ALPHA: String = "^[a-zA-Z]+$"

    /**
     * Alphanumeric pattern - letters and numbers only.
     */
    public const val ALPHANUMERIC: String = "^[a-zA-Z0-9]+$"

    /**
     * ASCII pattern - only ASCII characters.
     */
    public const val ASCII: String = "^[\\x00-\\x7F]+$"

    // === Network Patterns ===

    /**
     * IPv4 address pattern.
     * Matches valid IPv4 addresses (0.0.0.0 to 255.255.255.255).
     *
     * Simple and safe pattern with no ReDoS risk.
     */
    public const val IPV4: String = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"

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
    public const val IPV6: String = "^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$"

    /**
     * MAC address pattern.
     * Matches MAC addresses with colon or hyphen separators.
     */
    public const val MAC_ADDRESS: String = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

    // === Date/Time Patterns ===

    /**
     * ISO date pattern (YYYY-MM-DD).
     */
    public const val ISO_DATE: String = "^\\d{4}-\\d{2}-\\d{2}$"

    /**
     * ISO datetime pattern.
     * Matches ISO 8601 datetime format with optional timezone.
     */
    public const val ISO_DATETIME: String = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?$"

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
    public fun isValidJson(input: String): Boolean {
        if (input.isBlank()) return false

        val trimmed = input.trim()

        // Quick sanity check
        if (!(
                (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]")) ||
                    trimmed.startsWith("\"") ||
                    trimmed == "true" ||
                    trimmed == "false" ||
                    trimmed == "null" ||
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
     *
     * Tracks recursion depth to prevent stack overflow from maliciously deep payloads.
     */
    private class JsonParser(private val json: String) {
        private var index = 0
        private var depth = 0

        companion object {
            /** Maximum nesting depth to prevent StackOverflowError on deeply nested JSON. */
            private const val MAX_DEPTH = 100
        }

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
            depth++
            if (depth > MAX_DEPTH) {
                throw IllegalArgumentException("JSON nesting too deep (max: $MAX_DEPTH)")
            }
            try {
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
            } finally {
                depth--
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
    public fun isAccepted(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is String -> value.lowercase(Locale.ROOT) in setOf("1", "yes", "true", "on")
        is Int -> value == 1
        else -> false
    }

    /**
     * Validate port number range (1-65535).
     */
    public fun isValidPort(port: Int): Boolean = port in 1..65535

    /**
     * Check if a number is an integer (whole number).
     */
    public fun isInteger(value: Number): Boolean = when (value) {
        is Int, is Long, is Short, is Byte -> true
        is Float -> value % 1 == 0f
        is Double -> value % 1 == 0.0
        else -> false
    }

    /**
     * Check if a number has decimal places.
     */
    public fun isDecimal(value: Number): Boolean = when (value) {
        is Int, is Long, is Short, is Byte -> false
        is Float -> value % 1 != 0f
        is Double -> value % 1 != 0.0
        else -> false
    }

    /**
     * Validates an IPv4 address by pure syntactic parsing — **no DNS, no network**.
     *
     * Four dot-separated decimal octets (0–255). Leading zeros are rejected to avoid
     * octal/decimal-obfuscation bypasses (e.g. `0177.0.0.1`). This never calls
     * `InetAddress.getByName`, so a hostname passed here can never trigger a blocking
     * DNS lookup on the validation thread.
     *
     * @param address The IPv4 address string to validate
     * @return true if valid IPv4, false otherwise
     */
    public fun isValidIPv4(address: String): Boolean {
        val parts = address.split(".")
        if (parts.size != 4) return false
        return parts.all { isIPv4Octet(it) }
    }

    private fun isIPv4Octet(part: String): Boolean {
        if (part.isEmpty() || part.length > 3) return false
        if (!part.all { it in '0'..'9' }) return false
        if (part.length > 1 && part[0] == '0') return false // no leading zeros (octal-bypass guard)
        return part.toInt() in 0..255
    }

    /**
     * Validates an IPv6 address — **no DNS, no network**.
     *
     * The input must contain a `:` and consist only of hex digits, `:`, and `.`
     * (for IPv4-mapped forms) before it is handed to [java.net.InetAddress.getByName].
     * A `:` can never appear in a DNS hostname, so this guard guarantees `getByName`
     * parses the string as an IPv6 literal and never performs a blocking DNS lookup.
     * The JDK parser still handles compression (`::`), expansion, and IPv4-mapped forms.
     *
     * Examples of valid IPv6:
     * - 2001:0db8:85a3:0000:0000:8a2e:0370:7334
     * - 2001:db8:85a3::8a2e:370:7334 (compressed)
     * - ::1 (loopback), :: (all zeros), fe80::1 (link-local)
     *
     * @param address The IPv6 address string to validate
     * @return true if valid IPv6, false otherwise
     */
    public fun isValidIPv6(address: String): Boolean {
        if (':' !in address) return false
        if (address.any { it !in IPV6_LITERAL_CHARS }) return false
        return try {
            java.net.InetAddress.getByName(address) is java.net.Inet6Address
        } catch (e: Exception) {
            false
        }
    }

    private const val IPV6_LITERAL_CHARS = "0123456789abcdefABCDEF:."

    /**
     * Validates if address is either a valid IPv4 or IPv6 literal. Purely syntactic —
     * never resolves DNS.
     *
     * @param address The IP address string to validate
     * @return true if valid IPv4 or IPv6, false otherwise
     */
    public fun isValidIP(address: String): Boolean = isValidIPv4(address) || isValidIPv6(address)

    /**
     * Validates URL using Java's URI class.
     *
     * This is MUCH safer and more reliable than regex validation.
     * - No ReDoS risk
     * - Strict RFC 2396 parsing, consistent across JDK versions
     *   (java.net.URL is lenient about spaces on older JDKs and its
     *   String constructor is deprecated since JDK 20)
     * - Validates protocol, host, port, path
     * - No performance issues with malicious input
     *
     * Only allows http:// and https:// protocols for security.
     *
     * Examples of valid URLs:
     * - http://example.com
     * - https://www.example.com
     * - https://example.com:8080/path
     * - https://example.com/path?query=value
     * - https://user@example.com/path
     *
     * @param urlString The URL string to validate
     * @return true if valid HTTP/HTTPS URL, false otherwise
     */
    public fun isValidURL(urlString: String): Boolean {
        // Quick checks for empty or whitespace
        if (urlString.isBlank()) return false
        if (urlString.trim() != urlString) return false // Reject leading/trailing whitespace

        return try {
            val uri = java.net.URI(urlString)
            // Only allow http and https protocols
            val validProtocol = uri.scheme?.lowercase() in listOf("http", "https")
            // Ensure host is not empty
            val hasHost = !uri.host.isNullOrBlank()

            validProtocol && hasHost
        } catch (e: Exception) {
            false
        }
    }
}
