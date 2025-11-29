package com.noovoweb.validator.integration

import com.noovoweb.validator.Enum
import com.noovoweb.validator.Required
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import com.noovoweb.validator.Validated
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale
import kotlin.test.assertFalse
import kotlin.test.assertTrue

enum class Status {
    ACTIVE,
    INACTIVE,
    PENDING
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

@Validated
data class EnumTestData(
    @Enum(Status::class)
    val status: String?,
)

@Validated
data class RequiredEnumTestData(
    @Required
    @Enum(Status::class)
    val status: String,
)

@Validated
data class MultipleEnumTestData(
    @Enum(Status::class)
    val status: String?,

    @Enum(Priority::class)
    val priority: String?,
)

class EnumValidatorTest {
    @Test
    fun `@Enum should pass with valid enum value`() =
        runTest {
            val validator = EnumTestDataValidator()
            validator.validate(EnumTestData(status = "ACTIVE"))
            validator.validate(EnumTestData(status = "INACTIVE"))
            validator.validate(EnumTestData(status = "PENDING"))
        }

    @Test
    fun `@Enum should pass with null value`() =
        runTest {
            val validator = EnumTestDataValidator()
            validator.validate(EnumTestData(status = null))
        }

    @Test
    fun `@Enum should fail with invalid enum value`() =
        runTest {
            val validator = EnumTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EnumTestData(status = "INVALID"))
                }
            assertTrue(exception.errors.containsKey("status"))
        }

    @Test
    fun `@Enum should fail with lowercase enum value`() =
        runTest {
            val validator = EnumTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EnumTestData(status = "active"))
                }
            assertTrue(exception.errors.containsKey("status"))
        }

    @Test
    fun `@Enum should fail with empty string`() =
        runTest {
            val validator = EnumTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EnumTestData(status = ""))
                }
            assertTrue(exception.errors.containsKey("status"))
        }

    @Test
    fun `@Required @Enum should validate both required and enum`() =
        runTest {
            val validator = RequiredEnumTestDataValidator()
            // Valid value should pass
            validator.validate(RequiredEnumTestData(status = "ACTIVE"))
        }

    @Test
    fun `@Required @Enum should fail with empty string for required`() =
        runTest {
            val validator = RequiredEnumTestDataValidator()
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(RequiredEnumTestData(status = ""))
                }
            assertTrue(exception.errors.containsKey("status"))
        }

    @Test
    fun `@Enum should work with multiple different enums`() =
        runTest {
            val validator = MultipleEnumTestDataValidator()
            validator.validate(MultipleEnumTestData(status = "ACTIVE", priority = "HIGH"))
            validator.validate(MultipleEnumTestData(status = "PENDING", priority = "LOW"))
        }

    @Test
    fun `@Enum should fail when wrong enum value used for field`() =
        runTest {
            val validator = MultipleEnumTestDataValidator()
            // Using Priority value for Status field
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(MultipleEnumTestData(status = "HIGH", priority = "ACTIVE"))
                }
            assertTrue(exception.errors.containsKey("status") || exception.errors.containsKey("priority"))
        }

    @Test
    fun `@Enum error message should include allowed values with French locale`() =
        runTest {
            val validator = EnumTestDataValidator()
            val context = ValidationContext(locale = Locale.FRENCH)
            
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EnumTestData(status = "INVALID"), context)
                }
            
            val errorMessage = exception.errors["status"]?.firstOrNull() ?: ""
            
            // Verify the message contains actual enum values, not {0} placeholder
            assertFalse(
                errorMessage.contains("{0}"),
                "Error message should not contain unresolved placeholder {0}: $errorMessage"
            )
            assertTrue(
                errorMessage.contains("ACTIVE") || errorMessage.contains("INACTIVE") || errorMessage.contains("PENDING"),
                "Error message should contain allowed enum values: $errorMessage"
            )
        }

    @Test
    fun `@Enum error message should include allowed values with English locale`() =
        runTest {
            val validator = EnumTestDataValidator()
            val context = ValidationContext(locale = Locale.ENGLISH)
            
            val exception =
                assertThrows<ValidationException> {
                    validator.validate(EnumTestData(status = "INVALID"), context)
                }
            
            val errorMessage = exception.errors["status"]?.firstOrNull() ?: ""
            
            // Verify the message contains actual enum values, not {0} placeholder
            assertFalse(
                errorMessage.contains("{0}"),
                "Error message should not contain unresolved placeholder {0}: $errorMessage"
            )
            assertTrue(
                errorMessage.contains("ACTIVE") || errorMessage.contains("INACTIVE") || errorMessage.contains("PENDING"),
                "Error message should contain allowed enum values: $errorMessage"
            )
        }
}
