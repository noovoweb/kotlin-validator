package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {

    // Email Validator Tests
    @Test
    fun `email validator accepts valid email addresses`() = runTest {
        val validator = EmailValidator()

        validator.validate(Email(email = "user@example.com"))
        validator.validate(Email(email = "test.user@domain.co.uk"))
        validator.validate(Email(email = "name+tag@company.org"))
        validator.validate(Email(email = "valid_email123@test-domain.com"))
    }

    @Test
    fun `email validator rejects invalid email addresses`() = runTest {
        val validator = EmailValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(Email(email = "notanemail"))
        }
        assertTrue(exception1.errors.containsKey("email"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(Email(email = "missing@domain"))
        }
        assertTrue(exception2.errors.containsKey("email"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(Email(email = "@nodomain.com"))
        }
        assertTrue(exception3.errors.containsKey("email"))

        val exception4 = assertThrows<ValidationException> {
            validator.validate(Email(email = "spaces in@email.com"))
        }
        assertTrue(exception4.errors.containsKey("email"))
    }

    @Test
    fun `email validator allows null when not required`() = runTest {
        val validator = EmailValidator()
        validator.validate(Email(email = null))
    }

    @Test
    fun `email validator provides error message`() = runTest {
        val validator = EmailValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(Email(email = "invalid"))
        }

        assertTrue(exception.errors.containsKey("email"))
        assertFalse(exception.errors["email"]!!.isEmpty())
    }
}