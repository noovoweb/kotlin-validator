package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NullableDataValidatorTest {
    @Test
    fun `nullable validator accepts null value`() = runTest {
        val validator = NullableDataValidator()
        validator.validate(NullableData(email = null))
    }

    @Test
    fun `nullable validator accepts valid value`() = runTest {
        val validator = NullableDataValidator()
        validator.validate(NullableData(email = "test@example.com"))
    }

    @Test
    fun `nullable validator rejects invalid value when present`() = runTest {
        val validator = NullableDataValidator()
        assertThrows<ValidationException> {
            validator.validate(NullableData(email = "invalid-email"))
        }
    }

    @Test
    fun `nullable validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            NullableDataValidator().validate(NullableData(email = "not-an-email"))
        }
        assertTrue(exception.errors.containsKey("email"))
        assertFalse(exception.errors["email"]!!.isEmpty())
    }
}
