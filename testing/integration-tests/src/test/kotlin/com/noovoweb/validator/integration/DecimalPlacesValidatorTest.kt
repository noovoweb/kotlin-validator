package com.noovoweb.validator.integration

import com.noovoweb.validator.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

@Validated
data class PriceData(
    @DecimalPlaces(2)
    val priceString: String?
)

class DecimalPlacesValidatorTest {

    @Test
    fun `@DecimalPlaces should pass with exact decimal places (String)`() = runTest {
        val validator = PriceDataValidator()
        validator.validate(PriceData(priceString = "99.99"))
    }

    @Test
    fun `@DecimalPlaces should pass with null value`() = runTest {
        val validator = PriceDataValidator()
        validator.validate(PriceData(priceString = null))
    }

    @Test
    fun `@DecimalPlaces should fail with no decimal places (String)`() = runTest {
        val validator = PriceDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PriceData(priceString = "99"))
        }
        assertTrue(exception.errors.containsKey("priceString"))
        assertTrue(exception.errors["priceString"]!!.any { it.contains("2 decimal places") })
    }

    @Test
    fun `@DecimalPlaces should fail with too few decimal places (String)`() = runTest {
        val validator = PriceDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PriceData(priceString = "99.9"))
        }
        assertTrue(exception.errors.containsKey("priceString"))
    }

    @Test
    fun `@DecimalPlaces should fail with wrong number of decimal places (String)`() = runTest {
        val validator = PriceDataValidator()
        val exception = assertThrows<ValidationException> {
            validator.validate(PriceData(priceString = "99.999"))
        }
        assertTrue(exception.errors.containsKey("priceString"))
    }

    @Test
    fun `@DecimalPlaces should pass with valid prices`() = runTest {
        val validator = PriceDataValidator()
        validator.validate(PriceData(priceString = "0.00"))
        validator.validate(PriceData(priceString = "1234.56"))
        validator.validate(PriceData(priceString = "10.50"))
    }
}
