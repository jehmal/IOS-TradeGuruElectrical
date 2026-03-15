# audio-transcribe-no-file

**Timestamp:** 2026-03-14T23:47:10.783152

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/audio/transcribe
- **Auth:** device

## Response

- **Status Code:** 400 (expected: 400)
- **Duration:** 525.48ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json
Date: Sat, 14 Mar 2026 15:47:09 GMT
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::syd1::6tqln-1773503229494-a7325dddbd1b
Transfer-Encoding: chunked
```

**Body:**
```json
{
  "error": {
    "code": "INVALID_CONTENT_TYPE",
    "message": "Content-Type must be multipart/form-data"
  }
}
```

## Notes

No multipart file provided.
