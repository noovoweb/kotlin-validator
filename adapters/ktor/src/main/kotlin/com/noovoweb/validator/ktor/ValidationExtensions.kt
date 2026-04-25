package com.noovoweb.validator.ktor

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import java.util.concurrent.ConcurrentHashMap

/** Cache for stateless validator instances to avoid repeated reflection. */
@PublishedApi
internal val validatorCache: ConcurrentHashMap<String, GeneratedValidator<*>> = ConcurrentHashMap<String, GeneratedValidator<*>>()

/**
 * Get the validator class name for a given payload type.
 * Convention: MyRequest -> MyRequestValidator
 */
@PublishedApi
internal fun <T : Any> getValidatorClassName(payload: T): String {
    val className = payload::class.qualifiedName ?: throw IllegalArgumentException("Cannot determine class name")
    return "${className}Validator"
}

/**
 * Get validator instance for a payload using reflection with caching.
 *
 * Uses [ConcurrentHashMap.computeIfAbsent] to ensure the validator is constructed at most
 * once per class name, even under concurrent first-request load.
 */
@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> getValidatorFor(payload: T): GeneratedValidator<T> {
    val validatorClassName = getValidatorClassName(payload)
    val payloadClassLoader = payload::class.java.classLoader

    return try {
        validatorCache.computeIfAbsent(validatorClassName) { name ->
            val validatorClass = Class.forName(name, true, payloadClassLoader)
            validatorClass.getDeclaredConstructor().newInstance() as GeneratedValidator<*>
        } as GeneratedValidator<T>
    } catch (e: Exception) {
        val cause = (e as? RuntimeException)?.cause ?: e
        if (cause is ClassNotFoundException) {
            throw IllegalStateException(
                "Validator not found: $validatorClassName. " +
                    "Make sure your class is annotated with @Validated and that at least one field has a validation " +
                    "annotation (e.g. @Required, @MinLength). " +
                    "If no fields are annotated, KSP will skip validator generation.",
                cause,
            )
        }
        throw e
    }
}

// ============================================
// SIMPLIFIED API - Auto-discovery of validator
// ============================================

/**
 * **SIMPLIFIED API**: Validate payload using auto-discovered validator.
 *
 * Usage:
 * ```kotlin
 * post("/api/users") {
 *     val payload = call.receive<UserRequest>()
 *     payload.validate(call)  // That's it!
 * }
 * ```
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails
 */
public suspend fun <T : Any> T.validate(call: ApplicationCall) {
    val validator = getValidatorFor(this)
    val context = call.validationContext()
    validator.validate(this, context)
}

/**
 * **SIMPLIFIED API**: Validate payload with default context.
 *
 * Usage:
 * ```kotlin
 * post("/api/users") {
 *     val payload = call.receive<UserRequest>()
 *     payload.validate()  // Simplest possible!
 * }
 * ```
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails
 */
public suspend fun <T : Any> T.validate() {
    val validator = getValidatorFor(this)
    validator.validate(this, ValidationContext())
}

/**
 * **SIMPLIFIED API**: Receive and validate request body with auto-discovered validator.
 *
 * Usage:
 * ```kotlin
 * post("/api/users") {
 *     val request = call.receiveAndValidate<UserRequest>()
 *     // request is now validated
 * }
 * ```
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(): T {
    val payload = receive<T>()
    val validator = getValidatorFor(payload)
    val context = validationContext()
    validator.validate(payload, context)
    return payload
}

// ============================================
// EXPLICIT API - Manual validator instantiation
// ============================================

/**
 * Receive and validate request body in one call.
 *
 * Example:
 * ```kotlin
 * post("/api/users") {
 *     val request = call.receiveAndValidate<UserRequest>(UserRequestValidator())
 *     // request is now validated
 * }
 * ```
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails (automatically handled by plugin)
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(validator: GeneratedValidator<T>): T {
    val payload = receive<T>()
    val context = validationContext()
    validator.validate(payload, context)
    return payload
}

/**
 * Receive and validate request body with custom ValidationContext.
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails (automatically handled by plugin)
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(
    validator: GeneratedValidator<T>,
    context: ValidationContext,
): T {
    val payload = receive<T>()
    validator.validate(payload, context)
    return payload
}

/**
 * Validate a payload with automatic context extraction from request.
 *
 * @throws com.noovoweb.validator.ValidationException if validation fails (automatically handled by plugin)
 */
public suspend fun <T : Any> ApplicationCall.validate(payload: T, validator: GeneratedValidator<T>,) {
    val context = validationContext()
    validator.validate(payload, context)
}
