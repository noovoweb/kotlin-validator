package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

object ComplexValidators {
    suspend fun validateComplexData(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        val errors = mutableMapOf<String, List<String>>()
        val parts = value.split("-")

        if (parts.size != 3) {
            errors["format"] = listOf("Must have format: XXX-YYY-ZZZ")
        }
        if (parts.getOrNull(0)?.length != 3) {
            errors["prefix"] = listOf("First part must be 3 chars")
        }
        if (parts.getOrNull(1)?.all { it.isDigit() } != true) {
            errors["middle"] = listOf("Middle part must be digits")
        }
        if (parts.getOrNull(2)?.length != 3) {
            errors["suffix"] = listOf("Last part must be 3 chars")
        }

        return if (errors.isEmpty()) true else throw ValidationException(errors)
    }
}

@Validated
data class ComplexData(
    @CustomValidator(validator = "com.noovoweb.validator.integration.ComplexValidators::validateComplexData")
    val data: String?
)

class ValidationExceptionIntegrationTest {

    @Test
    fun `should throw ValidationException with multiple errors`() = runTest {
        val exception = assertThrows<ValidationException> {
            ComplexValidators.validateComplexData("invalid", ValidationContext())
        }
        assertTrue(exception.errors.isNotEmpty())
    }

    @Test
    fun `should validate correct complex format`() = runTest {
        val isValid = ComplexValidators.validateComplexData("ABC-123-XYZ", ValidationContext())
        assertTrue(isValid)
    }

    @Test
    fun `should collect specific error fields`() = runTest {
        val exception = assertThrows<ValidationException> {
            ComplexValidators.validateComplexData("AB-123-XY", ValidationContext())
        }
        assertTrue(exception.errors.containsKey("prefix"))
        assertTrue(exception.errors.containsKey("suffix"))
    }

    @Test
    fun `should validate all format requirements`() = runTest {
        val exception = assertThrows<ValidationException> {
            ComplexValidators.validateComplexData("ABC-ABC-XYZ", ValidationContext())
        }
        assertTrue(exception.errors.containsKey("middle"))
    }

    @Test
    fun `should reject invalid format with all errors`() = runTest {
        val exception = assertThrows<ValidationException> {
            ComplexValidators.validateComplexData("invalid-format", ValidationContext())
        }
        assertTrue(exception.errors.isNotEmpty())
    }

    @Test
    fun `should accept valid formatted data`() = runTest {
        assertTrue(ComplexValidators.validateComplexData("AAA-000-BBB", ValidationContext()))
        assertTrue(ComplexValidators.validateComplexData("XYZ-999-ABC", ValidationContext()))
        assertTrue(ComplexValidators.validateComplexData("DEF-123-GHI", ValidationContext()))
    }
}
