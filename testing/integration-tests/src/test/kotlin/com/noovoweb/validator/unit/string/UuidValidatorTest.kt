package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UuidValidatorTest {
    @Test
    fun `uuid validator accepts valid UUID formats`() =
        runTest {
            val validator = UuidValidator()

            validator.validate(Uuid(name = "123e4567-e89b-12d3-a456-426614174000"))
            validator.validate(Uuid(name = "550e8400-e29b-41d4-a716-446655440000"))
            validator.validate(Uuid(name = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"))
            validator.validate(Uuid(name = "00000000-0000-0000-0000-000000000000"))
            validator.validate(Uuid(name = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
        }

    @Test
    fun `uuid validator accepts lowercase and uppercase UUIDs`() =
        runTest {
            val validator = UuidValidator()

            validator.validate(Uuid(name = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
            validator.validate(Uuid(name = "A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"))
        }

    @Test
    fun `uuid validator rejects invalid UUID formats`() =
        runTest {
            val validator = UuidValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "not-a-uuid"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "123e4567-e89b-12d3-a456"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "123e4567e89b12d3a456426614174000"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "123e4567-e89b-12d3-a456-42661417400g"))
                }
            assertTrue(exception4.errors.containsKey("name"))

            val exception5 =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "123e4567-e89b-12d3-a456-4266141740000"))
                }
            assertTrue(exception5.errors.containsKey("name"))
        }

    @Test
    fun `uuid validator allows null when not required`() =
        runTest {
            val validator = UuidValidator()
            validator.validate(Uuid(name = null))
        }

    @Test
    fun `uuid validator provides error message`() =
        runTest {
            val validator = UuidValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Uuid(name = "invalid-uuid"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
