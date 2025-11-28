package com.noovoweb.validator

/**
 * Validates that the value equals another field's value.
 *
 * Commonly used for password confirmation fields.
 *
 * @param field Name of the field to compare with
 * @param message Custom error message (optional)
 *
 * Message key: `field.same`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class Registration(
 *     val password: String?,
 *     @Same("password") val passwordConfirmation: String?
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Same(
    val field: String,
    val message: String = "",
)

/**
 * Validates that the value differs from another field's value.
 *
 * @param field Name of the field to compare with
 * @param message Custom error message (optional)
 *
 * Message key: `field.different`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class AccountUpdate(
 *     val currentPassword: String?,
 *     @Different("currentPassword") val newPassword: String?
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Different(
    val field: String,
    val message: String = "",
)

/**
 * Validates that the field is required if another field equals a specific value.
 *
 * @param field Name of the field to check
 * @param value The value that triggers this field to be required
 * @param message Custom error message (optional)
 *
 * Message key: `field.requiredif`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class ShippingInfo(
 *     val shipToAddress: String?,  // "home" or "other"
 *     @RequiredIf("shipToAddress", "other") val customAddress: String?
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiredIf(
    val field: String,
    val value: String,
    val message: String = "",
)

/**
 * Validates that the field is required unless another field equals a specific value.
 *
 * @param field Name of the field to check
 * @param value The value that makes this field optional
 * @param message Custom error message (optional)
 *
 * Message key: `field.requiredunless`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class PaymentInfo(
 *     val paymentMethod: String?,  // "card" or "cash"
 *     @RequiredUnless("paymentMethod", "cash") val cardNumber: String?
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiredUnless(
    val field: String,
    val value: String,
    val message: String = "",
)

/**
 * Validates that the field is required if any of the specified fields are present.
 *
 * @param fields Array of field names to check
 * @param message Custom error message (optional)
 *
 * Message key: `field.requiredwith`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class ContactInfo(
 *     val email: String?,
 *     val phone: String?,
 *     @RequiredWith(["email", "phone"]) val name: String?  // Required if email OR phone present
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiredWith(
    val fields: Array<String>,
    val message: String = "",
)

/**
 * Validates that the field is required if all of the specified fields are absent.
 *
 * @param fields Array of field names to check
 * @param message Custom error message (optional)
 *
 * Message key: `field.requiredwithout`
 *
 * Example:
 * ```kotlin
 * @Validated
 * data class ContactInfo(
 *     val email: String?,
 *     val phone: String?,
 *     @RequiredWithout(["email", "phone"]) val mailingAddress: String?  // Required if no email AND no phone
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiredWithout(
    val fields: Array<String>,
    val message: String = "",
)
