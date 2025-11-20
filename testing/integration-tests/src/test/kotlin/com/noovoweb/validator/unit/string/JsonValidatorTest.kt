package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonValidatorTest {

    @Test
    fun `json validator accepts valid JSON strings`() = runTest {
        val validator = JsonValidator()

        validator.validate(Json(name = """{}"""))
        validator.validate(Json(name = """[]"""))
        validator.validate(Json(name = """{"key": "value"}"""))
        validator.validate(Json(name = """[1, 2, 3]"""))
        validator.validate(Json(name = """{"name": "John", "age": 30}"""))
        validator.validate(Json(name = """{"nested": {"key": "value"}}"""))
    }

    @Test
    fun `json validator rejects invalid JSON strings`() = runTest {
        val validator = JsonValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(Json(name = "not json"))
        }
        assertTrue(exception1.errors.containsKey("name"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(Json(name = "plain text"))
        }
        assertTrue(exception2.errors.containsKey("name"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(Json(name = "{"))
        }
        assertTrue(exception3.errors.containsKey("name"))
    }

    @Test
    fun `json validator allows null when not required`() = runTest {
        val validator = JsonValidator()
        validator.validate(Json(name = null))
    }

    @Test
    fun `json validator provides error message`() = runTest {
        val validator = JsonValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(Json(name = "invalid json"))
        }

        assertTrue(exception.errors.containsKey("name"))
        assertFalse(exception.errors["name"]!!.isEmpty())
    }
}
