# rating

**Timestamp:** 2026-03-14T23:47:16.144133

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/rating
- **Auth:** device

**Body:**
```json
{
  "response_id": "resp_1710000000_a1b2c3d4",
  "stars": 5,
  "mode": "fault_finder"
}
```

## Response

- **Status Code:** 201 (expected: 201)
- **Duration:** 2165.85ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: public, max-age=0, must-revalidate
Content-Length: 76
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: application/json; charset=utf-8
Date: Sat, 14 Mar 2026 15:47:14 GMT
Etag: W/"4c-i/XM6ry8rBxYB/+B6DhfBVGHxPE"
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::iad1::8fpft-1773503233034-c2b735adc6dd
```

**Body:**
```json
{
  "success": true,
  "rating_id": "rating:resp_1710000000_a1b2c3d4:1773503234163"
}
```

## Notes

Submit a 5-star rating.
