package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FailFastValidatorTest {
    @Test
    fun `failfast validator accepts valid value`() = runTest {
        val validator = FailFastAtMiddleValidator()
        validator.validate(FailFastAtMiddle(email = "test@example.com"))
    }

    @Test
    fun `failfast at beginning has checkpoint before all validators`() = runTest {
        val validator = FailFastAtBeginningValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtBeginning(email = null))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @FailFast is at position 0 (before @Required at position 1)
        // So there's NO checkpoint to trigger - all validators should run
        // This is different from before: @FailFast at start has no effect
        assertEquals(1, exception.errors["email"]!!.size,
            "Should have Required error (validators run until one fails)")
    }

    @Test
    fun `failfast at beginning with invalid email format`() = runTest {
        val validator = FailFastAtBeginningValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtBeginning(email = "invalid"))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @FailFast at position 0, @Required at 1 (passes), @Email at 2 (fails)
        // No checkpoint between validators, so all run
        assertEquals(1, exception.errors["email"]!!.size,
            "Should have Email error")
    }

    @Test
    fun `failfast at middle checks validators before checkpoint`() = runTest {
        val validator = FailFastAtMiddleValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtMiddle(email = null))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required at position 0 fails, @Email at position 1, @FailFast at position 2
        // Required fails, so we stop at the @FailFast checkpoint after position 1
        assertEquals(1, exception.errors["email"]!!.size,
            "Should have Required error and stop at checkpoint")
    }

    @Test
    fun `failfast at middle stops after checkpoint when earlier validator fails`() = runTest {
        val validator = FailFastAtMiddleValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtMiddle(email = "invalid"))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required passes, @Email at position 1 fails, @FailFast checkpoint at position 2
        // Email fails, checkpoint triggers, @MaxLength is skipped
        assertEquals(1, exception.errors["email"]!!.size,
            "Should have Email error and stop at checkpoint, skipping MaxLength")
    }

    @Test
    fun `failfast at end allows all validators to run`() = runTest {
        val validator = FailFastAtEndValidator()
        val veryLongInvalidEmail = "a".repeat(60)
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtEnd(email = veryLongInvalidEmail))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required passes, @Email fails, @MaxLength fails, @FailFast at end
        // All validators run because checkpoint is AFTER all of them
        assertTrue(exception.errors["email"]!!.size >= 2,
            "Should collect Email and MaxLength errors since checkpoint is at the end")
    }

    @Test
    fun `failfast at end with null value`() = runTest {
        val validator = FailFastAtEndValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(FailFastAtEnd(email = null))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required fails at position 0, but checkpoint is after position 2
        // So all validators still run
        assertEquals(1, exception.errors["email"]!!.size,
            "Should have Required error (other validators skip null)")
    }

    @Test
    fun `multiple failfast checkpoints - stops at first checkpoint when required fails`() = runTest {
        val validator = MultipleFailFastCheckpointsValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(MultipleFailFastCheckpoints(email = null))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required fails → 🛑 Checkpoint 1 triggers → @Email and @MaxLength are SKIPPED
        assertEquals(1, exception.errors["email"]!!.size,
            "Should only have Required error, stops at checkpoint 1")
        assertTrue(exception.errors["email"]!![0].contains("required"),
            "Error should be about required field")
    }

    @Test
    fun `multiple failfast checkpoints - stops at second checkpoint when email fails`() = runTest {
        val validator = MultipleFailFastCheckpointsValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(MultipleFailFastCheckpoints(email = "invalid"))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required passes → @Email fails → 🛑 Checkpoint 2 triggers → @MaxLength is SKIPPED
        assertEquals(1, exception.errors["email"]!!.size,
            "Should only have Email error, stops at checkpoint 2")
        assertTrue(exception.errors["email"]!![0].contains("email") ||
                   exception.errors["email"]!![0].contains("valid"),
            "Error should be about invalid email")
    }

    @Test
    fun `multiple failfast checkpoints - validates maxlength when all previous pass`() = runTest {
        val validator = MultipleFailFastCheckpointsValidator()
        val veryLongEmail = "a".repeat(60) + "@example.com"
        val exception = assertThrows<ValidationException> {
            validator.validate(MultipleFailFastCheckpoints(email = veryLongEmail))
        }

        assertTrue(exception.errors.containsKey("email"))
        // @Required passes → Checkpoint 1 → @Email passes → Checkpoint 2 → @MaxLength fails
        assertEquals(1, exception.errors["email"]!!.size,
            "Should only have MaxLength error")
        assertTrue(exception.errors["email"]!![0].contains("50") ||
                   exception.errors["email"]!![0].contains("length"),
            "Error should be about max length")
    }

    @Test
    fun `multiple failfast checkpoints - passes with valid short email`() = runTest {
        val validator = MultipleFailFastCheckpointsValidator()
        // This should pass all validators
        validator.validate(MultipleFailFastCheckpoints(email = "test@example.com"))
        // If no exception thrown, test passes
    }
}
