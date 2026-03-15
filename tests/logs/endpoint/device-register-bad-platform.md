# device-register-bad-platform

**Timestamp:** 2026-03-14T23:47:00.542717

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/device/register
- **Auth:** none

**Body:**
```json
{
  "platform": "windows",
  "os_version": "10",
  "app_version": "1.0.0",
  "locale": "en-US",
  "timezone": "America/New_York"
}
```

## Response

- **Status Code:** 400 (expected: 400)
- **Duration:** 704.79ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 92
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:46:59 GMT
Etag: W/"5c-Kz25p3n7WRb3IQnWC7uWQdNWJOs"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::nqbnq-1773503219571-2811fd789fd5
```

**Body:**
```json
{
  "error": {
    "code": "INVALID_PLATFORM",
    "message": "Platform must be one of: ios, android, web"
  }
}
```
