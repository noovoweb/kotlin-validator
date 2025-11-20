package com.noovoweb.validator.ktor

import com.noovoweb.validator.MessageProvider
import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.util.*

/**
 * Ktor plugin for kotlin-validator integration.
 *
 * This plugin provides:
 * - Automatic ValidationException handling with 422 status codes
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
class ValidationPlugin(configuration: Configuration) {

    private val defaultLocale = configuration.defaultLocale
    private val messageProvider = configuration.messageProvider
    private val clock = configuration.clock
    private val dispatcher = configuration.dispatcher

    class Configuration {
        var defaultLocale: Locale = Locale.getDefault()
        var messageProvider: MessageProvider = DefaultMessageProvider()
        var clock: Clock = Clock.systemDefaultZone()
        var dispatcher = Dispatchers.Default
    }

    companion object Plugin : BaseApplicationPlugin<Application, Configuration, ValidationPlugin> {
        override val key = AttributeKey<ValidationPlugin>("ValidationPlugin")

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): ValidationPlugin {
            val configuration = Configuration().apply(configure)
            val plugin = ValidationPlugin(configuration)

            // Install exception handler for ValidationException
            pipeline.intercept(ApplicationCallPipeline.Plugins) {
                try {
                    proceed()
                } catch (ex: ValidationException) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        ValidationErrorResponse(
                            status = HttpStatusCode.UnprocessableEntity.value,
                            error = "Validation Failed",
                            errors = ex.errors
                        )
                    )
                    finish()
                }
            }

            // Store plugin instance for access in routes
            pipeline.attributes.put(validationPluginKey, plugin)

            return plugin
        }
    }

    /**
     * Create a ValidationContext for the current request.
     * Extracts locale from Accept-Language header.
     */
    fun createContext(call: ApplicationCall): ValidationContext {
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
 * Structured validation error response for REST APIs.
 *
 * @property status HTTP status code (422)
 * @property error Error message ("Validation Failed")
 * @property errors Map of field names to error messages
 */
@kotlinx.serialization.Serializable
data class ValidationErrorResponse(
    val status: Int,
    val error: String,
    val errors: Map<String, List<String>>
)

/**
 * AttributeKey for storing the ValidationPlugin instance.
 */
private val validationPluginKey = AttributeKey<ValidationPlugin>("ValidationPluginInstance")

/**
 * Extension function to get ValidationContext for the current request.
 * Extracts locale from Accept-Language header automatically.
 */
fun ApplicationCall.validationContext(): ValidationContext {
    val plugin = application.attributes.getOrNull(validationPluginKey)
        ?: throw IllegalStateException(
            "ValidationPlugin not installed. Please install it in your Application configuration: install(ValidationPlugin)"
        )
    return plugin.createContext(this)
}
