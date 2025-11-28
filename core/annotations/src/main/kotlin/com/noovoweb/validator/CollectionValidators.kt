package com.noovoweb.validator

/**
 * Validates that a collection size is within the specified range.
 *
 * Supports: List, Set, Array, Map
 *
 * @param min Minimum size (inclusive)
 * @param max Maximum size (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.size`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Size(
    val min: Int,
    val max: Int,
    val message: String = "",
)

/**
 * Validates that a collection size is at least the specified value.
 *
 * Supports: List, Set, Array, Map
 *
 * @param value Minimum size (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.minsize`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MinSize(
    val value: Int,
    val message: String = "",
)

/**
 * Validates that a collection size does not exceed the specified value.
 *
 * Supports: List, Set, Array, Map
 *
 * @param value Maximum size (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.maxsize`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MaxSize(
    val value: Int,
    val message: String = "",
)

/**
 * Validates that a collection is not empty.
 *
 * Supports: List, Set, Array, Map, String
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.notempty`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class NotEmpty(
    val message: String = "",
)

/**
 * Validates that all elements in a collection are unique.
 *
 * Supports: List, Array
 * Note: Set is always distinct by definition
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.distinct`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Distinct(
    val message: String = "",
)

/**
 * Validates that a collection contains the specified value.
 *
 * Supports: List, Set, Array
 *
 * @param value The value that must be present
 * @param message Custom error message (optional)
 *
 * Message key: `field.containsvalue`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ContainsValue(
    val value: String,
    val message: String = "",
)

/**
 * Validates that a collection does not contain the specified value.
 *
 * Supports: List, Set, Array
 *
 * @param value The value that must not be present
 * @param message Custom error message (optional)
 *
 * Message key: `field.notcontains`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class NotContains(
    val value: String,
    val message: String = "",
)
