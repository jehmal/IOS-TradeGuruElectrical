# Endpoint Test: workos-auth-test-6-auth-proxy-path-traversal

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/workos-auth-proxy/../../etc/passwd
**Auth:** none
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -X POST https://tradeguru.com.au/api/workos-auth-proxy/../../etc/passwd \
  -H "Content-Type: application/json"
```

## Response
**HTTP Status:** 405
**Content-Type:** text/html; charset=utf-8

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 400 or 403 or 405 | 405 | PASS |

## Notes
Path traversal attempt blocked. Returns 405 Method Not Allowed (path normalization handled by Next.js routing).
