# Kotlin Validator

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-0.1.0--beta.3-blue.svg?logo=github)](https://github.com/noovoweb/kotlin-validator/packages)

A high-performance, type-safe, coroutine-native validation library for Kotlin. Validators are generated at compile time with KSP, eliminating runtime reflection, annotation scanning, and hidden behavior. Each validator is a `suspend` function, enabling parallel field validation and non-blocking execution of I/O-bound custom validators. The library integrates seamlessly with any coroutine-based stack, including Spring WebFlux, Ktor, and `runTest`.

```kotlin
@Validated
data class CreateUserRequest(
    @Required @Email val email: String?,
    @Required @MinLength(8) val password: String?,
    @Required @Min(18.0) @Max(120.0) val age: Int?,
)

// In a Spring WebFlux handler (with the kotlin-validator-spring-webflux adapter):
suspend fun register(request: ServerRequest): ServerResponse {
    val req = request.awaitBody<CreateUserRequest>()
    req.validate()  // throws ValidationException with structured errors
    return ServerResponse.ok().bodyValueAndAwait(req)
}
```

## Why

| | Kotlin Validator | Reflection-based libs |
|---|---|---|
| Validator code | Generated at compile time (KSP) | Built at runtime via reflection |
| Execution model | Non-blocking (`suspend`), coroutine-native | Blocking or reactive wrappers |
| Field validation | Parallel for suspending validators, sequential fast path otherwise | Sequential |
| Overhead | No reflection, no annotation scanning at runtime | Reflection + annotation scanning |
| Type safety | Statically checked | Runtime errors |
| Cold start | No reflection scan | Slower |

Plus: 60+ built-in validators, deep nested validation with precise error paths (`departments[0].teams[1].members[2].email`), parallel field validation for I/O-bound validators, full i18n, and first-class Spring (MVC + WebFlux) and Ktor adapters.

## Install

The library is published to **GitHub Packages**. You'll need a GitHub Personal Access Token (classic) with the `read:packages` scope.

### 1. Add credentials

In `~/.gradle/gradle.properties` (recommended) or your project's `gradle.properties`:

```properties
gpr.user=your-github-username
gpr.token=ghp_your_personal_access_token
```

Or export them as environment variables — `GITHUB_ACTOR` and `GITHUB_TOKEN`.

> Create a token at https://github.com/settings/tokens with the `read:packages` scope.

### 2. Add the repository and dependencies

```kotlin
plugins {
    kotlin("jvm") version "2.4.0"
    id("com.google.devtools.ksp") version "2.3.9"
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/noovoweb/kotlin-validator")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.token").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val kotlinValidatorVersion = "0.1.0-beta.3"

dependencies {
    implementation("com.noovoweb:kotlin-validator-annotations:$kotlinValidatorVersion")
    implementation("com.noovoweb:kotlin-validator-runtime:$kotlinValidatorVersion")
    ksp("com.noovoweb:kotlin-validator-processor:$kotlinValidatorVersion")

    // Pick the adapter for your framework (optional)
    // implementation("com.noovoweb:kotlin-validator-spring-webflux:$kotlinValidatorVersion")
    // implementation("com.noovoweb:kotlin-validator-spring-mvc:$kotlinValidatorVersion")
    // implementation("com.noovoweb:kotlin-validator-ktor:$kotlinValidatorVersion")
}
```

> The KSP Gradle plugin adds the generated sources to the compilation automatically. If your IDE doesn't resolve the generated `*Validator` classes, add:
>
> ```kotlin
> kotlin {
>     sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
> }
> ```

## Quick Start

```kotlin
import com.noovoweb.validator.annotations.*
import com.noovoweb.validator.ValidationContext
import com.noovoweb.validator.ValidationException

@Validated
data class CreateUserRequest(
    @Required @Email val email: String?,
    @Required @MinLength(8) val password: String?,
    @Required @Min(18.0) @Max(120.0) val age: Int?,
)

suspend fun main() {
    val req = CreateUserRequest(email = "invalid", password = "short", age = 15)
    try {
        CreateUserRequestValidator().validate(req, ValidationContext())
    } catch (e: ValidationException) {
        e.errors.forEach { (field, msgs) -> println("$field: ${msgs.joinToString()}") }
    }
}
```

```
email: Please enter a valid email address
password: This field must contain at least 8 characters
age: This field must be at least 18.0
```

Prefer a result type over exceptions? Use `validateResult(req)` which returns `ValidationResult.Success` or `ValidationResult.Failure`.

---

## Framework Integration

Each adapter exposes a simplified `payload.validate(...)` extension that auto-discovers the generated validator and uses the request's `Accept-Language` header for locale.

<details>
<summary><b>Spring Boot WebFlux</b> (reactive)</summary>

```kotlin
val kotlinValidatorVersion = "0.1.0-beta.3"

dependencies {
    implementation("com.noovoweb:kotlin-validator-spring-webflux:$kotlinValidatorVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}
```

```kotlin
@Component
class UserHandler(private val ctx: ValidationContextProvider) {

    // Coroutine handler
    suspend fun register(request: ServerRequest): ServerResponse {
        val payload = request.awaitBody<RegisterRequest>()
        payload.validate(request, ctx.getBase())  // throws ValidationException
        return ServerResponse.ok().bodyValueAndAwait(payload)
    }

    // Reactive (Mono) handler
    fun registerMono(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<RegisterRequest>()
            .flatMap { it.validateMono(request, ctx.getBase()).thenReturn(it) }
            .flatMap { ServerResponse.ok().bodyValue(it) }
}
```

Routes:

```kotlin
@Bean fun routes(h: UserHandler) = coRouter {
    "/api".nest { POST("/register", h::register) }
}
```

Handle `ValidationException` in your `@ControllerAdvice` (or equivalent) to shape the HTTP response — the library deliberately does not impose an error format.

</details>

<details>
<summary><b>Spring Boot MVC</b> (blocking)</summary>

```kotlin
val kotlinValidatorVersion = "0.1.0-beta.3"

dependencies {
    implementation("com.noovoweb:kotlin-validator-spring-mvc:$kotlinValidatorVersion")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

```kotlin
@RestController
@RequestMapping("/api")
class UserController(private val ctx: ValidationContextProvider) {

    @PostMapping("/register")
    fun register(
        @RequestBody payload: RegisterRequest,
        request: HttpServletRequest,
    ): Map<String, Any> = runBlocking {
        RegisterRequestValidator().validate(payload, ctx.get(request))
        mapOf("data" to payload)
    }
}
```

Spring's `@Valid` is also supported via the blocking adapter.

</details>

<details>
<summary><b>Ktor</b></summary>

```kotlin
val kotlinValidatorVersion = "0.1.0-beta.3"

dependencies {
    implementation("com.noovoweb:kotlin-validator-ktor:$kotlinValidatorVersion")
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
}
```

```kotlin
fun Application.module() {
    install(ContentNegotiation) { json() }
    install(ValidationPlugin) { defaultLocale = Locale.ENGLISH }

    routing {
        post("/api/register") {
            val request = call.receiveAndValidate<RegisterRequest>()
            call.respond(mapOf("data" to request))
        }
    }
}
```

Handle `ValidationException` in `StatusPages` to shape the HTTP response.

</details>

<details>
<summary><b>Adapter comparison</b></summary>

| | Spring MVC | Spring WebFlux | Ktor |
|---|---|---|---|
| Programming model | Blocking | Reactive | Coroutines |
| Validation API | `.validate()` (via `runBlocking`) | `.validate()` / `.validateMono()` | `.validate()` / `receiveAndValidate()` |
| Auto-config | Yes | Yes | Manual plugin install |
| i18n source | Spring `MessageSource` | Spring `MessageSource` | `Accept-Language` header + resource bundles |
| Error handling | `@ControllerAdvice` | `@ControllerAdvice` | `StatusPages` |

</details>

Working examples:

- **WebFlux** — [kotlin-validator-spring-webflux-example](https://github.com/noovoweb/kotlin-validator-spring-webflux-example)
- **MVC** — [kotlin-validator-spring-mvc-example](https://github.com/noovoweb/kotlin-validator-spring-mvc-example)
- **Ktor** — [kotlin-validator-ktor-example](https://github.com/noovoweb/kotlin-validator-ktor-example)

Each example is a runnable REST API covering every validator category, custom validators with meta-annotations, English/French i18n, nested validation, and a Postman collection.

---

## Nested Objects & Arrays

Use `@Valid` to recurse into a single nested object, and `@Valid(each = true)` to validate each element of a collection:

```kotlin
@Validated
data class CreateUserRequest(
    @Required @Email val email: String?,
    @Valid val address: Address?,            // validates the nested Address
    @Valid(each = true) val phones: List<Phone>?,  // validates each Phone
)

@Validated
data class Address(
    @Required val street: String?,
    @Required val city: String?,
)

@Validated
data class Phone(
    @Required val number: String?,
)
```

Deeper nesting works the same way:

```kotlin
@Validated
data class Company(
    @Required val name: String?,
    @Valid(each = true) val departments: List<Department>?,
)

@Validated
data class Department(
    @Required val name: String?,
    @Valid(each = true) val teams: List<Team>?,
)

@Validated
data class Team(
    @MinLength(3) val teamName: String?,
    @Valid(each = true) val members: List<TeamMember>?,
)

@Validated
data class TeamMember(
    @Required @Email val email: String?,
    @Required @Alpha val name: String?,
)
```

Errors carry the precise path:

```json
{
  "departments[0].teams[0].members[0].email": ["Please enter a valid email address"],
  "departments[0].teams[1].teamName": ["This field must contain at least 3 characters"],
  "departments[1].teams[0].members[2].name": ["This field must contain only alphabetic characters"]
}
```

Element validation runs in parallel. To prevent runaway recursion, `ValidationContext.maxValidationDepth` is enforced (default `10`); circular references are also detected at compile time.

---

## Validators

`@Validated` activates code generation for a data class. `@FailFast` on a field stops further validators on **that field** once one fails (other fields still validate independently).

<details>
<summary><b>String</b> (22)</summary>

| Annotation | Description |
|---|---|
| `@Required` | Not null and not blank |
| `@Email` | Valid email |
| `@Url` | Valid HTTP/HTTPS URL (uses `java.net.URL`) |
| `@Uuid` | UUID format |
| `@Length(min, max)` / `@MinLength(n)` / `@MaxLength(n)` | Length bounds |
| `@Pattern(regex)` | Matches regex |
| `@Alpha` / `@Alphanumeric` / `@Ascii` | Character class |
| `@Lowercase` / `@Uppercase` | Case (locale-invariant) |
| `@StartsWith(s)` / `@EndsWith(s)` / `@Contains(s)` | Substring checks |
| `@OneOf([…])` / `@NotOneOf([…])` / `@Enum(EnumClass::class)` | Enum membership |
| `@Json` | Valid JSON (depth-limited parser) |
| `@Luhn` / `@CreditCard` | Luhn-validated identifiers |

</details>

<details>
<summary><b>Numeric</b> (12)</summary>

`@Min(v)`, `@Max(v)`, `@Between(min, max)`, `@Positive`, `@Negative`, `@Zero`, `@Integer`, `@Decimal`, `@DivisibleBy(v)`, `@Even`, `@Odd`, `@DecimalPlaces(n)`

</details>

<details>
<summary><b>Collection</b> (7)</summary>

`@Size(min, max)`, `@MinSize(n)`, `@MaxSize(n)`, `@NotEmpty`, `@Distinct`, `@ContainsValue(v)`, `@NotContains(v)`

> Tip: pair `@Distinct` with `@MaxSize` on user-supplied collections.

</details>

<details>
<summary><b>Date / Time</b> (6)</summary>

`@DateFormat(pattern)`, `@IsoDate`, `@IsoDateTime`, `@Future`, `@Past`, `@Today`

</details>

<details>
<summary><b>Network</b> (5)</summary>

`@IPv4`, `@IPv6`, `@IP`, `@MacAddress`, `@Port`

IP validation uses `InetAddress` rather than regex (no ReDoS risk).

</details>

<details>
<summary><b>File</b> (3)</summary>

`@MimeType([…])`, `@FileExtension([…])`, `@MaxFileSize(bytes)`

</details>

<details>
<summary><b>Conditional</b> (6)</summary>

`@Same(field)`, `@Different(field)`, `@RequiredIf(field, value)`, `@RequiredUnless(field, value)`, `@RequiredWith(field)`, `@RequiredWithout(field)`

</details>

<details>
<summary><b>Boolean / Structural / Custom</b></summary>

- `@Accepted` — `true`, `1`, `yes`, `on`
- `@Valid` / `@Valid(each = true)` — recurse into nested objects / collections
- `@FailFast` — stop subsequent validators on this field after a failure
- `@CustomValidator(validator, message)` — your own logic (see below)

</details>

---

## Custom Validators

Build a meta-annotation that points to a `suspend` function returning `Boolean`. Returning `true` means valid.

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@CustomValidator(
    validator = "com.example.PasswordValidator::validateStrong",
    message = "password.strong",
)
annotation class StrongPassword

object PasswordValidator {
    suspend fun validateStrong(value: String?, context: ValidationContext): Boolean {
        if (value == null) return true
        return value.length in 8..128 &&
            value.any { it.isUpperCase() } &&
            value.any { it.isLowerCase() } &&
            value.any { it.isDigit() }
    }
}

@Validated
data class RegisterRequest(
    @Email val email: String?,
    @StrongPassword val password: String?,
)
```

<details>
<summary><b>I/O-bound custom validators (HTTP, DB, files)</b></summary>

`ValidationContext.dispatcher` defaults to `Dispatchers.Default` (CPU-bound). For I/O work — API calls, database lookups, file access — switch to `Dispatchers.IO` so you don't block the CPU pool:

```kotlin
suspend fun validateAddressExists(value: String?, ctx: ValidationContext): Boolean {
    if (value.isNullOrBlank()) return true
    return withContext(Dispatchers.IO) {
        runCatching { geocoder.lookup(value).isNotEmpty() }
            .getOrDefault(true)  // fail-open on transient error
    }
}
```

For rate-limited external services use `Dispatchers.IO.limitedParallelism(n)`. Keep `withContext(Dispatchers.IO)` scoped to the I/O call itself — don't swap the context-wide dispatcher to `IO`, since built-in validators are CPU-bound and would needlessly occupy I/O threads.

A complete geocoding example lives in the [WebFlux example repo](https://github.com/noovoweb/kotlin-validator-spring-webflux-example).

</details>

---

## Internationalization

Built-in messages ship in **English** and **French** for every built-in validator. Locale resolution comes from the `Accept-Language` header in adapter modules.

Override any message — built-in or custom — by passing a key to the annotation and defining it in your messages bundle:

```kotlin
@Validated
data class UpdateUserRequest(
    @Email("user.email.invalid") val email: String?,
    @MinLength(3, "user.name.too_short") val name: String?,
)
```

```properties
# src/main/resources/messages.properties
user.email.invalid=Please provide a valid email address for your account
user.name.too_short=Your name must be at least 3 characters long
```

```properties
# src/main/resources/messages_fr.properties
user.email.invalid=Veuillez fournir une adresse e-mail valide pour votre compte
user.name.too_short=Votre nom doit contenir au moins 3 caractères
```

For standalone use (no framework), implement `MessageProvider` and pass it to `ValidationContext`. Spring adapters consult Spring's `MessageSource` first, then fall back to the library bundle. Ktor uses `ValidationMessages.properties` resource bundles.

---

## Configuration

Most of the time you don't need to configure anything. The options below are the ones that actually move the needle.

### `ValidationContext` (core)

| Field | When to override | Default |
|---|---|---|
| `locale` | Render messages in a specific language (set per request, or globally) | `Locale.ENGLISH` |
| `messageProvider` | Plug in your own message source (resource bundles, DB-backed copy, Spring `MessageSource`) | `DefaultMessageProvider()` |
| `clock` | Inject a fixed `Clock` in tests so `@Past` / `@Future` are deterministic | `Clock.systemDefaultZone()` |
| `metadata` | Pass request-scoped data (tenant id, user id, feature flags) into custom validators, then read it via `ctx.metadata["key"]` inside the validator | `emptyMap()` |
| `maxValidationDepth` | Raise when you legitimately have deeply nested `@Valid` graphs; lower to harden against hostile input | `10` |

```kotlin
val context = ValidationContext()
    .withLocale(Locale.FRENCH)
    .withMetadata("tenantId", currentUser.tenantId)  // read inside custom validators via ctx.metadata["tenantId"]
    .withMaxValidationDepth(20)

UserRequestValidator().validate(request, context)
```

> **Note on `dispatcher`:** `ValidationContext` also exposes a `dispatcher`, but built-in validators are CPU-bound, so `Dispatchers.Default` is the right choice and you should leave it alone. For I/O inside a custom validator, wrap that specific call in `withContext(Dispatchers.IO) { ... }` — see [I/O-bound custom validators](#custom-validators).

### Spring Boot (`application.yml` / `application.properties`)

Both `kotlin-validator-spring-webflux` and `kotlin-validator-spring-mvc` register a `ValidatorProperties` bean under the `kotlin.validator` prefix:

```yaml
kotlin:
  validator:
    locale: en_US             # fallback locale when the request has none
    use-request-locale: true  # MVC only — see below
```

| Property | Modules | Purpose | Default |
|---|---|---|---|
| `kotlin.validator.locale` | `spring-webflux`, `spring-mvc` | Fallback locale for validation messages (e.g. `en_US`, `fr_FR`, `en`) | `Locale.getDefault()` |
| `kotlin.validator.use-request-locale` | `spring-mvc` only | When `true`, builds a per-request `ValidationContext` with the locale from `Accept-Language` (auto-configures a `LocaleResolver` if missing). When `false`, uses a singleton context with the fixed `locale` above. | `true` |

In WebFlux, the per-request locale is resolved automatically from `Accept-Language` via Spring's reactive `LocaleContextResolver` — no flag needed.

To customize messages, expose your own `MessageSource` bean (standard Spring i18n); the adapter consults it before falling back to the library bundle.

### Ktor (`install(ValidationPlugin) { ... }`)

```kotlin
install(ValidationPlugin) {
    defaultLocale   = Locale.ENGLISH         // fallback when Accept-Language is missing/invalid
    messageProvider = DefaultMessageProvider()
    clock           = Clock.systemDefaultZone()
}
```

| Field | When to override | Default |
|---|---|---|
| `defaultLocale` | Pick the fallback locale used when the request has no usable `Accept-Language` header | `Locale.getDefault()` |
| `messageProvider` | Swap in custom messages or a different bundle layout | `DefaultMessageProvider()` |
| `clock` | Inject a fixed `Clock` in tests | `Clock.systemDefaultZone()` |

Inside a route, `call.validationContext()` builds a `ValidationContext` for the current request, with `locale` extracted from the `Accept-Language` header.

### Per-annotation overrides

Most annotations let you override the message key inline (e.g. `@Email("user.email.invalid")`). See [Internationalization](#internationalization) for details.

---

## Execution Model

When a class has validators that can do real suspending work (custom validators, nested `@Valid`, file validators), its fields validate in **parallel** via coroutines. Classes with only built-in CPU-cheap validators use a **sequential fast path** — dispatching a coroutine per field would cost more than the validation itself.

Validators **within a field** always run sequentially in declaration order, so:

- Errors come back in the order you wrote them.
- `@FailFast` short-circuits the right scope.
- Cheap checks (`@Required`) gate expensive ones (`@Pattern`, custom I/O) when placed first.

---

## Modules

| Module | Purpose |
|---|---|
| `kotlin-validator-annotations` | All validation annotations |
| `kotlin-validator-engine` | Core types: `ValidationContext`, `ValidationResult`, `ValidationException` |
| `kotlin-validator-processor` | KSP processor (apply with `ksp(...)`) |
| `kotlin-validator-runtime` | `GeneratedValidator` interface + helpers |
| `kotlin-validator-spring-webflux` | Reactive Spring Boot adapter (auto-config, i18n) |
| `kotlin-validator-spring-mvc` | Servlet Spring Boot adapter (auto-config, i18n) |
| `kotlin-validator-ktor` | Ktor plugin + extensions |

Core modules depend only on Kotlin stdlib + coroutines.

---

## Security

The library is designed to be safe against malicious input by default:

- Regex patterns are compiled once and cached — no compile-on-call DoS.
- Pattern-based validators apply hard input length limits to mitigate ReDoS.
- IP and URL validation use `InetAddress` / `java.net.URL`, not unbounded regex.
- The built-in JSON validator caps recursion depth (100 levels) to prevent stack overflow on nested payloads.
- `@Distinct` uses `HashSet`-based detection with early exit, so oversized arrays cannot be used to slow validation.
- Case-sensitive comparisons are locale-invariant (`Locale.ROOT`) — no Turkish-i surprises.
- Validator instances are stateless and safely shared across requests.

Found a vulnerability? Email **info@noovoweb.com** rather than opening a public issue. We aim to respond within 48 hours.

---

## Contributing & Build

| Command | Description |
|---|---|
| `./gradlew test` | Run all tests |
| `./gradlew spotlessApply` | Format code |
| `./gradlew spotlessCheck` | Check formatting |
| `./gradlew clean build` | Full clean build |
| `./gradlew publishToMavenLocal` | Install to `~/.m2/repository` for local consumption |
| `./gradlew publishAllPublicationsToGitHubPackagesRepository` | Publish to GitHub Packages (requires `gpr.user` + `gpr.token` with `write:packages` scope) |

PRs welcome.

## License

Apache License 2.0 — see [LICENSE](LICENSE).

Copyright © 2025 Noovoweb.
