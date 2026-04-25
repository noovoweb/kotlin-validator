package com.noovoweb.validator

/**
 * Validates that a string matches the specified date format.
 *
 * Uses Java's DateTimeFormatter to parse and validate the format.
 *
 * Supports: String, LocalDate, LocalDateTime
 *
 * @param format Date format pattern (e.g., "yyyy-MM-dd", "dd/MM/yyyy")
 * @param message Custom error message (optional)
 *
 * Message key: `field.dateformat`
 *
 * Example:
 * ```kotlin
 * @DateFormat("yyyy-MM-dd")
 * val birthDate: String?
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class DateFormat(
    val format: String,
    val message: String = "",
)

/**
 * Validates that a string is a valid ISO 8601 date (YYYY-MM-DD).
 *
 * Supports: String, LocalDate
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.isodate`
 *
 * Example:
 * ```kotlin
 * @IsoDate
 * val createdAt: String?  // Must be "2024-11-14"
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class IsoDate(
    val message: String = "",
)

/**
 * Validates that a string is a valid ISO 8601 datetime.
 *
 * Supports: String, LocalDateTime, Instant
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.isodatetime`
 *
 * Example:
 * ```kotlin
 * @IsoDateTime
 * val timestamp: String?  // Must be "2024-11-14T10:30:00Z"
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class IsoDateTime(
    val message: String = "",
)

/**
 * Validates that a date is in the future.
 *
 * Uses the Clock from ValidationContext for current time (testable, non-blocking).
 *
 * Supports: String (parsed), LocalDate, LocalDateTime, Instant
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.future`
 *
 * Example:
 * ```kotlin
 * @Future
 * val eventDate: LocalDate?  // Must be after today
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Future(
    val message: String = "",
)

/**
 * Validates that a date is in the past.
 *
 * Uses the Clock from ValidationContext for current time (testable, non-blocking).
 *
 * Supports: String (parsed), LocalDate, LocalDateTime, Instant
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.past`
 *
 * Example:
 * ```kotlin
 * @Past
 * val birthDate: LocalDate?  // Must be before today
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Past(
    val message: String = "",
)

/**
 * Validates that a date is today.
 *
 * Uses the Clock from ValidationContext for current time (testable, non-blocking).
 *
 * Supports: String (parsed), LocalDate, LocalDateTime, Instant
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.today`
 *
 * Example:
 * ```kotlin
 * @Today
 * val checkInDate: LocalDate?  // Must be today's date
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Today(
    val message: String = "",
)
