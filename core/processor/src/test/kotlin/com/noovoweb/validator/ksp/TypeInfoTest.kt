package com.noovoweb.validator.ksp

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for TypeInfo data class.
 */
class TypeInfoTest {
    @Test
    fun `should create nullable type info`() {
        val typeInfo =
            TypeInfo(
                qualifiedName = "kotlin.String",
                simpleName = "String",
                isNullable = true,
            )

        assertEquals("kotlin.String", typeInfo.qualifiedName)
        assertEquals("String", typeInfo.simpleName)
        assertTrue(typeInfo.isNullable)
    }

    @Test
    fun `should create non-nullable type info`() {
        val typeInfo =
            TypeInfo(
                qualifiedName = "kotlin.Int",
                simpleName = "Int",
                isNullable = false,
            )

        assertEquals("kotlin.Int", typeInfo.qualifiedName)
        assertEquals("Int", typeInfo.simpleName)
        assertFalse(typeInfo.isNullable)
    }

    @Test
    fun `should handle collection types`() {
        val typeInfo =
            TypeInfo(
                qualifiedName = "kotlin.collections.List",
                simpleName = "List",
                isNullable = false,
            )

        assertEquals("kotlin.collections.List", typeInfo.qualifiedName)
        assertEquals("List", typeInfo.simpleName)
    }

    @Test
    fun `should handle custom class types`() {
        val typeInfo =
            TypeInfo(
                qualifiedName = "com.example.User",
                simpleName = "User",
                isNullable = true,
            )

        assertEquals("com.example.User", typeInfo.qualifiedName)
        assertEquals("User", typeInfo.simpleName)
        assertTrue(typeInfo.isNullable)
    }

    @Test
    fun `equality should work for same types`() {
        val type1 = TypeInfo("kotlin.String", "String", true)
        val type2 = TypeInfo("kotlin.String", "String", true)

        assertEquals(type1, type2)
        assertEquals(type1.hashCode(), type2.hashCode())
    }

    @Test
    fun `equality should fail for different nullability`() {
        val type1 = TypeInfo("kotlin.String", "String", true)
        val type2 = TypeInfo("kotlin.String", "String", false)

        assertTrue(type1 != type2)
    }
}
