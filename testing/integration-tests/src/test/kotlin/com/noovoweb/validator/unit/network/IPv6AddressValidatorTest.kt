package com.noovoweb.validator.unit.network

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IPv6AddressValidatorTest {

    @Test
    fun `ipv6 validator accepts valid IPv6 addresses`() = runTest {
        val validator = IPv6AddressValidator()

        validator.validate(IPv6Address(address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
        validator.validate(IPv6Address(address = "2001:db8:85a3::8a2e:370:7334"))
        validator.validate(IPv6Address(address = "::1"))
        validator.validate(IPv6Address(address = "::"))
    }

    @Test
    fun `ipv6 validator rejects invalid IPv6 addresses`() = runTest {
        val validator = IPv6AddressValidator()

        val exception1 = assertThrows<ValidationException> {
            validator.validate(IPv6Address(address = "192.168.1.1"))
        }
        assertTrue(exception1.errors.containsKey("address"))

        val exception2 = assertThrows<ValidationException> {
            validator.validate(IPv6Address(address = "not an ip"))
        }
        assertTrue(exception2.errors.containsKey("address"))
    }

    @Test
    fun `ipv6 validator allows null when not required`() = runTest {
        val validator = IPv6AddressValidator()
        validator.validate(IPv6Address(address = null))
    }

    @Test
    fun `ipv6 validator provides error message`() = runTest {
        val validator = IPv6AddressValidator()

        val exception = assertThrows<ValidationException> {
            validator.validate(IPv6Address(address = "invalid"))
        }

        assertTrue(exception.errors.containsKey("address"))
        assertFalse(exception.errors["address"]!!.isEmpty())
    }
}
