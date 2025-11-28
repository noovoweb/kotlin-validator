package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlphaValidatorTest {
    @Test
    fun `alpha validator accepts alphabetic strings`() =
        runTest {
            val validator = AlphaValidator()

            validator.validate(Alpha(name = "abc"))
            validator.validate(Alpha(name = "ABC"))
            validator.validate(Alpha(name = "AbCdEf"))
            validator.validate(Alpha(name = "z"))
        }

    @Test
    fun `alpha validator rejects non-alphabetic strings`() =
        runTest {
            val validator = AlphaValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Alpha(name = "abc123"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Alpha(name = "abc def"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Alpha(name = "abc-def"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(Alpha(name = "123"))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `alpha validator allows null when not required`() =
        runTest {
            val validator = AlphaValidator()
            validator.validate(Alpha(name = null))
        }

    @Test
    fun `alpha validator provides error message`() =
        runTest {
            val validator = AlphaValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Alpha(name = "abc123"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
