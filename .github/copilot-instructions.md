# Copilot Instructions for kotlin-validator

## Build & Test Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew :kotlin-validator-testing:test --tests "com.noovoweb.validator.unit.string.EmailValidatorTest"

# Run a single test method
./gradlew :kotlin-validator-testing:test --tests "com.noovoweb.validator.unit.string.EmailValidatorTest.email validator accepts valid email addresses"

# Lint check / auto-fix
./gradlew spotlessCheck
./gradlew spotlessApply

# Code coverage report
./gradlew jacocoTestReport
```

JDK 21 is required. Kotlin 2.0.21 with KSP 2.0.21-1.0.28.

## Architecture

This is a **multi-module Kotlin validation library** that uses KSP (Kotlin Symbol Processing) to generate validator code at compile time — zero reflection at runtime.

**Core modules** (under `core/`):
- **annotations** — 64 validation annotations (`@Email`, `@Required`, `@Min`, `@Valid`, `@CustomValidator`, etc.). No dependencies beyond Kotlin stdlib.
- **engine** — Runtime types: `ValidationContext`, `ValidationResult` (sealed: `Success`/`Failure`), `ValidationException`, `MessageProvider`. This is the shared vocabulary all modules use.
- **processor** — KSP processor that reads `@Validated` data classes, parses their field annotations via `AnnotationParser`, and generates validator classes using KotlinPoet (`ValidatorClassGenerator` + `FieldValidatorCodeGenerator`).
- **runtime** — `GeneratedValidator<T>` interface that all generated validators implement, plus `ValidationPatterns` (cached regex patterns).

**Framework adapters** (under `adapters/`):
- **spring-webflux** — Auto-configuration, `ValidationContextProvider`, `ValidationExceptionHandler` (422 responses), i18n via Spring `MessageSource`.
- **spring-mvc** — Same pattern as WebFlux but blocking-compatible. Suspend `validate()` calls bridge through `runBlocking`.
- **ktor** — Plugin-based integration with `call.validationContext()` and automatic 422 handling.

**Testing** (under `testing/integration-tests`):
- Contains `@Validated` model classes in `src/main/` that KSP processes at compile time, producing validators.
- Tests in `src/test/` exercise those generated validators. This is the only module with KSP configured for test fixtures.

### How code generation works

1. User annotates a data class with `@Validated` and field annotations.
2. At compile time, `ValidatorProcessor` finds these classes and delegates to `AnnotationParser` → `ValidatorClassGenerator`.
3. A `<ClassName>Validator` class is generated implementing `GeneratedValidator<T>` with suspend `validate()` and `validateResult()` methods.
4. Generated code lives in `build/generated/ksp/main/kotlin/`.

## Key Conventions

- **Explicit API mode** is enabled on all published modules (not on the testing module). All public declarations need explicit visibility modifiers and return types.
- **All validation is `suspend`** — tests use `runTest {}` from `kotlinx-coroutines-test`, and validators are invoked inside coroutine scopes.
- **Test model pattern** — Each validator has a dedicated `@Validated` data class in `testing/integration-tests/src/main/` (e.g., `Email.kt` for `@Email`). The corresponding test class references the KSP-generated validator (e.g., `EmailValidator()`).
- **Custom validators** use fully-qualified method references: `@CustomValidator(validator = "com.package.ClassName::methodName")`. The function signature must be `suspend fun(value: T?, context: ValidationContext): Boolean`.
- **Formatting** — Spotless with ktlint 1.0.1. Wildcard imports, trailing commas, and certain comment rules are disabled (see root `build.gradle.kts` for overrides). Run `./gradlew spotlessApply` before committing.
- **Module naming** — Gradle module names use `kotlin-validator-*` prefix. Physical directories use short names (`core/engine/`), mapped in `settings.gradle.kts`.
- **Publishing** — All modules except `kotlin-validator-testing` are published to Maven Central via `gradle/publish.gradle.kts`. Group: `com.noovoweb`.
