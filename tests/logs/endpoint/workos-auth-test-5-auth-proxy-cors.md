# Endpoint Test: workos-auth-test-5-auth-proxy-cors

**Tested:** 2026-03-15T00:00:00Z
**URL:** OPTIONS https://tradeguru.com.au/api/workos-auth-proxy/user_management/authorize
**Auth:** none (CORS preflight)
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -X OPTIONS https://tradeguru.com.au/api/workos-auth-proxy/user_management/authorize \
  -H "Origin: https://tradeguru.com.au" \
  -H "Access-Control-Request-Method: POST"
```

## Response
**HTTP Status:** 200
**Content-Type:** (empty)

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 200 or 204 | 200 | PASS |

## Notes
CORS preflight successful. Auth proxy handles OPTIONS correctly.
