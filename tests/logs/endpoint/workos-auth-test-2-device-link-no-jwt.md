# Endpoint Test: workos-auth-test-2-device-link-no-jwt

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/v1/device/link
**Auth:** device (without JWT)
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -X POST https://tradeguru.com.au/api/v1/device/link \
  -H "Content-Type: application/json" \
  -H "X-Device-ID: ec5da159-51ff-4f38-8e96-25e683b253c0" \
  -d '{"device_id":"ec5da159-51ff-4f38-8e96-25e683b253c0"}'
```

## Response
**HTTP Status:** 401
**Content-Type:** application/json; charset=utf-8
```json
{"error":{"code":"UNAUTHORIZED","message":"Missing authorization header"}}
```

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 401 | 401 | PASS |
| Error code | UNAUTHORIZED | UNAUTHORIZED | PASS |

## Notes
Correctly rejects device-only auth. JWT required for linking.
