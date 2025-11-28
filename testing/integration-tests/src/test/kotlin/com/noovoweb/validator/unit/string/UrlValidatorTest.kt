package com.noovoweb.validator.unit.string

import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlValidatorTest {
    @Test
    fun `url validator accepts valid URLs`() =
        runTest {
            val validator = UrlValidator()

            validator.validate(Url(name = "https://www.example.com"))
            validator.validate(Url(name = "http://example.com"))
            validator.validate(Url(name = "https://example.com/path/to/page"))
            validator.validate(Url(name = "https://subdomain.example.com:8080"))
            validator.validate(Url(name = "https://example.com?param=value&other=123"))
            validator.validate(Url(name = "https://example.com#anchor"))
        }

    @Test
    fun `url validator rejects invalid URLs`() =
        runTest {
            val validator = UrlValidator()

            val exception1 =
                assertThrows<ValidationException> {
                    validator.validate(Url(name = "not-a-url"))
                }
            assertTrue(exception1.errors.containsKey("name"))

            val exception2 =
                assertThrows<ValidationException> {
                    validator.validate(Url(name = "ftp://example.com"))
                }
            assertTrue(exception2.errors.containsKey("name"))

            val exception3 =
                assertThrows<ValidationException> {
                    validator.validate(Url(name = "example.com"))
                }
            assertTrue(exception3.errors.containsKey("name"))

            val exception4 =
                assertThrows<ValidationException> {
                    validator.validate(Url(name = "http://"))
                }
            assertTrue(exception4.errors.containsKey("name"))
        }

    @Test
    fun `url validator allows null when not required`() =
        runTest {
            val validator = UrlValidator()
            validator.validate(Url(name = null))
        }

    @Test
    fun `url validator provides error message`() =
        runTest {
            val validator = UrlValidator()

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(Url(name = "invalid"))
                }

            assertTrue(exception.errors.containsKey("name"))
            assertFalse(exception.errors["name"]!!.isEmpty())
        }
}
