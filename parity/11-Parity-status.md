# Parity Audit Report #11 — Chat Screen (Full Behaviour)

**Date:** 2026-03-15
**Auditor:** Claude Opus 4.6
**Scope:** Full behavioural parity audit of the chat screen
**Swift source of truth:** `ios/Tradeguruelectrical/`
**HTML preview:** `preview/chat.html`

---

## Summary

| Category | MATCH | PARTIAL | MISSING | EXTRA (removed) | Total |
|----------|-------|---------|---------|------------------|-------|
| Navigation & Layout | 7 | 0 | 0 | 0 | 7 |
| Mode Selector | 5 | 0 | 0 | 0 | 5 |
| Mode Info Card | 5 | 0 | 0 | 0 | 5 |
| Chat Messages | 6 | 0 | 0 | 0 | 6 |
| Content Blocks | 12 | 1 | 0 | 0 | 13 |
| Input Bar | 8 | 0 | 0 | 0 | 8 |
| Attachments | 6 | 0 | 0 | 0 | 6 |
| Voice Input | 4 | 0 | 0 | 0 | 4 |
| Streaming & Pipeline | 7 | 0 | 0 | 0 | 7 |
| Error Handling | 3 | 0 | 0 | 0 | 3 |
| Sidebar | 7 | 1 | 0 | 0 | 8 |
| API Endpoints | 7 | 0 | 0 | 0 | 7 |
| Conversation Mgmt | 5 | 0 | 0 | 2 | 7 |
| Rating & Feedback | 3 | 0 | 0 | 0 | 3 |
| **TOTALS** | **85** | **2** | **0** | **2** | **89** |

**Overall Parity: 97.8% (85 MATCH + 2 acceptable PARTIAL)**

---

## Fixes Applied in This Audit

| # | Issue | Fix Applied |
|---|-------|-------------|
| 1 | `selectConversation()` in HTML reset `modeCardDismissed` and `currentMode` (EXTRA vs Swift) | Removed `modeCardDismissed = false` and mode change from `selectConversation()` |
| 2 | `deleteConversation()` in HTML changed `currentMode` to fallback conversation's mode (EXTRA vs Swift) | Removed `currentMode = MOCK.conversations[0].mode` from `deleteConversation()` |
| 3 | Send button SVG did not match SF Symbol `arrow.up.circle.fill` | Replaced with upward arrow in filled circle SVG |
| 4 | Mic button SVG was a filled circle with mic (Swift is just a mic icon) | Replaced with standalone mic icon SVG matching `mic.fill` |
| 5 | Code block copy button showed "Copy" text (Swift shows `doc.on.doc` icon) | Replaced with copy icon SVG, shows "Copied" text on click then reverts |
| 6 | Typing indicator dot opacity was 0.3 (Swift uses 0.4) | Changed to 0.4 |
| 7 | Typing indicator animation duration was 1s (Swift uses 0.6s autoreverses = 1.2s) | Changed to 1.2s |
| 8 | `parts_list` snake_case block type from API was not handled | Added `case 'parts_list':` fallthrough |

---

## Detailed Behaviour Checklist

### Navigation & Layout

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 1 | Hamburger menu button opens sidebar | `showSidebar = true` with 0.25s ease | `toggleSidebar(true)` with CSS transition | MATCH |
| 2 | Settings gear button opens settings sheet | `showSettings = true` via `.sheet` | `setComponent('settings')` | MATCH |
| 3 | New chat button creates conversation | `viewModel.newConversation(mode:)` | `newChat()` | MATCH |
| 4 | Nav bar layout: hamburger left, gear + compose right | HStack with Spacer | flex layout | MATCH |
| 5 | Nav bar button sizes 44x44 | `.frame(width: 44, height: 44)` | `width:44px; height:44px` | MATCH |
| 6 | Background color `tradeBg` | `Color.tradeBg.ignoresSafeArea()` | `var(--trade-bg)` | MATCH |
| 7 | Dark mode support | Color(light:dark:) pattern | CSS variables with `[data-theme="dark"]` | MATCH |

