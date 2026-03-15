# chat

**Timestamp:** 2026-03-14T23:47:04.847739

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/chat
- **Auth:** device

**Body:**
```json
{
  "messages": [
    {
      "role": "user",
      "content": "Why is my RCD tripping?"
    }
  ],
  "mode": "fault_finder",
  "platform": "ios"
}
```

## Response

- **Status Code:** 200 (expected: 200)
- **Duration:** 4101.62ms

**Headers:**
```
Access-Control-Allow-Headers: Content-Type, Authorization, X-Device-ID, X-Idempotency-Key
Access-Control-Allow-Methods: POST, OPTIONS
Cache-Control: no-cache
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' https://js.stripe.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' blob: https://api.openai.com https://api.workos.com https://api.stripe.com https://*.upstash.io; frame-src https://js.stripe.com https://hooks.stripe.com;
Content-Type: text/event-stream
Date: Sat, 14 Mar 2026 15:47:01 GMT
Referrer-Policy: strict-origin-when-cross-origin
Server: Vercel
Strict-Transport-Security: max-age=63072000
Vary: Origin
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vercel-Cache: MISS
X-Vercel-Id: syd1::syd1::nqbnq-1773503220434-8828e9bfad11
Transfer-Encoding: chunked
```

**Body:**
```json
event: status
data: {"stage":"searching"}

event: status
data: {"stage":"synthesizing"}

event: error
data: {"error":"Response input messages must contain the word 'json' in some form to use 'text.format' of type 'json_object'."}


```

## Notes

Primary streaming endpoint. Expect SSE blocks then done event.
