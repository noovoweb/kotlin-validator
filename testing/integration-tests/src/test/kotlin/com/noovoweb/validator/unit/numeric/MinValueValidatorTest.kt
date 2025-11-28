package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinValueValidatorTest {
    @Test
    fun `min validator accepts values at or above minimum`() =
        runTest {
            val validator = MinValueValidator()

            validator.validate(MinValue(value = 10))
            validator.validate(MinValue(value = 11))
            validator.validate(MinValue(value = 100))
        }

    @Test
    fun `min validator rejects values below minimum`() =
        runTest {
            val validator = MinValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(MinValue(value = 9))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(MinValue(value = 0))
                }
            assertTrue(exception2.errors.containsKey("value"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(MinValue(value = -10))
                }
            assertTrue(exception3.errors.containsKey("value"))
        }

    @Test
    fun `min validator allows null when not required`() =
        runTest {
            val validator = MinValueValidator()
            validator.validate(MinValue(value = null))
        }

    @Test
    fun `min validator provides error message`() =
        runTest {
            val validator = MinValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(MinValue(value = 5))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
