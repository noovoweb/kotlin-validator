package com.noovoweb.validator.unit.boolean

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AcceptedValueValidatorTest {

    @Test
    fun `accepted validator accepts true value`() = runTest {
        val validator = AcceptedValueValidator()

        validator.validate(AcceptedValue(value = true))
    }

    @Test
    fun `accepted validator rejects false value`() = runTest {
        val validator = AcceptedValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(AcceptedValue(value = false))
        }
        assertTrue(exception.errors.containsKey("value"))
    }

    @Test
    fun `accepted validator allows null when not required`() = runTest {
        val validator = AcceptedValueValidator()
        validator.validate(AcceptedValue(value = null))
    }

    @Test
    fun `accepted validator provides error message`() = runTest {
        val validator = AcceptedValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(AcceptedValue(value = false))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
