package com.noovoweb.validator.ktor

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.MessageProvider
import com.noovoweb.validator.ValidationContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.util.*

/**
 * Ktor plugin for kotlin-validator integration.
 *
 * This plugin provides:
 * - Locale extraction from Accept-Language header
 * - ValidationContext configuration
 * - Convenient validation helpers
 *
 * Installation:
 * ```kotlin
 * install(ValidationPlugin) {
 *     defaultLocale = Locale.ENGLISH
 *     messageProvider = DefaultMessageProvider()
 * }
 * ```
 *
 * Usage in routes:
 * ```kotlin
 * post("/api/users") {
 *     val request = call.receive<UserRequest>()
 *     val context = call.validationContext()
 *     UserRequestValidator().validate(request, context)
 *     // ... proceed with valid data
 * }
 * ```
 */
public class ValidationPlugin(configuration: Configuration) {

    private val defaultLocale = configuration.defaultLocale
    private val messageProvider = configuration.messageProvider
    private val clock = configuration.clock
    private val dispatcher = configuration.dispatcher

    public class Configuration {
        public var defaultLocale: Locale = Locale.getDefault()
        public var messageProvider: MessageProvider = DefaultMessageProvider.DEFAULT
        public var clock: Clock = Clock.systemDefaultZone()
        public var dispatcher: CoroutineDispatcher = Dispatchers.Default
    }

    public companion object Plugin : BaseApplicationPlugin<Application, Configuration, ValidationPlugin> {
        override val key: AttributeKey<ValidationPlugin> = AttributeKey<ValidationPlugin>("ValidationPlugin")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): ValidationPlugin {
            val configuration = Configuration().apply(configure)
            val plugin = ValidationPlugin(configuration)

            // Store plugin instance for access in routes
            pipeline.attributes.put(validationPluginKey, plugin)

            return plugin
        }
    }

    /**
     * Create a ValidationContext for the current request.
     * Extracts locale from Accept-Language header.
     */
    public fun createContext(call: ApplicationCall): ValidationContext {
        val locale = extractLocale(call) ?: defaultLocale
        return ValidationContext(
            locale = locale,
            messageProvider = messageProvider,
            dispatcher = dispatcher,
            clock = clock
        )
    }

    /**
     * Extract locale from Accept-Language header.
     * Returns null if header is missing or invalid.
     */
    private fun extractLocale(call: ApplicationCall): Locale? {
        val acceptLanguage = call.request.headers[HttpHeaders.AcceptLanguage] ?: return null

        // Parse Accept-Language header (e.g., "en-US,en;q=0.9,fr;q=0.8")
        val languageTag = acceptLanguage
            .split(",")
            .firstOrNull()
            ?.split(";")
            ?.firstOrNull()
            ?.trim()
            ?: return null

        return try {
            Locale.forLanguageTag(languageTag)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * AttributeKey for storing the ValidationPlugin instance.
 */
private val validationPluginKey = AttributeKey<ValidationPlugin>("ValidationPluginInstance")

/**
 * Extension function to get ValidationContext for the current request.
 * Extracts locale from Accept-Language header automatically.
 */
public fun ApplicationCall.validationContext(): ValidationContext {
    val plugin = application.attributes.getOrNull(validationPluginKey)
        ?: throw IllegalStateException(
            "ValidationPlugin not installed. Please install it in your Application configuration: install(ValidationPlugin)"
        )
    return plugin.createContext(this)
}
