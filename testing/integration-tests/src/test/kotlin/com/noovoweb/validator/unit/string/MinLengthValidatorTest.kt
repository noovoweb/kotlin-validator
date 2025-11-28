package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinLengthValidatorTest {
    @Test
    fun `minlength validator accepts strings at or above minimum`() =
        runTest {
            val validator = MinLengthValidator()

            validator.validate(MinLength(name = "12345"))
            validator.validate(MinLength(name = "123456"))
            validator.validate(MinLength(name = "this is a long string"))
        }

    @Test
    fun `minlength validator rejects strings below minimum`() =
        runTest {
            val validator = MinLengthValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(MinLength(name = "1234"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(MinLength(name = ""))
                }
            assertTrue(exception2.errors.containsKey("name"))
        }

    @Test
    fun `minlength validator allows null when not required`() =
        runTest {
            val validator = MinLengthValidator()
            validator.validate(MinLength(name = null))
        }

    @Test
    fun `minlength validator provides error message`() =
        runTest {
            val validator = MinLengthValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(MinLength(name = "123"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
