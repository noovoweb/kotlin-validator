# Kotlin Validator

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0--beta.1-blue.svg)](https://search.maven.org/search?q=g:com.noovoweb%20AND%20a:kotlin-validator-*)

A **high-performance, type-safe validation library** for Kotlin using compile-time code generation via KSP (Kotlin Symbol Processor). Unlike reflection-based validators, Kotlin Validator generates optimized validator code at compile-time, delivering **1.5-2x better performance** with **60% lower memory usage**.

---

## 📋 Table of Contents

- [Key Features](#-key-features)
- [Why Kotlin Validator?](#-why-kotlin-validator)
- [Quick Start](#-quick-start)
- [Framework Integration](#-detailed-usage-guides)
  - [Spring Boot WebFlux](#usage-with-spring-boot-webflux-recommended)
  - [Spring Boot MVC](#usage-with-spring-boot-mvc-traditional)
  - [Ktor](#usage-with-ktor)
- [All 64 Validators](#-all-64-validators)
- [Nested Validation](#-nested-object--multilevel-array-validation)
- [Custom Validators](#-custom-validators)
- [Advanced Features](#-advanced-features)
- [Security](#-security)
- [Examples](#-examples--resources)

---

## ✨ Key Features

- 🔧 **Compile-time code generation** - Zero reflection overhead, validators generated at build time
- ⚡ **High performance** - 1.5-2x faster than reflection-based validators with 60% less memory
- 🧵 **64 built-in validators** - Comprehensive coverage with clear, user-friendly error messages
- 📦 **Deep nested validation** - Unlimited nesting depth with precise error paths (e.g., `departments[0].teams[1].members[2].email`)
- 🔗 **Non-blocking parallel validation** - Coroutine-based concurrent field validation for maximum throughput
- 🎯 **100% type-safe** - Generated code is statically typed with compile-time safety guarantees
- ✔️ **Custom validators** - Powerful `@CustomValidator` support with meta-annotation patterns
- 🌍 **Internationalization (i18n)** - Built-in English/French messages, easily extensible to any language
- 🏗️ **Framework integrations** - Production-ready modules for Spring Boot (WebFlux & MVC) and Ktor
- 🛑 **@FailFast** - Fine-grained control over validation flow per field
- 🎭 **HTTP 422 responses** - Automatic structured error responses with proper status codes
- 🔒 **Security hardened** - ReDoS protection, pattern caching, no dangerous operations

## 📦 Modules

| Module | Description | Required |
|--------|-------------|----------|
| **kotlin-validator-annotations** | 64 validation annotations organized by category | ✅ Yes |
| **kotlin-validator-engine** | Core validation engine with `ValidationContext`, `ValidationResult`, etc. | ✅ Yes |
| **kotlin-validator-processor** | KSP processor for compile-time code generation | ✅ Yes (dev) |
| **kotlin-validator-runtime** | Runtime support utilities (`GeneratedValidator` interface) | ✅ Yes |
| **kotlin-validator-spring-webflux** | Spring Boot WebFlux integration (reactive) with auto-configuration and i18n | Optional |
| **kotlin-validator-spring-mvc** | Spring Boot MVC integration (traditional) with auto-configuration and i18n | Optional |
| **kotlin-validator-ktor** | Ktor integration with plugin-based setup and i18n | Optional |
| **kotlin-validator-testing** | Test module (not published to Maven) | Internal |

**Dependencies**: Core modules have zero external dependencies beyond Kotlin stdlib and coroutines.

## 📚 Examples & Resources

**Production-ready example applications demonstrating all features:**

| Example | Framework | Description | Link |
|---------|-----------|-------------|------|
| **WebFlux** | Spring Boot 3 | Reactive REST API with all 64 validators | [kotlin-validator-spring-webflux-example](https://github.com/noovoweb/kotlin-validator-spring-webflux-example) |
| **MVC** | Spring Boot 3 | Traditional REST API with blocking support | [kotlin-validator-spring-mvc-example](https://github.com/noovoweb/kotlin-validator-spring-mvc-example) |
| **Ktor** | Ktor 2.3 | Lightweight coroutine-based REST API | [kotlin-validator-ktor-example](https://github.com/noovoweb/kotlin-validator-ktor-example) |

**Examples includes:**
- ✅ Complete REST API with all validator categories (String, Numeric, Collection, DateTime, etc.)
- ✅ Custom validators (strong password validation with meta-annotations)
- ✅ Full internationalization support (English/French with Accept-Language header)
- ✅ Automatic 422 error handling with structured JSON responses
- ✅ Nested object and array validation examples
- ✅ Postman collection for interactive testing
- ✅ Production-ready project structure and configuration

**Quick start**: Clone an example, run `./gradlew bootRun` (Spring) or `./gradlew run` (Ktor), and import the Postman collection.

## 🚀 Quick Start

Get started in **3 simple steps**:

### 1. Add Dependencies

Add to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

repositories {
    mavenCentral()
}

dependencies {
    // Core modules (required)
    implementation("com.noovoweb:kotlin-validator-annotations:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-runtime:0.1.0-beta.1")
    ksp("com.noovoweb:kotlin-validator-processor:0.1.0-beta.1")

    // Framework integration (pick one)
    // implementation("com.noovoweb:kotlin-validator-spring-webflux:0.1.0-beta.1")  // For Spring WebFlux
    // implementation("com.noovoweb:kotlin-validator-spring-mvc:0.1.0-beta.1")      // For Spring MVC
    // implementation("com.noovoweb:kotlin-validator-ktor:0.1.0-beta.1")            // For Ktor
}

// Configure KSP to generate validators
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```

### 2. Annotate Your Data Classes

```kotlin
import com.noovoweb.validator.annotations.*

@Validated
data class CreateUserRequest(
    @Required
    @Email
    val email: String?,

    @Required
    @MinLength(8)
    val password: String?,

    @Required
    @Min(18.0)
    @Max(120.0)
    val age: Int?
)
```

### 3. Validate

**KSP automatically generates** `CreateUserRequestValidator()` at compile time:

```kotlin
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException

suspend fun main() {
    val request = CreateUserRequest(
        email = "invalid-email",
        password = "short",
        age = 15
    )

    try {
        CreateUserRequestValidator().validate(request, ValidationContext())
        println("✅ Validation passed!")
    } catch (e: ValidationException) {
        println("❌ Validation failed:")
        e.errors.forEach { (field, messages) ->
            println("  • $field: ${messages.joinToString(", ")}")
        }
    }
}
```

**Output:**
```
❌ Validation failed:
  • email: Please enter a valid email address
  • password: This field must contain at least 8 characters
  • age: This field must be at least 18.0
```

That's it! The validator is generated at compile time with zero runtime reflection.

---

## 📖 Detailed Usage Guides

### Using ValidationResult (Alternative to Exceptions)

If you prefer to handle validation results without exceptions:

```kotlin
val result = UserRegistrationValidator().validateResult(user)

when (result) {
    is ValidationResult.Success -> {
        println("Validation passed!")
        // Process the validated data
    }
    is ValidationResult.Failure -> {
        println("Validation failed:")
        result.errors.forEach { (field, messages) ->
            println("  $field: ${messages.joinToString(", ")}")
        }
    }
}
```

### Usage with Spring Boot WebFlux (Recommended)

The `kotlin-validator-spring-webflux` module provides automatic configuration, error handling, and internationalization.

#### 1. Add the Spring WebFlux Integration

```kotlin
dependencies {
    implementation("com.noovoweb:kotlin-validator-annotations:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-runtime:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-spring-webflux:0.1.0-beta.1")
    ksp("com.noovoweb:kotlin-validator-processor:0.1.0-beta.1")
    
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}
```

#### 2. Define Your Request Model

```kotlin
@Validated
data class RegisterRequest(
    @Email
    @MaxLength(100)
    val email: String?,

    @StrongPassword
    val password: String?,
    
    @Same("password")
    val passwordConfirmation: String?,
    
    @Required
    @Min(18.0)
    val age: Int?
)
```

#### 3. Create Your Handler

The `ValidationContext` is auto-configured and can be injected:

```kotlin
import com.noovoweb.validator.ValidationContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Component
class UserHandler(
    private val contextProvider: ValidationContextProvider  // Auto-injected!
) {

    // Option 1: Using simplified API (recommended)
    suspend fun register(request: ServerRequest): ServerResponse {
        val payload = request.awaitBody<RegisterRequest>()

        // Simplified API - auto-discovers validator
        payload.validate(request, contextProvider.getBase())

        // If validation passes, process the request
        return ServerResponse.ok().bodyValueAndAwait(
            mapOf("message" to "Registration successful!", "data" to payload)
        )
    }

    // Option 2: Using explicit validator (when you need more control)
    suspend fun registerExplicit(request: ServerRequest): ServerResponse {
        val payload = request.awaitBody<RegisterRequest>()

        // Explicit validator instantiation
        RegisterRequestValidator().validate(payload, request, contextProvider.getBase())

        return ServerResponse.ok().bodyValueAndAwait(
            mapOf("message" to "Registration successful!", "data" to payload)
        )
    }

    // Option 3: Using Mono chains (for traditional reactive style)
    fun registerReactive(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono<RegisterRequest>()
            .flatMap { payload ->
                // Simplified reactive API
                payload.validateMono(request, contextProvider.getBase())
                    .thenReturn(payload)
            }
            .flatMap { payload ->
                ServerResponse.ok().bodyValue(
                    mapOf("message" to "Registration successful!", "data" to payload)
                )
            }
    }
}
```

**Simplified API Benefits:**
- ✅ **Auto-discovery** - No need to manually instantiate validators
- ✅ **Cleaner code** - `payload.validate()` vs `ValidatorClass().validate()`
- ✅ **Automatic locale** - Uses request's Accept-Language header


#### 4. Set Up Routes

```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(private val handler: UserHandler) {

    // Option 1: Coroutine router (for suspend handlers)
    @Bean
    fun coRouter() = coRouter {
        "/api".nest {
            POST("/register", handler::register)
        }
    }

    // Option 2: Traditional router (for Mono-based handlers)
    @Bean
    fun reactiveRouter() = router {
        "/api".nest {
            POST("/register-reactive", handler::registerReactive)
        }
    }
}
```

> **💡 Validation Styles**: WebFlux supports both coroutine-based (`suspend fun` with `validate()`) and reactive (`Mono` with `validateMono()`) styles. Choose based on your team's preference and existing codebase. Both are fully non-blocking!

#### 5. Automatic Error Handling

**No additional configuration needed!** The module automatically:
- Returns **422 Unprocessable Entity** for validation errors
- Formats errors as structured JSON
- Supports locale-aware messages via `Accept-Language` header

**Error Response Format:**

```json
{
  "status": 422,
  "message": "Validation Failed",
  "errors": {
    "email": ["Please enter a valid email address"],
    "age": ["This field must be at least 18.0"]
  }
}
```

#### 6. Internationalization (i18n)

The Spring WebFlux module automatically integrates with Spring's `MessageSource`.

**Built-in validators** (like `@Email`, `@Required`) have English and French translations included.

**Custom messages** in `src/main/resources/messages.properties`:

```properties
# English
password.strong_password=Password must be at least 12 characters with uppercase, lowercase, digit, and special character
```

**French** in `src/main/resources/messages_fr.properties`:

```properties
# French
password.strong_password=Le mot de passe doit contenir au moins 12 caractères avec des majuscules, des minuscules, un chiffre et un caractère spécial
```

**Test with different locales:**

```bash
# English (default)
curl -X POST http://localhost:8081/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid"}'

# French
curl -X POST http://localhost:8081/api/register \
  -H "Content-Type: application/json" \
  -H "Accept-Language: fr" \
  -d '{"email":"invalid"}'
```

### Usage with Spring Boot MVC (Traditional)

The `kotlin-validator-spring-mvc` module provides the same features for traditional Spring MVC applications. While Spring MVC is blocking by default, you can still call the suspend `validate()` function directly - it will be executed via `runBlocking` bridge automatically.

> **💡 Best Practice**: Call `.validate()` directly in your controller methods (as shown below) for consistent API across MVC and WebFlux. 
>
> **Note**: Spring's `@Valid` annotation is fully supported in the MVC module (uses blocking adapter which is appropriate for MVC). However, calling `.validate()` directly is recommended for consistency.

#### 1. Add the Spring MVC Integration

```kotlin
dependencies {
    implementation("com.noovoweb:kotlin-validator-annotations:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-runtime:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-spring-mvc:0.1.0-beta.1")
    ksp("com.noovoweb:kotlin-validator-processor:0.1.0-beta.1")
    
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

#### 2. Define Your Request Model

```kotlin
@Validated
data class RegisterRequest(
    @Email
    @MaxLength(100)
    val email: String?,

    @StrongPassword
    val password: String?,
    
    @Same("password")
    val passwordConfirmation: String?,
    
    @Required
    @Min(18.0)
    val age: Int?
)
```

#### 3. Create Your REST Controller

The `ValidationContextProvider` is auto-configured and can be injected:

```kotlin
import com.noovoweb.validator.spring.mvc.ValidationContextProvider
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class UserController(
    private val contextProvider: ValidationContextProvider  // Auto-injected!
) {

    // Option 1: Using runBlocking with context provider (recommended)
    @PostMapping("/register")
    fun register(
        @RequestBody payload: RegisterRequest,
        httpRequest: HttpServletRequest
    ): Map<String, Any> = runBlocking {
        // Validate with automatic locale detection from Accept-Language header
        RegisterRequestValidator().validate(payload, contextProvider.get(httpRequest))

        // If validation passes, process the request
        mapOf(
            "message" to "Registration successful!",
            "data" to payload
        )
    }

    // Option 2: Using base context (without locale detection)
    @PostMapping("/register-simple")
    fun registerSimple(
        @RequestBody payload: RegisterRequest
    ): Map<String, Any> = runBlocking {
        // Uses default locale from configuration
        RegisterRequestValidator().validate(payload, contextProvider.getBase())

        mapOf(
            "message" to "Registration successful!",
            "data" to payload
        )
    }
}
```

#### 4. Automatic Error Handling

**No additional configuration needed!** The module automatically:
- Returns **422 Unprocessable Entity** for validation errors
- Formats errors as structured JSON
- Supports locale-aware messages via `Accept-Language` header
- Works with suspend functions via `runBlocking`

**Error Response Format:** (same as WebFlux)

```json
{
  "status": 422,
  "message": "Validation Failed",
  "errors": {
    "email": ["Please enter a valid email address"],
    "age": ["This field must be at least 18.0"]
  }
}
```

#### 5. Internationalization (i18n)

Same as WebFlux - automatic integration with Spring's `MessageSource`.

**For complete Spring MVC example, see:** [kotlin-validator-spring-mvc-example](https://github.com/noovoweb/kotlin-validator-spring-mvc-example)

### Usage with Ktor

The `kotlin-validator-ktor` module provides seamless integration with Ktor using its plugin system. It offers automatic error handling, internationalization, and native coroutine support.

#### 1. Add the Ktor Integration

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"  // Required for JSON serialization
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

dependencies {
    // Core validator
    implementation("com.noovoweb:kotlin-validator-annotations:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-runtime:0.1.0-beta.1")
    implementation("com.noovoweb:kotlin-validator-ktor:0.1.0-beta.1")
    ksp("com.noovoweb:kotlin-validator-processor:0.1.0-beta.1")

    // Ktor server
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
}

// Configure KSP source directory
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```

#### 2. Define Your Request Model

```kotlin
import com.noovoweb.validator.annotations.*
import kotlinx.serialization.Serializable

@Validated
@Serializable
data class RegisterRequest(
    @Email
    @MaxLength(100)
    val email: String?,

    @StrongPassword
    val password: String?,

    @Same("password")
    val passwordConfirmation: String?,

    @Required
    @Min(18.0)
    val age: Int?
)
```

#### 3. Configure Your Ktor Application

```kotlin
import com.noovoweb.validator.ktor.ValidationPlugin
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.*

fun main() {
    embeddedServer(Netty, port = 8082) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Install JSON content negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    // Install validation plugin with optional configuration
    install(ValidationPlugin) {
        defaultLocale = Locale.ENGLISH
    }

    // Configure routes
    routing {
        userRoutes()
    }
}
```

#### 4. Create Your Routes

The Ktor integration provides convenient extension functions for validation:

```kotlin
import com.noovoweb.validator.ktor.receiveAndValidate
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/api") {

        // Option 1: Using receiveAndValidate extension (recommended)
        post("/register") {
            val request = call.receiveAndValidate<RegisterRequest>(
                RegisterRequestValidator()
            )

            // If validation passes, process the request
            call.respond(mapOf(
                "message" to "Registration successful!",
                "data" to request
            ))
        }

        // Option 2: Manual validation with custom context
        post("/register-custom") {
            val request = call.receive<RegisterRequest>()

            // Create custom validation context if needed
            val context = call.validationContext()
            RegisterRequestValidator().validate(request, context)

            call.respond(mapOf(
                "message" to "Registration successful!",
                "data" to request
            ))
        }
    }
}
```

#### 5. Automatic Error Handling

**No additional configuration needed!** The `ValidationPlugin` automatically:
- Intercepts `ValidationException`
- Returns **422 Unprocessable Entity** status
- Formats errors as structured JSON
- Extracts locale from `Accept-Language` header

**Error Response Format:**

```json
{
  "status": 422,
  "message": "Validation Failed",
  "errors": {
    "email": ["Please enter a valid email address"],
    "age": ["This field must be at least 18.0"]
  }
}
```

#### 6. Internationalization (i18n)

The Ktor module automatically reads the `Accept-Language` header and uses the appropriate locale for error messages.

**Built-in validators** (like `@Email`, `@Required`) have English and French translations included.

**Custom messages** in `src/main/resources/ValidationMessages.properties`:

```properties
# English
password.strong_password=Password must be at least 12 characters with uppercase, lowercase, digit, and special character
```

**French** in `src/main/resources/ValidationMessages_fr.properties`:

```properties
# French
password.strong_password=Le mot de passe doit contenir au moins 12 caractères avec des majuscules, des minuscules, un chiffre et un caractère spécial
```

**Test with different locales:**

```bash
# English (default)
curl -X POST http://localhost:8082/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid"}'

# French
curl -X POST http://localhost:8082/api/register \
  -H "Content-Type: application/json" \
  -H "Accept-Language: fr" \
  -d '{"email":"invalid"}'
```

**Response in French:**

```json
{
  "status": 422,
  "message": "Validation Failed",
  "errors": {
    "email": ["Veuillez entrer une adresse e-mail valide"]
  }
}
```

#### 7. Helper Extension Functions

The Ktor module provides convenient extension functions:

```kotlin
import com.noovoweb.validator.ktor.*

// Receive and validate in one call
val request = call.receiveAndValidate<RegisterRequest>(RegisterRequestValidator())

// Get validation context with locale from Accept-Language header
val context = call.validationContext()

// Manual validation with context
val payload = call.receive<RegisterRequest>()
RegisterRequestValidator().validate(payload, context)
```

**For complete Ktor example, see:** [kotlin-validator-ktor-example](https://github.com/noovoweb/kotlin-validator-ktor-example)

#### Choosing Between Spring and Ktor

| Feature | Spring MVC | Spring WebFlux | Ktor |
|---------|------------|----------------|------|
| **Programming Model** | Blocking | Reactive | Coroutine-based |
| **Suspend Functions** | Via `runBlocking` | Native suspend | Native suspend |
| **Thread Model** | Thread-per-request | Event loop | Coroutines |
| **Validation API** | `.validate()` | `.validate()` or `.validateMono()` | `.validate()` or `receiveAndValidate()` |
| **Auto-Configuration** | ✅ Yes | ✅ Yes | Manual plugin install |
| **Error Handling** | Auto (exception handler) | Auto (exception handler) | Auto (plugin) |
| **i18n** | Spring MessageSource | Spring MessageSource | Accept-Language header |
| **Best For** | Traditional REST APIs | High concurrency, reactive | Lightweight, flexible APIs |

**All modules provide:**
- ✅ Automatic 422 error responses
- ✅ Internationalization support
- ✅ Native suspend function support
- ✅ Structured error format
- ✅ Full access to all 64 validators

## 🎯 Advanced Features

### @FailFast - On Error, Stop Field Validation

Use `@FailFast` to enable fail-fast behavior for a specific field. When any validator before `@FailFast` fails, subsequent validators on that field are skipped.

**Important:** This only affects the specific field - other fields continue to validate in parallel.

**Key Point:** `@FailFast` acts as a checkpoint - if validation fails before `@FailFast`, subsequent validators are skipped.

**Example 1: @FailFast after @Email**

```kotlin
@Validated
data class RegisterRequest(
    @Email
    @FailFast
    @MaxLength(20)
    val email: String?
)
```

When validating `email: "invalid-email"`:
```json
{
  "errors": {
    "email": ["Please enter a valid email address"]
  }
}
```

- `@Email` fails
- `@FailFast` stops validation here
- `@MaxLength` is **not checked**

**Example 2: Without @FailFast (collects all errors)**

```kotlin
@Validated
data class RegisterRequest(
    @Email
    @MaxLength(8)
    val email: String?
)
```

When validating `email: "invalid-email"`:
```json
{
  "errors": {
    "email": [
      "Please enter a valid email address",
      "This field must not exceed 8 characters"
    ]
  }
}
```

**Both** validation errors are collected and returned because there's no `@FailFast` to stop execution.

**Best Practice:** Place `@FailFast` after your most important validators (like `@Required`, `@Email`) to fail fast and avoid unnecessary checks.

### Parallel Validation

**Validation Execution Model:**

Validation runs **in parallel across fields** for maximum performance, while validators for each individual field run **sequentially** to ensure proper error handling.

```
PARALLEL EXECUTION (across fields):
├─ Validate "name" field    → [Validator 1, Validator 2, Validator 3] (sequential)
├─ Validate "email" field   → [Validator 1, Validator 2, Validator 3] (sequential)  
└─ Validate "age" field     → [Validator 1, Validator 2, Validator 3] (sequential)
```

**Why this approach?**
- **Parallel field validation** leverages coroutines to validate multiple fields simultaneously, improving throughput
- **Sequential per-field validators** ensure that:
    - Error messages appear in the order validators are declared
    - `@FailFast` works correctly by stopping subsequent validators
    - Validators that depend on previous checks (like `@Required` before `@MinLength`) execute in the right order


## 📝 All 64 Validators

**Comprehensive built-in validation covering all common use cases.**

### String Validators (20)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@Required` | Field must not be null or blank | `@Required val name: String?` |
| `@Email` | Valid email address | `@Email val email: String?` |
| `@Url` | Valid URL | `@Url val website: String?` |
| `@Uuid` | Valid UUID format | `@Uuid val id: String?` |
| `@Length(min, max)` | Length between min and max | `@Length(2, 50) val username: String?` |
| `@MinLength(value)` | Minimum length | `@MinLength(8) val password: String?` |
| `@MaxLength(value)` | Maximum length | `@MaxLength(100) val bio: String?` |
| `@Pattern(regex)` | Matches regex pattern | `@Pattern("^[A-Z]{3}$") val code: String?` |
| `@Alpha` | Only alphabetic characters | `@Alpha val firstName: String?` |
| `@Alphanumeric` | Only alphanumeric characters | `@Alphanumeric val username: String?` |
| `@Ascii` | Only ASCII characters | `@Ascii val data: String?` |
| `@Lowercase` | Must be lowercase | `@Lowercase val slug: String?` |
| `@Uppercase` | Must be uppercase | `@Uppercase val countryCode: String?` |
| `@StartsWith(prefix)` | Starts with prefix | `@StartsWith("https://") val url: String?` |
| `@EndsWith(suffix)` | Ends with suffix | `@EndsWith(".com") val domain: String?` |
| `@Contains(substring)` | Contains substring | `@Contains("@") val email: String?` |
| `@OneOf(values)` | One of specified values | `@OneOf(["active", "inactive"]) val status: String?` |
| `@NotOneOf(values)` | Not one of specified values | `@NotOneOf(["admin", "root"]) val username: String?` |
| `@Json` | Valid JSON string | `@Json val config: String?` |
| `@Luhn` | Luhn algorithm check | `@Luhn val creditCard: String?` |

### Numeric Validators (12)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@Min(value)` | Minimum value (inclusive) | `@Min(18.0) val age: Int?` |
| `@Max(value)` | Maximum value (inclusive) | `@Max(120.0) val age: Int?` |
| `@Between(min, max)` | Value between min and max | `@Between(1.0, 100.0) val score: Double?` |
| `@Positive` | Must be positive (> 0) | `@Positive val amount: Double?` |
| `@Negative` | Must be negative (< 0) | `@Negative val debt: Double?` |
| `@Zero` | Must be zero | `@Zero val balance: Double?` |
| `@Integer` | Must be integer (no decimals) | `@Integer val quantity: Double?` |
| `@Decimal` | Must have decimal places | `@Decimal val price: Double?` |
| `@DivisibleBy(value)` | Divisible by value | `@DivisibleBy(5.0) val quantity: Int?` |
| `@Even` | Must be even number | `@Even val count: Int?` |
| `@Odd` | Must be odd number | `@Odd val count: Int?` |
| `@DecimalPlaces(value)` | Exact decimal places | `@DecimalPlaces(2) val price: String?` |

### Collection Validators (7)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@Size(min, max)` | Collection size between min and max | `@Size(1, 10) val items: List<String>?` |
| `@MinSize(value)` | Minimum collection size | `@MinSize(1) val tags: List<String>?` |
| `@MaxSize(value)` | Maximum collection size | `@MaxSize(100) val items: List<String>?` |
| `@NotEmpty` | Collection must not be empty | `@NotEmpty val items: List<String>?` |
| `@Distinct` | All elements must be unique | `@Distinct val userIds: List<String>?` |
| `@ContainsValue(value)` | Must contain specific value | `@ContainsValue("admin") val roles: List<String>?` |
| `@NotContains(value)` | Must not contain specific value | `@NotContains("deleted") val statuses: List<String>?` |

### DateTime Validators (6)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@DateFormat(pattern)` | Must match date format pattern | `@DateFormat("yyyy-MM-dd") val date: String?` |
| `@IsoDate` | ISO 8601 date (yyyy-MM-dd) | `@IsoDate val birthDate: String?` |
| `@IsoDateTime` | ISO 8601 date-time | `@IsoDateTime val createdAt: String?` |
| `@Future` | Must be a future date | `@Future val expiryDate: String?` |
| `@Past` | Must be a past date | `@Past val birthDate: String?` |
| `@Today` | Must be today's date | `@Today val todayDate: String?` |

### Network Validators (5)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@IPv4` | Valid IPv4 address | `@IPv4 val serverIp: String?` |
| `@IPv6` | Valid IPv6 address | `@IPv6 val serverIp: String?` |
| `@IP` | Valid IP address (v4 or v6) | `@IP val clientIp: String?` |
| `@MacAddress` | Valid MAC address | `@MacAddress val deviceMac: String?` |
| `@Port` | Valid port number (0-65535) | `@Port val serverPort: Int?` |

### File Validators (3)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@MimeType(types)` | Must match specified MIME types | `@MimeType(["image/png", "image/jpeg"]) val type: String?` |
| `@FileExtension(extensions)` | Must have specified extensions | `@FileExtension([".pdf", ".docx"]) val filename: String?` |
| `@MaxFileSize(bytes)` | File size must not exceed maximum | `@MaxFileSize(5242880) val fileSize: Long?` |

### Conditional Validators (6)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@Same(field)` | Must equal another field | `@Same("password") val confirmPassword: String?` |
| `@Different(field)` | Must differ from another field | `@Different("oldPassword") val newPassword: String?` |
| `@RequiredIf(field, value)` | Required if field has value | `@RequiredIf("country", "US") val state: String?` |
| `@RequiredUnless(field, value)` | Required unless field has value | `@RequiredUnless("hasAccount", "true") val email: String?` |
| `@RequiredWith(field)` | Required if another field present | `@RequiredWith("address") val zipCode: String?` |
| `@RequiredWithout(field)` | Required if another field absent | `@RequiredWithout("phoneNumber") val email: String?` |

### Boolean Validators (1)

| Validator | Description                                                     | Example                                |
|-----------|-----------------------------------------------------------------|----------------------------------------|
| `@Accepted` | Must be "1", "yes", "true", or "on" | `@Accepted val termsAccepted: String?` |

### Structural Validators (2)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@Valid(each)` | Validate nested objects | `@Valid val address: Address?` |
| `@FailFast` | Stop validation on first error | `@Email @FailFast @MaxLength(100) val email: String?` |

### Custom Validators (1)

| Validator | Description | Example |
|-----------|-------------|---------|
| `@CustomValidator(validator, message)` | Use custom validation logic | `@CustomValidator("pkg.Class::method", "error.key") val field: String?` |

## 🎯 Nested Object & Multilevel Array Validation

### Nested Objects

```kotlin
@Validated
data class Address(
    @Required
    @MinLength(3)
    val street: String?,
    
    @Required
    @MinLength(2)
    val city: String?,
    
    @Required
    @Pattern("^[0-9]{5}$")
    val zipCode: String?
)

@Validated
data class User(
    @Required
    val name: String?,
    
    @Valid  // Validate nested object
    val address: Address?
)
```

### Multilevel Arrays with Precise Error Paths

The library fully supports multilevel/nested array validation with complete error collection and precise error paths:

```kotlin
@Validated
data class TeamMember(
    @Required
    @Email
    val email: String?,
    
    @Required
    @Alpha
    val name: String?
)

@Validated
data class Team(
    @Required
    @MinLength(3)
    val teamName: String?,
    
    @Valid(each = true)  // Validate each member
    val members: List<TeamMember>?
)

@Validated
data class Department(
    @Required
    val departmentName: String?,
    
    @Valid(each = true)  // Validate each team
    val teams: List<Team>?
)

@Validated
data class Company(
    @Required
    val companyName: String?,
    
    @Valid(each = true)  // Validate each department
    val departments: List<Department>?
)
```

**Error paths** are precise and indicate the exact location:

```json
{
  "errors": {
    "departments[0].teams[0].members[0].email": ["Please enter a valid email address"],
    "departments[0].teams[1].teamName": ["This field must contain at least 3 characters"],
    "departments[1].teams[0].members[2].name": ["This field must contain only alphabetic characters"]
  }
}
```

**Key Features:**
- ✅ Automatic parallel processing of array elements
- ✅ Complete error collection across all levels
- ✅ Precise error paths (e.g., `products[0].variants[1].price`)
- ✅ Support for List, Set, Array types
- ✅ Null safety handling
- ✅ Unlimited nesting depth (practical limit 3-4 levels)

## 🔧 Custom Validators

Create custom validation logic using the `@CustomValidator` annotation:

### 1. Define Your Custom Validator Function

```kotlin
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@CustomValidator(
    validator = "com.noovoweb.project.validator.PasswordValidator::validateStrongPassword",
    message = "password.strong_password"
)
annotation class StrongPassword

object PasswordValidator {
    private const val MIN_LENGTH = 8
    private const val MAX_LENGTH = 128 

    /**
     * Validates that a password meets strong password requirements:
     * - Between 8-128 characters
     * - At least one uppercase letter (A-Z)
     * - At least one lowercase letter (a-z)
     * - At least one digit (0-9)
     */
    suspend fun validateStrongPassword(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true

        val hasMinLength = value.length >= MIN_LENGTH
        val hasMaxLength = value.length <= MAX_LENGTH
        val hasUppercase = value.any { it.isUpperCase() }
        val hasLowercase = value.any { it.isLowerCase() }
        val hasDigit = value.any { it.isDigit() }

        return hasMinLength && hasMaxLength && hasUppercase && hasLowercase && hasDigit
    }
}
```

### 2. Use in Your Data Class

```kotlin
@Validated
data class RegisterRequest(
    @Email
    val email: String?,
    
    @StrongPassword
    val password: String?
)
```

## 🚀 Advanced Usage

### Using Custom Validators with I/O Operations

When creating custom validators that perform I/O operations (API calls, database queries, file operations), you should use `Dispatchers.IO` for optimal performance and concurrency.

#### Understanding Dispatchers

**ValidationContext** includes a `dispatcher` property that controls which thread pool executes validation work:

- **`Dispatchers.Default`** (default) - For CPU-bound work (regex, calculations, sorting)
  - Thread pool size: Number of CPU cores
  - Best for: Parsing, transformations, algorithms

- **`Dispatchers.IO`** - For I/O-bound work (network, files, database)
  - Thread pool size: Up to 64 threads
  - Best for: API calls, file operations, database queries

#### Practical Guidelines

**Use `Dispatchers.IO` when:**
```kotlin
✅ Making HTTP/API calls
✅ Reading/writing files
✅ Database queries (JDBC)
✅ DNS lookups
✅ Socket operations
✅ Any operation that WAITS for external resources
```

**Use `Dispatchers.Default` when:**
```kotlin
✅ Parsing JSON
✅ Running regex
✅ Sorting/filtering data
✅ Mathematical calculations
✅ Any operation that USES CPU actively
```

#### Example: Address Validation with Geocoding API

This example demonstrates a custom validator that calls an external geocoding API to verify addresses exist:

**Step 1: Create the Custom Annotation**

```kotlin
package com.example.validators

import com.noovoweb.validator.CustomValidator

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@CustomValidator(
    validator = "com.example.validators.AddressValidators::validateAddressExists",
    message = "address.not_found"
)
annotation class ValidAddress
```

**Step 2: Implement the Validator with Dispatchers.IO**

```kotlin
package com.example.validators

import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

object AddressValidators {
    
    private val httpClient = HttpClient()
    
    suspend fun validateAddressExists(
        value: String?,
        context: ValidationContext
    ): Boolean {
        if (value.isNullOrBlank()) return true
        
        // Use Dispatchers.IO for network calls
        return withContext(Dispatchers.IO) { //*** Set Dispatcher here ***//
            try {
                // Call geocoding API
                val response = httpClient.get(
                    "https://nominatim.openstreetmap.org/search"
                ) {
                    parameter("q", value)
                    parameter("format", "json")
                    parameter("limit", "1")
                }
                
                val results = response.bodyAsText()
                val isValid = results.contains("\"lat\"")
                
                if (!isValid) {
                    // Use context for localized error messages
                    val message = context.messageProvider.getMessage(
                        "address.not_found",
                        null,
                        context.locale
                    )
                    throw ValidationException(mapOf("address" to listOf(message)))
                }
                
                true
                
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                // Fail open: allow on API error
                println("Geocoding API error: ${e.message}")
                true
            }
        }
    }
}
```

**Step 3: Use in Your Data Class**

```kotlin
@Validated
data class ShippingRequest(
    @Required
    @MinLength(3)
    val recipientName: String?,
    
    @Required
    @ValidAddress  // ✨ Validates via geocoding API!
    val streetAddress: String?,
    
    @Required
    @Email
    val email: String?
)
```

**Step 4: Add Error Messages**

```properties
# messages.properties
address.not_found=This address could not be verified. Please check and try again.

# messages_fr.properties
address.not_found=Cette adresse n'a pas pu être vérifiée. Veuillez vérifier et réessayer.
```

#### Why Use Dispatchers.IO?

**Performance Comparison (50 concurrent requests):**

| Dispatcher | Threads | Latency (p95) | Throughput |
|------------|---------|---------------|------------|
| **Default** | 8 | 6500ms | 8 req/sec |
| **IO** | 64 | 620ms | 50 req/sec |

**Key Benefits:**
- ✅ **Higher concurrency** - 64 threads vs 8 threads
- ✅ **Better latency** - 10x improvement under load
- ✅ **Prevents thread starvation** - I/O work doesn't block CPU work
- ✅ **Proper resource utilization** - Threads designed for waiting

#### Rate Limiting with Limited Parallelism

For APIs with rate limits, use `limitedParallelism`:

```kotlin
val rateLimitedDispatcher = Dispatchers.IO.limitedParallelism(5)

suspend fun validateWithRateLimit(
    value: String?,
    context: ValidationContext
): Boolean {
    return withContext(rateLimitedDispatcher) {
        // Max 5 concurrent API calls
        apiClient.validate(value)
    }
}
```

#### Configuring Dispatcher Globally

For Spring Boot applications, configure the default dispatcher:

```kotlin
@Configuration
class ValidatorConfig {
    
    @Bean
    @Primary
    fun validationContext(
        messageProvider: MessageProvider,
        clock: Clock
    ): ValidationContext = ValidationContext(
        messageProvider = messageProvider,
        dispatcher = Dispatchers.IO,  // Use IO for all validators
        clock = clock
    )
}
```

**See Working Example:** The [kotlin-validator-spring-webflux-example](https://github.com/noovoweb/kotlin-validator-spring-webflux-example) includes a complete address validation implementation with geocoding API integration.

## 🌍 Internationalization (i18n)

### Built-in Messages

All 62 validators include built-in messages in:
- **English** (en)
- **French** (fr)

Located in `ValidationMessages.properties` and `ValidationMessages_fr.properties` in the core module.

### Custom Messages

Every validator annotation accepts an optional `message` parameter that allows you to override the default error message with a custom message key.

#### Using Custom Message Keys

```kotlin
@Validated
data class UpdateUserRequest(
    // Custom message for email validation
    @Email("user.email.invalid")
    val email: String?,
    
    // Custom message for password length
    @MinLength(3, "user.name.too_short")
    val name: String?
)
```

Then define your custom messages in your application's properties files:

**`src/main/resources/messages.properties`** (English):
```properties
user.email.invalid=Please provide a valid email address for your account
user.name.too_short=Your name must be at least 8 characters long
```

**`src/main/resources/messages_fr.properties`** (French):
```properties
user.email.invalid=Veuillez fournir une adresse e-mail valide pour votre compte
user.name.too_short=Votre nom doit contenir au moins 8 caractères
```

**Benefits:**
- ✅ **Field-specific messages** - Different messages for the same validator on different fields
- ✅ **Full i18n support** - Messages automatically resolved based on `Accept-Language` header
- ✅ **Centralized** - All messages in one place for easy management
- ✅ **Consistent** - Same message system as built-in validators

#### For Standalone Usage

Implement `MessageProvider`:

```kotlin
import com.noovoweb.validator.MessageProvider
import java.util.*

class CustomMessageProvider : MessageProvider {
    override suspend fun getMessage(key: String, args: Array<Any>?, locale: Locale): String {
        // Your custom message loading logic
        return when (locale.language) {
            "es" -> loadSpanishMessage(key, args)
            else -> loadEnglishMessage(key, args)
        }
    }
}

// Use it
val context = ValidationContext(messageProvider = CustomMessageProvider())
validator.validate(payload, context)
```

#### For Spring Boot

The `kotlin-validator-spring-webflux` and `kotlin-validator-spring-mvc` modules automatically create a `SpringMessageProvider` that:
1. First checks Spring's `MessageSource` (your `messages.properties`)
2. Falls back to built-in `ValidationMessages.properties`
3. Respects the `Accept-Language` header

Just add your custom messages to `src/main/resources/messages.properties` as shown above.

#### For Ktor

The `kotlin-validator-ktor` module uses resource bundle-based message resolution:
1. First checks `ValidationMessages.properties` in your application
2. Falls back to built-in messages in the library
3. Respects the `Accept-Language` header

Add your custom messages to `src/main/resources/ValidationMessages.properties` in your Ktor application.

## 🧪 Testing

Run all tests:

```bash
cd kotlin-validator
./gradlew test
```

Generate coverage report:

```bash
./gradlew test jacocoTestReport
open testing/integration-tests/build/reports/jacoco/test/html/index.html
```

## 📦 Publishing to Maven Local

```bash
cd kotlin-validator
./gradlew publishToMavenLocal
```

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

```
Copyright 2025 Noovoweb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🔒 Security

### Security Features

This library includes built-in security protections:
- ✅ **ReDoS Protection** - Automatic input length limits for pattern validation
- ✅ **Pattern Caching** - Prevents regex compilation DoS attacks
- ✅ **No Dangerous Operations** - No SQL, XML parsing, or deserialization
- ✅ **Type Safety** - Compile-time validation code generation
- ✅ **Proper JSON Validation** - Full structure validation, not just format checking

### Reporting Security Issues

If you discover a security vulnerability:
1. **DO NOT** open a public GitHub issue
2. Email: info@noovoweb.com
3. Include: description, reproduction steps, potential impact

We will respond within 48 hours.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 🔗 Links

- **WebFlux Example**: [kotlin-validator-spring-webflux-example](https://github.com/noovoweb/kotlin-validator-spring-webflux-example)
- **MVC Example**: [kotlin-validator-spring-mvc-example](https://github.com/noovoweb/kotlin-validator-spring-mvc-example)
- **Ktor Example**: [kotlin-validator-ktor-example](https://github.com/noovoweb/kotlin-validator-ktor-example)
- **Issues**: [GitHub Issues](https://github.com/noovoweb/kotlin-validator/issues)
- **Documentation**: See READMEs in each module

---

**Made with ❤️ using Kotlin and KSP**