---
applyTo: "adapters/**/*.kt"
---

# Adapter Instructions

## Validator Auto-Discovery (Ktor + WebFlux)

Both adapters expose a `T.validate()` extension that resolves the generated validator at runtime:

- Convention: `MyRequest` → `MyRequestValidator` (suffix `Validator`).
- Uses the payload's classloader to handle Spring DevTools restarts.
- Cache: `ConcurrentHashMap<String, GeneratedValidator<*>>` keyed by FQCN.
- **Always use `ConcurrentHashMap.computeIfAbsent`** — never get-then-put. Get-then-put races under concurrent first-request load.
- Wrap `ClassNotFoundException` thrown inside the lambda — `ConcurrentHashMap` re-throws it as `RuntimeException`. Unwrap via `(e as? RuntimeException)?.cause ?: e` and re-throw as `IllegalStateException` with the helpful "Validator not found" message.

## Cache Visibility

- WebFlux: `private val validatorCache`.
- Ktor: `@PublishedApi internal val validatorCache` — Ktor needs `internal` for use from `inline fun receiveAndValidate`. Don't change it to `private`.

## Spring MVC

No auto-discovery cache — only Ktor and WebFlux have one. MVC bridges suspend `validate()` through `runBlocking`.

## Locale

Always derived from request headers (`Accept-Language` for Spring, `HttpHeaders.AcceptLanguage` for Ktor). Apply via `ValidationContext.withLocale(...)`.

## Exception Handling

`ValidationException` → 422 with structured error paths (`departments[0].teams[1].members[2].email`). Don't flatten the path.
