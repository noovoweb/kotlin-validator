# Hook: session-start-summary

**Type:** sessionStart  
**Trigger:** At the start of every agent session in this repository

## Action

```bash
echo "=== Session Start: $(date '+%Y-%m-%d %H:%M') ==="
echo ""
echo "--- Uncommitted changes ---"
git status --short
echo ""
echo "--- Diff summary ---"
git diff --stat HEAD
echo ""
echo "--- Last 3 commits ---"
git log --oneline -3
```

## Why

Instantly surfaces what was in progress from the last session without manual `git status`. Prevents re-doing work or losing track of uncommitted changes.

## Notes

- Read-only — never modifies git state
- If the working tree is clean, prints "Working tree clean"
