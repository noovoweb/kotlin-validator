package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proves that @Valid(each = true) bounds its coroutine fan-out to
 * [ValidationContext.maxElementConcurrency] instead of spawning one coroutine per
 * element (which a large collection could use to exhaust memory/scheduler resources).
 */
object ConcurrencyProbe {
    private val active = AtomicInteger(0)
    val maxObserved = AtomicInteger(0)
    val totalCalls = AtomicInteger(0)

    fun reset() {
        active.set(0)
        maxObserved.set(0)
        totalCalls.set(0)
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun track(value: String?, context: ValidationContext): Boolean {
        val now = active.incrementAndGet()
        maxObserved.updateAndGet { current -> maxOf(current, now) }
        totalCalls.incrementAndGet()
        try {
            // Hold the "slot" long enough that siblings in the same chunk overlap.
            delay(20)
        } finally {
            active.decrementAndGet()
        }
        return true
    }
}

@Validated
data class ProbeItem(
    @CustomValidator(validator = "com.noovoweb.validator.integration.ConcurrencyProbe::track")
    val token: String?
)

@Validated
data class ProbeBatch(
    @Valid(each = true)
    val items: List<ProbeItem>?
)

class EachElementConcurrencyIntegrationTest {
    @Test
    fun `each-element validation never exceeds maxElementConcurrency`() = runTest {
        ConcurrencyProbe.reset()
        val cap = 4
        val batch = ProbeBatch(items = List(100) { ProbeItem(token = "t$it") })

        val context = ValidationContext().withMaxElementConcurrency(cap)
        ProbeBatchValidator().validate(batch, context)

        assertEquals(100, ConcurrencyProbe.totalCalls.get(), "every element should be validated")
        assertTrue(
            ConcurrencyProbe.maxObserved.get() <= cap,
            "observed ${ConcurrencyProbe.maxObserved.get()} concurrent validations, cap was $cap"
        )
    }

    @Test
    fun `each-element validation still runs elements concurrently up to the cap`() = runTest {
        ConcurrencyProbe.reset()
        val cap = 8
        val batch = ProbeBatch(items = List(100) { ProbeItem(token = "t$it") })

        val context = ValidationContext().withMaxElementConcurrency(cap)
        ProbeBatchValidator().validate(batch, context)

        // With 100 elements and a 20ms hold, more than one must have overlapped —
        // proves the chunking didn't collapse into sequential validation.
        assertTrue(
            ConcurrencyProbe.maxObserved.get() >= 2,
            "expected concurrent validation, but max observed was ${ConcurrencyProbe.maxObserved.get()}"
        )
        assertTrue(ConcurrencyProbe.maxObserved.get() <= cap)
    }
}
