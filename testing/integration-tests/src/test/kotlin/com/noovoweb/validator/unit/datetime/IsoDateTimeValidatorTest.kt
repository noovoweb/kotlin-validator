package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsoDateTimeValidatorTest {
    @Test
    fun `isodatetime validator accepts valid ISO datetimes`() =
        runTest {
            val validator = IsoDateTimeValidator()

            validator.validate(IsoDateTime(datetime = "2024-11-16T10:30:00Z"))
            validator.validate(IsoDateTime(datetime = "2024-11-16T10:30:00"))
            validator.validate(IsoDateTime(datetime = "2024-11-16T10:30:00.123Z"))
            validator.validate(IsoDateTime(datetime = "2024-11-16T10:30:00+00:00"))
        }

    @Test
    fun `isodatetime validator rejects invalid ISO datetimes`() =
        runTest {
            val validator = IsoDateTimeValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(IsoDateTime(datetime = "2024-11-16"))
                }
            assertTrue(exception1.errors.containsKey("datetime"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(IsoDateTime(datetime = "16-11-2024 10:30:00"))
                }
            assertTrue(exception2.errors.containsKey("datetime"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(IsoDateTime(datetime = "not a datetime"))
                }
            assertTrue(exception3.errors.containsKey("datetime"))
        }

    @Test
    fun `isodatetime validator allows null when not required`() =
        runTest {
            val validator = IsoDateTimeValidator()
            validator.validate(IsoDateTime(datetime = null))
        }

    @Test
    fun `isodatetime validator provides error message`() =
        runTest {
            val validator = IsoDateTimeValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(IsoDateTime(datetime = "invalid"))
                }

            assertTrue(exception.errors.containsKey("datetime"))
            assertFalse(exception.errors["datetime"]!!.isEmpty())
        }
}
