package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaxSizeListValidatorTest {
    @Test
    fun `maxsize validator accepts lists at or below maximum`() = runTest {
        val validator = MaxSizeListValidator()
        validator.validate(MaxSizeList(items = listOf("a", "b", "c")))
        validator.validate(MaxSizeList(items = emptyList()))
    }

    @Test
    fun `maxsize validator rejects lists above maximum`() = runTest {
        val validator = MaxSizeListValidator()
        assertThrows<ValidationException> { validator.validate(MaxSizeList(items = listOf("a", "b", "c", "d", "e", "f"))) }
    }

    @Test
    fun `maxsize validator allows null`() = runTest {
        MaxSizeListValidator().validate(MaxSizeList(items = null))
    }

    @Test
    fun `maxsize validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            MaxSizeListValidator().validate(MaxSizeList(items = listOf("a", "b", "c", "d", "e", "f", "g")))
        }
        assertTrue(exception.errors.containsKey("items"))
        assertFalse(exception.errors["items"]!!.isEmpty())
    }
}
