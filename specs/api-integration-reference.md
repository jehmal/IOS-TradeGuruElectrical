# TradeGuru v1 API — Integration Reference

Base URL: `https://tradeguru.com/api/v1`

---

## Authentication (two methods)

1. **WorkOS JWT** (web users, full tier access)
   `Authorization: Bearer <workos_jwt_token>`

2. **Device ID** (iOS/Android, Free tier unless linked)
   `X-Device-ID: <uuid-v4>`

---

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/device/register` | Register anonymous device |
| POST | `/device/link` | Link device to WorkOS user |
| POST | `/device/unlink` | Unlink device |
| POST | `/chat` | AI chat (SSE streaming) |
| POST | `/chat/vision` | Photo analysis (SSE streaming) |
| POST | `/audio/transcribe` | Speech-to-text |
| POST | `/audio/speech` | Text-to-speech |
| POST | `/files/upload` | File upload |
| POST | `/rating` | Rate a response |
| POST | `/feedback` | Flag a response |

---

## Modes

`fault_finder` | `learn` | `research`

Swift enum uses camelCase (`faultFinder`), API uses snake_case (`fault_finder`). Convert at the API boundary.

---

## SSE Event Types

| Event | Data | When |
|-------|------|------|
| `block` | `ContentBlock` JSON | Each content block streamed |
| `done` | `{ response_id, usage, cached, category }` | Stream complete |
| `error` | `{ code, message, partial }` | Error mid-stream |

---

## Tier Limits (daily)

| Tier | Chat | Vision | Audio | Files | Max File |
|------|------|--------|-------|-------|----------|
| Free | 10 | 3 | 5 | 2 | 5 MB |
| Pro | 100 | 30 | 50 | 20 | 25 MB |
| Unlimited | 1000 | 200 | 500 | 100 | 50 MB |
