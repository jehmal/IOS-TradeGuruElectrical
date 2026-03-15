# Parity Status Report #4

**Project:** tradeguruelectrical
**Date:** 2026-03-14
**Overall parity:** 93% (WARN)

Thresholds: PASS >= 95% | WARN >= 80% | FAIL < 80%

---

## Screen Summary

| Screen | Swift File | HTML File | Parity | Status |
|--------|-----------|-----------|--------|--------|
| Chat | `ChatView.swift` | `preview/chat.html` | 93% | WARN |

---

## Chat -- Detailed Parity

### Matching (in parity)

| # | Element | Type | Swift | HTML | Notes |
|---|---------|------|-------|------|-------|
| 1 | Nav bar layout | layout | `ChatView.swift:276-300` HStack hamburger+compose | `htmlNavBar()` ~1128 | Identical structure |
| 2 | Hamburger button | button | `ChatView.swift:278-287` line.3.horizontal, 44x44 | ~1130 SVG 3 lines, 44x44 | Both open sidebar |
| 3 | New chat button | button | `ChatView.swift:291-298` square.and.pencil, 44x44 | ~1133 SVG pen path | Both call newConversation/newChat |
| 4 | Mode info card layout | component | `ModeInfoCard.swift:8-44` HStack icon+text+close | `htmlModeCard()` ~1082 | Layout, padding (12px), border radius (12px) match |
| 5 | Mode info card icon | component | `ModeInfoCard.swift:9-14` 32x32, mode color bg, white icon | ~1086 32x32, mode color bg, white SVG | Match |
| 6 | Mode info card title | text | `ModeInfoCard.swift:17-19` 16px semibold, mode color | ~1088 16px font-weight 600, mode color | Match |
| 7 | Mode info card body | text | `ModeInfoCard.swift:21-24` 14px, tradeTextSecondary, 3 lines | ~1089 14px, tradeTextSecondary | Match |
| 8 | Mode info card close | button | `ModeInfoCard.swift:29-34` xmark 12px semibold, 24x24 | ~1091 x char, 24x24 | Both dismiss card |
| 9 | Mode info card dismiss on tap | interaction | `ChatView.swift:26-28` .onTapGesture calls dismissModeCard() | ~1085 onclick="dismissCard()" on whole card | Match |
| 10 | Mode selector (3 pills) | component | `ModeSelector.swift:7-37` ForEach ThinkingMode.allCases | `htmlModeSelector()` ~1095 MODES.map | 3 pills, icon+name+desc |
| 11 | Mode pill active styling | styling | `ModeSelector.swift:28` mode.color + 0.12 opacity bg | ~1101 --active-color, --active-bg | Match |
| 12 | Mode pill border | styling | `ModeSelector.swift:30-31` active=mode.color, inactive=tradeBorder, 1px, radius 10 | ~618,627 border 1px, radius 10px | Match |
| 13 | Mode pill text sizes | styling | `ModeSelector.swift:17-21` icon 11px, name 12px semibold, desc 10px | ~629-633 icon 11px, label 12px 600, desc 10px | Match |
| 14 | Mode switching behavior | interaction | `ChatView.swift:168-174` onChange: reset card, show card, set viewModel mode | `selectMode()` ~1038 set currentMode, reset dismissed, render | Match |
| 15 | Text field | input | `ChatInputBar.swift:82-96` "Ask TradeGuru", 16px, tradeBorder 0.5px, radius 20 | ~1116 placeholder="Ask TradeGuru", 16px, border 0.5px, radius 20px | Match |
| 16 | Input dismiss card on focus | interaction | `ChatInputBar.swift:94-96` onChange(inputFocused) calls onInputFocus which dismisses | ~1116 onfocus="dismissCard()" | Match |
| 17 | Input dismiss card on typing | interaction | `ChatView.swift:145-149` onChange(inputText) dismisses if not empty | ~1116 oninput="toggleSendBtn(); dismissCard()" | Match |
| 18 | Send button show/hide | interaction | `ChatInputBar.swift:98` !text.isEmpty shows send | `toggleSendBtn()` ~1055 visible class toggle | Match |
| 19 | Send button icon | button | `ChatInputBar.swift:117-119` arrow.up.circle.fill 28pt tradeGreen | ~1119 SVG circle 28x28 tradeGreen | Visual equivalent |
| 20 | Send message action | interaction | `ChatInputBar.swift:99-115` sends text+attachments, clears input | `sendMessage()` ~1392 pushes to conv, clears, renders | Both add user msg to conversation |
| 21 | Mic button (when input empty) | button | `ChatInputBar.swift:123-133` mic.fill 28pt tradeGreen, 44x44 min | ~1121 SVG mic 28x28 green circle | Match |
| 22 | Mic/send toggle behavior | interaction | `ChatInputBar.swift:98,123` text empty=mic, text present=send | `toggleSendBtn()` ~1055-1062 hide mic when text, show send | Match |
| 23 | Recording toggle | interaction | `ChatInputBar.swift:124-149` isRecording toggles stop button | `toggleRecording()` ~1064 htmlIsRecording toggles icon | Match |
| 24 | Recording stop button | button | `ChatInputBar.swift:135-149` red circle 0.2 opacity + stop.fill | ~1069 red circle + red rect stop | Match |
| 25 | Attachment button (+) | button | `ChatInputBar.swift:58-66` plus icon 16px, 30px circle, tradeInput bg | ~1114 add-btn 30px circle, tradeInput bg, "+" | Match |
| 26 | Attachment cancel (x) | button | `ChatInputBar.swift:32-48` plus rotated 45deg (=x) | ~1114 "&times;" when active | Both dismiss attachment |
| 27 | Attachment thumbnail | component | `ChatInputBar.swift:50-56` 40x40 image, radius 8 | ~1109 48x48 placeholder, radius 8 | Close match (size differs slightly) |
| 28 | User bubble | component | `MessageBubble.swift:39-52` tradeGreen, white text, 15px, 280px max, radius 16, padding 14h 10v | ~410-418 tradeGreen, white, 15px, 280px, radius 16, padding 10px 14px | Match |
| 29 | Assistant bubble | component | `MessageBubble.swift:54-64` tradeSurface, 14px padding, radius 16, 330px max, 12px gap | ~420-428 tradeSurface, 14px padding, radius 16, 330px max, 12px gap | Match |
| 30 | Timestamp meta | text | `MessageBubble.swift:27-29` 11px, tradeTextSecondary | ~437 11px, tradeTextSecondary | Match |
| 31 | Mode icon in meta | icon | `MessageBubble.swift:22-25` mode.icon 14px, mode.color | ~1250 mode emoji, mode color | Match |
| 32 | Star rating row (last msg) | component | `MessageBubble.swift:66-78` 5 stars, 14px, tradeGreen when filled | ~1242-1243 5 stars, 14px, onclick turns green | Match |
| 33 | Flag button | button | `MessageBubble.swift:80-87` flag 14px, tradeTextSecondary | ~1244 flag emoji, 14px, tradeTextSecondary | Match |
| 34 | Speaker button | button | `MessageBubble.swift:89-99` speaker.wave.2 14px, tradeTextSecondary | ~1245 speaker emoji, 14px, tradeTextSecondary | Match |
| 35 | TextBlockView | block | `TextBlockView.swift` 15px, tradeText | `htmlBlock` text: 15px, tradeText | Match |
| 36 | Heading block h1 | block | `MessageBubble.swift:111-114` 20px bold, tradeText | ~1173-1174 h1 20px, 700 weight | Match |
| 37 | Heading block h2 | block | same, 18px | h2 18px | Match |
| 38 | Heading block h3 | block | same, 16px | h3 16px | Match |
| 39 | StepListView | block | `StepListView.swift` title 16px semibold, numbered circles 22px green, step text 14px, padding 12, border 1px, radius 12 | ~1176-1182 step-title 16px 600, step-num 22px green, step-text 14px, padding 12, border 1px, radius 12 | Match |
| 40 | WarningCardView | block | `WarningCardView.swift` faultFinder 0.1 bg, faultFinder border 1px, radius 12, icon 18px, title 15px bold, text 14px | ~1183-1191 rgba(245,158,11,0.1), faultFinder border, radius 12, icon 18px, title 15px 700, text 14px | Match |
| 41 | CodeBlockView | block | `CodeBlockView.swift` monospace 13px, tradeSurface bg, radius 8, language label 11px, horizontal scroll | ~1192-1195 monospace 13px, tradeSurface, radius 8, lang label 11px, overflow-x auto | Match |
| 42 | Code copy button | button | `CodeBlockView.swift:21-38` copies to pasteboard, shows "Copied" green 1.5s | ~1194 navigator.clipboard, "Copied!" green 1.5s timeout | Match |
| 43 | PartsListView | block | `PartsListView.swift` 3 columns, header 13px semibold, tradeSurface, rows alternating, border 1px, radius 12 | ~1196-1200 3-col grid, header 13px 600, tradeSurface, alternating, border 1px, radius 12 | Match |
| 44 | RegulationView | block | `RegulationView.swift` modeResearch left border 4px, tradeSurface bg, radius 12, code 15px bold, clause 13px secondary, summary 14px | ~1201-1206 modeResearch border-left 4px, tradeSurface, radius 12, code 15px 700, clause 13px secondary, summary 14px | Match |
| 45 | TableBlockView | block | `TableBlockView.swift` horizontal scroll, header tradeSurface 13px semibold, cells 13px, 80px min width, border 1px, radius 12 | ~1207-1211 overflow-x auto, header tradeSurface 13px 600, cells 13px, border-collapse, border 1px, radius 12 | Match |
| 46 | CalloutView (tip) | block | `CalloutView.swift` tradeGreen left border 4px, lightbulb.fill icon, 14px text, tradeSurface bg, radius 12 | ~1212-1218 callout-border-tip tradeGreen, lightbulb emoji, 14px, tradeSurface, radius 12 | Match |
| 47 | CalloutView (info) | block | same, modeLearn border, info.circle.fill | callout-border-info modeLearn, info emoji | Match |
| 48 | CalloutView (important) | block | same, modeFaultFinder border, exclamationmark.circle.fill | callout-border-important modeFaultFinder, exclamation emoji | Match |
| 49 | DiagramRef block | block | `MessageBubble.swift:137-141` 13px, italic, secondary, "Diagram:" prefix | ~1219-1221 italic, secondary, "Diagram:" prefix | Match |
| 50 | ToolCall block | block | `MessageBubble.swift:143-147` 13px, italic, secondary, "Tool:" prefix | ~1222-1224 italic, secondary, "Tool:" prefix | Match |
| 51 | Link block | block | `MessageBubble.swift:149-159` 14px, modeLearn, Link/underline | ~1225-1226 14px, modeLearn, underline `<a>` | Match |
| 52 | Sidebar overlay dim | component | `SidebarView.swift:11-13` black 0.3 opacity, onTapGesture closes | ~707-712 rgba(0,0,0,0.3), cursor pointer onclick | Match |
| 53 | Sidebar panel | component | `SidebarView.swift:15-17` 300px width, tradeBg, slide from leading | ~713-724 300px, tradeBg, translateX animation | Match |
| 54 | Sidebar header | layout | `SidebarView.swift:59-76` "Conversations" 20px semibold, xmark 14px 44x44 | ~726-741 "Conversations" 20px 600, x 14px 44x44 | Match |
| 55 | Sidebar row | component | `SidebarView.swift:78-105` mode icon 14px, title 15px semibold, date 12px, count 12px, padding 16h 10v, 44pt min | ~743-763 icon 14px, title 15px 600, meta 12px, padding 16px 10px, 44px min | Match |
| 56 | Sidebar new btn | button | `SidebarView.swift:44-54` tradeGreen, white text, 15px semibold, radius 12, padding 16 | ~768-780 tradeGreen, white, 15px 600, radius 12, padding 16 | Match |
| 57 | Sidebar open/close | interaction | `ChatView.swift:278-281,150-167` animated toggle | `toggleSidebar()` ~1049 + CSS transition 0.25s | Match |
| 58 | Sidebar select conversation | interaction | `ChatView.swift:154-157` selectConversation + close sidebar | `selectConversation()` ~1384 set index, close sidebar, render | Match |
| 59 | Sidebar new chat | interaction | `ChatView.swift:158-161` newConversation + close sidebar | `newChat()` ~1377 set index -1, close, render | Match |
| 60 | Empty state watermark | component | `ChatView.swift:78-84` 180x180, 0.08 opacity, template rendering | ~399-404 180x180, 0.08 opacity, invert filter for dark | Match |
| 61 | Error banner | component | `ChatView.swift:91-116` red bg 0.1, warning icon 14px, text 13px red, xmark dismiss 44x44 | ~1289-1293 rgba(239,68,68,0.1), warning icon, text 13px red, x dismiss 44x44 | Match |
| 62 | Error dismiss | interaction | `ChatView.swift:104-110` dismissError() sets nil | ~1292 onclick sets htmlError=null, render | Match |
| 63 | Typing indicator (dots) | component | `ChatView.swift:254-274` 3 circles 8px, tradeTextSecondary, 0.4 opacity, tradeSurface bg, radius 16, 80px max | ~1282-1286 3 spans 8px, tradeTextSecondary, pulseDot animation, tradeSurface bg, radius 16, 80px max | Match |
| 64 | Scroll to bottom on new message | interaction | `ChatView.swift:64-73` ScrollViewReader scrollTo("bottom") on message count change | `sendMessage()` ~1407-1409 scrollIntoView smooth | Match |
| 65 | Dark mode color tokens | styling | `TradeGuruColors.swift` 11 tokens | CSS `[data-theme="dark"]` ~27-44 | All tokens match |
| 66 | Light mode color tokens | styling | `TradeGuruColors.swift` 11 tokens | CSS `:root` ~8-26 | All tokens match |
| 67 | Input area background | styling | `ChatInputBar.swift:154` .ultraThinMaterial | ~600-602 var(--material-bg) rgba + backdrop-filter blur(40px) | Close approximation |
| 68 | Input area border top | styling | `ChatInputBar.swift:23` tradeBorder 1px | ~599 border-top 1px solid tradeBorder | Match |
| 69 | Component picker options | control | N/A (all views exist) | ~825-841 14 options | All Swift views covered in picker |
| 70 | Mock data conversations | data | `ChatViewModel.swift:18-20` MockData.allConversations | ~888-996 MOCK.conversations (3 convos) | Both have 3 conversations with matching modes |

