package com.noovoweb.validator.integration

import com.noovoweb.validator.CustomValidator
import com.noovoweb.validator.Email
import com.noovoweb.validator.Required
import com.noovoweb.validator.Same
import com.noovoweb.validator.Validated
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

/**
 * Meta-annotation: Custom annotation for validating Canadian postal codes.
 * This annotation is itself annotated with @CustomValidator.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@CustomValidator(
    validator = "com.noovoweb.validator.integration.PostalCodeValidators::validateCanadianPostalCode",
    message = "postal_code.invalid_canadian",
)
annotation class ValidCanadianPostalCode

/**
 * Meta-annotation: Custom annotation for validating strong passwords.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@CustomValidator(
    validator = "com.noovoweb.validator.integration.PasswordValidators::validateStrongPassword",
    message = "password.not_strong",
)
annotation class StrongPassword

/**
 * Validator functions for postal codes.
 */
object PostalCodeValidators {
    suspend fun validateCanadianPostalCode(
        value: String?,
        @Suppress("UNUSED_PARAMETER") context: ValidationContext,
    ): Boolean {
        if (value == null) return true

        // Canadian postal code format: A1A 1A1 (letter-digit-letter space digit-letter-digit)
        val regex = Regex("^[A-Z]\\d[A-Z] \\d[A-Z]\\d$")
        return value.matches(regex)
    }
}

/**
 * Validator functions for passwords.
 */
object PasswordValidators {
    suspend fun validateStrongPassword(
        value: String?,
        @Suppress("UNUSED_PARAMETER") context: ValidationContext,
    ): Boolean {
        if (value == null) return true

        val hasMinLength = value.length >= 8
        val hasUpperCase = value.any { it.isUpperCase() }
        val hasLowerCase = value.any { it.isLowerCase() }
        val hasDigit = value.any { it.isDigit() }
        val hasSpecial = value.any { !it.isLetterOrDigit() }

        return hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecial
    }
}

/**
 * Test data class using meta-annotations.
 */
@Validated
data class MetaAnnotationTestData(
    @ValidCanadianPostalCode
    val postalCode: String?,
    @StrongPassword
    val password: String?,
)

/**
 * Test data class combining meta-annotations with built-in validators.
 */
@Validated
data class CombinedMetaAnnotationData(
    @Required
    @Email
    val email: String?,
    @Required
    @StrongPassword
    val password: String?,
    @Required
    @Same("password")
    val passwordConfirmation: String?,
    @ValidCanadianPostalCode
    val postalCode: String?,
)

/**
 * Tests for meta-annotation support.
 */
class MetaAnnotationTest {
    @Test
    fun `@ValidCanadianPostalCode should accept valid postal code`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            validator.validate(
                MetaAnnotationTestData(
                    postalCode = "H3Z 2Y7",
                    password = "Valid123!@#",
                ),
            )
        }

    @Test
    fun `@ValidCanadianPostalCode should reject invalid postal code`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "12345",
                            password = "Valid123!@#",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("postalCode"))
        }

    @Test
    fun `@ValidCanadianPostalCode should reject postal code without space`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "H3Z2Y7",
                            password = "Valid123!@#",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("postalCode"))
        }

    @Test
    fun `@ValidCanadianPostalCode should accept null postal code`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            validator.validate(
                MetaAnnotationTestData(
                    postalCode = null,
                    password = "Valid123!@#",
                ),
            )
        }

    @Test
    fun `@StrongPassword should accept valid strong password`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            validator.validate(
                MetaAnnotationTestData(
                    postalCode = "H3Z 2Y7",
                    password = "StrongPass123!@#",
                ),
            )
        }

    @Test
    fun `@StrongPassword should reject weak password without uppercase`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "H3Z 2Y7",
                            password = "weakpass123!@#",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("password"))
        }

    @Test
    fun `@StrongPassword should reject weak password without digit`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "H3Z 2Y7",
                            password = "WeakPass!@#",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("password"))
        }

    @Test
    fun `@StrongPassword should reject weak password without special character`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "H3Z 2Y7",
                            password = "WeakPass123",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("password"))
        }

    @Test
    fun `@StrongPassword should reject short password`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        MetaAnnotationTestData(
                            postalCode = "H3Z 2Y7",
                            password = "Short1!",
                        ),
                    )
                }
            assertTrue(exception.errors.containsKey("password"))
        }

    @Test
    fun `@StrongPassword should accept null password`() =
        runTest {
            val validator = MetaAnnotationTestDataValidator()
            validator.validate(
                MetaAnnotationTestData(
                    postalCode = "H3Z 2Y7",
                    password = null,
                ),
            )
        }

    @Test
    fun `should support combining meta-annotations with built-in validators`() =
        runTest {
            val validator = CombinedMetaAnnotationDataValidator()
            validator.validate(
                CombinedMetaAnnotationData(
                    email = "user@example.com",
                    password = "StrongPass123!@#",
                    passwordConfirmation = "StrongPass123!@#",
                    postalCode = "H3Z 2Y7",
                ),
            )
        }

    @Test
    fun `should validate all fields when combining meta-annotations and built-in validators`() =
        runTest {
            val validator = CombinedMetaAnnotationDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        CombinedMetaAnnotationData(
                            email = "invalid-email",
                            password = "weak",
                            passwordConfirmation = "different",
                            postalCode = "12345",
                        ),
                    )
                }

            // Should have errors for email, password, passwordConfirmation, and postalCode
            assertTrue(exception.errors.containsKey("email"))
            assertTrue(exception.errors.containsKey("password"))
            assertTrue(exception.errors.containsKey("passwordConfirmation"))
            assertTrue(exception.errors.containsKey("postalCode"))
        }

    @Test
    fun `should fail when only meta-annotation validation fails`() =
        runTest {
            val validator = CombinedMetaAnnotationDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(
                        CombinedMetaAnnotationData(
                            email = "valid@example.com",
                            password = "StrongPass123!@#",
                            passwordConfirmation = "StrongPass123!@#",
                            postalCode = "INVALID",
                        ),
                    )
                }

            assertTrue(exception.errors.containsKey("postalCode"))
            assertTrue(!exception.errors.containsKey("email"))
            assertTrue(!exception.errors.containsKey("password"))
        }
}
