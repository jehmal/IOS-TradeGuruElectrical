# feedback-invalid-reason

**Timestamp:** 2026-03-14T23:47:21.043624

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/feedback
- **Auth:** device

**Body:**
```json
{
  "response_id": "resp_1710000000_a1b2c3d4",
  "reason": "garbage",
  "mode": "fault_finder"
}
```

## Response

- **Status Code:** 400 (expected: 400)
- **Duration:** 1054.68ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 106
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:19 GMT
Etag: W/"6a-Ni+Hsr1Qe1/AnnotZuz8Mf4BjzY"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::8hc2n-1773503238690-af754973fac1
```

**Body:**
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "reason must be one of: wrong, unsafe, unhelpful, outdated"
  }
}
```
