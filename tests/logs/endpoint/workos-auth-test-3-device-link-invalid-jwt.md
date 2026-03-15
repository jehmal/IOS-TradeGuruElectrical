# Endpoint Test: workos-auth-test-3-device-link-invalid-jwt

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/v1/device/link
**Auth:** invalid JWT
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -X POST https://tradeguru.com.au/api/v1/device/link \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-jwt-token" \
  -H "X-Device-ID: ec5da159-51ff-4f38-8e96-25e683b253c0" \
  -d '{"device_id":"ec5da159-51ff-4f38-8e96-25e683b253c0"}'
```

## Response
**HTTP Status:** 401
**Content-Type:** application/json; charset=utf-8
```json
{"error":{"code":"INVALID_TOKEN","message":"Invalid or expired token"}}
```

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 401 | 401 | PASS |
| Error code | INVALID_TOKEN | INVALID_TOKEN | PASS |

## Notes
Correctly validates JWT and rejects invalid tokens.
