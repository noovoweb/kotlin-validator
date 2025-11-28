package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateFormatValidatorTest {
    @Test
    fun `dateformat validator accepts valid date formats`() =
        runTest {
            val validator = DateFormatValidator()

            validator.validate(DateFormat(date = "2024-11-16"))
            validator.validate(DateFormat(date = "2000-01-01"))
            validator.validate(DateFormat(date = "1999-12-31"))
        }

    @Test
    fun `dateformat validator rejects invalid date formats`() =
        runTest {
            val validator = DateFormatValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(DateFormat(date = "16-11-2024"))
                }
            assertTrue(exception1.errors.containsKey("date"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(DateFormat(date = "2024/11/16"))
                }
            assertTrue(exception2.errors.containsKey("date"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(DateFormat(date = "not a date"))
                }
            assertTrue(exception3.errors.containsKey("date"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(DateFormat(date = "2024-13-01"))
                }
            assertTrue(exception4.errors.containsKey("date"))
        }

    @Test
    fun `dateformat validator allows null when not required`() =
        runTest {
            val validator = DateFormatValidator()
            validator.validate(DateFormat(date = null))
        }

    @Test
    fun `dateformat validator provides error message`() =
        runTest {
            val validator = DateFormatValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(DateFormat(date = "invalid"))
                }

            assertTrue(exception.errors.containsKey("date"))
            assertFalse(exception.errors["date"]!!.isEmpty())
        }
}
