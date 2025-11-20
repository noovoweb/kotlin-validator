package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LowercaseValidatorTest {

    @Test
    fun `lowercase validator accepts lowercase strings`() = runTest {
        val validator = LowercaseValidator()

        validator.validate(Lowercase(name = "abc"))
        validator.validate(Lowercase(name = "lowercase"))
        validator.validate(Lowercase(name = "test123"))
        validator.validate(Lowercase(name = "hello-world"))
    }

    @Test
    fun `lowercase validator rejects non-lowercase strings`() = runTest {
        val validator = LowercaseValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(Lowercase(name = "ABC"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(Lowercase(name = "Hello"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(Lowercase(name = "helloWorld"))
        }
        assertTrue(exception3.errors.containsKey("name"))
    }

    @Test
    fun `lowercase validator allows null when not required`() = runTest {
        val validator = LowercaseValidator()
        validator.validate(Lowercase(name = null))
    }

    @Test
    fun `lowercase validator provides error message`() = runTest {
        val validator = LowercaseValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(Lowercase(name = "UPPERCASE"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
