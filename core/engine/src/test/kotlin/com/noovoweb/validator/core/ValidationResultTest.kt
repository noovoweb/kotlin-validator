package com.noovoweb.validator.core

import com.noovoweb.validator.ValidationError
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.ValidationResult
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for ValidationResult sealed class and result operations.
 */
class ValidationResultTest {
    // ===== Success Path Tests =====

    @Test
    fun `ValidationResult_Success stores value correctly`() {
        val data = "test value"
        val result = ValidationResult.Success(data)

        assertTrue(result.isSuccess())
        assertEquals(data, result.getOrNull())
    }

    @Test
    fun `getOrThrow returns value for Success`() {
        val data = 42
        val result = ValidationResult.Success(data)

        assertEquals(data, result.getOrThrow())
    }

    @Test
    fun `getOrNull returns value for Success`() {
        val data = mapOf("key" to "value")
        val result = ValidationResult.Success(data)

        assertEquals(data, result.getOrNull())
    }

    @Test
    fun `map transforms Success value to new type`() {
        val result = ValidationResult.Success(42)
        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess())
        assertEquals(84, mapped.getOrNull())
    }

    @Test
    fun `map preserves Failure without transformation`() {
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)
        val mapped = result.map<String> { "unused" }

        assertTrue(mapped.isFailure())
        assertEquals(errors, (mapped as ValidationResult.Failure).errors)
    }

    @Test
    fun `onSuccess executes block for Success result`() {
        var executed = false
        var value: Int? = null
        val result = ValidationResult.Success(123)

        result.onSuccess {
            executed = true
            value = it
        }

        assertTrue(executed)
        assertEquals(123, value)
    }

    @Test
    fun `onSuccess returns same result for chaining`() {
        val result = ValidationResult.Success("test")
        val chained = result.onSuccess { /* do nothing */ }

        assertEquals(result, chained)
    }

    // ===== Failure Path Tests =====

    @Test
    fun `ValidationResult_Failure stores error map correctly`() {
        val errors =
            mapOf(
                "field1" to listOf(ValidationError("error1")),
                "field2" to listOf(ValidationError("error2")),
            )
        val result = ValidationResult.Failure(errors)

        assertTrue(result.isFailure())
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `getOrThrow throws ValidationException for Failure`() {
        val errors = mapOf("email" to listOf(ValidationError("Invalid email")))
        val result = ValidationResult.Failure(errors)

        val exception =
            assertThrows<ValidationException> {
                result.getOrThrow()
            }

        assertTrue(exception.hasFieldError("email"))
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)

        val nullable: String? = result.getOrNull() as? String?
        assertNull(nullable)
    }

    @Test
    fun `isFailure returns true for Failure result`() {
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)

        assertTrue(result.isFailure())
        assertFalse(result.isSuccess())
    }

    @Test
    fun `mapErrors transforms error structure in Failure`() {
        val errors = mapOf("field1" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)

        val mapped =
            result.mapErrors { originalErrors ->
                originalErrors.mapKeys { (field, _) -> "${field}_transformed" }
            }

        when (mapped) {
            is ValidationResult.Failure -> {
                assertTrue(mapped.errors.containsKey("field1_transformed"))
                assertFalse(mapped.errors.containsKey("field1"))
            }
            else -> throw AssertionError("Expected Failure")
        }
    }

    @Test
    fun `mapErrors preserves Success without transformation`() {
        val result = ValidationResult.Success(100)
        val mapped = result.mapErrors { emptyMap() }

        assertTrue(mapped.isSuccess())
        assertEquals(100, mapped.getOrNull())
    }

    @Test
    fun `onFailure executes block for Failure result`() {
        var executed = false
        var errorMap: Map<String, List<ValidationError>>? = null
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)

        result.onFailure {
            executed = true
            errorMap = it
        }

        assertTrue(executed)
        assertEquals(errors, errorMap)
    }

    @Test
    fun `onFailure returns same result for chaining`() {
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result = ValidationResult.Failure(errors)
        val chained = result.onFailure { /* do nothing */ }

        assertEquals(result, chained)
    }

    @Test
    fun `onFailure does not execute for Success result`() {
        var executed = false
        val result = ValidationResult.Success("value")

        result.onFailure {
            executed = true
        }

        assertFalse(executed)
    }

    // ===== Chaining Tests =====

    @Test
    fun `can chain map operations on Success`() {
        val result =
            ValidationResult.Success(5)
                .map { it + 10 }
                .map { it * 2 }
                .map { it - 3 }

        assertEquals(27, result.getOrNull())
    }

    @Test
    fun `map chain stops at first Failure`() {
        val errors = mapOf("field" to listOf(ValidationError("error")))
        val result: ValidationResult<Int> =
            ValidationResult.Failure(errors)
                .map<Int> { 1 }
                .map { it * 2 }

        assertTrue(result.isFailure())
    }

    @Test
    fun `onSuccess and onFailure can be chained`() {
        var successExecuted = false
        var failureExecuted = false

        val result =
            ValidationResult.Success(42)
                .onSuccess { successExecuted = true }
                .onFailure { failureExecuted = true }

        assertTrue(successExecuted)
        assertFalse(failureExecuted)
    }

    @Test
    fun `failure result skips all onSuccess in chain`() {
        var successExecuted = false
        val errors = mapOf("field" to listOf(ValidationError("error")))

        val result =
            ValidationResult.Failure(errors)
                .onSuccess { successExecuted = true }

        assertFalse(successExecuted)
    }

    // ===== Type Transformation Tests =====

    @Test
    fun `map can transform to completely different type`() {
        val result =
            ValidationResult.Success(42)
                .map { it.toString() }

        assertEquals("42", result.getOrNull())
    }

    @Test
    fun `map preserves generic type information`() {
        val result =
            ValidationResult.Success(listOf(1, 2, 3))
                .map { it.size }

        assertEquals(3, result.getOrNull())
    }
}
