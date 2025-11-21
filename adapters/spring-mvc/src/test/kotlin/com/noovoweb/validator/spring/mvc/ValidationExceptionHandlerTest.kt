package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for ValidationExceptionHandler.
 */
class ValidationExceptionHandlerTest {

    private val handler = ValidationExceptionHandler()

    @Test
    fun `should convert ValidationException to ResponseEntity with 422 status`() {
        val errors = mapOf(
            "email" to listOf("Must be a valid email address"),
            "age" to listOf("Must be at least 18")
        )
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals(422, response.body?.status)
        assertEquals("Validation Failed", response.body?.message)
    }

    @Test
    fun `should include all error fields in response`() {
        val errors = mapOf(
            "username" to listOf("Required"),
            "email" to listOf("Invalid format"),
            "password" to listOf("Too weak", "Must contain special characters")
        )
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        val responseErrors = response.body?.errors
        assertEquals(3, responseErrors?.size)
        assertTrue(responseErrors?.containsKey("username") == true)
        assertTrue(responseErrors?.containsKey("email") == true)
        assertTrue(responseErrors?.containsKey("password") == true)
    }

    @Test
    fun `should preserve multiple error messages per field`() {
        val errors = mapOf(
            "password" to listOf("Too short", "No uppercase", "No digits", "No special chars")
        )
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        val passwordErrors = response.body?.errors?.get("password")
        assertEquals(4, passwordErrors?.size)
        assertTrue(passwordErrors?.contains("Too short") == true)
        assertTrue(passwordErrors?.contains("No uppercase") == true)
        assertTrue(passwordErrors?.contains("No digits") == true)
        assertTrue(passwordErrors?.contains("No special chars") == true)
    }

    @Test
    fun `should handle single error field`() {
        val errors = mapOf("email" to listOf("Invalid email format"))
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        assertEquals(1, response.body?.errors?.size)
        assertEquals(listOf("Invalid email format"), response.body?.errors?.get("email"))
    }

    @Test
    fun `should handle empty error messages`() {
        val errors = emptyMap<String, List<String>>()
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertTrue(response.body?.errors?.isEmpty() == true)
    }

    @Test
    fun `ValidationErrorResponse should have correct structure`() {
        val errors = mapOf("field" to listOf("error"))
        val errorResponse = ValidationErrorResponse(
            status = 422,
            message = "Validation Failed",
            errors = errors
        )

        assertEquals(422, errorResponse.status)
        assertEquals("Validation Failed", errorResponse.message)
        assertEquals(errors, errorResponse.errors)
    }

    @Test
    fun `should handle nested field paths`() {
        val errors = mapOf(
            "user.address.city" to listOf("Required"),
            "user.phoneNumbers[0].number" to listOf("Invalid format")
        )
        val exception = ValidationException(errors)

        val response = handler.handleValidationException(exception)

        assertTrue(response.body?.errors?.containsKey("user.address.city") == true)
        assertTrue(response.body?.errors?.containsKey("user.phoneNumbers[0].number") == true)
    }
}
