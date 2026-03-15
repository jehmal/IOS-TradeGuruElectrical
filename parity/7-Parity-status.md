# Parity Status Report #7

**Project:** tradeguruelectrical
**Date:** 2026-03-15
**Screen:** Chat (full screen)
**Overall parity:** 98% (PASS)
**Behaviours audited:** 65
**Matched:** 64
**Fixed this session:** 3 (voice record, transcribe, TTS)
**Remaining gaps:** 1 (files/upload — API exists but no UI on either platform)

Thresholds: PASS >= 95% | WARN >= 80% | FAIL < 80%

---

## Fixes Applied This Run

| # | Gap | What Was Wrong | What Was Fixed | File | Line |
|---|-----|---------------|----------------|------|------|
| 1 | Voice recording was visual-only in HTML | `toggleRecording()` only toggled icon SVG, no actual mic capture | Replaced with real `MediaRecorder` implementation: requests mic permission, records audio, calls `transcribeAudioBlob()` on stop | preview/chat.html | ~1455-1491 |
| 2 | Audio transcription missing in HTML | No function to send audio to `/audio/transcribe` | Added `transcribeAudioBlob()`: sends multipart FormData to API, inserts transcribed text into input field, syncs `htmlInputText` | preview/chat.html | ~1493-1523 |
| 3 | Text-to-speech was no-op in both Swift and HTML | Swift `onSpeak: { _ in }` was empty closure; HTML speaker icon only changed color | Swift: wired `onSpeak` to `viewModel.speakText()` → `TradeGuruAPI.speak()` → `AVAudioPlayer`. HTML: added `apiSpeak()` → `POST /audio/speech` → `new Audio().play()` | ChatView.swift:48, ChatViewModel.swift:309, chat.html:~1788 |

---

## Full Behaviour Matrix

| # | Category | Behaviour | Status |
|---|----------|-----------|--------|
| 1 | NavBar | Hamburger opens sidebar | MATCH |
| 2 | NavBar | New chat button resets state | MATCH |
| 3 | Mode Selector | Pill tap updates mode | MATCH |
| 4 | Mode Selector | Mode change shows info card | MATCH |
| 5 | Mode Card | Card tap dismisses | MATCH |
| 6 | Mode Card | X button dismisses | MATCH |
| 7 | Input | Focus dismisses mode card | MATCH |
| 8 | Input | Text change dismisses mode card | MATCH |
| 9 | Input | Send/mic/stop toggle logic | MATCH |
| 10 | Input | Enter key submits | MATCH |
| 11 | Input | Textarea auto-grow (5 lines max) | MATCH |
| 12 | Input | Text preserved across render | MATCH |
| 13 | Attachment | Plus button picks image | MATCH |
| 14 | Attachment | Plus rotates 45 deg when active | MATCH |
| 15 | Attachment | Thumbnail 40x40 preview | MATCH |
| 16 | Attachment | Clear resets all state | MATCH |
| 17 | Send | Text-only calls apiChat | MATCH |
| 18 | Send | Image calls apiChatVision | MATCH |
| 19 | Send | Clears input and attachment | MATCH |
| 20 | Send | Creates conversation if none | MATCH |
| 21 | Send | Auto-title from first msg (40 chars) | MATCH |
| 22 | Streaming | Typing indicator (3 pulsing dots) | MATCH |
| 23 | Streaming | Blocks rendered as they arrive | MATCH |
| 24 | Streaming | Auto-scroll on new content | MATCH |
| 25 | Streaming | Pipeline status stages | MATCH |
| 26 | Streaming | Done finalizes message | MATCH |
| 27 | Streaming | Error preserves partial blocks | MATCH |
| 28 | Messages | User bubble green 280px max | MATCH |
| 29 | Messages | Assistant bubble 330px max | MATCH |
| 30 | Messages | Timestamp below message | MATCH |
| 31 | Messages | Mode icon next to timestamp | MATCH |
| 32 | Actions | 5-star rating → POST /rating | MATCH |
| 33 | Actions | Flag button → POST /feedback | MATCH |
| 34 | Actions | Speak button → POST /audio/speech → play | MATCH |
| 35 | Sidebar | Dim overlay closes | MATCH |
| 36 | Sidebar | X button closes | MATCH |
| 37 | Sidebar | Row shows icon+title+date+count | MATCH |
| 38 | Sidebar | Select conversation | MATCH |
| 39 | Sidebar | New conversation button | MATCH |
| 40 | Error | Error bar with dismiss | MATCH |
| 41 | Blocks | text | MATCH |
| 42 | Blocks | heading h1/h2/h3 | MATCH |
| 43 | Blocks | stepList / step_list | MATCH |
| 44 | Blocks | warning | MATCH |
| 45 | Blocks | code with copy button | MATCH |
| 46 | Blocks | partsList grid | MATCH |
| 47 | Blocks | regulation | MATCH |
| 48 | Blocks | table | MATCH |
| 49 | Blocks | callout (tip/info/important) | MATCH |
| 50 | Blocks | diagramRef | MATCH |
| 51 | Blocks | toolCall | MATCH |
| 52 | Blocks | link | MATCH |
| 53 | Voice | Mic start recording (MediaRecorder) | MATCH |
| 54 | Voice | Stop → transcribe → insert text | MATCH |
| 55 | Device | Auto-register on init | MATCH |
| 56 | API | POST /device/register | MATCH |
| 57 | API | POST /chat (SSE) | MATCH |
| 58 | API | POST /chat/vision (SSE) | MATCH |
| 59 | API | POST /audio/transcribe | MATCH |
| 60 | API | POST /audio/speech | MATCH |
| 61 | API | POST /files/upload | N/A (no UI on either platform) |
| 62 | API | POST /rating | MATCH |
| 63 | API | POST /feedback | MATCH |
| 64 | Vision | Partial error block preservation | MATCH |
| 65 | Vision | conv.count update on done | MATCH |

---

## API Endpoint Coverage

| Endpoint | Swift Method | HTML Function | Wired |
|----------|-------------|---------------|-------|
| POST /device/register | TradeGuruAPI.registerDevice() | ensureDevice() | YES |
| POST /chat | TradeGuruAPI.chat() | apiChat() | YES |
| POST /chat/vision | TradeGuruAPI.chatVision() | apiChatVision() | YES |
| POST /audio/transcribe | TradeGuruAPI.transcribe() | transcribeAudioBlob() | YES |
| POST /audio/speech | TradeGuruAPI.speak() | apiSpeak() | YES |
| POST /files/upload | TradeGuruAPI.uploadFile() | — | NO (no UI caller on either platform) |
| POST /rating | TradeGuruAPI.rate() | apiRate() | YES |
| POST /feedback | TradeGuruAPI.feedback() | apiFlag() | YES |

---

## Remaining Gaps

| # | Gap | Reason Not Fixed | Workaround |
|---|-----|-----------------|------------|
| 1 | POST /files/upload | API method exists in TradeGuruAPI.swift but is never called from any View or ViewModel. No UI trigger on either platform. | Will be wired when document upload feature is built. |

---

## Parity History

| Report # | Date | Overall | Audited | Matched | Fixed | Gaps |
|----------|------|---------|---------|---------|-------|------|
| 6 | 2026-03-15 | 95% PASS | 65 | 60 | 5 | 3 (browser) |
| 7 | 2026-03-15 | 98% PASS | 65 | 64 | 3 | 1 (no UI) |
