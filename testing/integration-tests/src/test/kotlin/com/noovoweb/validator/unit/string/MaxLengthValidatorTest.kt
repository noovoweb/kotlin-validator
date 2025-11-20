package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxLengthValidatorTest {

    @Test
    fun `maxlength validator accepts strings at or below maximum`() = runTest {
        val validator = MaxLengthValidator()

        validator.validate(MaxLength(name = "short"))
        validator.validate(MaxLength(name = "12345678901234567890"))
        validator.validate(MaxLength(name = ""))
    }

    @Test
    fun `maxlength validator rejects strings above maximum`() = runTest {
        val validator = MaxLengthValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(MaxLength(name = "123456789012345678901"))
        }
        assertTrue(exception.errors.containsKey("name"))
    }

    @Test
    fun `maxlength validator allows null when not required`() = runTest {
        val validator = MaxLengthValidator()
        validator.validate(MaxLength(name = null))
    }

    @Test
    fun `maxlength validator provides error message`() = runTest {
        val validator = MaxLengthValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(MaxLength(name = "this is way too long for the maximum length limit"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
