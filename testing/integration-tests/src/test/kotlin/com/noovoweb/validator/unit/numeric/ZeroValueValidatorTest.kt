package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZeroValueValidatorTest {

    @Test
    fun `zero validator accepts zero`() = runTest {
        val validator = ZeroValueValidator()

        validator.validate(ZeroValue(value = 0))
    }

    @Test
    fun `zero validator rejects positive values`() = runTest {
        val validator = ZeroValueValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(ZeroValue(value = 1))
        }
        assertTrue(exception1.errors.containsKey("value"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(ZeroValue(value = 100))
        }
        assertTrue(exception2.errors.containsKey("value"))
    }

    @Test
    fun `zero validator rejects negative values`() = runTest {
        val validator = ZeroValueValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(ZeroValue(value = -1))
        }
        assertTrue(exception1.errors.containsKey("value"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(ZeroValue(value = -100))
        }
        assertTrue(exception2.errors.containsKey("value"))
    }

    @Test
    fun `zero validator allows null when not required`() = runTest {
        val validator = ZeroValueValidator()
        validator.validate(ZeroValue(value = null))
    }

    @Test
    fun `zero validator provides error message`() = runTest {
        val validator = ZeroValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(ZeroValue(value = 5))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
