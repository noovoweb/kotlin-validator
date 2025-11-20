package com.noovoweb.validator.unit.custom

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvenNumberDataValidatorTest {
    @Test
    fun `custom validator accepts even numbers`() = runTest {
        val validator = EvenNumberDataValidator()
        validator.validate(EvenNumberData(number = 0))
        validator.validate(EvenNumberData(number = 2))
        validator.validate(EvenNumberData(number = 100))
        validator.validate(EvenNumberData(number = -4))
    }

    @Test
    fun `custom validator rejects odd numbers`() = runTest {
        val validator = EvenNumberDataValidator()
        assertThrows<ValidationException> {
            validator.validate(EvenNumberData(number = 1))
        }
        assertThrows<ValidationException> {
            validator.validate(EvenNumberData(number = 3))
        }
        assertThrows<ValidationException> {
            validator.validate(EvenNumberData(number = -5))
        }
    }

    @Test
    fun `custom validator allows null`() = runTest {
        val validator = EvenNumberDataValidator()
        validator.validate(EvenNumberData(number = null))
    }

    @Test
    fun `custom validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            EvenNumberDataValidator().validate(EvenNumberData(number = 7))
        }
        assertTrue(exception.errors.containsKey("number"))
        assertFalse(exception.errors["number"]!!.isEmpty())
    }
}
