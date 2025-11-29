package com.noovoweb.validator.spring

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.reactor.mono
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import java.util.Locale

/**
 * Extension property to get the locale from the ServerRequest's Accept-Language header.
 */
public val ServerRequest.locale: Locale
    get() = this.exchange().localeContext.locale ?: Locale.ENGLISH

/**
 * Extension function to create a localized ValidationContext from the request's locale.
 */
public fun ValidationContext.withLocale(request: ServerRequest): ValidationContext {
    val locale = request.locale
    return this.withLocale(locale)
}

// ============================================
// SIMPLIFIED API - Auto-discovery of validator
// ============================================

/**
 * Get the validator class name for a given payload type.
 * Convention: MyRequest -> MyRequestValidator
 */
private fun <T : Any> getValidatorClassName(payload: T): String {
    val className = payload::class.qualifiedName ?: throw IllegalArgumentException("Cannot determine class name")
    return "${className}Validator"
}

/**
 * Get validator instance for a payload using reflection.
 * Uses the payload's classloader to handle Spring DevTools restarts properly.
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> getValidatorFor(payload: T): GeneratedValidator<T> {
    val validatorClassName = getValidatorClassName(payload)
    val payloadClassLoader = payload::class.java.classLoader

    // Use payload's classloader to load validator (handles DevTools restarts)
    try {
        val validatorClass = Class.forName(validatorClassName, true, payloadClassLoader)
        return validatorClass.getDeclaredConstructor().newInstance() as GeneratedValidator<T>
    } catch (e: ClassNotFoundException) {
        throw IllegalStateException(
            "Validator not found: $validatorClassName. " +
                "Make sure your class is annotated with @Validated and KSP has generated the validator.",
            e,
        )
    }
}

/**
 * **SIMPLIFIED API**: Validate payload using auto-discovered validator.
 *
 * Automatically finds and instantiates the generated validator based on the payload type.
 * No need to manually instantiate validators!
 *
 * Usage:
 * ```kotlin
 * suspend fun register(request: ServerRequest): ServerResponse {
 *     val payload = request.awaitBody<RegisterRequest>()
 *     payload.validate(request, context)  // That's it!
 *     return ServerResponse.ok().bodyValueAndAwait(...)
 * }
 * ```
 *
 * @receiver The payload to validate (must be annotated with @Validated)
 * @param request ServerRequest for automatic locale extraction
 * @param baseContext Base validation context (injected)
 */
public suspend fun <T : Any> T.validate(
    request: ServerRequest,
    baseContext: ValidationContext,
) {
    val validator = getValidatorFor(this)
    val localizedContext = baseContext.withLocale(request)
    validator.validate(this, localizedContext)
}

/**
 * **SIMPLIFIED API**: Validate payload with default context.
 *
 * Even simpler - uses default ValidationContext (English locale, default dispatcher).
 *
 * Usage:
 * ```kotlin
 * suspend fun register(request: ServerRequest): ServerResponse {
 *     val payload = request.awaitBody<RegisterRequest>()
 *     payload.validate()  // Simplest possible!
 *     return ServerResponse.ok().bodyValueAndAwait(...)
 * }
 * ```
 */
public suspend fun <T : Any> T.validate() {
    val validator = getValidatorFor(this)
    validator.validate(this, ValidationContext())
}

/**
 * **SIMPLIFIED REACTIVE API**: Validate payload and return Mono.
 *
 * Auto-discovers validator and returns Mono for reactive chains.
 *
 * Usage:
 * ```kotlin
 * fun register(request: ServerRequest): Mono<ServerResponse> {
 *     return request.bodyToMono<RegisterRequest>()
 *         .flatMap { it.validateMono(request, context).thenReturn(it) }
 *         .flatMap { ServerResponse.ok().bodyValue(it) }
 * }
 * ```
 */
public fun <T : Any> T.validateMono(
    request: ServerRequest,
    baseContext: ValidationContext,
): Mono<Void> =
    mono {
        validate(request, baseContext)
    }.then()

/**
 * **SIMPLIFIED REACTIVE API**: Validate payload with default context.
 *
 * Usage:
 * ```kotlin
 * fun register(request: ServerRequest): Mono<ServerResponse> {
 *     return request.bodyToMono<RegisterRequest>()
 *         .flatMap { it.validateMono().thenReturn(it) }
 *         .flatMap { ServerResponse.ok().bodyValue(it) }
 * }
 * ```
 */
public fun <T : Any> T.validateMono(): Mono<Void> =
    mono {
        validate()
    }.then()

/**
 * Extension function to validate with locale extracted from the request (EXPLICIT VALIDATOR).
 *
 * **NOTE**: Consider using the simplified `payload.validate(request, context)` instead.
 */
public suspend fun <T> GeneratedValidator<T>.validate(
    payload: T,
    request: ServerRequest,
    baseContext: ValidationContext,
) {
    val localizedContext = baseContext.withLocale(request)
    this.validate(payload, localizedContext)
}

/**
 * Reactive validation that returns a Mono for integration with Spring WebFlux reactive chains.
 *
 * This extension converts the suspend validation function into a Mono, allowing you to
 * compose validation with other reactive operations.
 *
 * Usage:
 * ```kotlin
 * fun handle(request: ServerRequest): Mono<ServerResponse> {
 *     return request.bodyToMono<MyRequest>()
 *         .flatMap { payload ->
 *             MyRequestValidator().validateMono(payload, validationContext)
 *                 .thenReturn(payload)
 *         }
 *         .flatMap { payload ->
 *             ServerResponse.ok().bodyValue(payload)
 *         }
 * }
 * ```
 */
public fun <T> GeneratedValidator<T>.validateMono(
    payload: T,
    context: ValidationContext,
): Mono<Void> =
    mono {
        validate(payload, context)
    }.then()

/**
 * Reactive validation with automatic locale extraction from ServerRequest.
 *
 * Combines locale extraction with reactive validation for a complete solution.
 *
 * Usage:
 * ```kotlin
 * fun handle(request: ServerRequest): Mono<ServerResponse> {
 *     return request.bodyToMono<MyRequest>()
 *         .flatMap { payload ->
 *             MyRequestValidator().validateMono(payload, request, validationContext)
 *                 .thenReturn(payload)
 *         }
 *         .flatMap { payload ->
 *             ServerResponse.ok().bodyValue(payload)
 *         }
 * }
 * ```
 */
public fun <T> GeneratedValidator<T>.validateMono(
    payload: T,
    request: ServerRequest,
    baseContext: ValidationContext,
): Mono<Void> =
    mono {
        val localizedContext = baseContext.withLocale(request)
        validate(payload, localizedContext)
    }.then()
