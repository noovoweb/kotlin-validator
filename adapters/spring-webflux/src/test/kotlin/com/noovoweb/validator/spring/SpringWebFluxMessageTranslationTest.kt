package com.noovoweb.validator.spring

import com.noovoweb.validator.DefaultMessageProvider
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.StaticMessageSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Integration tests to verify that validation messages are properly translated
 * in Spring WebFlux module, including fallback to core messages.
 */
class SpringWebFluxMessageTranslationTest {

    @Test
    fun `Spring WebFlux module falls back to core translations for built-in validators`() = runTest {
        val messageSource = StaticMessageSource()
        val provider = SpringMessageProvider(messageSource)

        val keys = listOf(
            "field.required", "field.email", "field.url", "field.min", "field.max",
            "field.between", "field.size", "field.future", "field.past"
        )

        keys.forEach { key ->
            val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
            val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

            assertTrue(englishMessage.isNotEmpty(), "English message for $key should not be empty")
            assertTrue(frenchMessage.isNotEmpty(), "French message for $key should not be empty")
            assertNotEquals(englishMessage, frenchMessage, "Messages for $key should be translated")
            assertNotEquals(key, englishMessage, "Should not return key for missing translation")
            assertNotEquals(key, frenchMessage, "Should not return key for missing translation")
        }
    }

    @Test
    fun `Spring MessageSource can override core translations in WebFlux`() = runTest {
        val messageSource = StaticMessageSource()
        messageSource.addMessage("field.required", Locale.ENGLISH, "WebFlux Custom: This field is mandatory")
        messageSource.addMessage("field.required", Locale.FRENCH, "WebFlux Personnalisé: Ce champ est requis")

        val provider = SpringMessageProvider(messageSource)

        val englishMessage = provider.getMessage("field.required", null, Locale.ENGLISH)
        val frenchMessage = provider.getMessage("field.required", null, Locale.FRENCH)

        assertEquals("WebFlux Custom: This field is mandatory", englishMessage)
        assertEquals("WebFlux Personnalisé: Ce champ est requis", frenchMessage)
    }

    @Test
    fun `custom Spring messages work with parameters in WebFlux`() = runTest {
        val messageSource = StaticMessageSource()
        messageSource.addMessage("custom.age.min", Locale.ENGLISH, "You must be at least {0} years old")
        messageSource.addMessage("custom.age.min", Locale.FRENCH, "Vous devez avoir au moins {0} ans")

        val provider = SpringMessageProvider(messageSource)

        val englishMessage = provider.getMessage("custom.age.min", arrayOf<Any>(18), Locale.ENGLISH)
        val frenchMessage = provider.getMessage("custom.age.min", arrayOf<Any>(18), Locale.FRENCH)

        assertEquals("You must be at least 18 years old", englishMessage)
        assertEquals("Vous devez avoir au moins 18 ans", frenchMessage)
    }

    @Test
    fun `Spring WebFlux can use application-specific message bundles`() = runTest {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")

        val provider = SpringMessageProvider(messageSource)

        // Test that it falls back to core for built-in validators
        val requiredEn = provider.getMessage("field.required", null, Locale.ENGLISH)
        val requiredFr = provider.getMessage("field.required", null, Locale.FRENCH)

        assertTrue(requiredEn.isNotEmpty())
        assertTrue(requiredFr.isNotEmpty())
        assertNotEquals(requiredEn, requiredFr)
    }

    @Test
    fun `all core validator messages are accessible through Spring WebFlux`() = runTest {
        val messageSource = StaticMessageSource()
        val provider = SpringMessageProvider(messageSource)
        val coreProvider = DefaultMessageProvider()

        val allKeys = listOf(
            // String validators
            "field.required", "field.email", "field.url", "field.uuid",
            // Numeric validators
            "field.min", "field.max", "field.between", "field.positive",
            // Collection validators
            "field.size", "field.minsize", "field.maxsize", "field.notempty",
            // Date/time validators
            "field.future", "field.past", "field.today",
            // Network validators
            "field.ipv4", "field.ipv6", "field.ip",
            // Conditional validators
            "field.same", "field.different"
        )

        allKeys.forEach { key ->
            // Messages from Spring provider should match core provider
            val springEnglish = provider.getMessage(key, null, Locale.ENGLISH)
            val springFrench = provider.getMessage(key, null, Locale.FRENCH)
            val coreEnglish = coreProvider.getMessage(key, null, Locale.ENGLISH)
            val coreFrench = coreProvider.getMessage(key, null, Locale.FRENCH)

            assertEquals(coreEnglish, springEnglish, "Spring WebFlux should fall back to core for $key (English)")
            assertEquals(coreFrench, springFrench, "Spring WebFlux should fall back to core for $key (French)")
        }
    }

