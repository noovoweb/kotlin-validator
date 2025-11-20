package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UppercaseValidatorTest {

    @Test
    fun `uppercase validator accepts uppercase strings`() = runTest {
        val validator = UppercaseValidator()

        validator.validate(Uppercase(name = "ABC"))
        validator.validate(Uppercase(name = "UPPERCASE"))
        validator.validate(Uppercase(name = "TEST123"))
        validator.validate(Uppercase(name = "HELLO-WORLD"))
    }

    @Test
    fun `uppercase validator rejects non-uppercase strings`() = runTest {
        val validator = UppercaseValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(Uppercase(name = "abc"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(Uppercase(name = "Hello"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(Uppercase(name = "HELLOworld"))
        }
        assertTrue(exception3.errors.containsKey("name"))
    }

    @Test
    fun `uppercase validator allows null when not required`() = runTest {
        val validator = UppercaseValidator()
        validator.validate(Uppercase(name = null))
    }

    @Test
    fun `uppercase validator provides error message`() = runTest {
        val validator = UppercaseValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(Uppercase(name = "lowercase"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
