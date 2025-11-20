package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternValidatorTest {

    @Test
    fun `pattern validator accepts strings matching pattern`() = runTest {
        val validator = PatternValidator()

        validator.validate(Pattern(name = "John"))
        validator.validate(Pattern(name = "Alice"))
        validator.validate(Pattern(name = "Bob"))
    }

    @Test
    fun `pattern validator rejects strings not matching pattern`() = runTest {
        val validator = PatternValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(Pattern(name = "john"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(Pattern(name = "JOHN"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(Pattern(name = "123"))
        }
        assertTrue(exception3.errors.containsKey("name"))

        val exception4 = assertThrows<ValidationException> {
            validator.validate(Pattern(name = "John123"))
        }
        assertTrue(exception4.errors.containsKey("name"))
    }

    @Test
    fun `pattern validator allows null when not required`() = runTest {
        val validator = PatternValidator()
        validator.validate(Pattern(name = null))
    }

    @Test
    fun `pattern validator provides error message`() = runTest {
        val validator = PatternValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(Pattern(name = "invalid123"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
