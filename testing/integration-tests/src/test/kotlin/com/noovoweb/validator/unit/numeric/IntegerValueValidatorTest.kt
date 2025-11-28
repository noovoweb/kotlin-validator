package com.noovoweb.validator.unit.numeric

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntegerValueValidatorTest {
    @Test
    fun `integer validator accepts whole numbers`() =
        runTest {
            val validator = IntegerValueValidator()

            validator.validate(IntegerValue(value = 0.0))
            validator.validate(IntegerValue(value = 1.0))
            validator.validate(IntegerValue(value = 100.0))
            validator.validate(IntegerValue(value = -50.0))
        }

    @Test
    fun `integer validator rejects decimal numbers`() =
        runTest {
            val validator = IntegerValueValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(IntegerValue(value = 1.5))
                }
            assertTrue(exception1.errors.containsKey("value"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(IntegerValue(value = 0.1))
                }
            assertTrue(exception2.errors.containsKey("value"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(IntegerValue(value = -10.99))
                }
            assertTrue(exception3.errors.containsKey("value"))
        }

    @Test
    fun `integer validator allows null when not required`() =
        runTest {
            val validator = IntegerValueValidator()
            validator.validate(IntegerValue(value = null))
        }

    @Test
    fun `integer validator provides error message`() =
        runTest {
            val validator = IntegerValueValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(IntegerValue(value = 5.5))
                }

            assertTrue(exception.errors.containsKey("value"))
            assertFalse(exception.errors["value"]!!.isEmpty())
        }
}
