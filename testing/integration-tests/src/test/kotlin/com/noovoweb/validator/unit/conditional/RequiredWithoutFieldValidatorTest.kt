package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredWithoutFieldValidatorTest {
    @Test
    fun `requiredwithout validator accepts when all dependencies absent and field present`() = runTest {
        val validator = RequiredWithoutFieldValidator()
        validator.validate(RequiredWithoutField(email = null, phone = null, mailingAddress = "123 Main St"))
    }

    @Test
    fun `requiredwithout validator accepts when any dependency present`() = runTest {
        val validator = RequiredWithoutFieldValidator()
        validator.validate(RequiredWithoutField(email = "test@example.com", phone = null, mailingAddress = null))
    }

    @Test
    fun `requiredwithout validator rejects when all dependencies absent but field absent`() = runTest {
        val validator = RequiredWithoutFieldValidator()
        assertThrows<ValidationException> {
            validator.validate(RequiredWithoutField(email = null, phone = null, mailingAddress = null))
        }
    }

    @Test
    fun `requiredwithout validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            RequiredWithoutFieldValidator().validate(RequiredWithoutField(email = null, phone = null, mailingAddress = null))
        }
        assertTrue(exception.errors.containsKey("mailingAddress"))
        assertFalse(exception.errors["mailingAddress"]!!.isEmpty())
    }
}
