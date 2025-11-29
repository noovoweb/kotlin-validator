package com.noovoweb.validator

import kotlin.reflect.KClass

/**
 * Validates that a String value matches one of the enum constants.
 *
 * This validator is useful when you want to accept enum values as strings
 * (e.g., from JSON) while still validating they are valid enum constants.
 *
 * **Usage:**
 * ```kotlin
 * enum class Status { ACTIVE, INACTIVE, PENDING }
 *
 * @Validated
 * data class UpdateRequest(
 *     @Required
 *     @Enum(Status::class)
 *     val status: String
 * )
 * ```
 *
 * @param value The enum class to validate against
 * @param message Optional custom error message. Default message includes allowed values.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Enum(
    val value: KClass<out kotlin.Enum<*>>,
    val message: String = ""
)
