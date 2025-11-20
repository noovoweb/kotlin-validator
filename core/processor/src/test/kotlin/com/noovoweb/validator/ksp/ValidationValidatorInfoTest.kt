package com.noovoweb.validator.ksp

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for ValidationValidatorInfo sealed class and its subtypes.
 */
class ValidationValidatorInfoTest {

    @Test
    fun `RequiredValidator should store custom message`() {
        val validator = ValidationValidatorInfo.RequiredValidator(customMessage = "Custom required message")
        assertEquals("Custom required message", validator.customMessage)
    }

    @Test
    fun `RequiredValidator should handle null message`() {
        val validator = ValidationValidatorInfo.RequiredValidator(customMessage = null)
        assertEquals(null, validator.customMessage)
    }

    @Test
    fun `EmailValidator should store custom message`() {
        val validator = ValidationValidatorInfo.EmailValidator(customMessage = "Invalid email")
        assertEquals("Invalid email", validator.customMessage)
    }

    @Test
    fun `MinValidator should store value and message`() {
        val validator = ValidationValidatorInfo.MinValidator(value = 18.0, customMessage = "Too young")
        assertEquals(18.0, validator.value)
        assertEquals("Too young", validator.customMessage)
    }

    @Test
    fun `MaxValidator should store value and message`() {
        val validator = ValidationValidatorInfo.MaxValidator(value = 100.0, customMessage = "Too high")
        assertEquals(100.0, validator.value)
        assertEquals("Too high", validator.customMessage)
    }

    @Test
    fun `SizeValidator should store min, max, and message`() {
        val validator = ValidationValidatorInfo.SizeValidator(min = 1, max = 10, customMessage = "Invalid size")
        assertEquals(1, validator.min)
        assertEquals(10, validator.max)
        assertEquals("Invalid size", validator.customMessage)
    }

    @Test
    fun `MinLengthValidator should store value`() {
        val validator = ValidationValidatorInfo.MinLengthValidator(value = 3, customMessage = null)
        assertEquals(3, validator.value)
    }

    @Test
    fun `MaxLengthValidator should store value`() {
        val validator = ValidationValidatorInfo.MaxLengthValidator(value = 50, customMessage = null)
        assertEquals(50, validator.value)
    }

    @Test
    fun `AlphaValidator should be created without parameters`() {
        val validator = ValidationValidatorInfo.AlphaValidator(customMessage = null)
        assertNotNull(validator)
    }

    @Test
    fun `AlphanumericValidator should be created without parameters`() {
        val validator = ValidationValidatorInfo.AlphanumericValidator(customMessage = null)
        assertNotNull(validator)
    }

    @Test
    fun `SameValidator should store field name`() {
        val validator = ValidationValidatorInfo.SameValidator(field = "password", customMessage = null)
        assertEquals("password", validator.field)
    }

    @Test
    fun `DifferentValidator should store field name`() {
        val validator = ValidationValidatorInfo.DifferentValidator(field = "email", customMessage = null)
        assertEquals("email", validator.field)
    }

    @Test
    fun `PatternValidator should store pattern`() {
        val validator = ValidationValidatorInfo.PatternValidator(pattern = "[A-Z]+", customMessage = null)
        assertEquals("[A-Z]+", validator.pattern)
    }
}
