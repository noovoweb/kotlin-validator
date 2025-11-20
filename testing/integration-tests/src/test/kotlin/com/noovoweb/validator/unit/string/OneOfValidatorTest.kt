package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OneOfValidatorTest {

    @Test
    fun `oneof validator accepts values in allowed list`() = runTest {
        val validator = OneOfValidator()

        validator.validate(OneOf(name = "option1"))
        validator.validate(OneOf(name = "option2"))
        validator.validate(OneOf(name = "option3"))
    }

    @Test
    fun `oneof validator rejects values not in allowed list`() = runTest {
        val validator = OneOfValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(OneOf(name = "option4"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(OneOf(name = "invalid"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(OneOf(name = "OPTION1"))
        }
        assertTrue(exception3.errors.containsKey("name"))

        val exception4 = assertThrows<ValidationException> {
            validator.validate(OneOf(name = ""))
        }
        assertTrue(exception4.errors.containsKey("name"))
    }

    @Test
    fun `oneof validator allows null when not required`() = runTest {
        val validator = OneOfValidator()
        validator.validate(OneOf(name = null))
    }

    @Test
    fun `oneof validator provides error message`() = runTest {
        val validator = OneOfValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(OneOf(name = "invalid"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
