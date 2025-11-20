package com.noovoweb.validator.spring

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for ValidatorProperties configuration class.
 */
class ValidatorPropertiesTest {

    @Test
    fun `should have default values`() {
        val properties = ValidatorProperties()

        assertNull(properties.locale)
    }

    @Test
    fun `should allow setting locale`() {
        val properties = ValidatorProperties(locale = Locale.FRENCH)

        assertEquals(Locale.FRENCH, properties.locale)
    }

    @Test
    fun `should support various locales`() {
        val usProperties = ValidatorProperties(locale = Locale.US)
        val frProperties = ValidatorProperties(locale = Locale.FRANCE)
        val deProperties = ValidatorProperties(locale = Locale.GERMANY)

        assertEquals(Locale.US, usProperties.locale)
        assertEquals(Locale.FRANCE, frProperties.locale)
        assertEquals(Locale.GERMANY, deProperties.locale)
    }

    @Test
    fun `should be a data class with copy`() {
        val original = ValidatorProperties(locale = Locale.ENGLISH)
        val copy = original.copy(locale = Locale.FRENCH)

        assertEquals(Locale.ENGLISH, original.locale)
        assertEquals(Locale.FRENCH, copy.locale)
    }
}
