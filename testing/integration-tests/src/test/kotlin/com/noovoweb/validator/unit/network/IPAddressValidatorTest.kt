package com.noovoweb.validator.unit.network

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IPAddressValidatorTest {

    @Test
    fun `ip validator accepts valid IPv4 addresses`() = runTest {
        val validator = IPAddressValidator()

        validator.validate(IPAddress(address = "192.168.1.1"))
        validator.validate(IPAddress(address = "10.0.0.1"))
    }

    @Test
    fun `ip validator accepts valid IPv6 addresses`() = runTest {
        val validator = IPAddressValidator()

        validator.validate(IPAddress(address = "2001:0db8:85a3::8a2e:0370:7334"))
        validator.validate(IPAddress(address = "::1"))
    }

    @Test
    fun `ip validator rejects invalid IP addresses`() = runTest {
        val validator = IPAddressValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(IPAddress(address = "256.1.1.1"))
        }
        assertTrue(exception1.errors.containsKey("address"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(IPAddress(address = "not an ip"))
        }
        assertTrue(exception2.errors.containsKey("address"))
    }

    @Test
    fun `ip validator allows null when not required`() = runTest {
        val validator = IPAddressValidator()
        validator.validate(IPAddress(address = null))
    }

    @Test
    fun `ip validator provides error message`() = runTest {
        val validator = IPAddressValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(IPAddress(address = "invalid"))
        }

        assertTrue(exception.errors.containsKey("address"))
        assertFalse(exception.errors["address"]!!.isEmpty())
    }
}
