# Parity Status Report #9

**Project:** tradeguruelectrical
**Date:** 2026-03-15
**Screen:** Chat (full screen) + Onboarding
**Overall parity:** 99% (PASS)
**Behaviours audited:** 75
**Matched:** 74
**Fixed this session:** 0
**Remaining gaps:** 1
**Context:** Post-onboarding audit — new onboarding flow added to both Swift and HTML

Thresholds: PASS >= 95% | WARN >= 80% | FAIL < 80%

---

## New: Onboarding Parity Assessment

### Swift Implementation
- `ContentView.swift`: `@AppStorage("hasCompletedOnboarding")` gate — shows `OnboardingView` or `ChatView`
- `OnboardingView.swift`: TabView with `.page(indexDisplayMode: .always)`, 3 mode pages + 1 final CTA page, Skip button on pages 0-2
- `OnboardingPageView.swift`: SF Symbol icon in colored circle, title, description
- `OnboardingFinalPageView.swift`: TradeGuru logo, "Ready to start?" title, Get Started button, Sign In link

### HTML Implementation
- Component picker has "Onboarding" option (line 895) — YES
- `renderOnboarding()` function renders all 4 pages — YES
- `htmlOnboardingPage` state variable tracks current page — YES
- `setOnboardingPage(n)` navigates between pages — YES

### Onboarding Behaviour Matrix

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 66 | Onboarding shown before chat on first launch | @AppStorage gate in ContentView | Component picker "onboarding" option | YES |
| 67 | Page 0: Fault Finder mode page (amber icon, title, description) | OnboardingPageView with ThinkingMode.faultFinder | MODES[0] with --mode-fault-finder color | YES |
| 68 | Page 1: Learn mode page (blue icon, title, description) | OnboardingPageView with ThinkingMode.learn | MODES[1] with --mode-learn color | YES |
| 69 | Page 2: Research mode page (purple icon, title, description) | OnboardingPageView with ThinkingMode.research | MODES[2] with --mode-research color | YES |
| 70 | Page 3: Final CTA page with logo, "Ready to start?", Get Started button | OnboardingFinalPageView | renderOnboarding() page===3 branch | YES |
| 71 | Page dots (4 dots, active dot elongated, clickable) | TabView .page(indexDisplayMode: .always) | Custom dots with onclick setOnboardingPage(i) | YES |
| 72 | Skip button visible on pages 0-2, hidden on page 3 | `if currentPage < pages.count` condition | `if (page < 3)` condition in renderOnboarding | YES |
| 73 | Skip navigates to chat | Sets hasCompletedOnboarding = true | Sets currentComponent='full-chat' | YES |
| 74 | Get Started navigates to chat | onGetStarted sets hasCompletedOnboarding = true | Sets currentComponent='full-chat' | YES |
| 75 | Swipe navigation between pages | TabView .page style (native swipe) | ontouchstart/ontouchend with dx threshold | YES |
| 76 | Dark mode support on onboarding | Color.tradeBg, Color.tradeText (adaptive) | var(--trade-bg), var(--trade-text) (CSS vars) | YES |
| 77 | Sign In link on final page | Button "Already have an account? Sign In" | Button with same text, navigates to chat | YES |
| 78 | Mode colors match (amber/blue/purple) | .modeFaultFinder/.modeLearn/.modeResearch | --mode-fault-finder/--mode-learn/--mode-research | YES |
| 79 | Icon in colored circle on mode pages | SF Symbol in Circle with color.opacity(0.12) bg | SVG icon in div with mode bg color | YES |

---

## Previous Behaviours (from Report #8)

All 65 previously audited behaviours re-verified:

