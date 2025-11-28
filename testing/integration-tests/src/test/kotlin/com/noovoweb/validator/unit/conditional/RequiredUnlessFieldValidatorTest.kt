package com.noovoweb.validator.unit.conditional

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredUnlessFieldValidatorTest {
    @Test
    fun `requiredunless validator accepts when exception not met and field present`() =
        runTest {
            val validator = RequiredUnlessFieldValidator()
            validator.validate(RequiredUnlessField(paymentMethod = "card", cardNumber = "1234"))
        }

    @Test
    fun `requiredunless validator accepts when exception met`() =
        runTest {
            val validator = RequiredUnlessFieldValidator()
            validator.validate(RequiredUnlessField(paymentMethod = "cash", cardNumber = null))
        }

    @Test
    fun `requiredunless validator rejects when exception not met but field absent`() =
        runTest {
            val validator = RequiredUnlessFieldValidator()
            assertThrows<ValidationException> {
                validator.validate(RequiredUnlessField(paymentMethod = "card", cardNumber = null))
            }
        }

    @Test
    fun `requiredunless validator provides error message`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    RequiredUnlessFieldValidator().validate(RequiredUnlessField(paymentMethod = "card", cardNumber = null))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
            assertFalse(exception.errors["cardNumber"]!!.isEmpty())
        }
}
