---
name: swiftBuild
description: Runs a Swift build and returns success/failure with compiler errors
unlocked_by: base
category: swift-project
---

# swiftBuild

Builds the Swift project and returns build results with any compiler/build errors.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `appPath` | string | no | Path to the Swift app. Optional if only one Swift app exists in workspace |

## Returns

Build result object containing:
- `success`: boolean indicating build outcome
- Compiler/build errors when failing (file, line, column, message)

## Sequencing Rules

1. **Must run after any Swift code changes**
2. Fix the **first** compiler error, then rebuild — many errors cascade from a single root cause
3. Repeat build-fix cycle until success
4. Not required for non-code documentation changes

## Usage Pattern

```
Edit Swift file → swiftBuild → fix first error → swiftBuild → ... → success
```