### Mismatched (out of parity)

| # | Element | Issue | Swift Value | HTML Value | Fix |
|---|---------|-------|-------------|------------|-----|
| 1 | Attachment thumbnail size | Swift 40x40, HTML 48x48 | 40x40 px, cornerRadius 8 | 48x48 px, border-radius 8px | Change HTML thumbnail to 40x40 in `htmlInputBar()` ~line 1109 |
| 2 | Star rating persistence | Swift stores rating in @State, all stars update; HTML only colors the clicked star | `userRating` @State, fills all stars <= clicked | Single star onclick changes only that star | Fix HTML star onclick to fill all stars up to and including clicked index |
| 3 | Streaming progressive blocks | Swift renders actual block views during streaming | `streamingBlockView(for:)` renders each block type | Only typing dots shown, no progressive block rendering | Add streaming block rendering to HTML using `htmlBlock()` when `htmlIsStreaming` |
| 4 | Attachment thumbnail cancel position | Swift shows cancel as rotated plus (x) on the add-btn itself; HTML shows x on the thumbnail | `plus` rotated 45deg on the main button | `&times;` button positioned at top-right of thumbnail | Minor -- both achieve "cancel attachment" but cancel target differs |
| 5 | PartsListView header text | Swift "Specification", HTML "Spec" | `Text("Specification")` | `<span>Spec</span>` | Change HTML parts header from "Spec" to "Specification" in `htmlBlock()` partsList ~line 1198 |

