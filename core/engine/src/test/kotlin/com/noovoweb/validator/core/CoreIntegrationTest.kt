package com.noovoweb.validator.core

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.MessageProvider
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationError
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.ValidationResult
import kotlinx.coroutines.test.runTest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for core components working together.
 */
class CoreIntegrationTest {

    @Test
    fun `ValidationContext with custom MessageProvider works end-to-end`() = runTest {
        val provider = DefaultMessageProvider()
        val context = ValidationContext(
            locale = Locale.ENGLISH,
            messageProvider = provider
        )

        val message = context.messageProvider.getMessage("field.email", null, context.locale)

        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `ValidationResult chains with custom error messages`() {
        val errors = mapOf(
            "email" to listOf(ValidationError("Invalid email", "invalid_email")),
            "age" to listOf(ValidationError("Too young", "too_young"))
        )

        val result = ValidationResult.Failure(errors)
            .mapErrors { originalErrors ->
                originalErrors.mapValues { (_, errorList) ->
                    errorList.map { error ->
                        ValidationError(
                            message = "${error.message} [${error.code}]",
                            code = error.code
                        )
                    }
                }
            }

        val failure = result as ValidationResult.Failure
        assertTrue(failure.errors["email"]!!.first().message.contains("[invalid_email]"))
    }

    @Test
    fun `ValidationException integrates with MessageProvider`() = runTest {
        val provider = DefaultMessageProvider()
        val context = ValidationContext(messageProvider = provider)
        val errors = mapOf("field" to listOf("Invalid"))

        val exception = ValidationException(errors)
        val message = context.messageProvider.getMessage("field.required", null, context.locale)

        assertNotNull(message)
        assertTrue(exception.errors.isNotEmpty())
    }

    @Test
    fun `Locale switching changes message language`() = runTest {
        val provider = DefaultMessageProvider()

        val englishContext = ValidationContext(
            locale = Locale.ENGLISH,
            messageProvider = provider
        )
        val frenchContext = ValidationContext(
            locale = Locale.FRENCH,
            messageProvider = provider
        )

        val engMsg = englishContext.messageProvider.getMessage("field.required", null, englishContext.locale)
        val frMsg = frenchContext.messageProvider.getMessage("field.required", null, frenchContext.locale)

        assertTrue(engMsg.isNotEmpty())
        assertTrue(frMsg.isNotEmpty())
        assertTrue(engMsg != frMsg)
    }

    @Test
    fun `Clock injection supports date validation testing`() {
        val fixedInstant = Instant.parse("2025-01-01T12:00:00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val context = ValidationContext(clock = fixedClock)

        assertEquals(fixedClock, context.clock)
    }

    @Test
    fun `Full validation flow ValidationContext Result Exception`() = runTest {
        val provider = DefaultMessageProvider()
        val context = ValidationContext(
            locale = Locale.ENGLISH,
            messageProvider = provider
        )

        // Create a failure result
        val errors = mapOf(
            "name" to listOf(ValidationError("Name is required", "required")),
            "email" to listOf(
                ValidationError("Invalid email", "invalid_email"),
                ValidationError("Too long", "too_long")
            )
        )

        val result = ValidationResult.Failure(errors)

        // Verify the result
        assertTrue(result.isFailure())

        // Convert to exception
        val exception = try {
            result.getOrThrow()
        } catch (e: ValidationException) {
            e
        }

        // Verify exception structure
        assertTrue(exception.hasFieldError("name"))
        assertTrue(exception.hasFieldError("email"))
        assertEquals(2, exception.getFieldErrors("email").size)
    }

    @Test
    fun `Multiple components work together with immutability`() = runTest {
        val provider1 = DefaultMessageProvider()
        val context1 = ValidationContext(
            locale = Locale.ENGLISH,
            messageProvider = provider1
        )

        val context2 = context1
            .withLocale(Locale.FRENCH)
            .withMetadata("userId", 123)

        // Verify immutability
        assertEquals(Locale.ENGLISH, context1.locale)
        assertTrue(context1.metadata.isEmpty())

        assertEquals(Locale.FRENCH, context2.locale)
        assertEquals(123, context2.metadata["userId"])
    }

    @Test
    fun `ValidationContext builder with all options`() {
        val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val context = ValidationContext()
            .withLocale(Locale.FRENCH)
            .withClock(fixedClock)
            .withMetadata("requestId", "req-123")
            .withMetadata("userId", 456)

        assertEquals(Locale.FRENCH, context.locale)
        assertEquals(fixedClock, context.clock)
        assertEquals(2, context.metadata.size)
    }

    @Test
    fun `Error messages with parameters in validation context`() = runTest {
        val provider = DefaultMessageProvider()
        val context = ValidationContext(
            locale = Locale.ENGLISH,
            messageProvider = provider
        )

        val minMsg = context.messageProvider.getMessage("field.min", arrayOf(18), context.locale)
        val maxMsg = context.messageProvider.getMessage("field.max", arrayOf(100), context.locale)
        val betweenMsg = context.messageProvider.getMessage("field.between", arrayOf(1, 10), context.locale)

        assertTrue(minMsg.contains("18"))
        assertTrue(maxMsg.contains("100"))
        assertTrue(betweenMsg.contains("1"))
        assertTrue(betweenMsg.contains("10"))
    }
}
