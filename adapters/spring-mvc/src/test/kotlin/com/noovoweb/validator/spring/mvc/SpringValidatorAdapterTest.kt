package com.noovoweb.validator.spring.mvc

import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for SpringValidatorAdapter.
 */
class SpringValidatorAdapterTest {
    // Test data class
    data class TestUser(
        val username: String?,
        val email: String?,
    )

    // Mock validator that always passes
    class PassingValidator : GeneratedValidator<TestUser> {
        override suspend fun validate(
            target: TestUser,
            context: ValidationContext,
        ) {
            // Always passes
        }

        override suspend fun validateResult(
            payload: TestUser,
            context: ValidationContext,
        ): com.noovoweb.validator.ValidationResult<TestUser> {
            return com.noovoweb.validator.ValidationResult.Success(payload)
        }
    }

    // Mock validator that always fails
    class FailingValidator : GeneratedValidator<TestUser> {
        override suspend fun validate(
            target: TestUser,
            context: ValidationContext,
        ) {
            throw ValidationException(
                mapOf(
                    "username" to listOf("Username is invalid"),
                    "email" to listOf("Email is invalid"),
                ),
            )
        }

        override suspend fun validateResult(
            payload: TestUser,
            context: ValidationContext,
        ): com.noovoweb.validator.ValidationResult<TestUser> {
            return com.noovoweb.validator.ValidationResult.Failure(
                mapOf(
                    "username" to listOf(com.noovoweb.validator.ValidationError("Username is invalid")),
                    "email" to listOf(com.noovoweb.validator.ValidationError("Email is invalid")),
                ),
            )
        }
    }

    @Test
    fun `should support the target class`() {
        val adapter =
            SpringValidatorAdapter(
                validator = PassingValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        assertTrue(adapter.supports(TestUser::class.java))
    }

    @Test
    fun `should not support other classes`() {
        val adapter =
            SpringValidatorAdapter(
                validator = PassingValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        assertFalse(adapter.supports(String::class.java))
        assertFalse(adapter.supports(Int::class.java))
    }

    @Test
    fun `should pass validation when validator succeeds`() {
        val adapter =
            SpringValidatorAdapter(
                validator = PassingValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("john", "john@example.com")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertFalse(errors.hasErrors())
    }

    @Test
    fun `should populate errors when validator fails`() {
        val adapter =
            SpringValidatorAdapter(
                validator = FailingValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("", "invalid")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertTrue(errors.hasErrors())
        assertEquals(2, errors.errorCount)
    }

    @Test
    fun `should convert ValidationException errors to Spring errors`() {
        val adapter =
            SpringValidatorAdapter(
                validator = FailingValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("", "invalid")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertTrue(errors.hasFieldErrors("username"))
        assertTrue(errors.hasFieldErrors("email"))

        val usernameError = errors.getFieldError("username")
        assertEquals("Username is invalid", usernameError?.defaultMessage)

        val emailError = errors.getFieldError("email")
        assertEquals("Email is invalid", emailError?.defaultMessage)
    }

    @Test
    fun `should handle multiple errors per field`() {
        class MultiErrorValidator : GeneratedValidator<TestUser> {
            override suspend fun validate(
                target: TestUser,
                context: ValidationContext,
            ) {
                throw ValidationException(
                    mapOf(
                        "username" to listOf("Too short", "No uppercase", "No special chars"),
                    ),
                )
            }

            override suspend fun validateResult(
                payload: TestUser,
                context: ValidationContext,
            ): com.noovoweb.validator.ValidationResult<TestUser> {
                return com.noovoweb.validator.ValidationResult.Failure(
                    mapOf(
                        "username" to
                            listOf(
                                com.noovoweb.validator.ValidationError("Too short"),
                                com.noovoweb.validator.ValidationError("No uppercase"),
                                com.noovoweb.validator.ValidationError("No special chars"),
                            ),
                    ),
                )
            }
        }

        val adapter =
            SpringValidatorAdapter(
                validator = MultiErrorValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("user", "email@test.com")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertTrue(errors.hasFieldErrors("username"))
        assertEquals(3, errors.getFieldErrorCount("username"))
    }

    @Test
    fun `should use provided ValidationContext`() {
        val customContext = ValidationContext()
        val adapter =
            SpringValidatorAdapter(
                validator = PassingValidator(),
                context = customContext,
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("john", "john@example.com")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertFalse(errors.hasErrors())
    }

    @Test
    fun `should handle empty ValidationException errors`() {
        class EmptyErrorValidator : GeneratedValidator<TestUser> {
            override suspend fun validate(
                target: TestUser,
                context: ValidationContext,
            ) {
                throw ValidationException(emptyMap())
            }

            override suspend fun validateResult(
                payload: TestUser,
                context: ValidationContext,
            ): com.noovoweb.validator.ValidationResult<TestUser> {
                return com.noovoweb.validator.ValidationResult.Failure(emptyMap())
            }
        }

        val adapter =
            SpringValidatorAdapter(
                validator = EmptyErrorValidator(),
                context = ValidationContext(),
                targetClass = TestUser::class.java,
            )

        val testUser = TestUser("user", "email@test.com")
        val errors: Errors = BeanPropertyBindingResult(testUser, "testUser")

        adapter.validate(testUser, errors)

        assertFalse(errors.hasErrors())
    }
}
