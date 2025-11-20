package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DistinctListValidatorTest {
    @Test
    fun `distinct validator accepts lists with unique elements`() = runTest {
        val validator = DistinctListValidator()
        validator.validate(DistinctList(items = listOf("a", "b", "c")))
    }

    @Test
    fun `distinct validator rejects lists with duplicates`() = runTest {
        val validator = DistinctListValidator()
        assertThrows<ValidationException> { validator.validate(DistinctList(items = listOf("a", "b", "a"))) }
    }

    @Test
    fun `distinct validator allows null`() = runTest {
        DistinctListValidator().validate(DistinctList(items = null))
    }

    @Test
    fun `distinct validator provides error message`() = runTest {
        val exception = assertThrows<ValidationException> {
            DistinctListValidator().validate(DistinctList(items = listOf("a", "a")))
        }
        assertTrue(exception.errors.containsKey("items"))
        assertFalse(exception.errors["items"]!!.isEmpty())
    }
}
