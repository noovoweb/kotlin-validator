---
applyTo: "core/runtime/**/*.kt"
---

# Runtime Instructions

## ValidationPatterns

- Cached regex patterns must be bounded — no unbounded `*` / `+` on attacker-controlled input. Use `{0,N}`.
- `JsonParser` enforces `MAX_DEPTH = 100`. Don't remove the depth counter or the try/finally guards in `parseValue()`.
- Case-folding uses `Locale.ROOT`. Never the default locale.

## GeneratedValidator Interface

Every generated validator implements `GeneratedValidator<T>`. Adding a method here is a breaking change for already-published validators — avoid unless you bump the major version.

## ValidationContext

Carries locale, dispatcher, and `MessageProvider`. Pass it through — never construct a fresh one inside a validator unless explicitly required.
