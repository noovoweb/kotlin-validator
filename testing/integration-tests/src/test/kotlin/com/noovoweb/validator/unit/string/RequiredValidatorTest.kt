package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredValidatorTest {
    @Test
    fun `required validator accepts non-null non-blank strings`() =
        runTest {
            val validator = RequiredValidator()

            validator.validate(Required(name = "value"))
            validator.validate(Required(name = "a"))
            validator.validate(Required(name = "multiple words"))
        }

    @Test
    fun `required validator rejects null values`() =
        runTest {
            val validator = RequiredValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Required(name = null))
                }

            assertTrue(exception.errors.containsKey("name"))
        }

    @Test
    fun `required validator rejects blank strings`() =
        runTest {
            val validator = RequiredValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Required(name = ""))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Required(name = "   "))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Required(name = "\t\n"))
                }
            assertTrue(exception3.errors.containsKey("name"))
        }

    @Test
    fun `required validator provides error message`() =
        runTest {
            val validator = RequiredValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Required(name = null))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
