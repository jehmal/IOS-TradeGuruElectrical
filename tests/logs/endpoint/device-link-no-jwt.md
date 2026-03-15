# device-link-no-jwt

**Timestamp:** 2026-03-14T23:47:21.937822

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/device/link
- **Auth:** device

**Body:**
```json
{
  "device_id": "will-be-replaced"
}
```

## Response

- **Status Code:** 401 (expected: 401)
- **Duration:** 690.54ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 74
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:20 GMT
Etag: W/"4a-Ud6zkOkjRkEkKkMutp8CYXk7hls"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::nzn5m-1773503239883-383b852023e7
```

**Body:**
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Missing authorization header"
  }
}
```

## Notes

Device-only auth cannot link. Requires WorkOS JWT.
