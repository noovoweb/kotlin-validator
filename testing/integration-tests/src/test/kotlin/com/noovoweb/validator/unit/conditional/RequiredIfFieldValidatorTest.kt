package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredIfFieldValidatorTest {
    @Test
    fun `requiredif validator accepts when condition met and field present`() = runTest {
        val validator = RequiredIfFieldValidator()
        validator.validate(RequiredIfField(shipToAddress = "other", customAddress = "123 Main St"))
    }

    @Test
    fun `requiredif validator accepts when condition not met`() = runTest {
        val validator = RequiredIfFieldValidator()
        validator.validate(RequiredIfField(shipToAddress = "home", customAddress = null))
    }

    @Test
    fun `requiredif validator rejects when condition met but field absent`() = runTest {
        val validator = RequiredIfFieldValidator()
        assertThrows<ValidationException> {
            validator.validate(RequiredIfField(shipToAddress = "other", customAddress = null))
        }
    }

    @Test
    fun `requiredif validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            RequiredIfFieldValidator().validate(RequiredIfField(shipToAddress = "other", customAddress = null))
        }
        assertTrue(exception.errors.containsKey("customAddress"))
        assertFalse(exception.errors["customAddress"]!!.isEmpty())
    }
}
