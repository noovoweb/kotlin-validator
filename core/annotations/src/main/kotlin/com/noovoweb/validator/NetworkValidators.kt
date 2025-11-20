package com.noovoweb.validator

/**
 * Validates that a string is a valid IPv4 address.
 *
 * Checks for valid IPv4 format (e.g., "192.168.1.1").
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.ipv4`
 *
 * Example:
 * ```kotlin
 * @IPv4
 * val serverAddress: String?  // Must be "192.168.1.1"
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IPv4(
    val message: String = ""
)

/**
 * Validates that a string is a valid IPv6 address.
 *
 * Checks for valid IPv6 format (e.g., "2001:0db8:85a3::8a2e:0370:7334").
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.ipv6`
 *
 * Example:
 * ```kotlin
 * @IPv6
 * val serverAddress: String?  // Must be valid IPv6
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IPv6(
    val message: String = ""
)

/**
 * Validates that a string is a valid IP address (IPv4 or IPv6).
 *
 * Accepts both IPv4 and IPv6 formats.
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.ip`
 *
 * Example:
 * ```kotlin
 * @IP
 * val ipAddress: String?  // Can be IPv4 or IPv6
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IP(
    val message: String = ""
)

/**
 * Validates that a string is a valid MAC address.
 *
 * Checks for valid MAC address format (e.g., "00:1B:44:11:3A:B7" or "00-1B-44-11-3A-B7").
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.macaddress`
 *
 * Example:
 * ```kotlin
 * @MacAddress
 * val deviceMac: String?  // Must be "00:1B:44:11:3A:B7"
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MacAddress(
    val message: String = ""
)

/**
 * Validates that a number is a valid port number (1-65535).
 *
 * Supports: Int, String (parsed as Int)
 *
 * @param message Custom error message (optional)
 *
 * Message key: `field.port`
 *
 * Example:
 * ```kotlin
 * @Port
 * val serverPort: Int?  // Must be 1-65535
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Port(
    val message: String = ""
)
