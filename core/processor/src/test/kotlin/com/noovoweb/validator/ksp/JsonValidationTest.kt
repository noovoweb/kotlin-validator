package com.noovoweb.validator.ksp

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for improved JSON validation that actually parses JSON structure.
 * 
 * These tests ensure the JSON validator properly validates structure
 * and doesn't just check for braces.
 */
class JsonValidationTest {

    @Test
    fun `test valid JSON objects`() {
        val validJsonObjects = listOf(
            """{}""",
            """{"key": "value"}""",
            """{"name": "John", "age": 30}""",
            """{"nested": {"key": "value"}}""",
            """{"array": [1, 2, 3]}""",
            """{"bool": true, "null": null}""",
            """{"number": 42, "float": 3.14, "exp": 1.5e10}""",
            """  {  "whitespace"  :  "handled"  }  """,
            """{"escaped": "quote: \" and backslash: \\"}"""
        )

        validJsonObjects.forEach { json ->
            assertTrue(
                ValidationPatterns.isValidJson(json),
                "Expected valid JSON: $json"
            )
        }
    }

    @Test
    fun `test valid JSON arrays`() {
        val validJsonArrays = listOf(
            """[]""",
            """[1, 2, 3]""",
            """["a", "b", "c"]""",
            """[{"key": "value"}]""",
            """[1, "two", {"three": 3}, [4]]""",
            """[true, false, null]""",
            """  [  1  ,  2  ,  3  ]  """
        )

        validJsonArrays.forEach { json ->
            assertTrue(
                ValidationPatterns.isValidJson(json),
                "Expected valid JSON: $json"
            )
        }
    }

    @Test
    fun `test invalid JSON - structural errors`() {
        val invalidJson = listOf(
            """{invalid}""",                    // Unquoted key
            """{"key": }""",                    // Missing value
            """{"key" "value"}""",              // Missing colon
            """{key: "value"}""",               // Unquoted key
            """{"key": "value",}""",            // Trailing comma
            """[1, 2, 3,]""",                   // Trailing comma
            """{"unclosed": "string}""",        // Unclosed string
            """{"key": "value"""",              // Unclosed object
            """[1, 2, 3""",                     // Unclosed array
            """{"key": 'value'}""",             // Single quotes
            """{"key": undefined}"""            // Undefined value
            // Note: Duplicate keys are technically valid JSON (though bad practice)
        )

        invalidJson.forEach { json ->
            assertFalse(
                ValidationPatterns.isValidJson(json),
                "Expected invalid JSON: $json"
            )
        }
    }

    @Test
    fun `test invalid JSON - just braces`() {
        // These would pass the old validation but should fail the new one
        val justBraces = listOf(
            """{""",
            """}""",
            """}{""",
            """{{}""",
            """}}}""",
            """[""",
            """]""",
            """][""",
            """[[]""",
            """]]]"""
        )

        justBraces.forEach { json ->
            assertFalse(
                ValidationPatterns.isValidJson(json),
                "Expected invalid JSON: $json"
            )
        }
    }

    @Test
    fun `test invalid JSON - escape sequences`() {
        val invalidEscapes = listOf(
            """{"key": "invalid \x escape"}""",  // Invalid escape (\x is not valid)
            """{"key": "line
break"}"""                                      // Unescaped newline (actual newline in string)
            // Note: Control characters in Kotlin strings are already escaped by Kotlin compiler
        )

        invalidEscapes.forEach { json ->
            assertFalse(
                ValidationPatterns.isValidJson(json),
                "Expected invalid JSON: ${json.replace("\n", "\\n")}"
            )
        }
    }

    @Test
    fun `test invalid JSON - number formats`() {
        val invalidNumbers = listOf(
            """{"num": 01}""",                  // Leading zero
            """{"num": .5}""",                  // No leading digit
            """{"num": 5.}""",                  // No trailing digit
            """{"num": 1e}""",                  // Incomplete exponent
            """{"num": +5}""",                  // Leading plus
            """{"num": 0x10}"""                 // Hex notation
        )

        invalidNumbers.forEach { json ->
            assertFalse(
                ValidationPatterns.isValidJson(json),
                "Expected invalid JSON: $json"
            )
        }
    }

    @Test
    fun `test valid JSON - complex nested structure`() {
        val complexJson = """{
            "users": [
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@example.com",
                    "active": true,
                    "settings": {
                        "theme": "dark",
                        "notifications": {
                            "email": true,
                            "sms": false
                        }
                    },
                    "tags": ["admin", "developer"],
                    "score": 3.14159
                },
                {
                    "id": 2,
                    "name": "Jane Smith",
                    "email": null,
                    "active": false,
                    "settings": {},
                    "tags": [],
                    "score": 2.71828e10
                }
            ]
        }"""

        assertTrue(ValidationPatterns.isValidJson(complexJson))
    }

    @Test
    fun `test empty and blank strings`() {
        assertFalse(ValidationPatterns.isValidJson(""))
        assertFalse(ValidationPatterns.isValidJson("   "))
        assertFalse(ValidationPatterns.isValidJson("\n\t"))
    }

    @Test
    fun `test primitive values`() {
        // JSON spec allows standalone primitives
        assertTrue(ValidationPatterns.isValidJson("true"))
        assertTrue(ValidationPatterns.isValidJson("false"))
        assertTrue(ValidationPatterns.isValidJson("null"))
        assertTrue(ValidationPatterns.isValidJson("42"))
        assertTrue(ValidationPatterns.isValidJson("3.14"))
        assertTrue(ValidationPatterns.isValidJson(""""string""""))
    }

    @Test
    fun `test extra characters after JSON`() {
        val extraChars = listOf(
            """{"key": "value"} extra""",
            """[1, 2, 3] garbage""",
            """{}{}""",
            """[][]"""
        )

        extraChars.forEach { json ->
            assertFalse(
                ValidationPatterns.isValidJson(json),
                "Expected invalid JSON with extra characters: $json"
            )
        }
    }

    @Test
    fun `test security - deeply nested JSON`() {
        // Test that deeply nested JSON is still validated
        val depth = 100
        val opening = "{\"a\":".repeat(depth)
        val closing = "1" + "}".repeat(depth)
        val deeplyNested = opening + closing

        // Should validate correctly (not crash or hang)
        assertTrue(ValidationPatterns.isValidJson(deeplyNested))
    }

    @Test
    fun `test valid JSON with unicode`() {
        val unicodeJson = listOf(
            """{"emoji": "😀"}""",
            """{"chinese": "你好"}""",
            """{"arabic": "مرحبا"}"""
        )

        unicodeJson.forEach { json ->
            assertTrue(
                ValidationPatterns.isValidJson(json),
                "Expected valid JSON with unicode: $json"
            )
        }
    }
}
