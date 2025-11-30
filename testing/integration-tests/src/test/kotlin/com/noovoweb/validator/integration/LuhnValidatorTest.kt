package com.noovoweb.validator.integration

import com.noovoweb.validator.Luhn
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

@Validated
data class CreditCardData(
    @Luhn
    val cardNumber: String?,
)

class LuhnValidatorTest {
    @Test
    fun `@Luhn should pass with valid credit card number`() =
        runTest {
            val validator = CreditCardDataValidator()
            // Valid Visa test number
            validator.validate(CreditCardData(cardNumber = "4532015112830366"))
        }

    @Test
    fun `@Luhn should pass with spaces and hyphens`() =
        runTest {
            val validator = CreditCardDataValidator()
            // Same number with spaces
            validator.validate(CreditCardData(cardNumber = "4532 0151 1283 0366"))
            // Same number with hyphens
            validator.validate(CreditCardData(cardNumber = "4532-0151-1283-0366"))
        }

    @Test
    fun `@Luhn should pass with null value`() =
        runTest {
            val validator = CreditCardDataValidator()
            validator.validate(CreditCardData(cardNumber = null))
        }

    @Test
    fun `@Luhn should fail with invalid credit card number`() =
        runTest {
            val validator = CreditCardDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardData(cardNumber = "1234567890123456"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
            assertTrue(exception.errors["cardNumber"]!!.any { it.contains("Luhn") })
        }

    @Test
    fun `@Luhn should fail with non-digit characters`() =
        runTest {
            val validator = CreditCardDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardData(cardNumber = "4532abcd1283efgh"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
        }

    @Test
    fun `@Luhn should pass with valid MasterCard number`() =
        runTest {
            val validator = CreditCardDataValidator()
            // Valid MasterCard test number
            validator.validate(CreditCardData(cardNumber = "5425233430109903"))
        }
}
