package com.noovoweb.validator.core

import com.noovoweb.validator.DefaultMessageProvider
import kotlinx.coroutines.test.runTest
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Integration tests to verify that validation messages are properly translated
 * across different locales in the core module.
 */
class MessageTranslationTest {
    private val provider = DefaultMessageProvider()

    @Test
    fun `all string validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.required",
                    "field.email",
                    "field.url",
                    "field.uuid",
                    "field.length",
                    "field.minlength",
                    "field.maxlength",
                    "field.pattern",
                    "field.alpha",
                    "field.alphanumeric",
                    "field.ascii",
                    "field.lowercase",
                    "field.uppercase",
                    "field.startswith",
                    "field.endswith",
                    "field.contains",
                    "field.oneof",
                    "field.notoneof",
                    "field.json",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all numeric validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.min",
                    "field.max",
                    "field.between",
                    "field.positive",
                    "field.negative",
                    "field.zero",
                    "field.integer",
                    "field.decimal",
                    "field.divisibleby",
                    "field.even",
                    "field.odd",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all collection validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.size",
                    "field.minsize",
                    "field.maxsize",
                    "field.notempty",
                    "field.distinct",
                    "field.containsvalue",
                    "field.notcontains",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all date-time validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.dateformat",
                    "field.isodate",
                    "field.isodatetime",
                    "field.future",
                    "field.past",
                    "field.today",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all network validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.ipv4",
                    "field.ipv6",
                    "field.ip",
                    "field.macaddress",
                    "field.port",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all file validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.mimetype",
                    "field.fileextension",
                    "field.maxfilesize",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `all conditional validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.same",
                    "field.different",
                    "field.requiredif",
                    "field.requiredunless",
                    "field.requiredwith",
                    "field.requiredwithout",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `boolean validator messages are translated to French`() =
        runTest {
            val key = "field.accepted"
            val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
            val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

            assertTrue(englishMessage.isNotEmpty())
            assertTrue(frenchMessage.isNotEmpty())
            assertNotEquals(englishMessage, frenchMessage)
        }

    @Test
    fun `security validator messages are translated to French`() =
        runTest {
            val keys =
                listOf(
                    "field.pattern.too_long",
                    "field.too_long",
                )

            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")
            }
        }

    @Test
    fun `messages with parameters are translated correctly`() =
        runTest {
            val testCases =
                mapOf(
                    "field.min" to arrayOf<Any>(18),
                    "field.max" to arrayOf<Any>(100),
                    "field.between" to arrayOf<Any>(10, 50),
                    "field.length" to arrayOf<Any>(5, 20),
                    "field.minlength" to arrayOf<Any>(8),
                    "field.maxlength" to arrayOf<Any>(255),
                )

            testCases.forEach { (key, params) ->
                val englishMessage = provider.getMessage(key, params, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, params, Locale.FRENCH)

                assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
                assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
                assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be different between English and French")

                // Verify parameters are interpolated
                params.forEach { param ->
                    assertTrue(englishMessage.contains(param.toString()), "English message for $key should contain parameter $param")
                    assertTrue(frenchMessage.contains(param.toString()), "French message for $key should contain parameter $param")
                }
            }
        }

    @Test
    fun `French translations use correct French terminology`() =
        runTest {
            // Verify specific French terms are present
            val requiredFr = provider.getMessage("field.required", null, Locale.FRENCH)
            assertTrue(requiredFr.contains("obligatoire"), "Required field should use 'obligatoire' in French")

            val emailFr = provider.getMessage("field.email", null, Locale.FRENCH)
            assertTrue(emailFr.contains("adresse") || emailFr.contains("e-mail"), "Email should use proper French terminology")

            val minFr = provider.getMessage("field.min", arrayOf<Any>(10), Locale.FRENCH)
            assertTrue(minFr.contains("moins") || minFr.contains("au moins"), "Min should use 'au moins' in French")

            val maxFr = provider.getMessage("field.max", arrayOf<Any>(100), Locale.FRENCH)
            assertTrue(maxFr.contains("dépasser") || maxFr.contains("pas"), "Max should use proper French negative form")
        }

    @Test
    fun `all message keys exist in both English and French`() =
        runTest {
            // Get all keys from English properties
            val allKeys =
                listOf(
                    // String validators
                    "field.required", "field.email", "field.url", "field.uuid", "field.length",
                    "field.minlength", "field.maxlength", "field.pattern", "field.alpha",
                    "field.alphanumeric", "field.ascii", "field.lowercase", "field.uppercase",
                    "field.startswith", "field.endswith", "field.contains", "field.oneof",
                    "field.notoneof", "field.json",
                    // Numeric validators
                    "field.min", "field.max", "field.between", "field.positive", "field.negative",
                    "field.zero", "field.integer", "field.decimal", "field.divisibleby",
                    "field.even", "field.odd",
                    // Boolean validators
                    "field.accepted",
                    // Collection validators
                    "field.size", "field.minsize", "field.maxsize", "field.notempty",
                    "field.distinct", "field.containsvalue", "field.notcontains",
                    // Date/time validators
                    "field.dateformat", "field.isodate", "field.isodatetime", "field.future",
                    "field.past", "field.today",
                    // Network validators
                    "field.ipv4", "field.ipv6", "field.ip", "field.macaddress", "field.port",
                    // File validators
                    "field.mimetype", "field.fileextension", "field.maxfilesize",
                    // Conditional validators
                    "field.same", "field.different", "field.requiredif", "field.requiredunless",
                    "field.requiredwith", "field.requiredwithout",
                    // Security validators
                    "field.pattern.too_long", "field.too_long",
                )

            allKeys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                // Neither should return the key itself (which indicates missing translation)
                assertNotEquals(key, englishMessage, "Key $key should have English translation")
                assertNotEquals(key, frenchMessage, "Key $key should have French translation")
            }
        }
}