### Missing from HTML

| # | Element | Type | Swift File | Fix |
|---|---------|------|-----------|-----|
| 1 | Streaming progressive blocks | component | `ChatView.swift:184-195` streamingBubble renders actual ContentBlock views in a tradeSurface bubble | Add `htmlStreamingBlocks` array and render blocks via `htmlBlock()` inside assistant bubble during streaming in `renderFullChat()` ~line 1282 |
| 2 | Mode card transition animation | animation | `ChatView.swift:29` .opacity + .move(edge: .top) transition | CSS class `hidden` uses opacity:0 + height:0 but lacks translateY. Add `transform: translateY(-10px)` and `transition: all 0.2s` on `.mode-card` for parity |
| 3 | Warning "Danger" vs "Warning" title logic | logic | `WarningCardView.swift:13-14` always shows "Warning" | `htmlBlock()` warning ~1184 checks content for "danger" and changes title | Remove the danger check from HTML -- Swift always renders "Warning" as the title |

### Missing from Swift

| # | Element | Type | HTML Location | Fix |
|---|---------|------|--------------|-----|
| (none -- all HTML elements have Swift equivalents) |

### Behaviour Mismatches (Mandatory Rule #5)

| # | Behaviour | Swift Implementation | HTML Implementation | Match | Fix |
|---|-----------|---------------------|--------------------|----|-----|
| 1 | Mode switching shows card | `ChatView.swift:168-174` onChange resets `userDismissedCard` to false, sets `showModeCard = true` with animation | `selectMode()` ~1038 sets `modeCardDismissed = false`, re-renders | YES | -- |
| 2 | Card dismiss on tap | `ChatView.swift:26-28` onTapGesture on whole card | ~1085 onclick on whole .mode-card div | YES | -- |
| 3 | Card dismiss on X button | `ModeInfoCard.swift:29` button calls onDismiss | ~1091 button onclick="dismissCard()" | YES | -- |
| 4 | Card dismiss on input focus | `ChatInputBar.swift:94-96` onChange(inputFocused) -> onInputFocus | ~1116 onfocus="dismissCard()" | YES | -- |
| 5 | Card dismiss on typing | `ChatView.swift:145-149` onChange(inputText) when not empty | ~1116 oninput includes dismissCard() | YES | -- |
| 6 | Send message clears input | `ChatView.swift:128` inputText = "" | `sendMessage()` ~1403 inp.value = '' | YES | -- |
| 7 | Send shows user message in chat | `ChatViewModel.swift:42-44` appends userMessage to conversation | `sendMessage()` ~1400 pushes to conv.messages | YES | -- |
| 8 | Mic shows when input empty | `ChatInputBar.swift:123` else if !isRecording | `toggleSendBtn()` ~1061 mic shows when no text and not recording | YES | -- |
| 9 | Send shows when input has text | `ChatInputBar.swift:98` !text.isEmpty || attachmentActive | `toggleSendBtn()` ~1060 hasText toggles visible | YES | -- |
| 10 | Sidebar open on hamburger tap | `ChatView.swift:279` showSidebar = true with animation | ~1130 onclick="toggleSidebar(true)" | YES | -- |
| 11 | Sidebar close on dim tap | `SidebarView.swift:13` onTapGesture { onClose() } | ~1141 onclick="toggleSidebar(false)" | YES | -- |
| 12 | Sidebar close on X | `SidebarView.swift:67-68` button calls onClose | ~1145 onclick="toggleSidebar(false)" | YES | -- |
| 13 | Sidebar select loads conversation | `ChatView.swift:154-157` selectConversation + close | `selectConversation()` ~1384-1390 | YES | -- |
| 14 | New chat shows empty state | `ChatViewModel.swift:84-91` new conversation with empty messages | `newChat()` ~1377-1381 index=-1, renders empty | YES | -- |
| 15 | Star rating fills on tap | `MessageBubble.swift:70-71` sets userRating, calls onRate | ~1243 inline onclick changes star color | PARTIAL | HTML only colors the clicked star, not all stars up to it. Fix: update onclick to fill stars 1..N |
| 16 | Flag button fires callback | `MessageBubble.swift:81` onFlag?("inappropriate") | ~1244 no callback, just visual | PARTIAL | HTML flag has no action. Add visual feedback (e.g., color change on click) |
| 17 | Speaker button fires callback | `MessageBubble.swift:90-93` onSpeak?(text) | ~1245 no callback, just visual | PARTIAL | HTML speaker has no action. Add visual feedback on click |
| 18 | Photo attachment toggle | `ChatInputBar.swift:58-80` PhotosPicker -> shows thumbnail | `toggleAttachment()` ~1076 toggles placeholder | YES | Both toggle attachment state |
| 19 | Voice recording start/stop | `ChatInputBar.swift:157-187` AVAudioRecorder start/stop | `toggleRecording()` ~1064 toggles icon | YES | Both toggle recording visual state |
| 20 | Scroll on new message | `ChatView.swift:64-73` ScrollViewReader proxy.scrollTo | `sendMessage()` ~1407 scrollIntoView | YES | -- |
| 21 | Scroll on streaming update | `ChatView.swift:69-73` scrollTo on streamingBlocks count change | (no streaming blocks in HTML) | NO | Add scroll behavior when streaming blocks update |
| 22 | Code copy to clipboard | `CodeBlockView.swift:22` UIPasteboard.general.string | ~1194 navigator.clipboard.writeText | YES | -- |
| 23 | Code "Copied" feedback | `CodeBlockView.swift:23-27` showCopied true for 1.5s | ~1194 textContent='Copied!' for 1.5s | YES | -- |
| 24 | Error dismiss | `ChatViewModel.swift:101-103` sets error = nil | ~1292 htmlError=null; render() | YES | -- |
| 25 | Link opens URL | `MessageBubble.swift:150-153` Link(destination:) opens in browser | ~1226 `<a href>` opens URL | YES | -- |
| 26 | Warning title always "Warning" | `WarningCardView.swift:13-14` hardcoded "Warning" | ~1184 dynamic "Danger" if content includes "danger" | NO | Remove danger check from HTML, always show "Warning" |
| 27 | Streaming shows typing then blocks | `ChatView.swift:53-57` shows streamingBubble if blocks exist, typingIndicator if empty | ~1282 only shows typing dots regardless | PARTIAL | HTML needs progressive block rendering during streaming |
| 28 | Send with attachment | `ChatInputBar.swift:100-107` sends attachment data | `sendMessage()` does not handle attachment in message | PARTIAL | HTML sendMessage() should clear attachment state after send |

