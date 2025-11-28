package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SameFieldValidatorTest {
    @Test
    fun `same validator accepts matching values`() =
        runTest {
            val validator = SameFieldValidator()
            validator.validate(SameField(password = "secret123", passwordConfirmation = "secret123"))
        }

    @Test
    fun `same validator rejects different values`() =
        runTest {
            val validator = SameFieldValidator()
            assertThrows<ValidationException> {
                validator.validate(SameField(password = "secret123", passwordConfirmation = "different"))
            }
        }

    @Test
    fun `same validator allows null`() =
        runTest {
            SameFieldValidator().validate(SameField(password = null, passwordConfirmation = null))
        }

    @Test
    fun `same validator provides error message`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    SameFieldValidator().validate(SameField(password = "abc", passwordConfirmation = "xyz"))
                }
            assertTrue(exception.errors.containsKey("passwordConfirmation"))
            assertFalse(exception.errors["passwordConfirmation"]!!.isEmpty())
        }
}
