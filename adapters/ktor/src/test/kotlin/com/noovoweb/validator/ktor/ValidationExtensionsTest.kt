package com.noovoweb.validator.ktor

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
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import java.time.Clock
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationExtensionsTest {

    @Serializable
    data class TestRequest(
        val name: String,
        val age: Int
    )

    class PassingValidator : GeneratedValidator<TestRequest> {
        override suspend fun validate(target: TestRequest, context: ValidationContext) {
            // Always passes
        }

        override suspend fun validateResult(
            payload: TestRequest,
            context: ValidationContext
        ): com.noovoweb.validator.ValidationResult<TestRequest> {
            return com.noovoweb.validator.ValidationResult.Success(payload)
        }
    }

    class FailingValidator : GeneratedValidator<TestRequest> {
        override suspend fun validate(target: TestRequest, context: ValidationContext) {
            throw ValidationException(
                mapOf("name" to listOf("Name is required"))
            )
        }

        override suspend fun validateResult(
            payload: TestRequest,
            context: ValidationContext
        ): com.noovoweb.validator.ValidationResult<TestRequest> {
            return com.noovoweb.validator.ValidationResult.Failure(
                mapOf("name" to listOf(com.noovoweb.validator.ValidationError("Name is required")))
            )
        }
    }

    @Test
    fun `receiveAndValidate should validate successfully with passing validator`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val request = call.receiveAndValidate(PassingValidator())
                call.respond(HttpStatusCode.OK, "Name: ${request.name}, Age: ${request.age}")
            }
        }

        val response = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","age":30}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Name: John, Age: 30"))
    }

    @Test
    fun `receiveAndValidate should return 422 with failing validator`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val request = call.receiveAndValidate(FailingValidator())
                call.respond(HttpStatusCode.OK, "Name: ${request.name}")
            }
        }

        val response = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","age":30}""")
        }

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertTrue(response.bodyAsText().contains("Name is required"))
    }

    @Test
    fun `receiveAndValidate with custom context should use provided context`() = testApplication {
        var capturedLocale: Locale? = null

        class LocaleCapturingValidator : GeneratedValidator<TestRequest> {
            override suspend fun validate(target: TestRequest, context: ValidationContext) {
                capturedLocale = context.locale
            }

            override suspend fun validateResult(
                payload: TestRequest,
                context: ValidationContext
            ): com.noovoweb.validator.ValidationResult<TestRequest> {
                capturedLocale = context.locale
                return com.noovoweb.validator.ValidationResult.Success(payload)
            }
        }

        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val customContext = ValidationContext(
                    locale = Locale.FRENCH,
                    dispatcher = Dispatchers.Default,
                    clock = Clock.systemDefaultZone()
                )
                val request = call.receiveAndValidate(LocaleCapturingValidator(), customContext)
                call.respond(HttpStatusCode.OK, "OK")
            }
        }

        client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","age":30}""")
        }

        assertEquals(Locale.FRENCH, capturedLocale)
    }

    @Test
    fun `validate extension should validate successfully`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val payload = call.receiveText()
                val request = TestRequest("Jane", 25)
                call.validate(request, PassingValidator())
                call.respondText("Validated: ${request.name}", status = HttpStatusCode.OK)
            }
        }

        val response = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Jane","age":25}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Validated: Jane"))
    }

    @Test
    fun `validate extension should fail with failing validator`() = testApplication {
        application {
            install(ValidationPlugin)
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val request = TestRequest("Jane", 25)
                call.validate(request, FailingValidator())
                call.respond(HttpStatusCode.OK, "OK")
            }
        }

        val response = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Jane","age":25}""")
        }

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertTrue(response.bodyAsText().contains("Name is required"))
    }

    @Test
    fun `validation should extract locale from Accept-Language in receiveAndValidate`() = testApplication {
        var capturedLocale: Locale? = null

        class LocaleCapturingValidator : GeneratedValidator<TestRequest> {
            override suspend fun validate(target: TestRequest, context: ValidationContext) {
                capturedLocale = context.locale
            }

            override suspend fun validateResult(
                payload: TestRequest,
                context: ValidationContext
            ): com.noovoweb.validator.ValidationResult<TestRequest> {
                capturedLocale = context.locale
                return com.noovoweb.validator.ValidationResult.Success(payload)
            }
        }

        application {
            install(ValidationPlugin) {
                defaultLocale = Locale.ENGLISH
            }
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            post("/test") {
                val request = call.receiveAndValidate(LocaleCapturingValidator())
                call.respond(HttpStatusCode.OK, "OK")
            }
        }

        client.post("/test") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "es-ES,es;q=0.9")
            setBody("""{"name":"Juan","age":35}""")
        }

        assertEquals("es", capturedLocale?.language)
    }
}
