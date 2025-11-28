package com.noovoweb.validator.core

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.test.runTest
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for MessageProvider and localization support.
 */
class MessageProviderTest {
    @Test
    fun `DefaultMessageProvider loads English messages`() =
        runTest {
            val provider = DefaultMessageProvider()
            val context = ValidationContext(locale = Locale.ENGLISH)

            val message = provider.getMessage("field.required", null, Locale.ENGLISH)

            assertTrue(message.isNotEmpty())
            assertTrue(message.contains("required") || message.contains("Required"))
        }

    @Test
    fun `DefaultMessageProvider loads French messages`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.required", null, Locale.FRENCH)

            assertTrue(message.isNotEmpty())
            assertTrue(message.contains("obligatoire"))
        }

    @Test
    fun `getMessage returns English by default`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.email", null, Locale.ENGLISH)

            assertTrue(message.isNotEmpty())
        }

    @Test
    fun `getMessage returns French when requested`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.email", null, Locale.FRENCH)

            assertTrue(message.isNotEmpty())
            assertTrue(message.lowercase().contains("email") || message.lowercase().contains("adresse"))
        }

    @Test
    fun `getMessage interpolates single parameter`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.min", arrayOf(18), Locale.ENGLISH)

            assertTrue(message.contains("18"))
        }

    @Test
    fun `getMessage interpolates multiple parameters`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.between", arrayOf(1, 100), Locale.ENGLISH)

            assertTrue(message.contains("1"))
            assertTrue(message.contains("100"))
        }

    @Test
    fun `getMessage falls back to English for unsupported locale`() =
        runTest {
            val provider = DefaultMessageProvider()

            // Use an unsupported locale
            val unsupportedLocale = Locale("xx", "XX")
            val message = provider.getMessage("field.required", null, unsupportedLocale)

            assertTrue(message.isNotEmpty())
        }

    @Test
    fun `getMessage returns key if message not found`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("nonexistent.key", null, Locale.ENGLISH)

            assertEquals("nonexistent.key", message)
        }

    @Test
    fun `getMessage returns consistent results for same inputs`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message1 = provider.getMessage("field.email", null, Locale.ENGLISH)
            val message2 = provider.getMessage("field.email", null, Locale.ENGLISH)

            assertEquals(message1, message2)
        }

    @Test
    fun `DefaultMessageProvider supports multiple validator types`() =
        runTest {
            val provider = DefaultMessageProvider()

            val stringValidatorMsg = provider.getMessage("field.email", null, Locale.ENGLISH)
            val numericValidatorMsg = provider.getMessage("field.min", arrayOf(18), Locale.ENGLISH)
            val collectionValidatorMsg = provider.getMessage("field.size", arrayOf(1, 10), Locale.ENGLISH)
            val dateValidatorMsg = provider.getMessage("field.future", null, Locale.ENGLISH)

            assertTrue(stringValidatorMsg.isNotEmpty())
            assertTrue(numericValidatorMsg.isNotEmpty())
            assertTrue(collectionValidatorMsg.isNotEmpty())
            assertTrue(dateValidatorMsg.isNotEmpty())
        }

    @Test
    fun `getMessage with parameters in French`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.min", arrayOf(21), Locale.FRENCH)

            assertTrue(message.isNotEmpty())
            assertTrue(message.contains("21"))
        }

    @Test
    fun `getMessage handles parameter array correctly`() =
        runTest {
            val provider = DefaultMessageProvider()

            val singleParam = provider.getMessage("field.minlength", arrayOf(5), Locale.ENGLISH)
            val multiParam = provider.getMessage("field.length", arrayOf(5, 20), Locale.ENGLISH)

            assertTrue(singleParam.isNotEmpty())
            assertTrue(multiParam.isNotEmpty())
        }

    @Test
    fun `getMessage with empty parameters array`() =
        runTest {
            val provider = DefaultMessageProvider()

            val message = provider.getMessage("field.required", arrayOf(), Locale.ENGLISH)

            assertTrue(message.isNotEmpty())
        }

    @Test
    fun `getAllMessages loads for supported locales`() =
        runTest {
            val provider = DefaultMessageProvider()

            // Test that multiple locales can be loaded
            val engMsg1 = provider.getMessage("field.required", null, Locale.ENGLISH)
            val engMsg2 = provider.getMessage("field.email", null, Locale.ENGLISH)
            val frMsg1 = provider.getMessage("field.required", null, Locale.FRENCH)
            val frMsg2 = provider.getMessage("field.email", null, Locale.FRENCH)

            assertTrue(engMsg1.isNotEmpty())
            assertTrue(engMsg2.isNotEmpty())
            assertTrue(frMsg1.isNotEmpty())
            assertTrue(frMsg2.isNotEmpty())
        }
}
