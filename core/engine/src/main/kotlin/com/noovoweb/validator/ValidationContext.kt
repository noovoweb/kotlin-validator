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
public data class ValidationContext(
    public val locale: Locale = Locale.ENGLISH,
    public val messageProvider: MessageProvider = DefaultMessageProvider(),
    public val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val clock: Clock = Clock.systemDefaultZone(),
    public val metadata: Map<String, Any> = emptyMap(),
) {
    public fun withLocale(locale: Locale): ValidationContext = copy(locale = locale)

    public fun withMessageProvider(provider: MessageProvider): ValidationContext = copy(messageProvider = provider)

    public fun withDispatcher(dispatcher: CoroutineDispatcher): ValidationContext = copy(dispatcher = dispatcher)

    public fun withClock(clock: Clock): ValidationContext = copy(clock = clock)

    public fun withMetadata(
        key: String,
        value: Any,
    ): ValidationContext = copy(metadata = metadata + (key to value))

    public fun withMetadata(metadata: Map<String, Any>): ValidationContext = copy(metadata = metadata)

    public companion object {
        public fun forIO(
            locale: Locale = Locale.ENGLISH,
            messageProvider: MessageProvider = DefaultMessageProvider(),
        ): ValidationContext =
            ValidationContext(
                locale = locale,
                messageProvider = messageProvider,
                dispatcher = Dispatchers.IO,
                clock = Clock.systemDefaultZone(),
                metadata = emptyMap(),
            )

        public fun forTesting(
            clock: Clock = Clock.systemUTC(),
            locale: Locale = Locale.ENGLISH,
        ): ValidationContext =
            ValidationContext(
                locale = locale,
                messageProvider = DefaultMessageProvider(),
                dispatcher = Dispatchers.Default,
                clock = clock,
                metadata = emptyMap(),
            )
    }
}
