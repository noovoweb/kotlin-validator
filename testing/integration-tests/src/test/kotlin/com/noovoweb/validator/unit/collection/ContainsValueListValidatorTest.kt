package com.noovoweb.validator.unit.collection

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContainsValueListValidatorTest {
    @Test
    fun `containsvalue validator accepts lists with required value`() =
        runTest {
            val validator = ContainsValueListValidator()
            validator.validate(ContainsValueList(items = listOf("required", "other")))
        }

    @Test
    fun `containsvalue validator rejects lists without required value`() =
        runTest {
            val validator = ContainsValueListValidator()
            assertThrows<ValidationException> { validator.validate(ContainsValueList(items = listOf("a", "b"))) }
        }

    @Test
    fun `containsvalue validator allows null`() =
        runTest {
            ContainsValueListValidator().validate(ContainsValueList(items = null))
        }

    @Test
    fun `containsvalue validator provides error message`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    ContainsValueListValidator().validate(ContainsValueList(items = listOf("a")))
                }
            assertTrue(exception.errors.containsKey("items"))
            assertFalse(exception.errors["items"]!!.isEmpty())
        }
}
