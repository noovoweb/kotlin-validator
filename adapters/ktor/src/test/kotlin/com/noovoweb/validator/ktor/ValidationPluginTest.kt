package com.noovoweb.validator.ktor

import com.noovoweb.validator.DefaultMessageProvider
import com.noovoweb.validator.GeneratedValidator
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ValidationPluginTest {

    @Serializable
    data class TestUser(
        val username: String,
        val email: String
    )

    class PassingValidator : GeneratedValidator<TestUser> {
        override suspend fun validate(target: TestUser, context: ValidationContext) {
            // Always passes
        }

        override suspend fun validateResult(
            payload: TestUser,
            context: ValidationContext
        ): com.noovoweb.validator.ValidationResult<TestUser> {
            return com.noovoweb.validator.ValidationResult.Success(payload)
        }
    }

    class FailingValidator : GeneratedValidator<TestUser> {
        override suspend fun validate(target: TestUser, context: ValidationContext) {
            throw ValidationException(
                mapOf(
                    "username" to listOf("Username is required"),
                    "email" to listOf("Email must be valid")
                )
            )
        }

        override suspend fun validateResult(
            payload: TestUser,
            context: ValidationContext
        ): com.noovoweb.validator.ValidationResult<TestUser> {
            return com.noovoweb.validator.ValidationResult.Failure(
                mapOf(
                    "username" to listOf(com.noovoweb.validator.ValidationError("Username is required")),
                    "email" to listOf(com.noovoweb.validator.ValidationError("Email must be valid"))
                )
            )
        }
    }

    @Test
    fun `plugin should be installed successfully`() = testApplication {
        application {
            install(ValidationPlugin) {
                defaultLocale = Locale.ENGLISH
                messageProvider = DefaultMessageProvider()
            }
        }

        routing {
            get("/test") {
                call.respond(HttpStatusCode.OK, "test")
            }
        }

        val response = client.get("/test")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `plugin should handle ValidationException with 422 status`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/validate") {
                val user = call.receive<TestUser>()
                FailingValidator().validate(user, call.validationContext())
                call.respond(HttpStatusCode.OK)
            }
        }

        val response = client.post("/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"test","email":"test@example.com"}""")
        }

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Validation Failed"))
        assertTrue(body.contains("Username is required"))
        assertTrue(body.contains("Email must be valid"))
    }

    @Test
    fun `validationContext should extract locale from Accept-Language header`() = testApplication {
        var capturedLocale: Locale? = null

        application {
            install(ValidationPlugin) {
                defaultLocale = Locale.ENGLISH
            }
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/validate") {
                val user = call.receive<TestUser>()
                val context = call.validationContext()
                capturedLocale = context.locale
                PassingValidator().validate(user, context)
                call.respond(HttpStatusCode.OK)
            }
        }

        client.post("/validate") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "fr-FR,fr;q=0.9")
            setBody("""{"username":"test","email":"test@example.com"}""")
        }

        assertNotNull(capturedLocale)
        assertEquals("fr", capturedLocale?.language)
    }

    @Test
    fun `validationContext should use default locale when header is missing`() = testApplication {
        var capturedLocale: Locale? = null

        application {
            install(ValidationPlugin) {
                defaultLocale = Locale.GERMAN
            }
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/validate") {
                val user = call.receive<TestUser>()
                val context = call.validationContext()
                capturedLocale = context.locale
                PassingValidator().validate(user, context)
                call.respond(HttpStatusCode.OK)
            }
        }

        client.post("/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"test","email":"test@example.com"}""")
        }

        assertNotNull(capturedLocale)
        assertEquals(Locale.GERMAN, capturedLocale)
    }

    @Test
    fun `ValidationErrorResponse should be properly serialized`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/validate") {
                val user = call.receive<TestUser>()
                FailingValidator().validate(user, call.validationContext())
                call.respond(HttpStatusCode.OK)
            }
        }

        val response = client.post("/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"test","email":"test@example.com"}""")
        }

        val body = Json.decodeFromString<ValidationErrorResponse>(response.bodyAsText())
        assertEquals(422, body.status)
        assertEquals("Validation Failed", body.message)
        assertEquals(2, body.errors.size)
        assertTrue(body.errors.containsKey("username"))
        assertTrue(body.errors.containsKey("email"))
    }

    @Test
    fun `validationContext should throw exception when plugin not installed`() = testApplication {
        application {
            // No plugin installed
        }

        routing {
            get("/test") {
                try {
                    call.validationContext()
                    call.respond(HttpStatusCode.OK)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                }
            }
        }

        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("ValidationPlugin not installed"))
    }
}
