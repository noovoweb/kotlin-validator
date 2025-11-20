package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserWithCustomValidationValidatorTest {
    @Test
    fun `custom validator accepts valid username`() = runTest {
        val validator = UserWithCustomValidationValidator()
        validator.validate(UserWithCustomValidation(username = "john_doe"))
        validator.validate(UserWithCustomValidation(username = "user123"))
        validator.validate(UserWithCustomValidation(username = "test_user_456"))
    }

    @Test
    fun `custom validator rejects username too short`() = runTest {
        val validator = UserWithCustomValidationValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(UserWithCustomValidation(username = "ab"))
        }
        assertTrue(exception.errors.containsKey("username"))
    }

    @Test
    fun `custom validator rejects username with special chars`() = runTest {
        val validator = UserWithCustomValidationValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(UserWithCustomValidation(username = "john@doe"))
        }
        assertTrue(exception.errors.containsKey("username"))
    }

    @Test
    fun `custom validator rejects username with spaces`() = runTest {
        val validator = UserWithCustomValidationValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(UserWithCustomValidation(username = "john doe"))
        }
        assertTrue(exception.errors.containsKey("username"))
    }

    @Test
    fun `custom validator allows underscore in username`() = runTest {
        val validator = UserWithCustomValidationValidator()
        validator.validate(UserWithCustomValidation(username = "valid_user"))
    }

    @Test
    fun `custom validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            UserWithCustomValidationValidator().validate(UserWithCustomValidation(username = "a!"))
        }
        assertTrue(exception.errors.containsKey("username"))
        assertFalse(exception.errors["username"]!!.isEmpty())
    }
}
