package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotContainsListValidatorTest {
    @Test
    fun `notcontains validator accepts lists without forbidden value`() = runTest {
        val validator = NotContainsListValidator()
        validator.validate(NotContainsList(items = listOf("a", "b")))
    }

    @Test
    fun `notcontains validator rejects lists with forbidden value`() = runTest {
        val validator = NotContainsListValidator()
        assertThrows<ValidationException> { validator.validate(NotContainsList(items = listOf("forbidden", "other"))) }
    }

    @Test
    fun `notcontains validator allows null`() = runTest {
        NotContainsListValidator().validate(NotContainsList(items = null))
    }

    @Test
    fun `notcontains validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            NotContainsListValidator().validate(NotContainsList(items = listOf("forbidden")))
        }
        assertTrue(exception.errors.containsKey("items"))
        assertFalse(exception.errors["items"]!!.isEmpty())
    }
}
