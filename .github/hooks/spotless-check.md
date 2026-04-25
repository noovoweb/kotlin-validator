# Hook: spotless-check

**Type:** postToolUse  
**Trigger:** After any file edit on `*.kt` files inside `kotlin-validator/`

## Action

Run Spotless check on the changed file after editing:

```bash
./gradlew spotlessCheck 2>&1 | tail -20
```

Or apply formatting automatically:

```bash
./gradlew spotlessApply
```

## Why

Spotless is wired into `compileKotlin`, so unformatted code fails the build. This hook catches formatting issues immediately after edits rather than at the next build.

## Notes

- Non-blocking: report the result but do not prevent the edit from being saved
- Prefer `spotlessApply` over `spotlessCheck` when you know formatting needs fixing
