package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PastDateValidatorTest {
    @Test
    fun `past validator accepts past dates`() =
        runTest {
            val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
            val context = ValidationContext(clock = fixedClock)
            val validator = PastDateValidator()

            validator.validate(PastDate(date = LocalDate.parse("2024-11-15")), context)
            validator.validate(PastDate(date = LocalDate.parse("2024-01-01")), context)
            validator.validate(PastDate(date = LocalDate.parse("2023-12-31")), context)
        }

    @Test
    fun `past validator rejects future dates`() =
        runTest {
            val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
            val context = ValidationContext(clock = fixedClock)
            val validator = PastDateValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(PastDate(date = LocalDate.parse("2024-11-17")), context)
                }
            assertTrue(exception1.errors.containsKey("date"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(PastDate(date = LocalDate.parse("2025-01-01")), context)
                }
            assertTrue(exception2.errors.containsKey("date"))
        }

    @Test
    fun `past validator rejects today`() =
        runTest {
            val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
            val context = ValidationContext(clock = fixedClock)
            val validator = PastDateValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(PastDate(date = LocalDate.parse("2024-11-16")), context)
                }
            assertTrue(exception.errors.containsKey("date"))
        }

    @Test
    fun `past validator allows null when not required`() =
        runTest {
            val validator = PastDateValidator()
            validator.validate(PastDate(date = null))
        }

    @Test
    fun `past validator provides error message`() =
        runTest {
            val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
            val context = ValidationContext(clock = fixedClock)
            val validator = PastDateValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(PastDate(date = LocalDate.parse("2024-11-17")), context)
                }

            assertTrue(exception.errors.containsKey("date"))
            assertFalse(exception.errors["date"]!!.isEmpty())
        }
}
