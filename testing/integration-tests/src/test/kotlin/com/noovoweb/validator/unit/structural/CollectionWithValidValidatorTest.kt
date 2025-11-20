package com.noovoweb.validator.unit.structural

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionWithValidValidatorTest {
    @Test
    fun `valid each validator accepts valid collection items`() = runTest {
        val validator = CollectionWithValidValidator()
        validator.validate(
            CollectionWithValid(
                name = "Test",
                items = listOf(Item("value1"), Item("value2"))
            )
        )
    }

    @Test
    fun `valid each validator rejects invalid collection items`() = runTest {
        val validator = CollectionWithValidValidator()
        assertThrows<ValidationException> {
            validator.validate(
                CollectionWithValid(
                    name = "Test",
                    items = listOf(Item("value1"), Item(null))
                )
            )
        }
    }

    @Test
    fun `valid each validator allows null collection`() = runTest {
        val validator = CollectionWithValidValidator()
        validator.validate(CollectionWithValid(name = "Test", items = null))
    }

    @Test
    fun `valid each validator provides error path with index`() = runTest {
        val exception = assertThrows<ValidationException> {
            CollectionWithValidValidator().validate(
                CollectionWithValid(
                    name = "Test",
                    items = listOf(Item("value1"), Item(null), Item("value3"))
                )
            )
        }
        assertTrue(exception.errors.containsKey("items[1].value"))
        assertFalse(exception.errors["items[1].value"]!!.isEmpty())
    }

    @Test
    fun `valid each validator validates all invalid items`() = runTest {
        val exception = assertThrows<ValidationException> {
            CollectionWithValidValidator().validate(
                CollectionWithValid(
                    name = "Test",
                    items = listOf(Item(null), Item(null))
                )
            )
        }
        assertTrue(exception.errors.containsKey("items[0].value"))
        assertTrue(exception.errors.containsKey("items[1].value"))
    }
}
