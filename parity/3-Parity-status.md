# Parity Status Report #3

**Project:** tradeguruelectrical
**Date:** 2026-03-14
**Overall parity:** 88% (WARN)

Thresholds: PASS >= 95% | WARN >= 80% | FAIL < 80%

---

## Screen Summary

| Screen | Swift File | HTML File | Parity | Status |
|--------|-----------|-----------|--------|--------|
| Chat | `ios/Tradeguruelectrical/ChatView.swift` | `preview/chat.html` | 88% | WARN |

---

## Chat — Detailed Parity

### Matching (in parity)

| # | Element | Type | Swift | HTML | Notes |
|---|---------|------|-------|------|-------|
| 1 | Nav bar layout | layout | `ChatView.swift:269-293` | `htmlNavBar()` ~1096 | Identical — HStack with hamburger + compose |
| 2 | Hamburger button | button | `ChatView.swift:271` | ~1098 | Both open sidebar |
| 3 | New chat button | button | `ChatView.swift:284` | ~1101 | Both wired — Swift calls ViewModel, HTML calls `newChat()` |
| 4 | Mode info card | component | `ModeInfoCard.swift` | `htmlModeCard()` ~1055 | Layout, colors, dismiss all match |
| 5 | Mode selector (3 tabs) | component | `ModeSelector.swift` | `htmlModeSelector()` ~1068 | 3 pills, icon+name+desc, active highlight match |
| 6 | Text field | input | `ChatInputBar.swift:79` | ~1087 | Placeholder "Ask TradeGuru", dismiss card on focus, toggle send |
| 7 | Send button show/hide | interaction | `ChatInputBar.swift:95` | ~1049 | Both toggle visibility based on input content |
| 8 | Send button action | button | `ChatInputBar.swift:96` | ~1089 | Both wired — Swift sends to API, HTML appends to mock array |
| 9 | Plus/attachment button | button | `ChatInputBar.swift:55` | ~1085 | Both show "+" icon. Swift has PhotosPicker, HTML is visual |
| 10 | User message bubble | component | `MessageBubble.swift:39-52` | `htmlMessage()` ~1201 | Green bg, white text, right-aligned, 280/16px max, 16px radius |
| 11 | Assistant message bubble | component | `MessageBubble.swift:54-64` | `htmlMessage()` ~1215 | Surface bg, left-aligned, 330px max, 14px padding, 16px radius |
| 12 | Timestamp meta | text | `MessageBubble.swift:27` | ~1205/1219 | 11px, secondary color |
| 13 | Mode icon in meta | icon | `MessageBubble.swift:22` | ~1218 | Mode-colored icon before timestamp on assistant messages |
| 14 | TextBlockView | block | `TextBlockView.swift` | `htmlBlock()` text | 15px, tradeText color |
| 15 | StepListView | block | `StepListView.swift` | `htmlBlock()` stepList | Title, numbered circles (22px green), step text 14px |
| 16 | WarningCardView | block | `WarningCardView.swift` | `htmlBlock()` warning | Yellow/amber border, warning icon, title+text |
| 17 | CodeBlockView | block | `CodeBlockView.swift` | `htmlBlock()` code | Monospace 13px, surface bg, language label, copy button |
| 18 | PartsListView | block | `PartsListView.swift` | `htmlBlock()` partsList | 3-column grid, header row, alternating bg |
| 19 | RegulationView | block | `RegulationView.swift` | `htmlBlock()` regulation | Purple left border, code+clause+summary |
| 20 | TableBlockView | block | `TableBlockView.swift` | `htmlBlock()` table | Horizontal scroll, header bg, 13px, border |
| 21 | CalloutView | block | `CalloutView.swift` | `htmlBlock()` callout | Left border colored by style, icon+text |
| 22 | DiagramRef block | block | `MessageBubble.swift:137-141` | ~1187 | Italic, secondary color, "Diagram:" prefix |
| 23 | ToolCall block | block | `MessageBubble.swift:143-147` | ~1190 | Italic, secondary color, "Tool:" prefix |
| 24 | Link block | block | `MessageBubble.swift:149-159` | ~1193 | modeLearn color, 14px. Swift uses `Link`, HTML uses `<a>` |
| 25 | Sidebar overlay | component | `SidebarView.swift:10-19` | `htmlSidebar()` ~1107 | Dim bg, 300px panel, slide from left |
| 26 | Sidebar header | layout | `SidebarView.swift:59-76` | ~1111 | "Conversations" title + xmark close |
| 27 | Sidebar rows | component | `SidebarView.swift:78-105` | ~1116 | Mode icon, title, date, message count |
| 28 | Sidebar new btn | button | `SidebarView.swift:44-54` | ~1130 | Green bg, "New Conversation" text, both wired |
| 29 | Sidebar selection | interaction | `ChatView.swift:147` | ~1119 | Both load conversation — Swift via ViewModel, HTML via index |
| 30 | Empty state watermark | component | `ChatView.swift:78-84` | ~1239 | 180x180 logo, 0.08 opacity |
| 31 | Dark mode colors | styling | `TradeGuruColors.swift` | CSS variables | All 11 tokens match between Swift and CSS |
| 32 | Mock mode toggle | control | `APIConfig.swift:4` | ~842 | Both have boolean flag, HTML has button in controls bar |
| 33 | Rating stars (last msg) | component | `MessageBubble.swift:66-78` | ~1210 | Both show 5 stars on last assistant message, 14px |
| 34 | Flag button | component | `MessageBubble.swift:80-87` | ~1212 | Both show flag icon next to stars |
| 35 | Speaker button | component | `MessageBubble.swift:89-99` | ~1213 | Both show speaker icon in action row |
| 36 | Code copy button | component | `CodeBlockView.swift:21-38` | ~1161 | Both have copy button, Swift shows "Copied" toast |
| 37 | Component picker | control | N/A | ~823-838 | 14 options covering all Swift components |

