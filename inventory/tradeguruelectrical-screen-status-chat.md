# Screen Status: chat

**Project:** tradeguruelectrical
**Last checked:** 2026-03-14
**Screenshot:** `~/.claude/util/temp/screenshots/design-status_claude_20260314_130704.png`

---

## Screen Overview

The main chat screen for TradeGuruElectrical — an AI-powered electrical fault-finding assistant. Features a mode selector (Fault Find / Learn / Research), mode info card, centered logo watermark, and chat input bar. Currently frontend-only with no backend or message handling wired up.

| Property | Value |
|----------|-------|
| Screen name | Chat |
| Screen slug | `chat` |
| Primary SwiftUI file | `ios/Tradeguruelectrical/ChatView.swift` |
| View model | (not created) |
| Models/Enums | `ios/Tradeguruelectrical/ThinkingMode.swift` |
| Subcomponents | 4 files (ModeSelector, ModeInfoCard, ChatInputBar, TradeGuruColors) |
| Completion | 70% (frontend layout done, no messages/history/backend) |

---

## Connected HTML

| Property | Value |
|----------|-------|
| Preview file | `preview/chat.html` |
| Interactive | YES (mode selector cycles through modes, updates card text) |
| In sync with Swift | PARTIAL |
| Last modified | 2026-03-14 |
| Base64 assets embedded | YES (TradeGuru logo) |

### Sync Diff

| Element | SwiftUI | HTML | Match |
|---------|---------|------|-------|
| Status bar | not rendered (system) | implemented (mock) | N/A |
| Profile avatar | implemented | implemented | YES |
| Mode selector pill | implemented | implemented | YES |
| History button | implemented | implemented | YES |
| New chat button | implemented | implemented | YES |
| Mode info card | implemented | implemented | YES |
| Logo watermark | implemented | implemented | YES |
| Chat input field | implemented | implemented | YES |
| Attachment button | implemented | implemented | YES |
| Speak button | implemented | implemented | YES |
| Message bubbles | missing | missing | N/A |
| Chat history sheet | missing | missing | N/A |

---

## Inventory

### Implemented

| # | Component | Type | Swift File | HTML | Notes |
|---|-----------|------|-----------|------|-------|
| 1 | ChatView | view | `ios/Tradeguruelectrical/ChatView.swift` | YES | Main screen, owns @State for mode + input |
| 2 | ModeSelector | view | `ios/Tradeguruelectrical/ModeSelector.swift` | YES | Tappable pill with chevron, cycles modes |
| 3 | ModeInfoCard | view | `ios/Tradeguruelectrical/ModeInfoCard.swift` | YES | Shows mode icon + title + description |
| 4 | ChatInputBar | view | `ios/Tradeguruelectrical/ChatInputBar.swift` | YES | Text field + attachment + speak button |
| 5 | ThinkingMode | enum | `ios/Tradeguruelectrical/ThinkingMode.swift` | YES | 3 modes: faultFinder, learn, research. nonisolated |
| 6 | TradeGuruColors | utility | `ios/Tradeguruelectrical/TradeGuruColors.swift` | YES | Color tokens |
| 7 | ContentView | view | `ios/Tradeguruelectrical/ContentView.swift` | N/A | Root wrapper, shows ChatView |
| 8 | TradeguruelectricalApp | app | `ios/Tradeguruelectrical/TradeguruelectricalApp.swift` | N/A | @main entry point |
| 9 | Profile avatar | icon | in ChatView | YES | SF Symbol placeholder person.crop.circle.fill |
| 10 | History button | button | in ChatView | YES | clock.arrow.circlepath |
| 11 | New chat button | button | in ChatView | YES | square.and.pencil |
| 12 | Logo watermark | image | in ChatView | YES | TradeGuru logo, low opacity centered |

### Missing (in design but not in code)

| # | Component | Type | Seen In | Priority | Notes |
|---|-----------|------|---------|----------|-------|
| 1 | Message bubbles | view | mock analysis | high | No ChatMessage model or bubble views exist |
| 2 | Chat history sheet | sheet | user request | high | History button exists but tapping does nothing |
| 3 | ChatViewModel | viewmodel | — | high | No @Observable view model for message state, API calls |
| 4 | ChatMessage model | model | — | high | No struct for representing chat messages |
| 5 | Streaming response | view | — | med | No typing indicator or streaming text display |
| 6 | Photo attachment handling | logic | — | med | PhotosPicker in ChatInputBar but no processing |
| 7 | Voice input | logic | — | low | Speak button exists but no audio recording wired |

### Extra (in code but not in current design)

| # | Component | Type | Swift File | Notes |
|---|-----------|------|-----------|-------|
| — | (none) | — | — | All code maps to visible design elements |

---

## Backend Connection Status

| # | Component | Data Source | Status | Details |
|---|-----------|-----------|--------|---------|
| 1 | ChatInputBar | AI chat API | missing | No API call on send — text input goes nowhere |
| 2 | ModeSelector | local state | not-needed | Mode stored in @State, no persistence needed |
| 3 | ModeInfoCard | ThinkingMode enum | not-needed | Reads hardcoded descriptions from enum |
| 4 | Profile avatar | user profile API | missing | Placeholder icon, no user data |
| 5 | Chat history | local storage / API | missing | No message persistence or history fetch |
| 6 | Message streaming | AI chat API (SSE/WebSocket) | missing | No streaming response handling |
| 7 | Photo attachment | image upload API | missing | PhotosPicker selected but not processed |
| 8 | Voice input | speech-to-text API | missing | Speak button is visual only |

### Environment Variables Required

| Variable | Used By | Status |
|----------|---------|--------|
| `OPENAI_API_KEY` | Chat API calls | missing |
| `IMAGE_GEN_MODEL` | Image tools (CLI) | missing |

### API Endpoints Referenced

| Endpoint | Method | Used By | Status |
|----------|--------|---------|--------|
| (none) | — | — | No API endpoints referenced in Swift code |

---

## StockTake

| Date | Action | Implemented | Missing | Sync | Completion |
|------|--------|------------|---------|------|------------|
| 2026-03-14 | initial scan | 12 | 7 | 83% | 70% |
