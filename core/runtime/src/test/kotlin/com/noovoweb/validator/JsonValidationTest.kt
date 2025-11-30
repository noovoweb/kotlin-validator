package com.noovoweb.validator

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for JSON validation in ValidationPatterns.
 */
class JsonValidationTest {
    @Test
    fun `test valid JSON objects`() {
        val validJson = listOf(
            """{}""",
            """{"key": "value"}""",
            """{"name": "John", "age": 30}""",
            """{"nested": {"inner": "value"}}""",
            """{"array": [1, 2, 3]}""",
            """{"mixed": {"arr": [1, "two", true, null]}}""",
        )

        validJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON: $json")
        }
    }

    @Test
    fun `test valid JSON arrays`() {
        val validJson = listOf(
            """[]""",
            """[1, 2, 3]""",
            """["a", "b", "c"]""",
            """[{"id": 1}, {"id": 2}]""",
            """[true, false, null]""",
            """[[1, 2], [3, 4]]""",
        )

        validJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON: $json")
        }
    }

    @Test
    fun `test valid JSON primitives`() {
        val validJson = listOf(
            """"string"""",
            """123""",
            """-456""",
            """12.34""",
            """-12.34""",
            """1e10""",
            """1.5e-3""",
            """true""",
            """false""",
            """null""",
        )

        validJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON: $json")
        }
    }

    @Test
    fun `test valid JSON with whitespace`() {
        val validJson = listOf(
            """  {}  """,
            """
            {
                "key": "value"
            }
            """,
            """[ 1 , 2 , 3 ]""",
        )

        validJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON: $json")
        }
    }

    @Test
    fun `test valid JSON with escape sequences`() {
        val validJson = listOf(
            """{"text": "line1\nline2"}""",
            """{"path": "C:\\Users\\test"}""",
            """{"quote": "He said \"hello\""}""",
            """{"tab": "col1\tcol2"}""",
        )

        validJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON: $json")
        }
    }

    @Test
    fun `test invalid JSON - malformed`() {
        val invalidJson = listOf(
            "",
            "   ",
            "{",
            "}",
            "[",
            "]",
            "{{}",
            "{key: value}", // Unquoted key
            "{'key': 'value'}", // Single quotes
            """{"key": }""", // Missing value
            """{"key"}""", // Missing colon and value
            """{: "value"}""", // Missing key
        )

        invalidJson.forEach { json ->
            assertFalse(ValidationPatterns.isValidJson(json), "Expected invalid JSON: $json")
        }
    }

    @Test
    fun `test invalid JSON - trailing comma`() {
        val invalidJson = listOf(
            """{"key": "value",}""",
            """[1, 2, 3,]""",
            """{"a": 1, "b": 2,}""",
        )

        invalidJson.forEach { json ->
            assertFalse(ValidationPatterns.isValidJson(json), "Expected invalid JSON (trailing comma): $json")
        }
    }

    @Test
    fun `test invalid JSON - extra content`() {
        val invalidJson = listOf(
            """{}{}""",
            """[][]""",
            """{"key": "value"} extra""",
            """123 456""",
        )

        invalidJson.forEach { json ->
            assertFalse(ValidationPatterns.isValidJson(json), "Expected invalid JSON (extra content): $json")
        }
    }

    @Test
    fun `test invalid JSON - unclosed structures`() {
        val invalidJson = listOf(
            """{"key": "value"""",
            """{"nested": {"inner": "value"}""",
            """[1, 2, 3""",
            """{"arr": [1, 2, 3}""",
        )

        invalidJson.forEach { json ->
            assertFalse(ValidationPatterns.isValidJson(json), "Expected invalid JSON (unclosed): $json")
        }
    }

    @Test
    fun `test deeply nested JSON`() {
        val deepNested = """{"a":{"b":{"c":{"d":{"e":"value"}}}}}"""
        assertTrue(ValidationPatterns.isValidJson(deepNested))

        val deepArray = """[[[[["deep"]]]]]"""
        assertTrue(ValidationPatterns.isValidJson(deepArray))
    }

    @Test
    fun `test JSON with unicode`() {
        val unicodeJson = listOf(
            """{"emoji": "😀"}""",
            """{"chinese": "中文"}""",
            """{"arabic": "العربية"}""",
        )

        unicodeJson.forEach { json ->
            assertTrue(ValidationPatterns.isValidJson(json), "Expected valid JSON with unicode: $json")
        }
    }

    @Test
    fun `test security - no hang on malicious input`() {
        val maliciousInputs = listOf(
            "{" + "\"a\":{".repeat(100) + "}" + "}".repeat(100),
            "[" + "[".repeat(100) + "]".repeat(100) + "]",
            "\"" + "a".repeat(10000) + "\"",
        )

        maliciousInputs.forEach { input ->
            val startTime = System.currentTimeMillis()
            ValidationPatterns.isValidJson(input)
            val duration = System.currentTimeMillis() - startTime
            assertTrue(duration < 500, "Should complete quickly, took ${duration}ms")
        }
    }
}
