package com.noovoweb.validator.core

import com.noovoweb.validator.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ValidationException structure and utility methods.
 */
class ValidationExceptionTest {
    @Test
    fun `ValidationException stores errors by field`() {
        val errors =
            mapOf(
                "email" to listOf("Invalid email", "Email too long"),
                "age" to listOf("Age must be at least 18"),
                "name" to listOf("Name is required"),
            )
        val exception = ValidationException(errors)

        assertEquals(errors, exception.errors)
        assertEquals(3, exception.errors.size)
    }

    @Test
    fun `ValidationException message includes field count`() {
        val errors =
            mapOf(
                "field1" to listOf("error"),
                "field2" to listOf("error"),
                "field3" to listOf("error"),
            )
        val exception = ValidationException(errors)

        assertTrue(exception.message!!.contains("3 field"))
    }

    @Test
    fun `getFieldErrors retrieves errors for specific field`() {
        val errors =
            mapOf(
                "email" to listOf("Invalid email", "Too long"),
                "age" to listOf("Too young"),
            )
        val exception = ValidationException(errors)

        val emailErrors = exception.getFieldErrors("email")
        assertEquals(2, emailErrors.size)
        assertEquals("Invalid email", emailErrors[0])
        assertEquals("Too long", emailErrors[1])
    }

    @Test
    fun `getFieldErrors returns empty list for missing field`() {
        val errors = mapOf("email" to listOf("Invalid"))
        val exception = ValidationException(errors)

        val missingErrors = exception.getFieldErrors("nonexistent")
        assertTrue(missingErrors.isEmpty())
    }

    @Test
    fun `hasFieldError checks field error presence`() {
        val errors =
            mapOf(
                "email" to listOf("error"),
                "age" to listOf("error"),
            )
        val exception = ValidationException(errors)

        assertTrue(exception.hasFieldError("email"))
        assertTrue(exception.hasFieldError("age"))
        assertFalse(exception.hasFieldError("name"))
    }

    @Test
    fun `getAllMessages returns flat error list`() {
        val errors =
            mapOf(
                "email" to listOf("Invalid email", "Too long"),
                "age" to listOf("Too young"),
                "name" to listOf("Required", "Too short"),
            )
        val exception = ValidationException(errors)

        val allMessages = exception.getAllMessages()
        assertEquals(5, allMessages.size)
        assertTrue(allMessages.contains("Invalid email"))
        assertTrue(allMessages.contains("Too long"))
        assertTrue(allMessages.contains("Too young"))
        assertTrue(allMessages.contains("Required"))
        assertTrue(allMessages.contains("Too short"))
    }

    @Test
    fun `getAllMessages returns empty list when no errors`() {
        val exception = ValidationException(emptyMap())
        val allMessages = exception.getAllMessages()

        assertTrue(allMessages.isEmpty())
    }

    @Test
    fun `toJson formats errors as JSON string`() {
        val errors =
            mapOf(
                "email" to listOf("Invalid email"),
                "age" to listOf("Too young"),
            )
        val exception = ValidationException(errors)

        val json = exception.toJson()
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"email\""))
        assertTrue(json.contains("\"Invalid email\""))
        assertTrue(json.contains("\"age\""))
        assertTrue(json.contains("\"Too young\""))
    }

    @Test
    fun `toJson handles multiple errors per field`() {
        val errors =
            mapOf(
                "password" to listOf("Too short", "No uppercase", "No number"),
            )
        val exception = ValidationException(errors)

        val json = exception.toJson()
        assertTrue(json.contains("\"Too short\""))
        assertTrue(json.contains("\"No uppercase\""))
        assertTrue(json.contains("\"No number\""))
    }

    @Test
    fun `toJson escapes special characters in messages`() {
        val errors =
            mapOf(
                "field" to listOf("Message with \"quotes\" and \\backslash"),
            )
        val exception = ValidationException(errors)

        val json = exception.toJson()
        assertNotNull(json)
        assertFalse(json.contains("\"quotes\""))
    }

    @Test
    fun `toJson handles empty errors gracefully`() {
        val exception = ValidationException(emptyMap())
        val json = exception.toJson()

        assertEquals("{}", json)
    }

    @Test
    fun `ValidationException is a RuntimeException`() {
        val errors = mapOf("field" to listOf("error"))
        val exception = ValidationException(errors)

        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `ValidationException can be caught as RuntimeException`() {
        val errors = mapOf("field" to listOf("error"))
        var caught = false

        try {
            throw ValidationException(errors)
        } catch (e: RuntimeException) {
            caught = true
            assertTrue(e is ValidationException)
        }

        assertTrue(caught)
    }

    @Test
    fun `multiple field errors are independently accessible`() {
        val errors =
            mapOf(
                "firstName" to listOf("Required"),
                "lastName" to listOf("Required"),
                "email" to listOf("Invalid", "Too long"),
                "age" to listOf("Too young"),
                "address.city" to listOf("Required"),
            )
        val exception = ValidationException(errors)

        assertEquals(listOf("Required"), exception.getFieldErrors("firstName"))
        assertEquals(listOf("Required"), exception.getFieldErrors("lastName"))
        assertEquals(listOf("Invalid", "Too long"), exception.getFieldErrors("email"))
        assertEquals(listOf("Too young"), exception.getFieldErrors("age"))
        assertEquals(listOf("Required"), exception.getFieldErrors("address.city"))
    }

    @Test
    fun `error path construction for nested fields`() {
        val errors =
            mapOf(
                "user.address.city" to listOf("Required"),
                "user.phoneNumbers[0].number" to listOf("Invalid format"),
                "user.phoneNumbers[1].type" to listOf("Invalid type"),
            )
        val exception = ValidationException(errors)

        assertTrue(exception.hasFieldError("user.address.city"))
        assertTrue(exception.hasFieldError("user.phoneNumbers[0].number"))
        assertTrue(exception.hasFieldError("user.phoneNumbers[1].type"))
    }
}
