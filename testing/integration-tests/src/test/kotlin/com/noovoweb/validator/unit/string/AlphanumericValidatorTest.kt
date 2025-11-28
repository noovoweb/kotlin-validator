package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlphanumericValidatorTest {
    @Test
    fun `alphanumeric validator accepts alphanumeric strings`() =
        runTest {
            val validator = AlphanumericValidator()

            validator.validate(Alphanumeric(name = "abc123"))
            validator.validate(Alphanumeric(name = "ABC"))
            validator.validate(Alphanumeric(name = "123"))
            validator.validate(Alphanumeric(name = "test123TEST"))
        }

    @Test
    fun `alphanumeric validator rejects non-alphanumeric strings`() =
        runTest {
            val validator = AlphanumericValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Alphanumeric(name = "abc def"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Alphanumeric(name = "abc-123"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Alphanumeric(name = "abc@123"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(Alphanumeric(name = "abc_def"))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `alphanumeric validator allows null when not required`() =
        runTest {
            val validator = AlphanumericValidator()
            validator.validate(Alphanumeric(name = null))
        }

    @Test
    fun `alphanumeric validator provides error message`() =
        runTest {
            val validator = AlphanumericValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Alphanumeric(name = "abc def"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
