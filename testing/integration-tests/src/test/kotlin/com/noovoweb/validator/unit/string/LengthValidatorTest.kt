package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LengthValidatorTest {
    @Test
    fun `length validator accepts strings within range`() =
        runTest {
            val validator = LengthValidator()

            validator.validate(Length(name = "12345"))
            validator.validate(Length(name = "123456"))
            validator.validate(Length(name = "1234567890"))
        }

    @Test
    fun `length validator rejects strings too short`() =
        runTest {
            val validator = LengthValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Length(name = "1234"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Length(name = ""))
                }
            assertTrue(exception2.errors.containsKey("name"))
        }

    @Test
    fun `length validator rejects strings too long`() =
        runTest {
            val validator = LengthValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Length(name = "12345678901"))
                }
            assertTrue(exception.errors.containsKey("name"))
        }

    @Test
    fun `length validator allows null when not required`() =
        runTest {
            val validator = LengthValidator()
            validator.validate(Length(name = null))
        }

    @Test
    fun `length validator provides error message`() =
        runTest {
            val validator = LengthValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Length(name = "123"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