### Mode Selector

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 8 | Three mode pills: Fault Finder, Learn, Research | `ForEach(ThinkingMode.allCases)` | `MODES.map()` | MATCH |
| 9 | Selected pill shows mode color with 12% bg | `mode.color.opacity(0.12)` | `var(--mode-*-12)` | MATCH |
| 10 | Unselected pill shows tradeInput bg | `Color.tradeInput` | `var(--trade-input)` | MATCH |
| 11 | Pill shows icon + name + short description | icon, name, shortDescription | icon, name, desc | MATCH |
| 12 | Mode change animation 0.2s easeInOut | `.easeInOut(duration: 0.2)` | `transition: all 0.2s` | MATCH |

### Mode Info Card

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 13 | Shows on mode change | `onChange(of: selectedMode)` resets card | `selectMode()` resets `modeCardDismissed` | MATCH |
| 14 | Dismiss on X button tap | `onDismiss` callback | `dismissCard()` | MATCH |
| 15 | Dismiss on card body tap | `.onTapGesture { dismissModeCard() }` | `onclick="dismissCard()"` | MATCH |
| 16 | Dismiss on text input focus | `onInputFocus` callback | `onfocus="dismissCard()"` | MATCH |
| 17 | Dismiss when text is entered | `onChange(of: inputText)` | `oninput="...dismissCard()"` | MATCH |

### Chat Messages

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 18 | User bubble: green bg, white text, right-aligned | `Color.tradeGreen`, `.trailing` | `var(--trade-green)`, `align-items:flex-end` | MATCH |
| 19 | User bubble max width 280 | `maxWidth: 280` | `max-width: 280px` | MATCH |
| 20 | Assistant bubble: tradeSurface bg, left-aligned | `Color.tradeSurface`, `.leading` | `var(--trade-surface)`, `align-items:flex-start` | MATCH |
| 21 | Assistant bubble max width 330 | `maxWidth: 330` | `max-width: 330px` | MATCH |
| 22 | Timestamp shown below bubble | `Text(message.timestamp, style: .time)` | `msg.time` string | MATCH |
| 23 | Mode icon shown for assistant messages | `Image(systemName: message.mode.icon)` | mode emoji icon | MATCH |

### Content Blocks

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 24 | Text block: 15px, tradeText color | `font(.system(size: 15))` | `font-size: 15px` | MATCH |
| 25 | Heading block: h1=20px, h2=18px, h3=16px bold | `headingSize(for: block.level)` | `.h1{20px},.h2{18px},.h3{16px}` | MATCH |
| 26 | Step list: numbered circles, green bg | Green circles with numbers | Same CSS | MATCH |
| 27 | Warning card: amber border, triangle icon, "Warning" title | `exclamationmark.triangle.fill`, amber | emoji + amber border | MATCH |
| 28 | Code block: monospace, copy button, language label | SF Mono 13px, doc.on.doc icon | Monospace 13px, SVG copy icon | MATCH |
| 29 | Code block copy: icon -> "Copied" -> icon (1.5s) | `showCopied` state, `Task.sleep(for: .seconds(1.5))` | innerHTML swap, `setTimeout 1500` | MATCH |
| 30 | Parts list: 3-column grid (Item, Spec, Qty) | Grid layout with header | CSS grid | MATCH |
| 31 | Regulation card: left purple border, code/clause/summary | `Rectangle().fill(Color.modeResearch).frame(width: 4)` | `border-left: 4px solid var(--mode-research)` | MATCH |
| 32 | Table block: horizontal scroll, header row, bordered | `ScrollView(.horizontal)` | `overflow-x: auto` | MATCH |
| 33 | Callout: tip (green), info (blue), important (amber) | `CalloutStyle` enum with colors | CSS classes per style | MATCH |
| 34 | Diagram ref: italic, secondary color | `.italic()`, tradeTextSecondary | `font-style:italic`, secondary color | MATCH |
| 35 | Link block: clickable, modeLearn color | `Link(destination:)`, `Color.modeLearn` | `<a>` tag, `var(--mode-learn)` | MATCH |
| 36 | Tool call: italic, secondary color | `.italic()`, tradeTextSecondary | `font-style:italic` | PARTIAL (emoji differs) |

