# chat-no-auth

**Timestamp:** 2026-03-14T23:47:06.646225

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/chat
- **Auth:** none

**Body:**
```json
{
  "messages": [
    {
      "role": "user",
      "content": "test"
    }
  ],
  "mode": "fault_finder",
  "platform": "ios"
}
```

## Response

- **Status Code:** 401 (expected: 401)
- **Duration:** 461.24ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json
Date: Sat, 14 Mar 2026 15:47:05 GMT
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::syd1::nqbnq-1773503225588-8efbc4c4a82f
Transfer-Encoding: chunked
```

**Body:**
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required: provide Authorization header or X-Device-ID"
  }
}
```
