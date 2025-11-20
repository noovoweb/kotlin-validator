package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordDataValidatorTest {
    @Test
    fun `custom validator accepts strong password`() = runTest {
        val validator = PasswordDataValidator()
        validator.validate(PasswordData(password = "StrongP@ss123"))
        validator.validate(PasswordData(password = "Valid#Pass456"))
        validator.validate(PasswordData(password = "C0mplex!Password"))
    }

    @Test
    fun `custom validator rejects password without uppercase`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "weakpass123!"))
        }
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator rejects password without lowercase`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "WEAKPASS123!"))
        }
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator rejects password without digit`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "WeakPassword!"))
        }
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator rejects password without special character`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "WeakPass123"))
        }
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator rejects password too short`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "Pass1!"))
        }
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator combines with other validators`() = runTest {
        val validator = PasswordDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PasswordData(password = "Weak1!"))
        }
        // Should fail MinLength before reaching custom validator
        assertTrue(exception.errors.containsKey("password"))
    }

    @Test
    fun `custom validator uses custom message`() = runTest {
        val exception = assertThrows<ValidationException> {
            PasswordDataValidator().validate(PasswordData(password = "weakpassword"))
        }
        assertTrue(exception.errors.containsKey("password"))
        val errorMessages = exception.errors["password"]!!
        assertFalse(errorMessages.isEmpty())
    }
}