### Mismatched (out of parity)

| # | Element | Issue | Swift Value | HTML Value | Fix |
|---|---------|-------|-------------|------------|-----|
| 1 | Mic/voice button | Swift has mic button when input empty, HTML does not | `mic.fill` 28pt green | (absent) | Add mic SVG button in `htmlInputBar()` when input empty |
| 2 | Recording state | Swift has red stop button when recording | Red circle + `stop.fill` | (absent) | Add recording indicator in HTML |
| 3 | Photo thumbnail preview | Swift shows 40x40 thumbnail when photo selected | 40x40 clipShape rounded | (absent) | Add simulated thumbnail in HTML attachment state |
| 4 | Error banner | Swift shows red dismissible banner above input | Red bg 0.1 opacity, xmark | (absent) | Add error banner div in `renderFullChat()` |
| 5 | Streaming bubble | Swift shows progressive blocks during streaming | tradeSurface bg, 330px max | (absent) | Add streaming placeholder in HTML (mock mode shows data instantly) |
| 6 | Typing indicator | Swift shows 3 pulsing dots while waiting | 8px circles, 0.4 opacity | (absent) | Add CSS pulsing dots animation |
| 7 | Star rating interaction | Swift stars turn green on tap permanently | `star.fill` tradeGreen | Inline style change only | Minor — HTML uses inline onclick, Swift uses @State. Functionally similar. |
| 8 | Code copy feedback | Swift shows "Copied" text for 1.5s | "Copied" in tradeGreen | No feedback | Add brief "Copied!" text change after click |

### Missing from HTML

| # | Element | Type | Swift File | Fix |
|---|---------|------|-----------|-----|
| 1 | Mic button | button | `ChatInputBar.swift:120-130` | Add mic SVG in `htmlInputBar()` after send button, show when input empty |
| 2 | Stop recording button | button | `ChatInputBar.swift:131-147` | Add red stop button that shows during "recording" state |
| 3 | Photo thumbnail | component | `ChatInputBar.swift:47-53` | Add 40x40 rounded image preview when attachment active |
| 4 | Error banner | component | `ChatView.swift:91-116` | Add error bar between chat-content and input-area in `renderFullChat()` |
| 5 | Streaming bubble | component | `ChatView.swift:177-188` | Add streaming blocks preview (or "loading..." message) |
| 6 | Typing indicator | component | `ChatView.swift:247-267` | Add 3 animated dots in assistant bubble style |
| 7 | ScrollViewReader auto-scroll | interaction | `ChatView.swift:35,64-73` | Add `scrollIntoView()` call after `sendMessage()` |
| 8 | Voice input picker option | picker | N/A | Add `<option value="voice-input">Voice Input</option>` to component picker (Rule #2) |

### Missing from Swift

| # | Element | Type | HTML Location | Fix |
|---|---------|------|--------------|-----|
| (none — all HTML elements have Swift equivalents) |

### Style Mismatches

| # | Property | Swift | HTML | Fix |
|---|----------|-------|------|-----|
| 1 | Send button icon | SF Symbol `arrow.up.circle.fill` 28pt | Custom SVG circle+arrow 28x28 | Acceptable — visual equivalent |
| 2 | Hamburger icon | SF Symbol `line.3.horizontal` 18pt | SVG 3 lines 20x14 | Acceptable — visual equivalent |
| 3 | Compose icon | SF Symbol `square.and.pencil` 20pt | SVG pen path 22x22 | Acceptable — visual equivalent |
| 4 | Mode icons | SF Symbols (bolt, book, magnifyingglass) | Emoji (lightning, book, magnifier) | Minor — HTML uses emoji, Swift uses SF Symbols |
| 5 | Input bar bg | `.ultraThinMaterial` | `var(--material-bg)` rgba(255,255,255,0.85) | Close match — material approx |
| 6 | Attachment cancel icon | `plus` rotated 45deg | Static "+" text | Minor — rotation not visible in HTML |

---

## Fix Checklist

Numbered list of every fix needed to reach 100% parity, ordered by priority:

- [ ] 1. Chat HTML: Add mic button SVG in `htmlInputBar()` — show when input is empty and not recording — `preview/chat.html`, ~line 1085
- [ ] 2. Chat HTML: Add recording state with red stop button — toggle via `isRecording` JS state — `preview/chat.html`, ~line 1085
- [ ] 3. Chat HTML: Add photo thumbnail preview (40x40 placeholder) when attachment active — `preview/chat.html`, ~line 1085
- [ ] 4. Chat HTML: Add error banner div in `renderFullChat()` between chat-content and input — `preview/chat.html`, ~line 1252
- [ ] 5. Chat HTML: Add typing indicator (3 animated dots in assistant bubble) — `preview/chat.html`, ~line 1249
- [ ] 6. Chat HTML: Add streaming blocks placeholder message during "loading" state — `preview/chat.html`, ~line 1249
- [ ] 7. Chat HTML: Add `scrollIntoView()` call at end of `sendMessage()` — `preview/chat.html`, ~line 1354
- [ ] 8. Chat HTML: Add "Copied!" text feedback after code copy button click — `preview/chat.html`, ~line 1162
- [ ] 9. Chat HTML: Add `voice-input` option to component picker dropdown (Mandatory Rule #2) — `preview/chat.html`, ~line 838

---

## Parity History

| Report # | Date | Overall | Screens Checked | Fixes Needed |
|----------|------|---------|----------------|-------------|
| 1 | 2026-03-14 | — | 1 | — |
| 2 | 2026-03-14 | — | 1 | — |
| 3 | 2026-03-14 | 88% | 1 | 9 |
