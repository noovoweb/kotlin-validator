---
applyTo: "core/processor/**/*.kt"
---

# KSP Processor Instructions

## Pipeline

`@Validated` discovery → `AnnotationParser` (parses field annotations into a typed model) → `ValidatorClassGenerator` (emits the validator class) → `FieldValidatorCodeGenerator` (emits per-field validation logic). Output lands in `build/generated/ksp/main/kotlin/`.

## When Changing `FieldValidatorCodeGenerator`

- Generated code runs in user projects — be conservative.
- Generated case-folding must use `java.util.Locale.ROOT`.
- Distinct/uniqueness checks should not be `O(n²)`. Prefer a `HashSet` with early exit.
- Public symbols you reference from generated code must be stable (the generated code lives outside this repo's control once published).

## When Adding a New Annotation

1. Add the annotation to `core/annotations/src/main/kotlin/com/noovoweb/validator/`.
2. Add parsing in `AnnotationParser` (`core/processor/.../parser/`).
3. Add code generation in `FieldValidatorCodeGenerator`.
4. Add a `@Validated` test model in `testing/integration-tests/src/main/`.
5. Add a unit test under `testing/integration-tests/src/test/` referencing the generated validator.

## Patterns Used by the Processor

- `core/processor/.../ValidationPatterns.kt` mirrors `core/runtime/.../ValidationPatterns.kt`. Keep them in sync.
- Avoid unbounded regex quantifiers (`*`, `+`) on user input — bound them (`{0,N}`) to prevent ReDoS in the generated validator.