### Layout & Navigation (1-8)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 1 | Dark background (tradeBg) | Color.tradeBg | var(--trade-bg) | YES |
| 2 | Nav bar with hamburger + new chat | navBar() with line.3.horizontal + square.and.pencil | htmlNavBar() with SVG icons | YES |
| 3 | Sidebar opens on hamburger tap | showSidebar = true | toggleSidebar(true) | YES |
| 4 | Sidebar closes on dim tap | onClose callback | sidebar-dim onclick | YES |
| 5 | Sidebar close X button | xmark button in header | sidebar-close button | YES |
| 6 | Sidebar conversation list with mode icon + title + date + count | conversationRow() | sidebar-row rendering | YES |
| 7 | Sidebar "New Conversation" button (green) | onNewChat callback | newChat() | YES |
| 8 | New chat clears messages and resets state | newConversation() | newChat() | YES |

### Sidebar Search & Delete (9-12)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 9 | Sidebar search bar with magnifying glass icon | SidebarView searchText + TextField | Search input with magnifying glass emoji | YES |
| 10 | Sidebar search filters conversations by title | filteredConversations computed property | filter on htmlSidebarSearch | YES |
| 11 | Sidebar search clear button (xmark.circle.fill) | Button to clear searchText | Button to clear htmlSidebarSearch | YES |
| 12 | Sidebar delete conversation button per row | onDelete callback + .onDelete modifier | deleteConversation() with trash icon per row | YES |

### Mode Selector & Info Card (13-22)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 13 | 3-mode selector (Fault Finder / Learn / Research) | ModeSelector with ForEach ThinkingMode.allCases | htmlModeSelector with MODES array | YES |
| 14 | Mode pill shows icon + name + short description | HStack with icon, name, desc | pill-top + pill-desc | YES |
| 15 | Active mode pill has colored border + tinted background | mode.color border + opacity(0.12) bg | --active-color border + --active-bg | YES |
| 16 | Mode info card appears on mode change | showModeCard = true on onChange | modeCardDismissed = false on selectMode | YES |
| 17 | Mode info card has icon badge + title + description + close | ModeInfoCard with icon/title/fullDescription/xmark | htmlModeCard with iconSvg/name/fullDesc/close | YES |
| 18 | Mode card dismisses on tap | .onTapGesture { dismissModeCard() } | onclick="dismissCard()" | YES |
| 19 | Mode card dismisses on close button | Button(action: onDismiss) | mode-card-close onclick | YES |
| 20 | Mode card dismisses on text input focus | onInputFocus callback | onfocus="dismissCard()" on textarea | YES |
| 21 | Mode card dismisses when text entered | onChange(of: inputText) | oninput handler calls dismissCard() | YES |
| 22 | Fault Finder amber / Learn blue / Research purple colors | modeFaultFinder/modeLearn/modeResearch | --mode-fault-finder/--mode-learn/--mode-research | YES |

### Empty State & Watermark (23-24)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 23 | Empty state shows TradeGuru logo watermark at 8% opacity | Image("TradeGuruLogo") .opacity(0.08) | watermark-logo with opacity:0.08 | YES |
| 24 | Logo uses tradeguru-logo.png file | Image("TradeGuruLogo") asset catalog | url('tradeguru-logo.png') | YES |

### Message Display (25-32)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 25 | User bubble: green bg, white text, right-aligned, 280px max | tradeGreen bg, .white text, .trailing, maxWidth 280 | trade-green bg, #fff, flex-end, max-width 280px | YES |
| 26 | Assistant bubble: surface bg, left-aligned, 330px max | tradeSurface bg, .leading, maxWidth 330 | trade-surface bg, flex-start, max-width 330px | YES |
| 27 | Message timestamp below bubble | Text(timestamp, style: .time) | msg-meta-time | YES |
| 28 | Assistant messages show mode icon next to timestamp | Image(systemName: mode.icon) | msg-meta-icon with mode emoji | YES |
| 29 | Auto-scroll on new messages | ScrollViewReader proxy.scrollTo("bottom") | scrollIntoView({behavior: 'smooth'}) | YES |
| 30 | Auto-scroll on streaming blocks | onChange(of: streamingBlocks.count) | setTimeout scrollIntoView after block push | YES |
| 31 | Typing indicator (3 pulsing dots) when streaming with no blocks | typingIndicator with 3 Circles | 3 spans with pulseDot animation | YES |
| 32 | Streaming bubble shows blocks as they arrive | streamingBubble with ForEach streamingBlocks | msg-bubble-assistant with htmlStreamingBlocks.map | YES |

