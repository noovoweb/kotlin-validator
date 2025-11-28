package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SizeListValidatorTest {
    @Test
    fun `size validator accepts lists within range`() =
        runTest {
            val validator = SizeListValidator()
            validator.validate(SizeList(items = listOf("a", "b")))
            validator.validate(SizeList(items = listOf("a", "b", "c")))
            validator.validate(SizeList(items = listOf("a", "b", "c", "d", "e")))
        }

    @Test
    fun `size validator rejects lists outside range`() =
        runTest {
            val validator = SizeListValidator()
            assertThrows<ValidationException> { validator.validate(SizeList(items = listOf("a"))) }
            assertThrows<ValidationException> { validator.validate(SizeList(items = listOf("a", "b", "c", "d", "e", "f"))) }
        }

    @Test
    fun `size validator allows null`() =
        runTest {
            SizeListValidator().validate(SizeList(items = null))
        }

    @Test
    fun `size validator provides error message`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    SizeListValidator().validate(SizeList(items = emptyList()))
                }
            assertTrue(exception.errors.containsKey("items"))
            assertFalse(exception.errors["items"]!!.isEmpty())
        }
}
