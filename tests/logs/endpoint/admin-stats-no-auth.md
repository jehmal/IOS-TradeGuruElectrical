# admin-stats-no-auth

**Timestamp:** 2026-03-14T23:47:24.553306

**Status:** PASS

## Request

- **Method:** GET
- **Path:** /api/v1/admin/stats
- **Auth:** none

## Response

- **Status Code:** 403 (expected: 403)
- **Duration:** 1491.16ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: GET, DELETE, OPTIONS
Age: 0
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 72
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:22 GMT
Etag: W/"48-r5ABlumV90Fb39OXo7m9xEAcoKE"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::6tqln-1773503241611-3748e5b60b90
```

**Body:**
```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "Admin endpoint not configured"
  }
}
```

## Notes

Admin endpoint requires ADMIN_TOKEN + IP allowlist.