### Input Bar

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 37 | Text field placeholder "Ask TradeGuru" | `TextField("Ask TradeGuru", ...)` | `placeholder="Ask TradeGuru"` | MATCH |
| 38 | Multi-line input (1-5 lines) | `.lineLimit(1...5)` | `autoGrowTextarea()` max 5 lines | MATCH |
| 39 | Send button appears when text or attachment present | `!text.isEmpty \|\| attachmentActive` | `toggleSendBtn()` same logic | MATCH |
| 40 | Send button: green filled circle with arrow up | `arrow.up.circle.fill`, tradeGreen | SVG upward arrow in circle | MATCH |
| 41 | Send clears text and attachment state | Resets all state vars | Resets all state vars | MATCH |
| 42 | Input focus dismisses mode card | `onInputFocus` callback | `onfocus="dismissCard()"` | MATCH |
| 43 | Border top on input area | `Color.tradeBorder.frame(height: 1)` | `border-top: 1px solid var(--trade-border)` | MATCH |
| 44 | Backdrop blur material | `.ultraThinMaterial` | `backdrop-filter: blur(40px)` | MATCH |

### Attachments

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 45 | Plus button opens attachment menu | `Menu { PhotosPicker, Button }` | `toggleAttachment()` popup menu | MATCH |
| 46 | Photo Library option | `PhotosPicker(matching: .any(of: [.images, .videos]))` | `pickImage()` with file input `accept: image/*,video/*` | MATCH |
| 47 | Browse Files option | `fileImporter(allowedContentTypes: [.pdf, .plainText, .data])` | `pickDocument()` with file input | MATCH |
| 48 | Image thumbnail preview (40x40, rounded 8) | `Image(uiImage:).frame(width:40, height:40).clipShape(.rect(cornerRadius: 8))` | `width:40px;height:40px;border-radius:8px` | MATCH |
| 49 | Document icon + filename preview | `doc.fill` icon + filename text | emoji + filename span | MATCH |
| 50 | Plus rotates 45deg when attachment active (becomes X) | `.rotationEffect(.degrees(45))` | `transform:rotate(45deg)` | MATCH |

### Voice Input

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 51 | Mic button shown when no text and not recording | Conditional in SwiftUI | `toggleSendBtn()` hides when text present | MATCH |
| 52 | Mic icon: green mic symbol | `mic.fill` with tradeGreen | SVG mic icon with tradeGreen | MATCH |
| 53 | Recording state: red circle with stop icon | `Circle().fill(Color.red.opacity(0.2))` + `stop.fill` | SVG red circle + red rect | MATCH |
| 54 | Stop recording transcribes audio | `onAudioRecorded` -> `transcribeAudio` | `toggleRecording()` -> `transcribeAudioBlob()` | MATCH |

### Streaming & Pipeline

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 55 | Typing indicator: 3 pulsing dots | 3 circles, 8x8, 0.4 opacity, 1.2s cycle | 3 spans, 8x8, 0.4 opacity, 1.2s cycle | MATCH |
| 56 | Streaming blocks render progressively | `ForEach(viewModel.streamingBlocks)` | `htmlStreamingBlocks.map(htmlBlock)` | MATCH |
| 57 | Auto-scroll on new messages | `ScrollViewReader.scrollTo("bottom")` | `scrollIntoView({behavior: 'smooth'})` | MATCH |
| 58 | Auto-scroll on streaming block count change | `onChange(of: streamingBlocks.count)` | `setTimeout scrollIntoView` after push | MATCH |
| 59 | Pipeline status: searching/synthesizing/streaming | `PipelineStage` enum + SSE events | `updatePipelineStatus()` + SSE events | MATCH |
| 60 | Pipeline status icons match stages | magnifyingglass.circle / brain.head.profile / text.bubble | emoji equivalents | MATCH |
| 61 | Pipeline status hidden when idle/error | `stage != .idle && stage != .error` | `el.classList.add('hidden')` | MATCH |

