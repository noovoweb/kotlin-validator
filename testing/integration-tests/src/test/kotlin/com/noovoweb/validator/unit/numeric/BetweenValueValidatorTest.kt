package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BetweenValueValidatorTest {

    @Test
    fun `between validator accepts values within range`() = runTest {
        val validator = BetweenValueValidator()

        validator.validate(BetweenValue(value = 10))
        validator.validate(BetweenValue(value = 50))
        validator.validate(BetweenValue(value = 100))
    }

    @Test
    fun `between validator rejects values below minimum`() = runTest {
        val validator = BetweenValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(BetweenValue(value = 9))
        }
        assertTrue(exception.errors.containsKey("value"))
    }

    @Test
    fun `between validator rejects values above maximum`() = runTest {
        val validator = BetweenValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(BetweenValue(value = 101))
        }
        assertTrue(exception.errors.containsKey("value"))
    }

    @Test
    fun `between validator allows null when not required`() = runTest {
        val validator = BetweenValueValidator()
        validator.validate(BetweenValue(value = null))
    }

    @Test
    fun `between validator provides error message`() = runTest {
        val validator = BetweenValueValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(BetweenValue(value = 5))
        }

        assertTrue(exception.errors.containsKey("value"))
        assertFalse(exception.errors["value"]!!.isEmpty())
    }
}
