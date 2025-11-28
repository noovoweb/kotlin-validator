package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object LocaleAwareValidators {
    suspend fun validateDateFormat(
        value: String?,
        context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        return when (context.locale.language) {
            "en" -> value.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))
            "fr" -> value.matches(Regex("""\d{1,2}-\d{1,2}-\d{4}"""))
            "de" -> value.matches(Regex("""\d{1,2}\.\d{1,2}\.\d{4}"""))
            else -> true
        }
    }
}

@Validated
data class LocaleDate(
    @CustomValidator(validator = "com.noovoweb.validator.integration.LocaleAwareValidators::validateDateFormat")
    val date: String?,
)

class LocaleAwareValidatorIntegrationTest {
    @Test
    fun `should validate US date format`() =
        runTest {
            val context = ValidationContext(locale = java.util.Locale.US)
            val isValid = LocaleAwareValidators.validateDateFormat("12/31/2024", context)
            assertTrue(isValid)
        }

    @Test
    fun `should validate French date format`() =
        runTest {
            val context = ValidationContext(locale = java.util.Locale.FRANCE)
            val isValid = LocaleAwareValidators.validateDateFormat("31-12-2024", context)
            assertTrue(isValid)
        }

    @Test
    fun `should validate German date format`() =
        runTest {
            val context = ValidationContext(locale = java.util.Locale.GERMANY)
            val isValid = LocaleAwareValidators.validateDateFormat("31.12.2024", context)
            assertTrue(isValid)
        }

    @Test
    fun `should reject wrong format for locale`() =
        runTest {
            val context = ValidationContext(locale = java.util.Locale.US)
            val isValid = LocaleAwareValidators.validateDateFormat("31-12-2024", context)
            assertFalse(isValid)
        }

    @Test
    fun `should accept different formats for different locales`() =
        runTest {
            val usContext = ValidationContext(locale = java.util.Locale.US)
            val frContext = ValidationContext(locale = java.util.Locale.FRANCE)
            val deContext = ValidationContext(locale = java.util.Locale.GERMANY)

            assertTrue(LocaleAwareValidators.validateDateFormat("12/31/2024", usContext))
            assertTrue(LocaleAwareValidators.validateDateFormat("31-12-2024", frContext))
            assertTrue(LocaleAwareValidators.validateDateFormat("31.12.2024", deContext))
        }

    @Test
    fun `should use default locale when not specified`() =
        runTest {
            val context = ValidationContext()
            // Default locale behavior - accepts any format or uses system locale
            val isValid = LocaleAwareValidators.validateDateFormat("12/31/2024", context)
            assertTrue(isValid)
        }
}