### Style Mismatches

| # | Property | Swift | HTML | Fix |
|---|----------|-------|------|-----|
| 1 | Send button icon | SF Symbol `arrow.up.circle.fill` 28pt | Custom SVG circle+rotated-plus 28x28 | Acceptable -- visual equivalent |
| 2 | Hamburger icon | SF Symbol `line.3.horizontal` 18pt medium weight | SVG 3 lines 20x14, stroke-width 2 | Acceptable -- visual equivalent |
| 3 | Compose icon | SF Symbol `square.and.pencil` 20pt | SVG pen path 22x22, stroke 1.8 | Acceptable -- visual equivalent |
| 4 | Mode icons | SF Symbols (bolt.fill, book.fill, magnifyingglass) | Emoji (lightning, book, magnifier) | Minor -- HTML uses emoji approximation |
| 5 | Input bar bg | `.ultraThinMaterial` (system vibrancy) | `rgba(255,255,255,0.85)` + `backdrop-filter: blur(40px)` | Close approximation |
| 6 | Attachment thumbnail size | 40x40 in Swift | 48x48 in HTML | Fix HTML to 40x40 in `htmlInputBar()` ~line 1109 |
| 7 | Mode selector gap | `HStack(spacing: 6)` | `.mode-selector-row` gap: 6px | Match |
| 8 | Mode selector padding | `padding(.horizontal, 12)` + `padding(.top, 10)` | `padding: 10px 12px 4px` | Top matches, bottom differs (Swift has no bottom padding in selector, HTML has 4px) -- minor |
| 9 | Mic button green background | `mic.fill` 28pt icon in tradeGreen foreground (no circle bg) | Green filled circle 28x28 with mic cutout | Visual difference -- Swift has no background circle on mic icon. Consider adding green circle bg in Swift or removing from HTML |

