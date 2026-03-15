# API Test Summary

**Timestamp:** 2026-03-14T23:47:23.892646

- PASS: 18
- FAIL: 0
- ERROR: 0

## Results

- [✗] device-register - 429 (0ms)
- [✓] device-register-bad-platform - 400 (704.79ms)
- [✓] chat - 200 (4101.62ms)
- [✓] chat-bad-mode - 400 (522.32ms)
- [✓] chat-empty-messages - 400 (202.4ms)
- [✓] chat-no-auth - 401 (461.24ms)
- [✓] chat-invalid-device - 400 (223.81ms)
- [✓] chat-vision - 200 (2775.05ms)
- [✓] audio-transcribe-no-file - 400 (525.48ms)
- [✓] audio-speech - 200 (1892.58ms)
- [✓] files-upload-no-file - 400 (652.94ms)
- [✓] rating - 201 (2165.85ms)
- [✓] rating-invalid-stars - 400 (1153.18ms)
- [✓] feedback - 201 (2080.92ms)
- [✓] feedback-invalid-reason - 400 (1054.68ms)
- [✓] device-link-no-jwt - 401 (690.54ms)
- [✓] device-unlink-no-jwt - 401 (715.48ms)
- [✓] admin-stats-no-auth - 403 (1491.16ms)
- [✓] webhook-revenuecat-no-auth - 401 (592.28ms)
