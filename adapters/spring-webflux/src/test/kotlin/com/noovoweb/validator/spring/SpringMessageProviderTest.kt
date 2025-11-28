package com.noovoweb.validator.spring

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.context.support.StaticMessageSource
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for SpringMessageProvider.
 */
class SpringMessageProviderTest {
    @Test
    fun `should delegate to Spring MessageSource for custom messages`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("custom.key", Locale.ENGLISH, "Custom message")

            val provider = SpringMessageProvider(messageSource)
            val message = provider.getMessage("custom.key", null, Locale.ENGLISH)

            assertEquals("Custom message", message)
        }

    @Test
    fun `should fall back to DefaultMessageProvider for built-in messages`() =
        runTest {
            val messageSource = StaticMessageSource()
            val provider = SpringMessageProvider(messageSource)

            val message = provider.getMessage("field.required", null, Locale.ENGLISH)

            assertTrue(message.isNotEmpty())
            assertTrue(message.contains("required", ignoreCase = true))
        }

    @Test
    fun `should interpolate parameters from Spring MessageSource`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("custom.min", Locale.ENGLISH, "Value must be at least {0}")

            val provider = SpringMessageProvider(messageSource)
            val message = provider.getMessage("custom.min", arrayOf(18), Locale.ENGLISH)

            assertEquals("Value must be at least 18", message)
        }

    @Test
    fun `should handle different locales from Spring`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("greeting", Locale.ENGLISH, "Hello")
            messageSource.addMessage("greeting", Locale.FRENCH, "Bonjour")

            val provider = SpringMessageProvider(messageSource)

            val englishMessage = provider.getMessage("greeting", null, Locale.ENGLISH)
            val frenchMessage = provider.getMessage("greeting", null, Locale.FRENCH)

            assertEquals("Hello", englishMessage)
            assertEquals("Bonjour", frenchMessage)
        }

    @Test
    fun `should return key if message not found anywhere`() =
        runTest {
            val messageSource = StaticMessageSource()
            val provider = SpringMessageProvider(messageSource)

            val message = provider.getMessage("nonexistent.key", null, Locale.ENGLISH)

            assertEquals("nonexistent.key", message)
        }

    @Test
    fun `should prioritize Spring messages over default`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("field.required", Locale.ENGLISH, "Spring custom required message")

            val provider = SpringMessageProvider(messageSource)
            val message = provider.getMessage("field.required", null, Locale.ENGLISH)

            assertEquals("Spring custom required message", message)
        }

    @Test
    fun `should handle null parameters gracefully`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("simple.message", Locale.ENGLISH, "No parameters")

            val provider = SpringMessageProvider(messageSource)
            val message = provider.getMessage("simple.message", null, Locale.ENGLISH)

            assertEquals("No parameters", message)
        }

    @Test
    fun `should handle empty parameters array`() =
        runTest {
            val messageSource = StaticMessageSource()
            messageSource.addMessage("simple.message", Locale.ENGLISH, "No parameters")

            val provider = SpringMessageProvider(messageSource)
            val message = provider.getMessage("simple.message", arrayOf(), Locale.ENGLISH)

            assertEquals("No parameters", message)
        }
}
