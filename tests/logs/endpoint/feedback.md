# feedback

**Timestamp:** 2026-03-14T23:47:19.785305

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/feedback
- **Auth:** device

**Body:**
```json
{
  "response_id": "resp_1710000000_a1b2c3d4",
  "reason": "wrong",
  "mode": "fault_finder"
}
```

## Response

- **Status Code:** 201 (expected: 201)
- **Duration:** 2080.92ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 80
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:18 GMT
Etag: W/"50-aWKKxndxTqBotNn+W3xb+i1uMvw"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::nqbnq-1773503236519-450d3fd7323b
```

**Body:**
```json
{
  "success": true,
  "feedback_id": "feedback:resp_1710000000_a1b2c3d4:1773503237621"
}
```
