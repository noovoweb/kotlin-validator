package com.noovoweb.validator

/**
 * Marker annotation for data classes that should have validators generated.
 *
 * Apply this annotation to a data class to trigger compile-time validator generation
 * via Kotlin Symbol Processing (KSP).
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class User(
 *     @Required @Email val email: String?,
 *     @Min(18.0) val age: Int?
 * )
 * ```
 *
 * The KSP processor will generate a `UserValidator` class that implements
 * `GeneratedValidator<User>` with all validation logic.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Validated
