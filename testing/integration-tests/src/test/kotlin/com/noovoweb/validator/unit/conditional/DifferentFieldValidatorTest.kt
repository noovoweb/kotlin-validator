package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DifferentFieldValidatorTest {
    @Test
    fun `different validator accepts different values`() = runTest {
        val validator = DifferentFieldValidator()
        validator.validate(DifferentField(currentPassword = "old", newPassword = "new"))
    }

    @Test
    fun `different validator rejects same values`() = runTest {
        val validator = DifferentFieldValidator()
        assertThrows<ValidationException> {
            validator.validate(DifferentField(currentPassword = "same", newPassword = "same"))
        }
    }

    @Test
    fun `different validator accepts when one is null`() = runTest {
        DifferentFieldValidator().validate(DifferentField(currentPassword = "old", newPassword = null))
    }

    @Test
    fun `different validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            DifferentFieldValidator().validate(DifferentField(currentPassword = "same", newPassword = "same"))
        }
        assertTrue(exception.errors.containsKey("newPassword"))
        assertFalse(exception.errors["newPassword"]!!.isEmpty())
    }
}
