---
applyTo: "**/*.kt"
---

> **Note:** Intentional overlap with `AGENTS.md` and `.github/copilot-instructions.md` (different tools).

# Kotlin / KSP Instructions

## Formatting

- Run `./gradlew spotlessApply` after editing Kotlin files.
- Spotless is wired into `compileKotlin`; unformatted code fails the build.

## Explicit API Mode

All published modules enable `explicitApi()`. Public declarations must have explicit visibility modifiers and explicit return types.

```kotlin
// ✅
public fun foo(): String = "x"
internal fun bar() { /* ... */ }

// ❌
fun foo() = "x"   // missing visibility + explicit return type
```

The `kotlin-validator-testing` module is the only exception.

## Suspend Validation

All validation is `suspend`. Use `runTest {}` from `kotlinx-coroutines-test` in tests.

```kotlin
@Test
fun `validates email`() = runTest {
    val result = EmailValidator().validateResult(payload, ValidationContext())
    assertTrue(result is ValidationResult.Success)
}
```

## Custom Validators

Use fully-qualified method references; signature must match exactly.

```kotlin
@CustomValidator(validator = "com.package.ClassName::methodName")
val field: String?

// In ClassName:
suspend fun methodName(value: String?, context: ValidationContext): Boolean { ... }
```

## Locale Safety

Never call `String.lowercase()` / `String.uppercase()` without a locale — always use `Locale.ROOT` to avoid Turkish-locale bugs (`"I".lowercase(Locale("tr"))` → `"ı"`).

```kotlin
// ✅
value.lowercase(Locale.ROOT)

// ❌
value.lowercase()
```
