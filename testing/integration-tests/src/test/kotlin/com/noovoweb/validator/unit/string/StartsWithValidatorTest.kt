package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StartsWithValidatorTest {
    @Test
    fun `startswith validator accepts strings with correct prefix`() =
        runTest {
            val validator = StartsWithValidator()

            validator.validate(StartsWith(name = "prefix"))
            validator.validate(StartsWith(name = "prefixtest"))
            validator.validate(StartsWith(name = "prefix123"))
            validator.validate(StartsWith(name = "prefix_anything"))
        }

    @Test
    fun `startswith validator rejects strings without correct prefix`() =
        runTest {
            val validator = StartsWithValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(StartsWith(name = "test"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(StartsWith(name = "prefi"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(StartsWith(name = "PREFIX"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(StartsWith(name = "testprefix"))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `startswith validator allows null when not required`() =
        runTest {
            val validator = StartsWithValidator()
            validator.validate(StartsWith(name = null))
        }

    @Test
    fun `startswith validator provides error message`() =
        runTest {
            val validator = StartsWithValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(StartsWith(name = "invalid"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
