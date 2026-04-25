# AGENTS.md — kotlin-validator

Guidance for AI agents working in this Kotlin/KSP validation library.

> **Note:** Intentional overlap with `.github/copilot-instructions.md` and `.github/instructions/` (different tools).

## Agent Behaviour Rules

- **Be ultra concise.** In code, comments, documentation, and responses — say as little as possible to convey the point.
- **Never commit on behalf of the user.** Do not run `git commit` unless explicitly asked.
- **When writing or updating documentation and comments, write the correct content directly.** Do not add text explaining why something was changed. The content should stand on its own.
- **Run `./gradlew spotlessApply` after editing Kotlin files.** Spotless is wired into `compileKotlin`, so an unformatted change will fail the build.

## Technologies

- Kotlin 2.0.21, KSP 2.0.21-1.0.28, JDK 21 toolchain
- Gradle multi-module build, published to Maven Central (group `com.noovoweb`)
- Spotless + ktlint 1.8.0 for formatting; Jacoco for coverage
- Adapters: Spring WebFlux, Spring MVC, Ktor

## Commands

| Command | Description |
|---|---|
| `./gradlew build` | Build all modules + run all tests + spotlessCheck |
| `./gradlew spotlessApply` | Auto-format Kotlin sources |
| `./gradlew spotlessCheck` | Verify formatting (runs as part of `build` via `compileKotlin`) |
| `./gradlew jacocoTestReport` | Aggregated coverage report |
| `./gradlew :kotlin-validator-testing:test --tests "<fqcn>.<method>"` | Run a single test |
| `./gradlew publishToMavenLocal` | Publish all publishable modules locally for example apps |

## Architecture

### Module Structure

Physical directories use short names; Gradle module names are prefixed with `kotlin-validator-` (mapped in `settings.gradle.kts`).

- `core/annotations` — Pure annotation declarations (`@Email`, `@Required`, `@Min`, `@Valid`, `@CustomValidator`, …). No runtime deps beyond Kotlin stdlib.
- `core/engine` — `ValidationContext`, `ValidationResult` (sealed `Success`/`Failure`), `ValidationException`, `MessageProvider`. Shared vocabulary.
- `core/processor` — KSP processor. Reads `@Validated` data classes, parses field annotations via `AnnotationParser`, generates validators using KotlinPoet (`ValidatorClassGenerator` + `FieldValidatorCodeGenerator`).
- `core/runtime` — `GeneratedValidator<T>` interface, `ValidationPatterns` (cached regexes + bounded JSON parser).
- `adapters/spring-webflux` — Auto-configuration, `ValidationContextProvider`, 422 exception handler, i18n via Spring `MessageSource`. Provides the `T.validate()` extension.
- `adapters/spring-mvc` — Same pattern as WebFlux but blocking-compatible.
- `adapters/ktor` — Plugin-based integration with `call.validationContext()` and `receiveAndValidate<T>()`.
- `testing/integration-tests` — `@Validated` model classes in `src/main/` are processed by KSP at compile time; tests in `src/test/` exercise the generated validators.

### How Code Generation Works

1. User annotates a data class with `@Validated` and field annotations.
2. At compile time, `ValidatorProcessor` finds these classes and delegates to `AnnotationParser` → `ValidatorClassGenerator`.
3. A `<ClassName>Validator` class is generated implementing `GeneratedValidator<T>` with suspend `validate()` and `validateResult()` methods.
4. Generated code lives in `build/generated/ksp/main/kotlin/`.

## Key Conventions

- **Explicit API mode** is enabled on every published module (not on `kotlin-validator-testing`). Public declarations need explicit visibility modifiers and return types.
- **All validation is `suspend`.** Tests use `runTest {}` from `kotlinx-coroutines-test`.
- **Test model pattern.** Each validator has a dedicated `@Validated` data class in `testing/integration-tests/src/main/` (e.g. `Email.kt` for `@Email`); the corresponding test class references the KSP-generated validator (`EmailValidator()`).
- **Custom validators** use fully-qualified method references: `@CustomValidator(validator = "com.package.ClassName::methodName")`. Signature: `suspend fun(value: T?, context: ValidationContext): Boolean`.
- **Module naming.** Gradle module names use the `kotlin-validator-*` prefix; physical directories use short names.
- **Publishing.** All modules except `kotlin-validator-testing` are published via `gradle/publish.gradle.kts`. Group: `com.noovoweb`.

## Performance & Security

The library is performance- and security-sensitive. When changing `core/runtime/ValidationPatterns.kt` or anything in `core/processor/ksp/`:

- Avoid unbounded regex quantifiers — they enable ReDoS.
- Honour the JSON parser's `MAX_DEPTH` guard; never remove the depth counter.
- Use `Locale.ROOT` for any case-folding (`lowercase`/`uppercase`) — never the default locale.
- Adapter validator caches must use `ConcurrentHashMap.computeIfAbsent` (atomic). Don't revert to get-then-put.

## Adapter Conventions

- Spring WebFlux & Ktor expose a `T.validate()` extension that auto-discovers the generated validator via reflection and caches it in a `ConcurrentHashMap` keyed by `"${className}Validator"`.
- Spring MVC bridges suspend `validate()` calls through `runBlocking`.
- Adapters convert `ValidationException` into a 422 response with structured error paths.
- Locale is picked up from `Accept-Language` (Spring) or `call.request.headers[HttpHeaders.AcceptLanguage]` (Ktor).
