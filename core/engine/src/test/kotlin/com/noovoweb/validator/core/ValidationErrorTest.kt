package com.noovoweb.validator.core

import com.noovoweb.validator.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for ValidationError data structure.
 */
class ValidationErrorTest {
    @Test
    fun `ValidationError stores message and code`() {
        val error =
            ValidationError(
                message = "Invalid email format",
                code = "invalid_email",
            )

        assertEquals("Invalid email format", error.message)
        assertEquals("invalid_email", error.code)
    }

    @Test
    fun `ValidationError stores optional metadata`() {
        val metadata = mapOf("fieldName" to "email", "value" to "invalid@")
        val error =
            ValidationError(
                message = "Invalid email",
                code = "invalid",
                metadata = metadata,
            )

        assertEquals(metadata, error.metadata)
    }

    @Test
    fun `ValidationError with default code is null`() {
        val error = ValidationError(message = "Some error")

        assertNull(error.code)
    }

    @Test
    fun `ValidationError with default metadata is empty map`() {
        val error = ValidationError(message = "Some error")

        assertTrue(error.metadata.isEmpty())
    }

    @Test
    fun `ValidationError_required factory creates required error`() {
        val error = ValidationError.required("email")

        assertEquals("Field 'email' is required", error.message)
        assertEquals("required", error.code)
    }

    @Test
    fun `ValidationError_invalid factory creates invalid error`() {
        val error = ValidationError.invalid("email", "format")

        assertEquals("Field 'email' is invalid: format", error.message)
        assertEquals("invalid", error.code)
    }

    @Test
    fun `ValidationError is immutable data class`() {
        val error =
            ValidationError(
                message = "Test",
                code = "test_code",
            )

        // Trying to verify immutability through equals/hashCode
        val samError =
            ValidationError(
                message = "Test",
                code = "test_code",
            )

        assertEquals(error, samError)
        assertEquals(error.hashCode(), samError.hashCode())
    }

    @Test
    fun `ValidationError factory with custom message and code`() {
        val error =
            ValidationError(
                message = "Custom validation failed",
                code = "custom_validation",
            )

        assertNotNull(error.message)
        assertNotNull(error.code)
    }
}
