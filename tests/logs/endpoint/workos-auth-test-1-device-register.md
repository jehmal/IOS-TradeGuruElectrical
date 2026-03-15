# Endpoint Test: workos-auth-test-1-device-register

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/v1/device/register
**Auth:** none
**Status:** PASS

## Request
```bash
curl -s -X POST https://tradeguru.com.au/api/v1/device/register \
  -H "Content-Type: application/json" \
  -d '{"platform":"ios","os_version":"18.0","app_version":"1.0.0","locale":"en-AU","timezone":"Australia/Sydney"}' \
  -w "\n%{http_code}\n%{content_type}"
```

## Response
**HTTP Status:** 201
**Content-Type:** application/json; charset=utf-8
```json
{"device_id":"ec5da159-51ff-4f38-8e96-25e683b253c0"}
```

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 201 | 201 | PASS |
| Body contains device_id | Yes | Yes | PASS |

## Notes
Device registration successful. Device ID extracted for subsequent tests.
