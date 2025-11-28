package com.noovoweb.validator

/**
 * Validates that a number is greater than or equal to the specified value.
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.min`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Min(
    val value: Double,
    val message: String = "",
)

/**
 * Validates that a number is less than or equal to the specified value.
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.max`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Max(
    val value: Double,
    val message: String = "",
)

/**
 * Validates that a number is within the specified range.
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param min Minimum value (inclusive)
 * @param max Maximum value (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.between`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Between(
    val min: Double,
    val max: Double,
    val message: String = "",
)

/**
 * Validates that a number is positive (greater than zero).
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.positive`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Positive(
    val message: String = "",
)

/**
 * Validates that a number is negative (less than zero).
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.negative`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Negative(
    val message: String = "",
)

/**
 * Validates that a number equals zero.
 *
 * Supports: Int, Long, Float, Double, Short, Byte
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.zero`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Zero(
    val message: String = "",
)

/**
 * Validates that a value is a whole number (no decimal places).
 *
 * For floating-point types, checks that the value has no fractional part.
 * For integer types, always passes.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.integer`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Integer(
    val message: String = "",
)

/**
 * Validates that a value has decimal places.
 *
 * For floating-point types, checks that the value has a fractional part.
 * For integer types, always fails.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.decimal`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Decimal(
    val message: String = "",
)

/**
 * Validates that a number is divisible by the specified value.
 *
 * Checks if number % divisor == 0
 *
 * @param value The divisor
 * @param message Custom error message (optional)
 *
 * Message key: `field.divisibleby`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DivisibleBy(
    val value: Int,
    val message: String = "",
)

/**
 * Validates that a number is even.
 *
 * Checks if number % 2 == 0
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.even`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Even(
    val message: String = "",
)

/**
 * Validates that a number is odd.
 *
 * Checks if number % 2 != 0
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.odd`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Odd(
    val message: String = "",
)

/**
 * Validates that a string representation of a decimal number has exactly the specified number of decimal places.
 *
 * For example, @DecimalPlaces(2) validates that "99.99" is valid, but "99.9" or "99.999" are not.
 *
 * **Note**: This validator only works on String types. For Float/Double types, use custom validation.
 *
 * @param value Number of required decimal places
 * @param message Custom error message (optional)
 *
 * Message key: `field.decimalplaces`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DecimalPlaces(
    val value: Int,
    val message: String = "",
)
