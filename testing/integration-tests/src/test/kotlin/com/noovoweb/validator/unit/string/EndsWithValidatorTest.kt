package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EndsWithValidatorTest {
    @Test
    fun `endswith validator accepts strings with correct suffix`() =
        runTest {
            val validator = EndsWithValidator()

            validator.validate(EndsWith(name = "suffix"))
            validator.validate(EndsWith(name = "testsuffix"))
            validator.validate(EndsWith(name = "123suffix"))
            validator.validate(EndsWith(name = "anything_suffix"))
        }

    @Test
    fun `endswith validator rejects strings without correct suffix`() =
        runTest {
            val validator = EndsWithValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(EndsWith(name = "test"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(EndsWith(name = "suffi"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(EndsWith(name = "SUFFIX"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(EndsWith(name = "suffixtest"))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `endswith validator allows null when not required`() =
        runTest {
            val validator = EndsWithValidator()
            validator.validate(EndsWith(name = null))
        }

    @Test
    fun `endswith validator provides error message`() =
        runTest {
            val validator = EndsWithValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EndsWith(name = "invalid"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
