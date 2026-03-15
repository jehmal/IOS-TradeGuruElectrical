---
description: Test all v1 API endpoints against live Vercel deployment. Reads tests/endpoints.yaml, runs each test, logs results per endpoint, inspects Vercel logs on failure, loops until all pass.
---

# Test Endpoint

Test all TradeGuru v1 API endpoints against the live deployment.

## Variables

ENDPOINTS_FILE: tests/endpoints.yaml
LOG_DIR: tests/logs/endpoint
BASE_URL: https://tradeguru.com.au
VERCEL_PROJECT_DIR: /mnt/c/Users/jehma/Desktop/TradeGuru/expo-chatgpt-clone

## Instructions

### Step 1: Read Configuration

Read `$ENDPOINTS_FILE` to load all endpoint definitions.

### Step 2: Register Test Device

Before running tests, register a fresh test device:

```bash
curl -s -X POST $BASE_URL/api/v1/device/register \
  -H "Content-Type: application/json" \
  -d '{"platform":"ios","os_version":"18.0","app_version":"1.0.0","locale":"en-AU","timezone":"Australia/Sydney"}'
```

Extract the `device_id` from the response. This device_id is used for all tests with `auth: device`.

If registration fails, STOP and report the error. All subsequent tests depend on a valid device.

### Step 3: Run All Endpoint Tests

For EACH endpoint in the YAML file, execute the test:

**Build the curl command based on endpoint config:**

- `auth: none` — No auth headers
- `auth: device` — Add `-H "X-Device-ID: {device_id}"` and inject `device_id` into body if body has `device_id` field
- `auth: device_override` — Use the `device_id` field from the endpoint config (for testing invalid IDs)
- `method` — Use `-X {method}`
- `body` — If not null, serialize as JSON with `-d '{...}'`
- `content_type` — Default `application/json` unless specified otherwise

**For SSE streaming endpoints** (`expect.content_type: text/event-stream`):
- Use `curl -N --max-time 30` to capture the stream
- Look for `event: block` and `event: done` in output
- If `event: error` appears, capture the error data

**Check the response against expectations:**
- `expect.status` — HTTP status code must match
- `expect.body_contains` — Response body must contain this string
- `expect.content_type` — Response Content-Type header must match
- `expect.content_type_contains` — Content-Type must contain this substring
- `expect.sse_events` — For SSE, these event types must appear

**Record result as PASS or FAIL.**

### Step 4: Write Individual Log Files

For EACH endpoint test, write a markdown log file at `$LOG_DIR/{slug}.md`:

```markdown
# Endpoint Test: {slug}

**Tested:** {ISO timestamp}
**URL:** {method} {base_url}{path}
**Auth:** {auth type}
**Status:** PASS / FAIL

## Request

```bash
{exact curl command used}
```

## Response

**HTTP Status:** {status code}
**Content-Type:** {content-type header}

```
{response body - first 2000 chars}
```

## Expectations

| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | {expected} | {actual} | PASS/FAIL |
| Body contains | {expected} | {found?} | PASS/FAIL |

## Vercel Logs (if FAIL)

```
{vercel log output for this endpoint}
```

## Diagnosis (if FAIL)

{AI analysis of what went wrong and suggested fix}
```

### Step 5: Inspect Vercel Logs on Failure

For ANY test that FAILs with a 500 error:

```bash
cd $VERCEL_PROJECT_DIR && npx vercel logs --limit 20 2>&1
```

Capture the relevant log entries and include them in the endpoint's log file.

Then READ the source file for the failing endpoint to diagnose the issue.

### Step 6: Fix and Re-test Loop

If ANY test FAILs:

1. Analyze the failure using Vercel logs + source code
2. Identify the root cause
3. Fix the source file in the Expo project at $VERCEL_PROJECT_DIR
4. Before saving Edge Runtime files, check for Node.js-only APIs (Buffer, require('crypto'), fs, path, etc.)
5. Run `cd $VERCEL_PROJECT_DIR && npx tsc --noEmit` to verify compilation
6. Commit and push the fix: `cd $VERCEL_PROJECT_DIR && git add -A && git commit -m "fix: {description}" && git push origin main`
7. Wait 30-60 seconds for Vercel to deploy
8. Re-run ONLY the failing tests
9. Repeat until all tests PASS

**Maximum iterations: 10.** If tests still fail after 10 fix cycles, stop and report all remaining failures with full diagnostics.

### Step 7: Final Summary

After all tests pass (or max iterations reached), print a summary table:

```
============================================================
 ENDPOINT TEST RESULTS
============================================================

 Total: {N} endpoints
 Passed: {P}
 Failed: {F}
 Fix iterations: {I}

 Results:
 {PASS/FAIL icon} {slug} — {status code} {notes}
 ...

 Log files: tests/logs/endpoint/*.md
============================================================
```

## Important Rules

- ALWAYS register a fresh device before testing — don't reuse stale device IDs
- For streaming endpoints (chat, vision), use `--max-time 30` to prevent hanging
- Include `-s` (silent) flag on curl for cleaner output parsing
- Use `-w "\n%{http_code}"` to reliably extract HTTP status codes
- Never modify existing endpoints (assistants-*, stripe-*, user-*) while fixing v1 bugs
- After fixing Edge Runtime files, always verify no Node.js-only APIs are used (Buffer, require('crypto'), fs, path, __dirname)
- Log files are append-safe — each run overwrites the slug's file with latest results
