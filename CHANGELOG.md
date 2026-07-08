# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
While the version is `0.x` / pre-release, the public API may still change between releases.

## [Unreleased]

### Added
- `CHANGELOG.md`.
- Binary-compatibility validation (`apiCheck` / `apiDump`) with committed `*.api`
  files, run in CI so public API changes are caught in review.
- CI now builds and tests on both JDK 17 and JDK 21 (the supported floor and a
  current release). The compile/test JDK is selectable via `-PbuildJavaVersion`.

### Fixed
- Documentation drift: `@Url` / URL validation is described as using `java.net.URI`
  (the actual implementation) instead of `java.net.URL`.

## [0.1.0-beta.7] - 2026-07-07

### Changed
- The project-level `gradle.properties` (shared build settings) is now tracked in
  the repository; the machine-specific `gradle.properties.template` was removed.
  GitHub Packages credentials still belong in `~/.gradle/gradle.properties`.

## [0.1.0-beta.6] - 2026-07-06

### Security
- `@Valid(each = true)` now validates collection elements with bounded concurrency
  (`ValidationContext.maxElementConcurrency`, default 64) instead of spawning one
  coroutine per element, preventing resource exhaustion on large collections.
- `@MimeType` detects the file type from its content ("magic bytes") rather than the
  filename extension, so a mismatched extension can no longer spoof the type. Formats
  without a recognizable signature fall back to the OS content probe.

### Added
- `ValidationContext.maxElementConcurrency` and `withMaxElementConcurrency(...)`.
- `FileSignatures` runtime helper for content-based MIME detection.

## [0.1.0-beta.5] - 2026-07-06

### Changed
- Java baseline lowered from 21 to 17, so the library is usable by Java 17 / Spring
  Boot 3 consumers.
- URL validation switched from `java.net.URL` to `java.net.URI` for strict, JDK-consistent
  parsing (rejects spaces and other malformed input the older path accepted).

### Added
- Aggregated test-coverage reporting (`jacocoTestReport`) via the
  `jacoco-report-aggregation` plugin, produced in CI.

### Dependencies
- Spring Boot 4.1.0, Ktor 3.5.1, JUnit 6.1.1, MockK 1.14.11, Gradle wrapper 9.6.1.

## [0.1.0-beta.4] - 2026-07-06

### Changed
- Published version is single-sourced in `build.gradle.kts`, so CI and tag-triggered
  publishing no longer depend on an untracked file.
- A single shared `DefaultMessageProvider` instance is reused across validation
  contexts instead of reloading resource bundles per context.
- Generated validators use a sequential fast path when a class has only CPU-cheap
  built-in validators, and parallel (coroutine) validation only when custom, nested,
  or file validators are present.
- The processor's duplicated copy of `ValidationPatterns` was removed; generated code
  references the runtime constants directly.

## [0.1.0-beta.3] - 2026-04-25

- First published pre-release: compile-time (KSP) validator generation, 60+ built-in
  validators, nested/collection validation with precise error paths, coroutine-native
  execution, i18n, and Spring MVC / Spring WebFlux / Ktor adapters.

[Unreleased]: https://github.com/noovoweb/kotlin-validator/compare/v0.1.0-beta.7...HEAD
[0.1.0-beta.7]: https://github.com/noovoweb/kotlin-validator/compare/v0.1.0-beta.6...v0.1.0-beta.7
[0.1.0-beta.6]: https://github.com/noovoweb/kotlin-validator/compare/v0.1.0-beta.5...v0.1.0-beta.6
[0.1.0-beta.5]: https://github.com/noovoweb/kotlin-validator/compare/v0.1.0-beta.4...v0.1.0-beta.5
[0.1.0-beta.4]: https://github.com/noovoweb/kotlin-validator/compare/v0.1.0-beta.3...v0.1.0-beta.4
[0.1.0-beta.3]: https://github.com/noovoweb/kotlin-validator/releases/tag/v0.1.0-beta.3
