package com.noovoweb.validator.ktor

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import io.ktor.server.application.*
import io.ktor.server.request.*

/**
 * Convenience extensions for validation in Ktor routes.
 */

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
 * @throws ValidationException if validation fails (automatically handled by plugin)
 */
suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(
    validator: GeneratedValidator<T>
): T {
    val payload = receive<T>()
    val context = validationContext()
    validator.validate(payload, context)
    return payload
}

/**
 * Receive and validate request body with custom ValidationContext.
 *
 * Example:
 * ```kotlin
 * post("/api/users") {
 *     val customContext = ValidationContext(locale = Locale.FRENCH)
 *     val request = call.receiveAndValidate(UserRequestValidator(), customContext)
 *     // request is now validated with French messages
 * }
 * ```
 *
 * @throws ValidationException if validation fails (automatically handled by plugin)
 */
suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(
    validator: GeneratedValidator<T>,
    context: ValidationContext
): T {
    val payload = receive<T>()
    validator.validate(payload, context)
    return payload
}

/**
 * Validate a payload with automatic context extraction from request.
 *
 * Example:
 * ```kotlin
 * post("/api/users") {
 *     val request = call.receive<UserRequest>()
 *     call.validate(request, UserRequestValidator())
 *     // request is now validated
 * }
 * ```
 *
 * @throws ValidationException if validation fails (automatically handled by plugin)
 */
suspend fun <T : Any> ApplicationCall.validate(
    payload: T,
    validator: GeneratedValidator<T>
) {
    val context = validationContext()
    validator.validate(payload, context)
}
