package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContainsValidatorTest {
    @Test
    fun `contains validator accepts strings with substring`() =
        runTest {
            val validator = ContainsValidator()

            validator.validate(Contains(name = "substring"))
            validator.validate(Contains(name = "testsubstringtest"))
            validator.validate(Contains(name = "123substring456"))
            validator.validate(Contains(name = "substringonly"))
        }

    @Test
    fun `contains validator rejects strings without substring`() =
        runTest {
            val validator = ContainsValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Contains(name = "test"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Contains(name = "substr"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Contains(name = "SUBSTRING"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(Contains(name = ""))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `contains validator allows null when not required`() =
        runTest {
            val validator = ContainsValidator()
            validator.validate(Contains(name = null))
        }

    @Test
    fun `contains validator provides error message`() =
        runTest {
            val validator = ContainsValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Contains(name = "invalid"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
