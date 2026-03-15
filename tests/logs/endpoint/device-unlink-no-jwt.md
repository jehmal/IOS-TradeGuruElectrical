# device-unlink-no-jwt

**Timestamp:** 2026-03-14T23:47:22.858247

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/device/unlink
- **Auth:** device

**Body:**
```json
{
  "device_id": "will-be-replaced"
}
```

## Response

- **Status Code:** 401 (expected: 401)
- **Duration:** 715.48ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 74
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:21 GMT
Etag: W/"4a-Ud6zkOkjRkEkKkMutp8CYXk7hls"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::9ht7p-1773503240733-fb1bffcacc49
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

Device-only auth cannot unlink. Requires WorkOS JWT.
