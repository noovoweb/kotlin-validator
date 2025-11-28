package com.noovoweb.validator.ksp

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ValidatedClassInfo and PropertyInfo data classes.
 */
class ValidatedClassInfoTest {

    @Test
    fun `should create ValidatedClassInfo with properties`() {
        val property = PropertyInfo(
            name = "email",
            type = TypeInfo("kotlin.String", "String", true),
            validators = listOf(
                ValidationValidatorInfo.RequiredValidator(null),
                ValidationValidatorInfo.EmailValidator(null)
            ),
            isNullable = true,
            failFastPositions = emptyList(),
            nestedValidation = null
        )

        val classInfo = ValidatedClassInfo(
            packageName = "com.example",
            className = "User",
            properties = listOf(property)
        )

        assertEquals("com.example", classInfo.packageName)
        assertEquals("User", classInfo.className)
        assertEquals(1, classInfo.properties.size)
        assertEquals("email", classInfo.properties[0].name)
    }

    @Test
    fun `should create PropertyInfo with validators`() {
        val property = PropertyInfo(
            name = "age",
            type = TypeInfo("kotlin.Int", "Int", true),
            validators = listOf(
                ValidationValidatorInfo.RequiredValidator(null),
                ValidationValidatorInfo.MinValidator(18.0, null)
            ),
            isNullable = true,
            failFastPositions = emptyList(),
            nestedValidation = null
        )

        assertEquals("age", property.name)
        assertEquals("Int", property.type.simpleName)
        assertEquals(2, property.validators.size)
        assertTrue(property.isNullable)
        assertTrue(property.failFastPositions.isEmpty())
    }

    @Test
    fun `should handle property with @FailFast annotation`() {
        val property = PropertyInfo(
            name = "email",
            type = TypeInfo("kotlin.String", "String", true),
            validators = listOf(
                ValidationValidatorInfo.RequiredValidator(null),
                ValidationValidatorInfo.EmailValidator(null)
            ),
            isNullable = true,
            failFastPositions = listOf(2),
            nestedValidation = null
        )

        assertTrue(property.failFastPositions.isNotEmpty())
    }

    @Test
    fun `should handle nested validation`() {
        val nestedValidation = NestedValidationInfo(
            validateEachElement = true
        )

        val property = PropertyInfo(
            name = "addresses",
            type = TypeInfo("kotlin.collections.List", "List", false),
            validators = emptyList(),
            isNullable = false,
            failFastPositions = emptyList(),
            nestedValidation = nestedValidation
        )

        assertEquals(true, property.nestedValidation?.validateEachElement)
    }

    @Test
    fun `ValidatedClassInfo should support multiple properties`() {
        val properties = listOf(
            PropertyInfo(
                name = "username",
                type = TypeInfo("kotlin.String", "String", true),
                validators = listOf(ValidationValidatorInfo.RequiredValidator(null)),
                isNullable = true,
                failFastPositions = emptyList(),
                nestedValidation = null
            ),
            PropertyInfo(
                name = "email",
                type = TypeInfo("kotlin.String", "String", true),
                validators = listOf(ValidationValidatorInfo.EmailValidator(null)),
                isNullable = true,
                failFastPositions = emptyList(),
                nestedValidation = null
            ),
            PropertyInfo(
                name = "age",
                type = TypeInfo("kotlin.Int", "Int", true),
                validators = listOf(ValidationValidatorInfo.MinValidator(18.0, null)),
                isNullable = true,
                failFastPositions = emptyList(),
                nestedValidation = null
            )
        )

        val classInfo = ValidatedClassInfo(
            packageName = "com.example",
            className = "UserProfile",
            properties = properties
        )

        assertEquals(3, classInfo.properties.size)
        assertEquals("username", classInfo.properties[0].name)
        assertEquals("email", classInfo.properties[1].name)
        assertEquals("age", classInfo.properties[2].name)
    }
}
