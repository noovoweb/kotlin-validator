package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecimalValueValidatorTest {

    @Test
    fun `decimal validator accepts decimal numbers`() = runTest {
        val validator = DecimalValueValidator()

        validator.validate(DecimalValue(value = 1.5))
        validator.validate(DecimalValue(value = 0.1))
        validator.validate(DecimalValue(value = -10.99))
        validator.validate(DecimalValue(value = 3.14159))
    }

    @Test
    fun `decimal validator rejects whole numbers`() = runTest {
        val validator = DecimalValueValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(DecimalValue(value = 0.0))
        }
        assertTrue(exception1.errors.containsKey("value"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(DecimalValue(value = 1.0))
        }
        assertTrue(exception2.errors.containsKey("value"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(DecimalValue(value = -100.0))
        }
        assertTrue(exception3.errors.containsKey("value"))
    }

    @Test
    fun `decimal validator allows null when not required`() = runTest {
        val validator = DecimalValueValidator()
        validator.validate(DecimalValue(value = null))
    }

    @Test
    fun `decimal validator provides error message`() = runTest {
        val validator = DecimalValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(DecimalValue(value = 5.0))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
