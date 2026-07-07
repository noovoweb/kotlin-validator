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
 * @property currentDepth Current nesting depth during @Valid traversal (0 = top-level)
 * @property maxValidationDepth Maximum allowed nesting depth for @Valid (prevents circular reference stack overflows)
 * @property maxElementConcurrency Maximum number of collection elements validated concurrently during @Valid(each = true)
 *   (bounds coroutine fan-out so a large collection cannot exhaust memory/scheduler resources)
 */
public data class ValidationContext(
    public val locale: Locale = Locale.ENGLISH,
    public val messageProvider: MessageProvider = DefaultMessageProvider.DEFAULT,
    public val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val clock: Clock = Clock.systemDefaultZone(),
    public val metadata: Map<String, Any> = emptyMap(),
    public val currentDepth: Int = 0,
    public val maxValidationDepth: Int = DEFAULT_MAX_VALIDATION_DEPTH,
    public val maxElementConcurrency: Int = DEFAULT_MAX_ELEMENT_CONCURRENCY
) {
    public fun withLocale(locale: Locale): ValidationContext = copy(locale = locale)

    public fun withMessageProvider(provider: MessageProvider): ValidationContext = copy(messageProvider = provider)

    public fun withDispatcher(dispatcher: CoroutineDispatcher): ValidationContext = copy(dispatcher = dispatcher)

    public fun withClock(clock: Clock): ValidationContext = copy(clock = clock)

    public fun withMetadata(key: String, value: Any): ValidationContext = copy(metadata = metadata + (key to value))

    public fun withMetadata(metadata: Map<String, Any>): ValidationContext = copy(metadata = metadata)

    public fun withMaxValidationDepth(maxDepth: Int): ValidationContext = copy(maxValidationDepth = maxDepth)

    public fun withMaxElementConcurrency(maxConcurrency: Int): ValidationContext {
        require(maxConcurrency > 0) { "maxElementConcurrency must be > 0, was $maxConcurrency" }
        return copy(maxElementConcurrency = maxConcurrency)
    }

    /**
     * Returns a copy with incremented depth for nested @Valid traversal.
     */
    public fun withIncrementedDepth(): ValidationContext = copy(currentDepth = currentDepth + 1)

    /**
     * Returns true if another level of nested validation is allowed.
     */
    public fun canGoDeeper(): Boolean = currentDepth < maxValidationDepth

    public companion object {
        /**
         * Default maximum depth for nested @Valid traversal.
         * Prevents stack overflows from circular references.
         */
        public const val DEFAULT_MAX_VALIDATION_DEPTH: Int = 10

        /**
         * Default cap on concurrently-validated collection elements for @Valid(each = true).
         * Bounds coroutine fan-out so a large collection cannot spawn one coroutine per element.
         */
        public const val DEFAULT_MAX_ELEMENT_CONCURRENCY: Int = 64

        public fun forIO(
            locale: Locale = Locale.ENGLISH,
            messageProvider: MessageProvider = DefaultMessageProvider.DEFAULT
        ): ValidationContext = ValidationContext(
            locale = locale,
            messageProvider = messageProvider,
            dispatcher = Dispatchers.IO,
            clock = Clock.systemDefaultZone(),
            metadata = emptyMap()
        )

        public fun forTesting(clock: Clock = Clock.systemUTC(), locale: Locale = Locale.ENGLISH): ValidationContext = ValidationContext(
            locale = locale,
            messageProvider = DefaultMessageProvider.DEFAULT,
            dispatcher = Dispatchers.Default,
            clock = clock,
            metadata = emptyMap()
        )
    }
}
