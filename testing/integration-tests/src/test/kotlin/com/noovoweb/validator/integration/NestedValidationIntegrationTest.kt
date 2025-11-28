package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Valid
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationContext
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

object NestedValidators {
    suspend fun validateOrderItem(
        value: String?,
        context: ValidationContext,
    ): Boolean {
        if (value == null) return true
        return value.length >= 3
    }
}

@Validated
data class OrderItem(
    @CustomValidator(validator = "com.noovoweb.validator.integration.NestedValidators::validateOrderItem")
    val name: String?,
)

@Validated
data class Order(
    @Valid(each = true)
    val items: List<OrderItem>?,
)

class NestedValidationIntegrationTest {
    @Test
    fun `should validate nested objects in lists`() =
        runTest {
            val validator = OrderValidator()
            val order =
                Order(
                    items =
                        listOf(
                            OrderItem(name = "Item1"),
                            OrderItem(name = "Item2"),
                        ),
                )
            validator.validate(order)
        }

    @Test
    fun `should handle empty nested list`() =
        runTest {
            val validator = OrderValidator()
            val order = Order(items = emptyList())
            validator.validate(order)
        }

    @Test
    fun `should handle null nested list`() =
        runTest {
            val validator = OrderValidator()
            val order = Order(items = null)
            validator.validate(order)
        }

    @Test
    fun `should validate each item in nested list`() =
        runTest {
            val validator = OrderValidator()
            val order =
                Order(
                    items =
                        listOf(
                            OrderItem(name = "ValidItem"),
                            OrderItem(name = "AnotherValidItem"),
                        ),
                )
            validator.validate(order)
        }
}
