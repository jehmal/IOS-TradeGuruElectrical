# chat-vision

**Timestamp:** 2026-03-15T12:00:00Z

**Status:** PASS

## Request

- **Method:** POST
- **Path:** /api/v1/chat/vision
- **Auth:** device

**Body:**
```json
{
  "message": "What cable is this?",
  "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
  "mode": "fault_finder",
  "platform": "web_preview"
}
```

## Response

- **Status Code:** 200 (expected: 200)
- **Content-Type:** text/event-stream

**Body:**
```
event: status
data: {"stage":"searching"}

event: status
data: {"stage":"synthesizing"}

event: status
data: {"stage":"streaming"}

event: block
data: {"type":"text","content":"I can't identify cables or objects in images..."}

event: done
data: {"response_id":"resp_0019d363ea1cf0290069b6095d985c8195b2448259a6d20453","usage":{"input_tokens":4336,"output_tokens":40},"cached":false}
```

## Fixes Applied (this session)

1. Backend `vision.ts`: `type: 'text'` → `type: 'input_text'`, `type: 'image_url'` → `type: 'input_image'` (Responses API format)
2. Backend `vision.ts`: Added `web_preview` to allowed platform values
3. HTML `chat.html`: Changed from `{ messages: [...] }` to `{ message, image }` flat format
4. Swift `TradeGuruAPI.swift` + `ChatViewModel.swift`: Same flat format fix for iOS app

## Notes

Vision endpoint with minimal 1x1 test image. Previous failure was due to Chat Completions content type format being used with Responses API.
