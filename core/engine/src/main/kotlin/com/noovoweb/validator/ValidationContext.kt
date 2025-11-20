package com.noovoweb.validator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.util.Locale

/**
 * Configuration context for validation.
 *
 * **NON-BLOCKING DESIGN**: All fields support non-blocking operation:
 * - MessageProvider uses suspend functions
 * - Clock is injectable (no hidden system calls)
 * - Dispatcher is configurable for I/O operations
 *
 * @property locale Locale for error messages (default: English)
 * @property messageProvider Provider for localized messages (default: DefaultMessageProvider with pre-cached messages)
 * @property dispatcher Coroutine dispatcher for validation (default: Dispatchers.Default)
 * @property clock Clock for date/time validation (default: system clock, injectable for testing)
 * @property metadata Custom metadata for extensibility
 */
data class ValidationContext(
    val locale: Locale = Locale.ENGLISH,
    val messageProvider: MessageProvider = DefaultMessageProvider(),
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val clock: Clock = Clock.systemDefaultZone(),
    val metadata: Map<String, Any> = emptyMap()
) {

    /**
     * Create a copy with a different locale.
     */
    fun withLocale(locale: Locale): ValidationContext = copy(locale = locale)

    /**
     * Create a copy with a different message provider.
     */
    fun withMessageProvider(provider: MessageProvider): ValidationContext = copy(messageProvider = provider)

    /**
     * Create a copy with a different dispatcher.
     *
     * Recommended dispatchers:
     * - Dispatchers.Default: CPU-bound validations (regex, comparisons)
     * - Dispatchers.IO: I/O-bound validations (file operations, database, API calls)
     * - Custom dispatcher: Fine-grained control (e.g., limitedParallelism(4))
     */
    fun withDispatcher(dispatcher: CoroutineDispatcher): ValidationContext = copy(dispatcher = dispatcher)

    /**
     * Create a copy with a different clock.
     *
     * Useful for testing date/time validations (@Future, @Past, @Today).
     */
    fun withClock(clock: Clock): ValidationContext = copy(clock = clock)

    /**
     * Create a copy with additional metadata.
     */
    fun withMetadata(key: String, value: Any): ValidationContext =
        copy(metadata = metadata + (key to value))

    /**
     * Create a copy with completely replaced metadata.
     */
    fun withMetadata(metadata: Map<String, Any>): ValidationContext = copy(metadata = metadata)

    companion object {
        /**
         * Create a context optimized for I/O-bound validations.
         *
         * Uses Dispatchers.IO for file operations, database queries, API calls.
         */
        fun forIO(
            locale: Locale = Locale.ENGLISH,
            messageProvider: MessageProvider = DefaultMessageProvider()
        ): ValidationContext = ValidationContext(
            locale = locale,
            messageProvider = messageProvider,
            dispatcher = Dispatchers.IO,
            clock = Clock.systemDefaultZone(),
            metadata = emptyMap()
        )

        /**
         * Create a context optimized for testing.
         *
         * Uses a fixed clock for deterministic date/time validation.
         */
        fun forTesting(
            clock: Clock = Clock.systemUTC(),
            locale: Locale = Locale.ENGLISH
        ): ValidationContext = ValidationContext(
            locale = locale,
            messageProvider = DefaultMessageProvider(),
            dispatcher = Dispatchers.Default,
            clock = clock,
            metadata = emptyMap()
        )
    }
}
