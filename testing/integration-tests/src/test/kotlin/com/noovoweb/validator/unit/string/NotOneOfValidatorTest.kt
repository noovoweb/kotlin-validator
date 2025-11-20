package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotOneOfValidatorTest {

    @Test
    fun `notoneof validator accepts values not in forbidden list`() = runTest {
        val validator = NotOneOfValidator()

        validator.validate(NotOneOf(name = "allowed"))
        validator.validate(NotOneOf(name = "valid"))
        validator.validate(NotOneOf(name = "acceptable"))
        validator.validate(NotOneOf(name = ""))
    }

    @Test
    fun `notoneof validator rejects values in forbidden list`() = runTest {
        val validator = NotOneOfValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(NotOneOf(name = "forbidden1"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(NotOneOf(name = "forbidden2"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(NotOneOf(name = "forbidden3"))
        }
        assertTrue(exception3.errors.containsKey("name"))
    }

    @Test
    fun `notoneof validator is case sensitive`() = runTest {
        val validator = NotOneOfValidator()

        validator.validate(NotOneOf(name = "FORBIDDEN1"))
        validator.validate(NotOneOf(name = "Forbidden1"))
    }

    @Test
    fun `notoneof validator allows null when not required`() = runTest {
        val validator = NotOneOfValidator()
        validator.validate(NotOneOf(name = null))
    }

    @Test
    fun `notoneof validator provides error message`() = runTest {
        val validator = NotOneOfValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(NotOneOf(name = "forbidden1"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
