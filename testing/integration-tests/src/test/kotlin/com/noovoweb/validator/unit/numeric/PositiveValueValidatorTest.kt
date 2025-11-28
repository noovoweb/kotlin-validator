package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PositiveValueValidatorTest {
    @Test
    fun `positive validator accepts positive values`() =
        runTest {
            val validator = PositiveValueValidator()

            validator.validate(PositiveValue(value = 1))
            validator.validate(PositiveValue(value = 10))
            validator.validate(PositiveValue(value = 100))
        }

    @Test
    fun `positive validator rejects zero`() =
        runTest {
            val validator = PositiveValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(PositiveValue(value = 0))
                }
            assertTrue(exception.errors.containsKey("value"))
        }

    @Test
    fun `positive validator rejects negative values`() =
        runTest {
            val validator = PositiveValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(PositiveValue(value = -1))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(PositiveValue(value = -100))
                }
            assertTrue(exception2.errors.containsKey("value"))
        }

    @Test
    fun `positive validator allows null when not required`() =
        runTest {
            val validator = PositiveValueValidator()
            validator.validate(PositiveValue(value = null))
        }

    @Test
    fun `positive validator provides error message`() =
        runTest {
            val validator = PositiveValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(PositiveValue(value = -5))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
