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

class FutureDateValidatorTest {

    @Test
    fun `future validator accepts future dates`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = FutureDateValidator()

        validator.validate(FutureDate(date = LocalDate.parse("2024-11-17")), context)
        validator.validate(FutureDate(date = LocalDate.parse("2024-12-01")), context)
        validator.validate(FutureDate(date = LocalDate.parse("2025-01-01")), context)
    }

    @Test
    fun `future validator rejects past dates`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = FutureDateValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(FutureDate(date = LocalDate.parse("2024-11-15")), context)
        }
        assertTrue(exception1.errors.containsKey("date"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(FutureDate(date = LocalDate.parse("2024-01-01")), context)
        }
        assertTrue(exception2.errors.containsKey("date"))
    }

    @Test
    fun `future validator rejects today`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = FutureDateValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(FutureDate(date = LocalDate.parse("2024-11-16")), context)
        }
        assertTrue(exception.errors.containsKey("date"))
    }

    @Test
    fun `future validator allows null when not required`() = runTest {
        val validator = FutureDateValidator()
        validator.validate(FutureDate(date = null))
    }

    @Test
    fun `future validator provides error message`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = FutureDateValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(FutureDate(date = LocalDate.parse("2024-11-15")), context)
        }

        assertTrue(exception.errors.containsKey("date"))
        assertFalse(exception.errors["date"]!!.isEmpty())
    }
}
