package com.noovoweb.validator.spring

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.ValidationResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for reactive validation extensions (validateMono).
 */
class ReactiveValidationExtensionsTest {

    // Test data class
    data class TestRequest(
        val email: String?,
        val name: String?
    )

    // Mock validator for testing
    class TestRequestValidator(private val shouldFail: Boolean = false) : GeneratedValidator<TestRequest> {
        override suspend fun validate(payload: TestRequest, context: ValidationContext) {
            if (shouldFail) {
                val errors = mutableMapOf<String, MutableList<String>>()
                if (payload.email.isNullOrEmpty()) {
                    errors.getOrPut("email") { mutableListOf() }.add("Email is required")
                }
                if (payload.name != null && payload.name.length < 5) {
                    errors.getOrPut("name") { mutableListOf() }.add("Name must be at least 5 characters")
                }
                if (errors.isNotEmpty()) {
                    throw ValidationException(errors)
                }
            }
        }

        override suspend fun validateResult(payload: TestRequest, context: ValidationContext): ValidationResult<TestRequest> {
            return try {
                validate(payload, context)
                ValidationResult.Success(payload)
            } catch (ex: ValidationException) {
                // Convert String errors to ValidationError
                val validationErrors = ex.errors.mapValues { (_, messages) ->
                    messages.map { com.noovoweb.validator.ValidationError(it) }
                }
                ValidationResult.Failure(validationErrors)
            }
        }
    }

    @Test
    fun `validateMono should complete successfully for valid data`() = runTest {
        val validator = TestRequestValidator(shouldFail = false)
        val request = TestRequest(email = "test@example.com", name = "Alice")
        val context = ValidationContext()

        // Mono completes without error
        val result = validator.validateMono(request, context).block()
        // If no exception, test passes
        assertTrue(result == null) // Mono<Void> returns null
    }

    @Test
    fun `validateMono should throw ValidationException for invalid data`() = runTest {
        val validator = TestRequestValidator(shouldFail = true)
        val request = TestRequest(email = null, name = "Bob")
        val context = ValidationContext()

        val mono = validator.validateMono(request, context)

        // Using block() will propagate the exception
        val exception = assertThrows<ValidationException> {
            mono.block()
        }

        assertTrue(exception.errors.containsKey("email"))
        assertTrue(exception.errors["email"]?.contains("Email is required") == true)
        assertTrue(exception.errors.containsKey("name"))
    }

    @Test
    fun `validateMono can be chained with other Mono operations`() = runTest {
        val validator = TestRequestValidator(shouldFail = false)
        val request = TestRequest(email = "test@example.com", name = "Alice")
        val context = ValidationContext()

        val result = validator.validateMono(request, context)
            .thenReturn("Success")
            .block()

        assertEquals("Success", result)
    }
}
