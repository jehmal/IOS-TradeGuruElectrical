# webhook-revenuecat-no-auth

**Timestamp:** 2026-03-14T23:47:23.687307

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/webhook/revenuecat
- **Auth:** none

**Body:**
```json
{
  "api_version": "1.0",
  "event": {
    "type": "TEST",
    "app_user_id": "test_user"
  }
}
```

## Response

- **Status Code:** 401 (expected: 401)
- **Duration:** 592.28ms

**Headers:**
```
Cache-Control: public, max-age=0, must-revalidate
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json
Date: Sat, 14 Mar 2026 15:47:23 GMT
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::syd1::6tqln-1773503243223-228f91ac9dee
Transfer-Encoding: chunked
```

**Body:**
```json
{
  "error": "Unauthorized"
}
```

## Notes

RevenueCat webhook requires auth header.