### Error Handling

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 62 | Error banner: red bg, warning icon, message, dismiss X | Red background, triangle icon, text, xmark button | Same layout | MATCH |
| 63 | Dismiss error button | `viewModel.dismissError()` sets `error = nil` | `htmlError=null;render()` | MATCH |
| 64 | Partial error preserves streaming blocks as message | `payload.partial == true` check | `parsed.partial` check | MATCH |

### Sidebar

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 65 | Sidebar width 300px | `.frame(width: 300)` | `width:300px` | MATCH |
| 66 | Dim overlay behind sidebar | `Color.black.opacity(0.3)` | `background:rgba(0,0,0,0.3)` | MATCH |
| 67 | Tap overlay closes sidebar | `.onTapGesture { onClose() }` | `onclick="toggleSidebar(false)"` | MATCH |
| 68 | Search bar with magnifying glass | `Image(systemName: "magnifyingglass")` | emoji magnifying glass | MATCH |
| 69 | Search clear button | `xmark.circle.fill` when text present | X button when text present | MATCH |
| 70 | Conversation rows: mode icon, title, date, count | icon + title + date + count | Same layout | MATCH |
| 71 | New Conversation button: green, full width | `Color.tradeGreen`, `.frame(maxWidth: .infinity)` | `background:var(--trade-green);width:100%` | MATCH |
| 72 | Swipe-to-delete vs button delete | `.onDelete` modifier (native swipe) | Trash icon button on hover | PARTIAL |

### API Endpoints

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 73 | `POST /device/register` | `TradeGuruAPI.registerDevice()` | `ensureDevice()` | MATCH |
| 74 | `POST /chat` (SSE streaming) | `TradeGuruAPI.chat()` -> `StreamParser` | `apiChat()` -> reader loop | MATCH |
| 75 | `POST /chat/vision` (SSE streaming) | `TradeGuruAPI.chatVision()` | `apiChatVision()` | MATCH |
| 76 | `POST /audio/transcribe` (multipart) | `TradeGuruAPI.transcribe()` | `transcribeAudioBlob()` | MATCH |
| 77 | `POST /audio/speech` | `TradeGuruAPI.speak()` | `apiSpeak()` | MATCH |
| 78 | `POST /rating` | `TradeGuruAPI.rate()` | `apiRate()` | MATCH |
| 79 | `POST /feedback` | `TradeGuruAPI.feedback()` | `apiFlag()` | MATCH |

### Conversation Management

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 80 | New conversation: title "New Conversation", uses current mode | `Conversation(title: "New Conversation", mode: mode)` | `{title: 'New Conversation', mode: currentMode}` | MATCH |
| 81 | Select conversation: clears streaming, error | `streamingBlocks = []; isStreaming = false; error = nil` | Same state resets | MATCH |
| 82 | Delete conversation: falls back to first | `activeConversation = conversations.first` | `currentConversation = 0` | MATCH |
| 83 | Auto-title on 2nd message (first user text, prefix 40) | `String(firstUserText.prefix(40))` | `.substring(0, 40)` | MATCH |
| 84 | ~~Select conversation resets mode card~~ | NOT in Swift | REMOVED from HTML (was EXTRA) | FIXED |
| 85 | ~~Delete conversation changes mode to fallback's mode~~ | NOT in Swift | REMOVED from HTML (was EXTRA) | FIXED |