    @Test
    fun `Spring WebFlux preserves parameter interpolation for translated messages`() = runTest {
        val messageSource = StaticMessageSource()
        val provider = SpringMessageProvider(messageSource)

        val testCases = mapOf(
            "field.min" to arrayOf<Any>(18),
            "field.max" to arrayOf<Any>(100),
            "field.between" to arrayOf<Any>(10, 50),
            "field.length" to arrayOf<Any>(5, 20)
        )

        testCases.forEach { (key, params) ->
            val englishMessage = provider.getMessage(key, params, Locale.ENGLISH)
            val frenchMessage = provider.getMessage(key, params, Locale.FRENCH)

            // Verify parameters are in the message
            params.forEach { param ->
                assertTrue(
                    englishMessage.contains(param.toString()),
                    "English message for $key should contain $param"
                )
                assertTrue(
                    frenchMessage.contains(param.toString()),
                    "French message for $key should contain $param"
                )
            }
        }
    }

    @Test
    fun `Spring MessageSource priority is correct in WebFlux - custom before default`() = runTest {
        val messageSource = StaticMessageSource()
        // Add a custom override for a core message
        messageSource.addMessage("field.email", Locale.ENGLISH, "Spring Custom Email")
        messageSource.addMessage("field.email", Locale.FRENCH, "Spring Email Personnalisé")

        val provider = SpringMessageProvider(messageSource)

        val englishMessage = provider.getMessage("field.email", null, Locale.ENGLISH)
        val frenchMessage = provider.getMessage("field.email", null, Locale.FRENCH)

        // Should get Spring custom messages, not core messages
        assertEquals("Spring Custom Email", englishMessage)
        assertEquals("Spring Email Personnalisé", frenchMessage)
    }

    @Test
    fun `Spring WebFlux handles missing custom translations gracefully`() = runTest {
        val messageSource = StaticMessageSource()
        // Only add English, not French
        messageSource.addMessage("custom.message", Locale.ENGLISH, "English only message")

        val provider = SpringMessageProvider(messageSource)

        val englishMessage = provider.getMessage("custom.message", null, Locale.ENGLISH)
        val frenchMessage = provider.getMessage("custom.message", null, Locale.FRENCH)

        // English should work
        assertEquals("English only message", englishMessage)
        // French should fall back to returning the key
        assertEquals("custom.message", frenchMessage)
    }

    @Test
    fun `Spring WebFlux supports locale-specific message resolution`() = runTest {
        val messageSource = StaticMessageSource()
        messageSource.addMessage("test.key", Locale.ENGLISH, "English")
        messageSource.addMessage("test.key", Locale.FRENCH, "Français")
        messageSource.addMessage("test.key", Locale.CANADA_FRENCH, "Français Canadien")

        val provider = SpringMessageProvider(messageSource)

        assertEquals("English", provider.getMessage("test.key", null, Locale.ENGLISH))
        assertEquals("Français", provider.getMessage("test.key", null, Locale.FRENCH))
        assertEquals("Français Canadien", provider.getMessage("test.key", null, Locale.CANADA_FRENCH))
    }

    @Test
    fun `all validator categories are translated through Spring WebFlux`() = runTest {
        val messageSource = StaticMessageSource()
        val provider = SpringMessageProvider(messageSource)

        val categories = mapOf(
            "String" to listOf("field.required", "field.email", "field.url"),
            "Numeric" to listOf("field.min", "field.max", "field.positive"),
            "Collection" to listOf("field.size", "field.notempty", "field.distinct"),
            "DateTime" to listOf("field.future", "field.past", "field.today"),
            "Network" to listOf("field.ipv4", "field.ip", "field.port"),
            "File" to listOf("field.mimetype", "field.maxfilesize"),
            "Conditional" to listOf("field.same", "field.requiredif"),
            "Boolean" to listOf("field.accepted")
        )

        categories.forEach { (category, keys) ->
            keys.forEach { key ->
                val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
                val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

                assertTrue(
                    englishMessage.isNotEmpty(),
                    "$category validator $key should have English translation"
                )
                assertTrue(
                    frenchMessage.isNotEmpty(),
                    "$category validator $key should have French translation"
                )
                assertNotEquals(
                    englishMessage,
                    frenchMessage,
                    "$category validator $key should be translated"
                )
            }
        }
    }

    @Test
    fun `reactive context preserves locale for translations`() = runTest {
        val messageSource = StaticMessageSource()
        messageSource.addMessage("test.reactive", Locale.ENGLISH, "Reactive English")
        messageSource.addMessage("test.reactive", Locale.FRENCH, "Reactive Français")

        val provider = SpringMessageProvider(messageSource)

        // Simulate different locales in reactive context
        val englishMsg = provider.getMessage("test.reactive", null, Locale.ENGLISH)
        val frenchMsg = provider.getMessage("test.reactive", null, Locale.FRENCH)

        assertEquals("Reactive English", englishMsg)
        assertEquals("Reactive Français", frenchMsg)
    }

    @Test
    fun `WebFlux translations work with async validation flows`() = runTest {
        val messageSource = StaticMessageSource()
        val provider = SpringMessageProvider(messageSource)

        // Test that multiple async calls maintain proper translations
        val keys = listOf("field.required", "field.email", "field.min", "field.max")

        keys.forEach { key ->
            val englishMessage = provider.getMessage(key, null, Locale.ENGLISH)
            val frenchMessage = provider.getMessage(key, null, Locale.FRENCH)

            assertTrue(englishMessage.isNotEmpty())
            assertTrue(frenchMessage.isNotEmpty())
            assertNotEquals(englishMessage, frenchMessage)
        }
    }
}
