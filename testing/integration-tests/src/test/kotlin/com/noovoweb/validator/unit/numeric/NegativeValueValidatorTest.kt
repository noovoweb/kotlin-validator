package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NegativeValueValidatorTest {
    @Test
    fun `negative validator accepts negative values`() =
        runTest {
            val validator = NegativeValueValidator()

            validator.validate(NegativeValue(value = -1))
            validator.validate(NegativeValue(value = -10))
            validator.validate(NegativeValue(value = -100))
        }

    @Test
    fun `negative validator rejects zero`() =
        runTest {
            val validator = NegativeValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(NegativeValue(value = 0))
                }
            assertTrue(exception.errors.containsKey("value"))
        }

    @Test
    fun `negative validator rejects positive values`() =
        runTest {
            val validator = NegativeValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(NegativeValue(value = 1))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(NegativeValue(value = 100))
                }
            assertTrue(exception2.errors.containsKey("value"))
        }

    @Test
    fun `negative validator allows null when not required`() =
        runTest {
            val validator = NegativeValueValidator()
            validator.validate(NegativeValue(value = null))
        }

    @Test
    fun `negative validator provides error message`() =
        runTest {
            val validator = NegativeValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(NegativeValue(value = 5))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
