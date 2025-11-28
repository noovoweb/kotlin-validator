package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OddValueValidatorTest {
    @Test
    fun `odd validator accepts odd numbers`() =
        runTest {
            val validator = OddValueValidator()

            validator.validate(OddValue(value = 1))
            validator.validate(OddValue(value = 3))
            validator.validate(OddValue(value = 7))
            validator.validate(OddValue(value = -13))
            validator.validate(OddValue(value = 99))
        }

    @Test
    fun `odd validator rejects even numbers`() =
        runTest {
            val validator = OddValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(OddValue(value = 0))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(OddValue(value = 2))
                }
            assertTrue(exception2.errors.containsKey("value"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(OddValue(value = -8))
                }
            assertTrue(exception3.errors.containsKey("value"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(OddValue(value = 100))
                }
            assertTrue(exception4.errors.containsKey("value"))
        }

    @Test
    fun `odd validator allows null when not required`() =
        runTest {
            val validator = OddValueValidator()
            validator.validate(OddValue(value = null))
        }

    @Test
    fun `odd validator provides error message`() =
        runTest {
            val validator = OddValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(OddValue(value = 4))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
