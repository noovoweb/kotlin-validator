package com.noovoweb.validator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap

/**
 * Default implementation of MessageProvider with pre-cached messages.
 *
 * **NON-BLOCKING**: Messages are pre-loaded at initialization, making getMessage() a pure
 * memory lookup with no I/O operations.
 *
 * Features:
 * - Pre-caches all messages at initialization (one-time blocking operation)
 * - Zero I/O during validation (fully non-blocking)
 * - Thread-safe message lookup
 * - Automatic fallback to English if locale not found
 * - MessageFormat parameter interpolation
 *
 * Supported locales out of the box:
 * - English (en)
 * - French (fr)
 */
class DefaultMessageProvider : MessageProvider {

    private val messageCache = ConcurrentHashMap<Pair<Locale, String>, String>()

    init {
        // Pre-load messages for supported locales at initialization
        loadMessagesForLocale(Locale.ENGLISH)
        loadMessagesForLocale(Locale.FRENCH)
        // Add more locales here as needed
    }

    /**
     * Load all messages for a locale into the cache.
     *
     * This is called during initialization and is the only blocking operation.
     */
    private fun loadMessagesForLocale(locale: Locale) {
        try {
            val bundle = ResourceBundle.getBundle("ValidationMessages", locale)
            bundle.keys.asSequence().forEach { key ->
                messageCache[locale to key] = bundle.getString(key)
            }
        } catch (e: MissingResourceException) {
            // Locale not supported, skip
        }
    }

    /**
     * Get a localized message (NON-BLOCKING).
     *
     * Performs pure memory lookup - no I/O operations.
     * Falls back to English if the locale is not cached.
     */
    override suspend fun getMessage(key: String, args: Array<Any>?, locale: Locale): String {
        // Pure memory lookup - fully non-blocking
        val template = messageCache[locale to key]
            ?: messageCache[Locale.ENGLISH to key]
            ?: key // Fallback to key if not found

        return if (args != null && args.isNotEmpty()) {
            // MessageFormat is CPU-bound, use Default dispatcher
            withContext(Dispatchers.Default) {
                try {
                    MessageFormat(template, locale).format(args)
                } catch (e: Exception) {
                    // If formatting fails, return the template as-is
                    template
                }
            }
        } else {
            template
        }
    }

    /**
     * Get the number of cached messages.
     */
    fun getCacheSize(): Int = messageCache.size

    /**
     * Check if a locale is loaded.
     */
    fun isLocaleLoaded(locale: Locale): Boolean {
        return messageCache.keys.any { it.first == locale }
    }
}
