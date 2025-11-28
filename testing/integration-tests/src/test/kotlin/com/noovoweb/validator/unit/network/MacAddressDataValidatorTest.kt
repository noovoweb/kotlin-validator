package com.noovoweb.validator.unit.network

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacAddressDataValidatorTest {
    @Test
    fun `mac validator accepts valid MAC addresses`() =
        runTest {
            val validator = MacAddressDataValidator()

            validator.validate(MacAddressData(address = "00:1B:44:11:3A:B7"))
            validator.validate(MacAddressData(address = "00-1B-44-11-3A-B7"))
            validator.validate(MacAddressData(address = "00:1b:44:11:3a:b7"))
        }

    @Test
    fun `mac validator rejects invalid MAC addresses`() =
        runTest {
            val validator = MacAddressDataValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(MacAddressData(address = "00:1B:44:11:3A"))
                }
            assertTrue(exception1.errors.containsKey("address"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(MacAddressData(address = "not a mac"))
                }
            assertTrue(exception2.errors.containsKey("address"))
        }

    @Test
    fun `mac validator allows null when not required`() =
        runTest {
            val validator = MacAddressDataValidator()
            validator.validate(MacAddressData(address = null))
        }

    @Test
    fun `mac validator provides error message`() =
        runTest {
            val validator = MacAddressDataValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(MacAddressData(address = "invalid"))
                }

            assertTrue(exception.errors.containsKey("address"))
            assertFalse(exception.errors["address"]!!.isEmpty())
        }
}