### Content Blocks (33-44)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 33 | Text block (15px, trade-text color) | TextBlockView | block-text | YES |
| 34 | Heading block (h1=20, h2=18, h3=16, bold) | headingSize(for: level) | block-heading h1/h2/h3 | YES |
| 35 | Step list with numbered green circles | StepListView with step numbers | block-step-list with step-num | YES |
| 36 | Warning card (amber border, warning icon) | WarningCardView | block-warning | YES |
| 37 | Code block (monospaced, surface bg, copy button) | CodeBlockView with UIPasteboard copy | block-code with clipboard.writeText | YES |
| 38 | Code block shows language label | Text(language) | code-lang span | YES |
| 39 | Code copy shows "Copied" feedback | showCopied state, 1.5s timeout | textContent='Copied!' with setTimeout | YES |
| 40 | Parts list (grid: Item / Specification / Qty) | PartsListView | block-parts with grid columns | YES |
| 41 | Regulation card (purple left border, code/clause/summary) | RegulationView | block-regulation | YES |
| 42 | Table block (headers + rows) | TableBlockView | block-table with table-inner | YES |
| 43 | Callout block (tip/info/important variants) | CalloutView with style parameter | block-callout with callout-border-* | YES |
| 44 | Link block (blue underlined, opens URL) | Link() or Text().underline() | anchor tag with mode-learn color | YES |

### Rating & Feedback (45-49)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 45 | 5-star rating on last assistant message | actionRow with ForEach 1...5 stars | ratingRow with 5 rate-star spans | YES |
| 46 | Star fill changes on tap | userRating state, star.fill vs star | style.color toggle on click | YES |
| 47 | Flag button on last assistant message | flag icon, onFlag("inappropriate") | flag span, apiFlag('inappropriate') | YES |
| 48 | Rating calls POST /rating | TradeGuruAPI.rate() | apiRate() | YES |
| 49 | Feedback calls POST /feedback | TradeGuruAPI.feedback() | apiFlag() | YES |

### Voice & Audio (50-56)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 50 | Mic button when input is empty | mic.fill icon button | micBtnEl with mic SVG | YES |
| 51 | Mic button hidden when text present | send button replaces mic | micBtn display:none when showSend | YES |
| 52 | Recording state shows stop button (red circle) | stop.fill in red circle | Red square in circle SVG | YES |
| 53 | Audio recorded and sent for transcription | AVAudioRecorder → onAudioRecorded → transcribeAudio | MediaRecorder → transcribeAudioBlob | YES |
| 54 | Transcription result fills input field | inputText = transcribedText | inp.value = data.text | YES |
| 55 | TTS speaker button on last assistant message | speaker.wave.2 icon, onSpeak | speaker emoji, apiSpeak | YES |
| 56 | TTS calls POST /audio/speech and plays audio | TradeGuruAPI.speak() → AVAudioPlayer | fetch /audio/speech → new Audio().play() | YES |

### Attachments & Vision (57-62)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 57 | Plus button opens attachment menu (Photo Library / Browse Files) | Menu with PhotosPicker + document button | toggleAttachment with pickImage/pickDocument | YES |
| 58 | Image attachment shows thumbnail preview | Image(uiImage:) 40x40 | img tag 40x40 object-fit cover | YES |
| 59 | Document attachment shows file icon + name | doc.fill icon + fileName text | doc emoji + fileName text | YES |
| 60 | Attachment cancel (rotated plus = X) | plus rotated 45deg, clears state | plus rotated 45deg, clearAttachment() | YES |
| 61 | Image attachment sends via POST /chat/vision | sendWithVision → fetchVisionResponse | apiChatVision with imageBase64 | YES |
| 62 | Document attachment uploads via POST /files/upload then chats | sendWithDocument → uploadDocumentAndChat | apiUploadAndChat with FormData | YES |