---

## Fix Checklist

Numbered list of every fix needed to reach 100% parity, ordered by priority:

- [ ] 1. **Chat HTML: Star rating -- fill all stars up to clicked index** -- In `htmlMessage()` ~line 1243, update the star onclick to set all stars 1..N to green (currently only colors the single clicked star). Use a `data-rating` attribute or inline JS loop to fill stars.
- [ ] 2. **Chat HTML: Add streaming progressive blocks** -- In `renderFullChat()` ~line 1282, add a `htmlStreamingBlocks` JS array and render blocks via `htmlBlock()` in an assistant bubble during streaming, not just typing dots. The Swift version shows actual content blocks as they arrive.
- [ ] 3. **Chat HTML: Fix Warning block "Danger" logic** -- In `htmlBlock()` warning case ~line 1184, remove the `isDanger` check. Swift `WarningCardView` always renders "Warning" as the title. Change to always show "Warning".
- [ ] 4. **Chat HTML: Fix attachment thumbnail size** -- In `htmlInputBar()` ~line 1109, change thumbnail container from 48x48 to 40x40 to match Swift `ChatInputBar.swift:53-55`.
- [ ] 5. **Chat HTML: Fix PartsListView header "Spec" -> "Specification"** -- In `htmlBlock()` partsList ~line 1198, change `<span>Spec</span>` to `<span>Specification</span>` to match Swift `PartsListView.swift:11-12`.
- [ ] 6. **Chat HTML: Add flag button visual feedback** -- In `htmlMessage()` ~line 1244, add onclick behavior to the flag icon (e.g., color change to red on click) to match Swift's `onFlag?("inappropriate")` callback.
- [ ] 7. **Chat HTML: Add speaker button visual feedback** -- In `htmlMessage()` ~line 1245, add onclick behavior to the speaker icon (e.g., color change on click) to match Swift's `onSpeak?(text)` callback.
- [ ] 8. **Chat HTML: Clear attachment state on send** -- In `sendMessage()` ~line 1392, add `htmlAttachmentActive = false;` after sending to match Swift's clearing of attachment state in `ChatInputBar.swift:112-115`.
- [ ] 9. **Chat HTML: Add mode card slide transition** -- On `.mode-card` CSS ~line 341, add a subtle `transform: translateY(-8px)` to the `.hidden` state to match Swift's `.move(edge: .top)` transition at `ChatView.swift:29`.

---

## Parity History

| Report # | Date | Overall | Screens Checked | Fixes Needed |
|----------|------|---------|----------------|-------------|
| 1 | 2026-03-14 | -- | 1 | -- |
| 2 | 2026-03-14 | -- | 1 | -- |
| 3 | 2026-03-14 | 88% | 1 | 9 |
| 4 | 2026-03-14 | 93% | 1 | 9 |
