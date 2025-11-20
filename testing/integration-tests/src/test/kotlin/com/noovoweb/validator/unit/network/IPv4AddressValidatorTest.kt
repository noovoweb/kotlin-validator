package com.noovoweb.validator.unit.network

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IPv4AddressValidatorTest {

    @Test
    fun `ipv4 validator accepts valid IPv4 addresses`() = runTest {
        val validator = IPv4AddressValidator()

        validator.validate(IPv4Address(address = "192.168.1.1"))
        validator.validate(IPv4Address(address = "10.0.0.1"))
        validator.validate(IPv4Address(address = "255.255.255.255"))
        validator.validate(IPv4Address(address = "0.0.0.0"))
    }

    @Test
    fun `ipv4 validator rejects invalid IPv4 addresses`() = runTest {
        val validator = IPv4AddressValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(IPv4Address(address = "256.1.1.1"))
        }
        assertTrue(exception1.errors.containsKey("address"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(IPv4Address(address = "192.168.1"))
        }
        assertTrue(exception2.errors.containsKey("address"))

        val exception3 = assertThrows<ValidationException> {
            validator.validate(IPv4Address(address = "not an ip"))
        }
        assertTrue(exception3.errors.containsKey("address"))
    }

    @Test
    fun `ipv4 validator allows null when not required`() = runTest {
        val validator = IPv4AddressValidator()
        validator.validate(IPv4Address(address = null))
    }

    @Test
    fun `ipv4 validator provides error message`() = runTest {
        val validator = IPv4AddressValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(IPv4Address(address = "invalid"))
        }

        assertTrue(exception.errors.containsKey("address"))
        assertFalse(exception.errors["address"]!!.isEmpty())
    }
}
