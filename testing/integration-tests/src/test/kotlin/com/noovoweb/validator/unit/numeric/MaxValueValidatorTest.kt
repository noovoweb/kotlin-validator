package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxValueValidatorTest {
    @Test
    fun `max validator accepts values at or below maximum`() =
        runTest {
            val validator = MaxValueValidator()

            validator.validate(MaxValue(value = 100))
            validator.validate(MaxValue(value = 99))
            validator.validate(MaxValue(value = 0))
            validator.validate(MaxValue(value = -10))
        }

    @Test
    fun `max validator rejects values above maximum`() =
        runTest {
            val validator = MaxValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(MaxValue(value = 101))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(MaxValue(value = 200))
                }
            assertTrue(exception2.errors.containsKey("value"))
        }

    @Test
    fun `max validator allows null when not required`() =
        runTest {
            val validator = MaxValueValidator()
            validator.validate(MaxValue(value = null))
        }

    @Test
    fun `max validator provides error message`() =
        runTest {
            val validator = MaxValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(MaxValue(value = 150))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
