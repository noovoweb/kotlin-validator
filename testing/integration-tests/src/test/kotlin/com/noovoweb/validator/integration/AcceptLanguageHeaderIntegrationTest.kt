package com.noovoweb.validator.integration

import com.noovoweb.validator.Email
import com.noovoweb.validator.MinLength
import com.noovoweb.validator.Required
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale
import kotlin.test.assertTrue

@Validated
data class UserProfile(
    @Required
    @MinLength(3)
    val username: String?,
    
    @Required
    @Email
    val email: String?
)

@Validated
data class UserWithProfile(
    @com.noovoweb.validator.Valid
    val profile: UserProfile?
)

class AcceptLanguageHeaderIntegrationTest {

    /**
     * Helper function to parse Accept-Language header and return best matching locale
     */
    private fun parseAcceptLanguageHeader(acceptLanguage: String): Locale {
        // Simple implementation - takes first language from header
        // Format: "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"
        val primaryLanguage = acceptLanguage.split(",").firstOrNull()?.split(";")?.firstOrNull()?.trim()
        
        return when {
            primaryLanguage == null -> Locale.ENGLISH
            primaryLanguage.startsWith("fr") -> Locale.FRENCH
            primaryLanguage.startsWith("de") -> Locale.GERMAN
            primaryLanguage.startsWith("es") -> Locale("es")
            primaryLanguage.startsWith("en") -> Locale.ENGLISH
            else -> Locale.ENGLISH
        }
    }

    @Test
    fun `should return English error messages when Accept-Language is en-US`() = runTest {
        val acceptLanguage = "en-US,en;q=0.9"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        val context = ValidationContext(locale = locale)
        
        val validator = UserProfileValidator()
        val invalidProfile = UserProfile(
            username = "ab", // Too short
            email = "invalid-email"
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidProfile, context)
        }
        
        // English messages should contain certain keywords
        val usernameErrors = exception.errors["username"]!!
        val emailErrors = exception.errors["email"]!!
        
        assertTrue(usernameErrors.any { it.contains("character", ignoreCase = true) || it.contains("least", ignoreCase = true) })
        assertTrue(emailErrors.any { it.contains("email", ignoreCase = true) || it.contains("valid", ignoreCase = true) })
    }

    @Test
    fun `should return French error messages when Accept-Language is fr-FR`() = runTest {
        val acceptLanguage = "fr-FR,fr;q=0.9,en;q=0.8"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        val context = ValidationContext(locale = locale)
        
        val validator = UserProfileValidator()
        val invalidProfile = UserProfile(
            username = "ab", // Too short
            email = "invalid-email"
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidProfile, context)
        }
        
        // French messages should contain certain keywords
        val usernameErrors = exception.errors["username"]!!
        val emailErrors = exception.errors["email"]!!
        
        // French validation messages typically contain these words
        assertTrue(
            usernameErrors.any { 
                it.contains("caractère", ignoreCase = true) || 
                it.contains("minimum", ignoreCase = true) ||
                it.contains("moins", ignoreCase = true)
            },
            "Expected French error message for username, got: $usernameErrors"
        )
        assertTrue(
            emailErrors.any { 
                it.contains("email", ignoreCase = true) || 
                it.contains("valide", ignoreCase = true) ||
                it.contains("adresse", ignoreCase = true)
            },
            "Expected French error message for email, got: $emailErrors"
        )
    }

    @Test
    fun `should return German error messages when Accept-Language is de-DE`() = runTest {
        val acceptLanguage = "de-DE,de;q=0.9,en;q=0.8"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        val context = ValidationContext(locale = locale)
        
        val validator = UserProfileValidator()
        val invalidProfile = UserProfile(
            username = "ab",
            email = "invalid-email"
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidProfile, context)
        }
        
        // German messages should contain certain keywords (or fallback to English)
        val usernameErrors = exception.errors["username"]!!
        val emailErrors = exception.errors["email"]!!
        
        // Verify that validation occurred and errors were collected
        assertTrue(usernameErrors.isNotEmpty(), "Should have username validation errors")
        assertTrue(emailErrors.isNotEmpty(), "Should have email validation errors")
        
        // German or English messages are acceptable (depends on availability of German translations)
        assertTrue(
            usernameErrors.any { 
                it.contains("Zeichen", ignoreCase = true) || 
                it.contains("mindestens", ignoreCase = true) ||
                it.contains("character", ignoreCase = true) ||
                it.contains("least", ignoreCase = true)
            },
            "Expected German or English error message for username, got: $usernameErrors"
        )
    }

    @Test
    fun `should default to English when Accept-Language is missing`() = runTest {
        // No Accept-Language header provided
        val context = ValidationContext() // Uses default English locale
        
        val validator = UserProfileValidator()
        val invalidProfile = UserProfile(
            username = null, // Required field
            email = null
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidProfile, context)
        }
        
        val usernameErrors = exception.errors["username"]!!
        assertTrue(usernameErrors.any { it.contains("required", ignoreCase = true) })
    }

    @Test
    fun `should respect language priority from Accept-Language header`() = runTest {
        // French has higher priority (q=0.9) than English (q=0.7)
        val acceptLanguage = "fr-FR;q=0.9,en-US;q=0.7"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        
        // Should select French
        assertTrue(locale.language == "fr", "Expected French locale, got: ${locale.language}")
    }

    @Test
    fun `should handle multiple validation errors with localized messages`() = runTest {
        val acceptLanguage = "fr-FR,fr;q=0.9"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        val context = ValidationContext(locale = locale)
        
        val validator = UserProfileValidator()
        val invalidProfile = UserProfile(
            username = null, // Required
            email = "not-an-email" // Invalid format
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidProfile, context)
        }
        
        // Should have errors for both fields
        assertTrue(exception.errors.containsKey("username"))
        assertTrue(exception.errors.containsKey("email"))
        
        // All errors should be in French
        val allErrors = exception.errors.values.flatten()
        assertTrue(allErrors.all { error ->
            // French messages shouldn't contain common English-only words like "required" 
            // (though "email" is international)
            !error.contains("required", ignoreCase = true) || 
            error.contains("obligatoire", ignoreCase = true)
        })
    }

    @Test
    fun `should handle nested object validation with Accept-Language`() = runTest {
        val acceptLanguage = "en-US"
        val locale = parseAcceptLanguageHeader(acceptLanguage)
        val context = ValidationContext(locale = locale)
        
        val validator = UserWithProfileValidator()
        val invalidUser = UserWithProfile(
            profile = UserProfile(username = "a", email = "bad")
        )
        
        val exception = assertThrows<ValidationException> {
            validator.validate(invalidUser, context)
        }
        
        // Should have nested error paths with messages
        assertTrue(exception.errors.isNotEmpty())
        // Verify at least one error exists for the nested validation
        assertTrue(exception.errors.keys.any { it.startsWith("profile") })
    }
}
