package com.noovoweb.validator.core

import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ValidationContext configuration and builder pattern.
 */
class ValidationContextTest {
    @Test
    fun `ValidationContext creates with default values`() {
        val context = ValidationContext()

        assertEquals(Locale.ENGLISH, context.locale)
        assertEquals(Dispatchers.Default, context.dispatcher)
        assertTrue(context.metadata.isEmpty())
    }

    @Test
    fun `withLocale builder updates locale`() {
        val context = ValidationContext()
        val updatedContext = context.withLocale(Locale.FRENCH)

        assertEquals(Locale.FRENCH, updatedContext.locale)
        // Original should not be mutated
        assertEquals(Locale.ENGLISH, context.locale)
    }

    @Test
    fun `withLocale builder returns new instance`() {
        val context1 = ValidationContext()
        val context2 = context1.withLocale(Locale.FRENCH)

        assertNotEquals(context1, context2)
    }

    @Test
    fun `withDispatcher builder updates dispatcher`() {
        val context = ValidationContext()
        val updatedContext = context.withDispatcher(Dispatchers.IO)

        assertEquals(Dispatchers.IO, updatedContext.dispatcher)
        assertEquals(Dispatchers.Default, context.dispatcher)
    }

    @Test
    fun `withClock builder updates clock for testability`() {
        val context = ValidationContext()
        val testClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val updatedContext = context.withClock(testClock)

        assertEquals(testClock, updatedContext.clock)
        assertNotEquals(testClock, context.clock)
    }

    @Test
    fun `withMetadata builder adds metadata key-value pair`() {
        val context = ValidationContext()
        val updatedContext = context.withMetadata("userId", 123)

        assertEquals(123, updatedContext.metadata["userId"])
        assertTrue(context.metadata.isEmpty())
    }

    @Test
    fun `withMetadata builder can chain multiple additions`() {
        val context =
            ValidationContext()
                .withMetadata("userId", 123)
                .withMetadata("requestId", "req-456")
                .withMetadata("timestamp", System.currentTimeMillis())

        assertEquals(123, context.metadata["userId"])
        assertEquals("req-456", context.metadata["requestId"])
        assertTrue(context.metadata.containsKey("timestamp"))
    }

    @Test
    fun `builder chain maintains immutability`() {
        val context1 = ValidationContext()
        val context2 =
            context1
                .withLocale(Locale.FRENCH)
                .withDispatcher(Dispatchers.IO)
                .withMetadata("test", "value")

        assertEquals(Locale.ENGLISH, context1.locale)
        assertEquals(Dispatchers.Default, context1.dispatcher)
        assertTrue(context1.metadata.isEmpty())

        assertEquals(Locale.FRENCH, context2.locale)
        assertEquals(Dispatchers.IO, context2.dispatcher)
        assertEquals("value", context2.metadata["test"])
    }

    @Test
    fun `can create context with custom locale and dispatcher`() {
        val context =
            ValidationContext(
                locale = Locale.FRENCH,
                dispatcher = Dispatchers.IO,
            )

        assertEquals(Locale.FRENCH, context.locale)
        assertEquals(Dispatchers.IO, context.dispatcher)
    }

    @Test
    fun `metadata is immutable copy`() {
        val metadataMap = mapOf("key" to "value")
        val context = ValidationContext(metadata = metadataMap)

        assertEquals("value", context.metadata["key"])
        assertEquals(1, context.metadata.size)
    }
}
