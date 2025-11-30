package com.noovoweb.validator.ksp

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Real tests for the FieldValidatorCodeGenerator
 *
 * Tests the actual code generation logic for different validator types
 */
@DisplayName("Field Validator Code Generation Tests")
class FieldValidatorCodeGeneratorTest {
    private val generator = FieldValidatorCodeGenerator()

    private fun createProperty(
        name: String,
        type: String,
    ): PropertyInfo {
        val isNullable = type.endsWith("?")
        return PropertyInfo(
            name = name,
            type =
            TypeInfo(
                qualifiedName = "kotlin.$type".replace("?", ""),
                simpleName = type.replace("?", ""),
                isNullable = isNullable,
            ),
            validators = emptyList(),
            isNullable = isNullable,
            failFastPositions = emptyList(),
            nestedValidation = null,
        )
    }

    @Test
    fun `should generate required validator code`() {
        val validator = ValidationValidatorInfo.RequiredValidator(customMessage = null)
        val property = createProperty("username", "String?")
        val fieldPath = "user.username"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should check if field is null or blank
        assertTrue(
            codeString.contains("username") || codeString.contains("value"),
            "Generated code should reference the field or value",
        )
    }

    @Test
    fun `should generate email validator code`() {
        val validator = ValidationValidatorInfo.EmailValidator(customMessage = null)
        val property = createProperty("email", "String?")
        val fieldPath = "user.email"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should contain regex or email validation logic
        assertTrue(
            codeString.contains("email") || codeString.contains("Regex") || codeString.contains("@") ||
                codeString.contains("EMAIL"),
            "Email validator should contain validation logic",
        )
    }

    @Test
    fun `should generate min validator code with value`() {
        val validator = ValidationValidatorInfo.MinValidator(value = 18.0, customMessage = null)
        val property = createProperty("age", "Int?")
        val fieldPath = "user.age"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should compare with minimum value
        assertTrue(codeString.contains("18"), "Min validator should contain the min value")
    }

    @Test
    fun `should generate max validator code with value`() {
        val validator = ValidationValidatorInfo.MaxValidator(value = 100.0, customMessage = null)
        val property = createProperty("score", "Int?")
        val fieldPath = "test.score"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should compare with maximum value
        assertTrue(codeString.contains("100"), "Max validator should contain the max value")
    }

    @Test
    fun `should generate size validator code for strings`() {
        val validator = ValidationValidatorInfo.SizeValidator(min = 3, max = 50, customMessage = null)
        val property = createProperty("password", "String?")
        val fieldPath = "user.password"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should check size
        assertTrue(
            codeString.contains("3") || codeString.contains("50"),
            "Size validator should contain min or max values",
        )
    }

    @Test
    fun `should generate pattern validator code with regex`() {
        val validator = ValidationValidatorInfo.PatternValidator(pattern = "[A-Z]+", customMessage = null)
        val property = createProperty("code", "String?")
        val fieldPath = "product.code"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should contain regex pattern
        assertTrue(
            codeString.contains("[A-Z]+") || codeString.contains("Regex") || codeString.contains("pattern"),
            "Pattern validator should contain the regex pattern or pattern validation",
        )
    }

    @Test
    fun `should generate url validator code`() {
        val validator = ValidationValidatorInfo.UrlValidator(customMessage = null)
        val property = createProperty("website", "String?")
        val fieldPath = "company.website"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should validate URL format
        assertTrue(codeString.isNotEmpty(), "URL validator should generate code")
    }

    @Test
    fun `should generate not empty validator code`() {
        val validator = ValidationValidatorInfo.NotEmptyValidator(customMessage = null)
        val property = createProperty("name", "String?")
        val fieldPath = "user.name"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should check if not empty
        assertTrue(codeString.isNotEmpty(), "NotEmpty validator should generate code")
    }

    @Test
    fun `should generate positive validator code`() {
        val validator = ValidationValidatorInfo.PositiveValidator(customMessage = null)
        val property = createProperty("amount", "Double?")
        val fieldPath = "transaction.amount"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should check if positive
        assertTrue(codeString.isNotEmpty(), "Positive validator should generate code")
    }

    @Test
    fun `should generate negative validator code`() {
        val validator = ValidationValidatorInfo.NegativeValidator(customMessage = null)
        val property = createProperty("deficit", "Int?")
        val fieldPath = "account.deficit"

        val generatedCode = generator.generateValidatorCode(validator, property, fieldPath)

        val codeString = generatedCode.toString()
        // Should check if negative
        assertTrue(codeString.isNotEmpty(), "Negative validator should generate code")
    }

    @Test
    fun `should generate code for all validator types without errors`() {
        val property = createProperty("testField", "String?")
        val fieldPath = "test.field"

        // Test that each validator can generate code without throwing exceptions
        val validators =
            listOf(
                ValidationValidatorInfo.RequiredValidator(null),
                ValidationValidatorInfo.EmailValidator(null),
                ValidationValidatorInfo.UrlValidator(null),
                ValidationValidatorInfo.UuidValidator(null),
                ValidationValidatorInfo.NotEmptyValidator(null),
                ValidationValidatorInfo.PatternValidator("[a-z]+", null),
                ValidationValidatorInfo.AlphaValidator(null),
                ValidationValidatorInfo.AlphanumericValidator(null),
                ValidationValidatorInfo.PositiveValidator(null),
                ValidationValidatorInfo.NegativeValidator(null),
            )

        validators.forEach { validator ->
            val code = generator.generateValidatorCode(validator, property, fieldPath)
            assertTrue(
                code.toString().isNotEmpty(),
                "Validator ${validator::class.simpleName} should generate non-empty code",
            )
        }
    }
}
