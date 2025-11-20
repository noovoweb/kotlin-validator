package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DivisibleByValueValidatorTest {

    @Test
    fun `divisibleby validator accepts values divisible by divisor`() = runTest {
        val validator = DivisibleByValueValidator()

        validator.validate(DivisibleByValue(value = 0))
        validator.validate(DivisibleByValue(value = 5))
        validator.validate(DivisibleByValue(value = 10))
        validator.validate(DivisibleByValue(value = 25))
        validator.validate(DivisibleByValue(value = -15))
    }

    @Test
    fun `divisibleby validator rejects values not divisible by divisor`() = runTest {
        val validator = DivisibleByValueValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(DivisibleByValue(value = 1))
        }
        assertTrue(exception1.errors.containsKey("value"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(DivisibleByValue(value = 7))
        }
        assertTrue(exception2.errors.containsKey("value"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(DivisibleByValue(value = 13))
        }
        assertTrue(exception3.errors.containsKey("value"))
    }

    @Test
    fun `divisibleby validator allows null when not required`() = runTest {
        val validator = DivisibleByValueValidator()
        validator.validate(DivisibleByValue(value = null))
    }

    @Test
    fun `divisibleby validator provides error message`() = runTest {
        val validator = DivisibleByValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(DivisibleByValue(value = 3))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
