package com.noovoweb.validator.unit.network

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortNumberValidatorTest {
    @Test
    fun `port validator accepts valid port numbers`() =
        runTest {
            val validator = PortNumberValidator()

            validator.validate(PortNumber(port = 1))
            validator.validate(PortNumber(port = 80))
            validator.validate(PortNumber(port = 443))
            validator.validate(PortNumber(port = 8080))
            validator.validate(PortNumber(port = 65535))
        }

    @Test
    fun `port validator rejects invalid port numbers`() =
        runTest {
            val validator = PortNumberValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(PortNumber(port = 0))
                }
            assertTrue(exception1.errors.containsKey("port"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(PortNumber(port = -1))
                }
            assertTrue(exception2.errors.containsKey("port"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(PortNumber(port = 65536))
                }
            assertTrue(exception3.errors.containsKey("port"))
        }

    @Test
    fun `port validator allows null when not required`() =
        runTest {
            val validator = PortNumberValidator()
            validator.validate(PortNumber(port = null))
        }

    @Test
    fun `port validator provides error message`() =
        runTest {
            val validator = PortNumberValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(PortNumber(port = 70000))
                }

            assertTrue(exception.errors.containsKey("port"))
            assertFalse(exception.errors["port"]!!.isEmpty())
        }
}
