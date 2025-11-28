package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object AsyncValidators {
    suspend fun validateUsernameAsync(
        value: String?,
        context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        // Simulate async operation (e.g., database lookup)
        kotlinx.coroutines.delay(5)
        val blacklisted = setOf("admin", "root", "system", "test")
        return !blacklisted.contains(value.lowercase())
    }
}

@Validated
data class AsyncUsername(
    @CustomValidator(validator = "com.noovoweb.validator.integration.AsyncValidators::validateUsernameAsync")
    val username: String?,
)

class AsyncValidatorIntegrationTest {
    @Test
    fun `should support async operations`() =
        runTest {
            val context = ValidationContext()
            val isValid = AsyncValidators.validateUsernameAsync("john_doe", context)
            assertTrue(isValid)
        }

    @Test
    fun `should reject blacklisted usernames`() =
        runTest {
            val context = ValidationContext()
            val isValid = AsyncValidators.validateUsernameAsync("admin", context)
            assertFalse(isValid)
        }

    @Test
    fun `should be case-insensitive for blacklist`() =
        runTest {
            val context = ValidationContext()
            assertFalse(AsyncValidators.validateUsernameAsync("ADMIN", context))
            assertFalse(AsyncValidators.validateUsernameAsync("Admin", context))
            assertFalse(AsyncValidators.validateUsernameAsync("ROOT", context))
        }

    @Test
    fun `should accept non-blacklisted usernames`() =
        runTest {
            val context = ValidationContext()
            assertTrue(AsyncValidators.validateUsernameAsync("user123", context))
            assertTrue(AsyncValidators.validateUsernameAsync("john_doe", context))
            assertTrue(AsyncValidators.validateUsernameAsync("valid_user", context))
        }

    @Test
    fun `should handle async validation with delay`() =
        runTest {
            val validator = AsyncUsernameValidator()
            val startTime = System.currentTimeMillis()
            validator.validate(AsyncUsername(username = "john_doe"))
            val endTime = System.currentTimeMillis()
            // Verify that async operation took some time
            assertTrue(endTime - startTime >= 5)
        }
}
