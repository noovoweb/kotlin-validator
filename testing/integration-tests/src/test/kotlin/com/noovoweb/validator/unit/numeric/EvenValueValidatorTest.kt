package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvenValueValidatorTest {

    @Test
    fun `even validator accepts even numbers`() = runTest {
        val validator = EvenValueValidator()

        validator.validate(EvenValue(value = 0))
        validator.validate(EvenValue(value = 2))
        validator.validate(EvenValue(value = 10))
        validator.validate(EvenValue(value = -8))
        validator.validate(EvenValue(value = 100))
    }

    @Test
    fun `even validator rejects odd numbers`() = runTest {
        val validator = EvenValueValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(EvenValue(value = 1))
        }
        assertTrue(exception1.errors.containsKey("value"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(EvenValue(value = 7))
        }
        assertTrue(exception2.errors.containsKey("value"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(EvenValue(value = -13))
        }
        assertTrue(exception3.errors.containsKey("value"))
    }

    @Test
    fun `even validator allows null when not required`() = runTest {
        val validator = EvenValueValidator()
        validator.validate(EvenValue(value = null))
    }

    @Test
    fun `even validator provides error message`() = runTest {
        val validator = EvenValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(EvenValue(value = 3))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
