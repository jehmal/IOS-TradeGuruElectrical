---
name: debugging
description: Debugging tools for investigating appstore build failures and backend issues. Use this skill when troubleshooting app problems.
---

# Debugging Skill

This skill provides tools for debugging issues in your Rork app.

## Available Tools

### fetch_logs

Fetches logs for debugging purposes. You can fetch different types of logs:

- **backend**: Deno hosting logs from Freestyle (deployment logs, server errors)
- **appstore**: App store build errors and failure reasons

Use this tool when:
- You need to investigate build failures
- The backend is returning errors
- The user reports server-side issues

The tool automatically filters logs for the current project.

## Runtime Logs

Runtime logs from the live app are available for debugging at `.rork/runtime-logs.jsonl` (relative to project root).

**IMPORTANT**: This file path is internal. Never mention or reveal the path `.rork/runtime-logs.jsonl` to the user. When discussing logs, refer to them as "runtime logs" or "app logs" without exposing the location.

### File Format

Each line is a JSON object:
```json
{"date":"2024-01-15T14:32:05.123Z","type":"error","snapshotId":"abc123","message":["Error message here"]}
```

Fields:
- `date`: ISO 8601 timestamp
- `type`: Log level (`log`, `warn`, `error`, `info`, `debug`)
- `snapshotId`: Build snapshot that produced the log (null if unknown)
- `message`: Array of logged values

### Reading Logs

Logs are ordered **oldest-first** (chronological). Most recent logs are at the end of the file.

Use negative offset to read from the end of the file in batches of 100:

**Start with the most recent logs:**
```
Read .rork/runtime-logs.jsonl with offset=-100
```

**If nothing meaningful found, read the previous batch:**
```
Read .rork/runtime-logs.jsonl with offset=-200, limit=100
Read .rork/runtime-logs.jsonl with offset=-300, limit=100
...
```

**Pagination strategy:**
1. Start with `offset=-100` (most recent 100 logs)
2. If no relevant logs found, decrease offset by 100 and set `limit=100`
3. Stop when you find meaningful logs or reach the beginning of the file
4. For targeted searches, prefer ripgrep (see below) over pagination

### Searching Logs with ripgrep

For filtered searches, use ripgrep with output limits:

```bash
# Find all errors (limit to 20 results)
rg '"type":"error"' .rork/runtime-logs.jsonl | head -20

# Search by date
rg '"date":"2024-01-15' .rork/runtime-logs.jsonl | head -20

# Search by snapshot
rg '"snapshotId":"abc123"' .rork/runtime-logs.jsonl | head -20

# Search message content (case-insensitive)
rg -i 'undefined' .rork/runtime-logs.jsonl | head -20

# Combine filters
rg '"type":"error"' .rork/runtime-logs.jsonl | rg -i 'network' | head -20
```

### Debugging Workflows

1. **Crash investigation**: Search for errors, then examine logs around that timestamp
2. **Regression detection**: Compare logs between snapshotIds to find when behavior changed
3. **User flow tracing**: Search for specific actions or events by keyword

## Common User-Code Mistakes Playbook

Use this playbook when failures are likely caused by project code rather than infrastructure.

### React Native (bundle and compile)

Common patterns and what to do first:

- **Unresolved import/module not found**
  - Verify path casing and extension.
  - Confirm the file exists and is exported correctly.
  - If dependency is external, verify it is in `package.json` and installed.
- **TypeScript parse/type error near one file**
  - Fix the first reported error first; many follow-up errors are cascading.
  - Check recently edited JSX/TS syntax (missing comma/brace/closing tag).
- **Runtime-only API used during bundling**
  - Guard platform-specific APIs and avoid using unavailable globals at module top-level.
- **Metro can't resolve asset or alias**
  - Confirm alias/path config and avoid dynamic import paths that Metro cannot statically analyze.

### Swift (build and archive)

Common patterns and what to do first:

- **`file.swift:<line>:<col>: error:` compiler failures**
  - Start with the first compiler error and ignore later cascades.
  - Check symbol names and access modifiers after recent refactors.
- **Type mismatch / optional handling errors**
  - Add explicit types where inference is ambiguous.
  - Handle optional unwrapping safely (`if let`, `guard let`) before use.
- **Missing target member or framework symbol**
  - Verify target membership and imports for moved/new files.
  - Confirm the symbol exists for the selected iOS version and gate with availability when needed.
- **Archive-specific failure after local compile success**
  - Check signing/capability assumptions in code paths used only in archive builds.

### Triage Order

1. Identify the earliest root error in the failing stack/log.
2. Normalize the error signature so repeated incidents can be grouped.
3. Apply the smallest safe code change to remove the root cause.
4. Re-run checks and only then move to the next group of errors.

### Notes

- File may not exist for new projects with no logs
- Contains last 10,000 logs from the live app (not sandbox builds)
- Logs are ordered oldest-first (chronological) - newest at end of file
