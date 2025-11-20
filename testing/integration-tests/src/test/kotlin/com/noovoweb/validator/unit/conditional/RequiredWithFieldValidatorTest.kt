package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredWithFieldValidatorTest {
    @Test
    fun `requiredwith validator accepts when any dependency present and field present`() = runTest {
        val validator = RequiredWithFieldValidator()
        validator.validate(RequiredWithField(email = "test@example.com", phone = null, name = "John"))
    }

    @Test
    fun `requiredwith validator accepts when no dependencies present`() = runTest {
        val validator = RequiredWithFieldValidator()
        validator.validate(RequiredWithField(email = null, phone = null, name = null))
    }

    @Test
    fun `requiredwith validator rejects when dependency present but field absent`() = runTest {
        val validator = RequiredWithFieldValidator()
        assertThrows<ValidationException> {
            validator.validate(RequiredWithField(email = "test@example.com", phone = null, name = null))
        }
    }

    @Test
    fun `requiredwith validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            RequiredWithFieldValidator().validate(RequiredWithField(email = "test@example.com", phone = null, name = null))
        }
        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
