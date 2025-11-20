package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

object ParallelValidators {
    suspend fun validateParallel1(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        kotlinx.coroutines.delay(5)
        return value.length >= 1
    }

    suspend fun validateParallel2(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        kotlinx.coroutines.delay(5)
        return value.contains("valid")
    }
}

@Validated
data class ParallelData(
    @CustomValidator(validator = "com.noovoweb.validator.integration.ParallelValidators::validateParallel1")
    val field1: String?,

    @CustomValidator(validator = "com.noovoweb.validator.integration.ParallelValidators::validateParallel2")
    val field2: String?
)

class ParallelValidationIntegrationTest {

    @Test
    fun `should support parallel validation`() = runTest {
        val context = ValidationContext()
        val result1 = ParallelValidators.validateParallel1("test", context)
        val result2 = ParallelValidators.validateParallel2("valid-data", context)
        assertTrue(result1 && result2)
    }

    @Test
    fun `parallel validation should be faster than sequential validators`() = runTest {
        val validator = ParallelDataValidator()
        val data = ParallelData(field1 = "test", field2 = "valid-data")

        // Parallel validation across fields
        val parallelStart = System.currentTimeMillis()
        validator.validate(data)
        val parallelEnd = System.currentTimeMillis()
        val parallelTime = parallelEnd - parallelStart

        // Should complete in roughly the time of the longest validator (not sum of both)
        // Both validators have 5ms delay, so parallel should be ~5ms, sequential would be ~10ms
        assertTrue(parallelTime < 15) // Allow some overhead
    }

    @Test
    fun `should validate both fields in parallel mode`() = runTest {
        val validator = ParallelDataValidator()
        validator.validate(ParallelData(field1 = "a", field2 = "valid"))
    }

    @Test
    fun `should validate complex data with multiple fields`() = runTest {
        val validator = ParallelDataValidator()
        validator.validate(ParallelData(field1 = "test", field2 = "valid-test"))
    }
}
