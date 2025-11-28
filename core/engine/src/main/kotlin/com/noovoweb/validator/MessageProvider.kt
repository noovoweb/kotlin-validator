package com.noovoweb.validator

import java.util.Locale

/**
 * Interface for providing localized validation messages.
 *
 * **NON-BLOCKING**: This interface uses suspend functions to ensure 100% non-blocking operation.
 * Implementations should either pre-cache messages or wrap I/O operations in appropriate dispatchers.
 *
 * Example implementations:
 * - DefaultMessageProvider: Pre-caches all messages at initialization
 * - DatabaseMessageProvider: Wraps database queries in Dispatchers.IO
 * - ApiMessageProvider: Wraps HTTP requests in Dispatchers.IO
 */
interface MessageProvider {
    /**
     * Get localized message for the given key with optional parameters.
     *
     * This is a suspend function to support non-blocking I/O operations.
     * Implementations should:
     * - Pre-cache messages for instant lookup (recommended), OR
     * - Wrap I/O operations in withContext(Dispatchers.IO)
     *
     * @param key Message key (e.g., "field.required", "field.email")
     * @param args Optional parameters for message interpolation (MessageFormat style)
     * @param locale Target locale for the message
     * @return Localized message with parameters interpolated
     *
     * Example:
     * ```kotlin
     * // Simple message
     * getMessage("field.required", null, Locale.ENGLISH)
     * // Returns: "This field is required"
     *
     * // Message with parameters
     * getMessage("field.min", arrayOf(18), Locale.ENGLISH)
     * // Returns: "This field must be at least 18"
     * ```
     */
    suspend fun getMessage(
        key: String,
        args: Array<Any>?,
        locale: Locale,
    ): String
}
