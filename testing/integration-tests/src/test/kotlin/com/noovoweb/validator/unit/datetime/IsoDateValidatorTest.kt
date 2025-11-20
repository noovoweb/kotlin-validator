package com.noovoweb.validator.unit.datetime

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsoDateValidatorTest {

    @Test
    fun `isodate validator accepts valid ISO dates`() = runTest {
        val validator = IsoDateValidator()

        validator.validate(IsoDate(date = "2024-11-16"))
        validator.validate(IsoDate(date = "2000-01-01"))
        validator.validate(IsoDate(date = "1999-12-31"))
        validator.validate(IsoDate(date = "2024-02-29"))
    }

    @Test
    fun `isodate validator rejects invalid ISO dates`() = runTest {
        val validator = IsoDateValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(IsoDate(date = "not a date"))
        }
        assertTrue(exception.errors.containsKey("date"))
    }

    @Test
    fun `isodate validator allows null when not required`() = runTest {
        val validator = IsoDateValidator()
        validator.validate(IsoDate(date = null))
    }

    @Test
    fun `isodate validator provides error message`() = runTest {
        val validator = IsoDateValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(IsoDate(date = "invalid"))
        }

        assertTrue(exception.errors.containsKey("date"))
        assertFalse(exception.errors["date"]!!.isEmpty())
    }
}
