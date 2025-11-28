package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NestedObjectValidatorTest {
    @Test
    fun `valid validator accepts valid nested object`() =
        runTest {
            val validator = NestedObjectValidator()
            validator.validate(
                NestedObject(
                    name = "John",
                    address = Address(street = "123 Main St", city = "New York"),
                ),
            )
        }

    @Test
    fun `valid validator rejects invalid nested object`() =
        runTest {
            val validator = NestedObjectValidator()
            assertThrows<ValidationException> {
                validator.validate(
                    NestedObject(
                        name = "John",
                        address = Address(street = null, city = "New York"),
                    ),
                )
            }
        }

    @Test
    fun `valid validator rejects invalid parent field`() =
        runTest {
            val validator = NestedObjectValidator()
            assertThrows<ValidationException> {
                validator.validate(
                    NestedObject(
                        name = null,
                        address = Address(street = "123 Main St", city = "New York"),
                    ),
                )
            }
        }

    @Test
    fun `valid validator allows null nested object`() =
        runTest {
            val validator = NestedObjectValidator()
            validator.validate(NestedObject(name = "John", address = null))
        }

    @Test
    fun `valid validator provides nested error path`() =
        runTest {
            val exception =
                assertThrows<ValidationException> {
                    NestedObjectValidator().validate(
                        NestedObject(
                            name = "John",
                            address = Address(street = null, city = null),
                        ),
                    )
                }
            println("Error keys: ${exception.errors.keys}")
            assertTrue(exception.errors.isNotEmpty())
            assertFalse(exception.errors.values.flatten().isEmpty())
        }
}
