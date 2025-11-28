package com.noovoweb.validator

/**
 * Validates that a value is not null and not blank.
 *
 * For strings, checks that the value is not null, not empty, and not just whitespace.
 * For other types, checks that the value is not null.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.required`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Required(
    val message: String = "",
)

/**
 * Validates that a string is a valid email address.
 *
 * Uses a standard email regex pattern to validate format.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.email`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Email(
    val message: String = "",
)

/**
 * Validates that a string is a valid URL.
 *
 * Checks for valid HTTP/HTTPS URL format.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.url`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Url(
    val message: String = "",
)

/**
 * Validates that a string is a valid UUID.
 *
 * Checks for standard UUID format (8-4-4-4-12 hexadecimal).
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.uuid`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Uuid(
    val message: String = "",
)

/**
 * Validates that a string length is within a specified range.
 *
 * @param min Minimum length (inclusive)
 * @param max Maximum length (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.length`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Length(
    val min: Int,
    val max: Int,
    val message: String = "",
)

/**
 * Validates that a string length is at least the specified value.
 *
 * @param value Minimum length (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.minlength`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MinLength(
    val value: Int,
    val message: String = "",
)

/**
 * Validates that a string length does not exceed the specified value.
 *
 * @param value Maximum length (inclusive)
 * @param message Custom error message (optional)
 *
 * Message key: `field.maxlength`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MaxLength(
    val value: Int,
    val message: String = "",
)

/**
 * Validates that a string matches a regular expression pattern.
 *
 * **SECURITY WARNING**: Be careful with regex patterns to avoid ReDoS attacks!
 *
 * **Safe patterns:**
 * - Simple character classes: `^[a-z]+$`
 * - Fixed length: `^\d{5}$`
 * - Anchored patterns: `^user_.*$`
 *
 * **DANGEROUS patterns (will cause compile error):**
 * - Nested quantifiers: `(a+)+` or `(a*)*`
 * - Multiple overlapping: `.*.*` or `.+.+`
 * - Complex alternations: `(a|ab)+`
 *
 * **Best practices:**
 * 1. Keep patterns simple
 * 2. Use `@MaxLength` before `@Pattern` to limit input size
 * 3. Test patterns with long inputs (10,000+ chars)
 * 4. Avoid `.*` and `.+` when possible
 * 5. Use anchors (^ and $) to prevent unnecessary backtracking
 *
 * **Input length limit:** Maximum 10,000 characters for ReDoS protection.
 *
 * @param value Regular expression pattern
 * @param message Custom error message (optional)
 *
 * Message key: `field.pattern`
 *
 * @see <a href="https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS">OWASP ReDoS</a>
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Pattern(
    val value: String,
    val message: String = "",
)

/**
 * Validates that a string contains only alphabetic characters (a-z, A-Z).
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.alpha`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Alpha(
    val message: String = "",
)

/**
 * Validates that a string contains only alphanumeric characters (a-z, A-Z, 0-9).
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.alphanumeric`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Alphanumeric(
    val message: String = "",
)

/**
 * Validates that a string contains only ASCII characters.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.ascii`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Ascii(
    val message: String = "",
)

/**
 * Validates that a string is entirely lowercase.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.lowercase`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Lowercase(
    val message: String = "",
)

/**
 * Validates that a string is entirely uppercase.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.uppercase`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Uppercase(
    val message: String = "",
)

/**
 * Validates that a string starts with the specified value.
 *
 * @param value The prefix to check for
 * @param message Custom error message (optional)
 *
 * Message key: `field.startswith`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StartsWith(
    val value: String,
    val message: String = "",
)

/**
 * Validates that a string ends with the specified value.
 *
 * @param value The suffix to check for
 * @param message Custom error message (optional)
 *
 * Message key: `field.endswith`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class EndsWith(
    val value: String,
    val message: String = "",
)

/**
 * Validates that a string contains the specified value.
 *
 * @param value The substring to check for
 * @param message Custom error message (optional)
 *
 * Message key: `field.contains`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Contains(
    val value: String,
    val message: String = "",
)

/**
 * Validates that a value is one of the allowed values.
 *
 * @param values Array of allowed values
 * @param message Custom error message (optional)
 *
 * Message key: `field.oneof`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class OneOf(
    val values: Array<String>,
    val message: String = "",
)

/**
 * Validates that a value is not one of the forbidden values.
 *
 * @param values Array of forbidden values
 * @param message Custom error message (optional)
 *
 * Message key: `field.notoneof`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class NotOneOf(
    val values: Array<String>,
    val message: String = "",
)

/**
 * Validates that a string is valid JSON.
 *
 * Performs basic JSON validation (checks for valid structure).
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.json`
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Json(
    val message: String = "",
)

/**
 * Validates that a string passes the Luhn algorithm check.
 *
 * The Luhn algorithm (mod 10) is used to validate credit card numbers,
 * IMEI numbers, and other identification numbers.
 *
 * The string must contain only digits (spaces and hyphens are automatically removed).
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.luhn`
 *
 * @see <a href="https://en.wikipedia.org/wiki/Luhn_algorithm">Luhn algorithm</a>
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Luhn(
    val message: String = "",
)
