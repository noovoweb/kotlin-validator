package com.noovoweb.validator.integration

import com.noovoweb.validator.CreditCard
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

@Validated
data class CreditCardTestData(
    @CreditCard
    val cardNumber: String?,
)

class CreditCardValidatorTest {
    @Test
    fun `@CreditCard should pass with valid Visa card`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            validator.validate(CreditCardTestData(cardNumber = "4532015112830366"))
            validator.validate(CreditCardTestData(cardNumber = "4532 0151 1283 0366"))
            validator.validate(CreditCardTestData(cardNumber = "4532-0151-1283-0366"))
        }

    @Test
    fun `@CreditCard should pass with valid MasterCard`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            validator.validate(CreditCardTestData(cardNumber = "5425233430109903"))
        }

    @Test
    fun `@CreditCard should pass with valid American Express`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            validator.validate(CreditCardTestData(cardNumber = "378282246310005"))
        }

    @Test
    fun `@CreditCard should pass with valid Discover`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            validator.validate(CreditCardTestData(cardNumber = "6011111111111117"))
        }

    @Test
    fun `@CreditCard should pass with null value`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            validator.validate(CreditCardTestData(cardNumber = null))
        }

    @Test
    fun `@CreditCard should fail with invalid card number`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardTestData(cardNumber = "1234567890123456"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
        }

    @Test
    fun `@CreditCard should fail with wrong length for card type`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            // Visa should be 13 or 16 digits, not 15
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardTestData(cardNumber = "453201511283036"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
        }

    @Test
    fun `@CreditCard should fail with non-digit characters`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardTestData(cardNumber = "4532abcd1283efgh"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
        }

    @Test
    fun `@CreditCard should fail with too short number`() =
        runTest {
            val validator = CreditCardTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(CreditCardTestData(cardNumber = "123456789012"))
                }
            assertTrue(exception.errors.containsKey("cardNumber"))
        }
}