### Error Handling & API (63-65)

| # | Behaviour | Swift | HTML | Match |
|---|-----------|-------|------|-------|
| 63 | Error banner (red bg, warning icon, message, dismiss X) | error state → red HStack | htmlError → red div with dismiss | YES |
| 64 | Device registration on launch | registerDeviceIfNeeded → POST /device/register | ensureDevice() on load | YES |
| 65 | Mock mode toggle | APIConfig.useMockData | mockMode toggle button | YES |

---

## API Endpoint Coverage

| Endpoint | Swift Method | HTML Function | Wired |
|----------|-------------|---------------|-------|
| POST /device/register | TradeGuruAPI.registerDevice() | ensureDevice() | YES |
| POST /chat | TradeGuruAPI.chat() | apiChat() | YES |
| POST /chat/vision | TradeGuruAPI.chatVision() | apiChatVision() | YES |
| POST /audio/transcribe | TradeGuruAPI.transcribe() | transcribeAudioBlob() | YES |
| POST /audio/speech | TradeGuruAPI.speak() | apiSpeak() | YES |
| POST /files/upload | TradeGuruAPI.uploadFile() | apiUploadAndChat() | YES |
| POST /rating | TradeGuruAPI.rate() | apiRate() | YES |
| POST /feedback | TradeGuruAPI.feedback() | apiFlag() | YES |

---

## Dark Mode Verification

| Element | Swift Light/Dark | HTML Light/Dark | Match |
|---------|-----------------|-----------------|-------|
| Background | white / #1A1A1C | #FFFFFF / #1A1A1C | YES |
| Surface | #F7F2F9 / #2F2D32 | #F7F2F9 / #2F2D32 | YES |
| Text | #242026 / white | #242026 / #FFFFFF | YES |
| Text Secondary | #6B7280 / #9CA3AF | #6B7280 / #9CA3AF | YES |
| Border | #B8B3BA / #6B7280 | #B8B3BA / #6B7280 | YES |
| Input | #EEE9F0 / #3D3A40 | #EEE9F0 / #3D3A40 | YES |
| Fault Finder | #F59E0B / #FBBF24 | #F59E0B / #FBBF24 | YES |
| Learn | #3B82F6 / #60A5FA | #3B82F6 / #60A5FA | YES |
| Research | #8B5CF6 / #A78BFA | #8B5CF6 / #A78BFA | YES |
| Onboarding pages | Uses same adaptive colors | Uses same CSS vars | YES |

---

## Remaining Gaps

| # | Gap | Severity | Notes |
|---|-----|----------|-------|
| 1 | Onboarding @AppStorage persistence not simulated in HTML | Low | Swift uses @AppStorage to persist onboarding completion across launches. HTML uses in-memory `currentComponent` state — reloading the page does not remember completion. This is acceptable since the HTML is a design preview, not a production app. |

---

## Resolved from Previous Reports

| Gap | Status | Resolution |
|-----|--------|------------|
| deleteConversation() had no UI (Reports #7-8) | RESOLVED | SidebarView now has delete buttons per row + .onDelete modifier; HTML has deleteConversation() with trash icon |
| searchConversations() had no UI (Report #8) | RESOLVED | SidebarView now has search bar with TextField; HTML has search input with filter |
| POST /files/upload had no UI (Report #7) | RESOLVED | Document picker (fileImporter) wired in ChatInputBar; HTML has pickDocument() with file input |

---

## Parity History

| Report # | Date | Overall | Audited | Matched | Fixed | Gaps |
|----------|------|---------|---------|---------|-------|------|
| 6 | 2026-03-15 | 95% PASS | 65 | 60 | 5 | 3 (browser) |
| 7 | 2026-03-15 | 98% PASS | 65 | 64 | 3 | 1 (no UI) |
| 8 | 2026-03-15 | 98% PASS | 65 | 64 | 0 | 1 (no UI) |
| 9 | 2026-03-15 | 99% PASS | 75 | 74 | 0 | 1 (cosmetic) |
