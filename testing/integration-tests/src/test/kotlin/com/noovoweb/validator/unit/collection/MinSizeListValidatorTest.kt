package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinSizeListValidatorTest {
    @Test
    fun `minsize validator accepts lists at or above minimum`() = runTest {
        val validator = MinSizeListValidator()
        validator.validate(MinSizeList(items = listOf("a", "b", "c")))
        validator.validate(MinSizeList(items = listOf("a", "b", "c", "d")))
    }

    @Test
    fun `minsize validator rejects lists below minimum`() = runTest {
        val validator = MinSizeListValidator()
        assertThrows<ValidationException> { validator.validate(MinSizeList(items = listOf("a", "b"))) }
    }

    @Test
    fun `minsize validator allows null`() = runTest {
        MinSizeListValidator().validate(MinSizeList(items = null))
    }

    @Test
    fun `minsize validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            MinSizeListValidator().validate(MinSizeList(items = listOf("a")))
        }
        assertTrue(exception.errors.containsKey("items"))
        assertFalse(exception.errors["items"]!!.isEmpty())
    }
}
