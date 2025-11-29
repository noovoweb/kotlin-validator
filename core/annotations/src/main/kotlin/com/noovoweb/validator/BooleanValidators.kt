@file:Suppress("MatchingDeclarationName", "Filename")

package com.noovoweb.validator

/**
 * Validates that a value is accepted (truthy).
 *
 * Accepts the following values as valid:
 * - Boolean: true
 * - String: "1", "yes", "true", "on" (case-insensitive)
 * - Int: 1
 *
 * Commonly used for terms of service acceptance, privacy policy agreements, etc.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.accepted`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class Registration(
 *     @Accepted val termsAccepted: Boolean?,
 *     @Accepted val privacyAccepted: String?
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Accepted(
    val message: String = "",
)
