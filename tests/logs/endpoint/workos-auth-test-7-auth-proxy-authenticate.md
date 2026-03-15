# Endpoint Test: workos-auth-test-7-auth-proxy-authenticate

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/workos-auth-proxy/user_management/authenticate
**Auth:** none (testing endpoint existence)
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -X POST https://tradeguru.com.au/api/workos-auth-proxy/user_management/authenticate \
  -H "Content-Type: application/json" \
  -d '{"grant_type":"authorization_code","client_id":"client_01JWQK8QD9RJVTCTMR8ACE9CKB","code":"invalid_code","code_verifier":"test_verifier"}'
```

## Response
**HTTP Status:** 400
**Content-Type:** application/json; charset=utf-8
```json
{"error":"invalid_grant","error_description":"Invalid code verifier."}
```

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | NOT 404 | 400 | PASS |
| Route exists | Yes | Yes | PASS |

## Notes
Auth proxy /authenticate endpoint exists and forwards to WorkOS. Returns 400 from WorkOS (invalid code), not 404 (route not found).
