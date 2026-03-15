# device-register

**Timestamp:** 2026-03-14T23:46:59.634577

**Status:** SKIP

## Request

- **Method:** POST
- **Path:** /api/v1/device/register
- **Auth:** none

**Body:**
```json
{
  "platform": "ios",
  "os_version": "18.0",
  "app_version": "1.0.0",
  "locale": "en-AU",
  "timezone": "Australia/Sydney"
}
```

## Response

- **Status Code:** 429 (expected: 201)
- **Duration:** 0ms

**Body:**
```json
Skipped due to rate limit (expected behavior)
```

## Notes

Returns UUID v4 device_id. Store for subsequent tests.
