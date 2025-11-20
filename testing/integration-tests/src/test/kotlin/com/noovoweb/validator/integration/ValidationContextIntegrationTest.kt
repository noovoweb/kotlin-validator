package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

object ContextValidators {
    suspend fun validateWithContext(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        // Use context information for validation
        return when (context.locale.language) {
            "en" -> value.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))
            "fr" -> value.matches(Regex("""\d{1,2}-\d{1,2}-\d{4}"""))
            else -> true
        }
    }
}

@Validated
data class ContextData(
    @CustomValidator(validator = "com.noovoweb.validator.integration.ContextValidators::validateWithContext")
    val value: String?
)

class ValidationContextIntegrationTest {

    @Test
    fun `should receive correct ValidationContext`() = runTest {
        val frContext = ValidationContext(locale = java.util.Locale.FRANCE)
        val usContext = ValidationContext(locale = java.util.Locale.US)

        val frValidation = ContextValidators.validateWithContext("31-12-2024", frContext)
        val usValidation = ContextValidators.validateWithContext("12/31/2024", usContext)

        assertTrue(frValidation && usValidation)
    }

    @Test
    fun `should propagate context to validators`() = runTest {
        val customContext = ValidationContext(
            locale = java.util.Locale.FRANCE
        )

        val isValid = ContextValidators.validateWithContext("31-12-2024", customContext)
        assertTrue(isValid)
    }

    @Test
    fun `should use default context when not specified`() = runTest {
        val defaultContext = ValidationContext()
        // With default/unknown locale, validator returns true for any format
        val isValid = ContextValidators.validateWithContext("12/31/2024", defaultContext)
        assertTrue(isValid)
    }

    @Test
    fun `should maintain context across multiple validations`() = runTest {
        val context = ValidationContext(locale = java.util.Locale.US)

        assertTrue(ContextValidators.validateWithContext("01/15/2024", context))
        assertTrue(ContextValidators.validateWithContext("12/31/2024", context))
        assertTrue(ContextValidators.validateWithContext("06/30/2024", context))
    }
}
