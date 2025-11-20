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

class TodayDateValidatorTest {

    @Test
    fun `today validator accepts today's date`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = TodayDateValidator()

        validator.validate(TodayDate(date = LocalDate.parse("2024-11-16")), context)
    }

    @Test
    fun `today validator rejects past dates`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = TodayDateValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(TodayDate(date = LocalDate.parse("2024-11-15")), context)
        }
        assertTrue(exception1.errors.containsKey("date"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(TodayDate(date = LocalDate.parse("2024-01-01")), context)
        }
        assertTrue(exception2.errors.containsKey("date"))
    }

    @Test
    fun `today validator rejects future dates`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = TodayDateValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(TodayDate(date = LocalDate.parse("2024-11-17")), context)
        }
        assertTrue(exception1.errors.containsKey("date"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(TodayDate(date = LocalDate.parse("2025-01-01")), context)
        }
        assertTrue(exception2.errors.containsKey("date"))
    }

    @Test
    fun `today validator allows null when not required`() = runTest {
        val validator = TodayDateValidator()
        validator.validate(TodayDate(date = null))
    }

    @Test
    fun `today validator provides error message`() = runTest {
        val fixedClock = Clock.fixed(Instant.parse("2024-11-16T00:00:00Z"), ZoneId.of("UTC"))
        val context = ValidationContext(clock = fixedClock)
        val validator = TodayDateValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(TodayDate(date = LocalDate.parse("2024-11-17")), context)
        }

        assertTrue(exception.errors.containsKey("date"))
        assertFalse(exception.errors["date"]!!.isEmpty())
    }
}
