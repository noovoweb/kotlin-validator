package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsciiValidatorTest {
    @Test
    fun `ascii validator accepts ASCII strings`() =
        runTest {
            val validator = AsciiValidator()

            validator.validate(Ascii(name = "abc123"))
            validator.validate(Ascii(name = "Hello World!"))
            validator.validate(Ascii(name = "test@example.com"))
            validator.validate(Ascii(name = "Special: !@#$%^&*()"))
        }

    @Test
    fun `ascii validator rejects non-ASCII strings`() =
        runTest {
            val validator = AsciiValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Ascii(name = "café"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Ascii(name = "Hello 世界"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Ascii(name = "Ñoño"))
                }
            assertTrue(exception3.errors.containsKey("name"))
        }

    @Test
    fun `ascii validator allows null when not required`() =
        runTest {
            val validator = AsciiValidator()
            validator.validate(Ascii(name = null))
        }

    @Test
    fun `ascii validator provides error message`() =
        runTest {
            val validator = AsciiValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Ascii(name = "café"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