### Rating & Feedback

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 86 | 5-star rating on last assistant message | `ForEach(1...5)` stars, `onRate` callback | `rateStars(n)` with star elements | MATCH |
| 87 | Flag button sends "inappropriate" reason | `onFlag?("inappropriate")` | `apiFlag('inappropriate')` | MATCH |
| 88 | Speaker button triggers text-to-speech | `onSpeak?(text)` -> `speakText` | `apiSpeak(el)` | MATCH |

### Empty State

| # | Behaviour | Swift | HTML | Status |
|---|-----------|-------|------|--------|
| 89 | Watermark logo: 180x180, 8% opacity | `frame(width: 180, height: 180).opacity(0.08)` | `width:180px;height:180px;opacity:0.08` | MATCH |

---

## PARTIAL Items Detail

| # | Item | Swift Behaviour | HTML Behaviour | Reason |
|---|------|----------------|----------------|--------|
| 36 | Tool call block | `Text("Tool: \(content)")` with `tradeTextSecondary`, italic | Gear emoji + `Tool:` text, italic, secondary | Emoji vs SF Symbol (acceptable in HTML) |
| 72 | Sidebar delete | Native swipe-to-delete via `.onDelete` | Trash icon button visible on hover | HTML cannot replicate native iOS swipe gesture |

---

## File Summary

| File | Role | Lines Read |
|------|------|-----------|
| `ios/Tradeguruelectrical/ChatView.swift` | Main chat screen layout | 344 |
| `ios/Tradeguruelectrical/ContentView.swift` | App entry point | 30 |
| `ios/Tradeguruelectrical/ChatInputBar.swift` | Input bar with attachments, voice | 239 |
| `ios/Tradeguruelectrical/Views/MessageBubble.swift` | Message rendering with actions | 171 |
| `ios/Tradeguruelectrical/Views/SidebarView.swift` | Conversation sidebar | 142 |
| `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` | All business logic | 461 |
| `ios/Tradeguruelectrical/Models/ChatMessage.swift` | Message model | 35 |
| `ios/Tradeguruelectrical/Models/ContentBlock.swift` | Block type definitions | 72 |
| `ios/Tradeguruelectrical/Models/Conversation.swift` | Conversation model | 29 |
| `ios/Tradeguruelectrical/ThinkingMode.swift` | Mode enum with properties | 52 |
| `ios/Tradeguruelectrical/Services/TradeGuruAPI.swift` | API client | 251 |
| `ios/Tradeguruelectrical/Services/StreamParser.swift` | SSE parser | 104 |
| `ios/Tradeguruelectrical/Services/DeviceManager.swift` | Device ID management | 67 |
| `ios/Tradeguruelectrical/Services/APIConfig.swift` | API configuration | 38 |
| `ios/Tradeguruelectrical/ModeSelector.swift` | Mode pill selector | 44 |
| `ios/Tradeguruelectrical/ModeInfoCard.swift` | Mode description card | 50 |
| `ios/Tradeguruelectrical/Models/PipelineStage.swift` | Pipeline stage enum | 10 |
| `ios/Tradeguruelectrical/Views/PipelineStatusView.swift` | Pipeline status UI | 56 |
| `ios/Tradeguruelectrical/Views/PipelineStatusDots.swift` | Animated dots | 26 |
| `ios/Tradeguruelectrical/Models/PartsItem.swift` | Parts list item | 19 |
| `ios/Tradeguruelectrical/Models/MessageAttachment.swift` | Attachment model | 33 |
| `ios/Tradeguruelectrical/TradeGuruColors.swift` | Color definitions | 37 |
| `ios/Tradeguruelectrical/Models/MockData.swift` | Mock conversation data | 254 |
| `ios/Tradeguruelectrical/Views/Blocks/*.swift` | All 8 block views | ~280 |
| `ios/Tradeguruelectrical/Views/Legal/SafetyDisclaimerView.swift` | Disclaimer view | 189 |
| `preview/chat.html` | HTML preview (modified) | 2663 |
