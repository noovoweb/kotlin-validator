package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotEmptyListValidatorTest {
    @Test
    fun `notempty validator accepts non-empty lists`() =
        runTest {
            val validator = NotEmptyListValidator()
            validator.validate(NotEmptyList(items = listOf("a")))
            validator.validate(NotEmptyList(items = listOf("a", "b")))
        }

    @Test
    fun `notempty validator rejects empty lists`() =
        runTest {
            val validator = NotEmptyListValidator()
            assertThrows<ValidationException> { validator.validate(NotEmptyList(items = emptyList())) }
        }

    @Test
    fun `notempty validator allows null`() =
        runTest {
            NotEmptyListValidator().validate(NotEmptyList(items = null))
        }

    @Test
    fun `notempty validator provides error message`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    NotEmptyListValidator().validate(NotEmptyList(items = emptyList()))
                }
            assertTrue(exception.errors.containsKey("items"))
            assertFalse(exception.errors["items"]!!.isEmpty())
        }
}
