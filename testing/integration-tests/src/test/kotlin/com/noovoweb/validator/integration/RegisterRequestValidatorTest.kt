package com.noovoweb.validator.integration

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class RegisterRequestValidatorTest {

    @Test
    fun `valid register request passes validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        validator.validate(request)
    }

    @Test
    fun `invalid email fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "invalid-email",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("email"))
    }

    @Test
    fun `email too long fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "verylongemailaddress@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("email"))
    }

    @Test
    fun `password mismatch fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "DifferentPassword",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("passwordConfirmation"))
    }

    @Test
    fun `first name too short fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "J",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("firstName"))
    }

    @Test
    fun `first name with non-alpha characters fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John123",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("firstName"))
    }

    @Test
    fun `nullable last name can be null`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = null,
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        validator.validate(request)
    }

    @Test
    fun `age below minimum fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 16,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("age"))
    }

    @Test
    fun `age above maximum fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 130,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("age"))
    }

    @Test
    fun `invalid phone number pattern fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "abc123",
            acceptTerms = true
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("phoneNumber"))
    }

    @Test
    fun `terms not accepted fails validation`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = false
        )

        val exception = assertThrows<ValidationException> {
            validator.validate(request)
        }

        assertTrue(exception.errors.containsKey("acceptTerms"))
    }

    @Test
    fun `validateResult returns Success for valid request`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "user@example.com",
            password = "StrongP@ss123",
            passwordConfirmation = "StrongP@ss123",
            firstName = "John",
            lastName = "Doe",
            age = 25,
            phoneNumber = "+1234567890",
            acceptTerms = true
        )

        val result = validator.validateResult(request)

        assertTrue(result.isSuccess())
    }

    @Test
    fun `validateResult returns Failure for invalid request`() = runTest {
        val validator = RegisterRequestValidator()
        val request = RegisterRequest(
            email = "invalid-email",
            password = "StrongP@ss123",
            passwordConfirmation = "DifferentPassword",
            firstName = "J",
            lastName = "Doe",
            age = 16,
            phoneNumber = "abc123",
            acceptTerms = false
        )

        val result = validator.validateResult(request)

        assertTrue(result.isFailure())
        val failure = result as com.noovoweb.validator.ValidationResult.Failure
        assertTrue(failure.errors.containsKey("email"))
        assertTrue(failure.errors.containsKey("passwordConfirmation"))
        assertTrue(failure.errors.containsKey("firstName"))
        assertTrue(failure.errors.containsKey("age"))
        assertTrue(failure.errors.containsKey("phoneNumber"))
        assertTrue(failure.errors.containsKey("acceptTerms"))
    }
}
