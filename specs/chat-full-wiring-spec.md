# Chat Screen Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Screen | Chat |
| Total inventory items | 65 |
| Already wired | 37 |
| Needs wiring | 16 |
| Target | 100% functional against live API |
| Mock mode | Toggleable via `USE_MOCK_DATA` flag |
| Backend base URL | `https://tradeguru.com/api/v1` |
| API reference | `specs/api-integration-reference.md` |
| Date | 2026-03-14 |

---

## Objective

Wire every button, input, state variable, and component on the chat screen to the live TradeGuru API at `tradeguru.com/api/v1/*`. Convert hardcoded mock data into a toggleable testing mode so developers can switch between real API and mock responses. After implementation, every element on the screen must be fully functional — zero empty action closures, zero placeholder data, zero dead code paths.

---

## Pain Point

The chat screen has 16 items that are incomplete — streaming blocks that accumulate but never render, a photo picker that selects but never uploads, voice input that doesn't exist, rating/feedback APIs that are implemented but have no UI, error states that are captured but never shown, and conversations that vanish on restart. The API client (`TradeGuruAPI.swift`) is fully wired with all 10 endpoints implemented. The gap is between the API layer and the UI layer — the plumbing exists but the faucets aren't connected.

---

## Mock Data Toggle

Before wiring individual items, implement a global mock toggle:

### Swift
- File: `ios/Tradeguruelectrical/Services/APIConfig.swift`
- Add: `static var useMockData = false` (default false for production, true for testing)
- ChatViewModel checks this flag: if true, use MockData responses; if false, call TradeGuruAPI

### HTML
- Add a "Mock Mode" toggle in the workbench controls bar (next to dark/light toggle)
- When enabled, all API-dependent components show mock data (current behavior)
- When disabled, show "Connect to API" placeholder states

---

## Inventory Item Checklist

### 1. Mock Data Toggle Flag

**Pain Point:** No way to switch between real API and mock data for testing. Developers must choose one path at compile time.

| Property | Value |
|----------|-------|
| Type | service |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Services/APIConfig.swift` |
| Swift line(s) | after line 3 |
| HTML location | line ~840 in `preview/chat.html` |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `APIConfig.swift:4` — Add `static var useMockData = false` property
- [ ] `ChatViewModel.swift:63` — In `fetchResponse()`, check `APIConfig.useMockData`. If true, call new `mockResponse(mode:)` that returns MockData blocks after a short delay. If false, call `TradeGuruAPI.chat()` (current behavior).
- [ ] `ChatViewModel.swift:16` — In `init()`, if `APIConfig.useMockData`, populate `conversations` from `MockData.allConversations` and set `activeConversation` to first

**Changes needed (HTML):**
- [ ] `chat.html:~840` — Add `<button id="mockBtn" onclick="toggleMock()">Mock: OFF</button>` after the theme button (inside controls-bar, NOT workbench section)
- [ ] `chat.html:~997` — Add `let mockMode = false;` state variable
- [ ] `chat.html:~1296` — Add `toggleMock()` function that flips `mockMode`, updates button text, and re-renders

**Note:** Keep mock toggle simple — a single boolean flag, not a protocol/DI pattern.

---

### 2. Streaming Blocks Progressive Rendering

**Pain Point:** When the API streams content blocks, they accumulate in `streamingBlocks` but the UI only shows completed messages. Users see nothing until the entire response finishes.

| Property | Value |
|----------|-------|
| Type | state / component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatView.swift` |
| Swift line(s) | 33-43 |
| HTML location | N/A (HTML uses static mock) |
| API endpoint | `/chat` (SSE stream) |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `ChatView.swift:33-43` — After the `ForEach(conversation.messages)` block, add a conditional section: if `viewModel.isStreaming && !viewModel.streamingBlocks.isEmpty`, render a temporary assistant `MessageBubble`-style view showing `viewModel.streamingBlocks` progressively
- [ ] `ChatView.swift:33-43` — Add a typing indicator (e.g., pulsing dots) when `viewModel.isStreaming && viewModel.streamingBlocks.isEmpty` (waiting for first block)

**Changes needed (HTML):**
- [ ] `chat.html:~1224` — Add a simulated streaming indicator div (animated dots) that shows when mock mode is off and "sending" state is active

**Note:** The `streamingBlocks` array is already populated correctly by `fetchResponse()`. This is purely a UI rendering gap.

---

### 3. Error State Display

**Pain Point:** When the API returns an error, `viewModel.error` is set but nothing in the UI reads it. Users get no feedback when requests fail.

| Property | Value |
|----------|-------|
| Type | state / component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatView.swift` |
| Swift line(s) | 18-57 |
| HTML location | N/A |
| API endpoint | All endpoints |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `ChatView.swift:~57` — After the chat content `VStack`, add an error banner: if `viewModel.error != nil`, show a dismissible red banner with the error message and a "Retry" button
- [ ] `ChatViewModel.swift:11` — Ensure `error` is cleared on new send (already done at line 73) and add a `dismissError()` method that sets `error = nil`

**Changes needed (HTML):**
- [ ] `chat.html:~1224` — Add error banner HTML in `renderFullChat()` that shows when an error state variable is set

**Note:** Error strings already come from the API via `StreamErrorPayload.message` and HTTP error body parsing.

---

### 4. Photo Attachment Processing

**Pain Point:** The PhotosPicker selects an item and toggles the `attachmentActive` state, but the selected `PhotosPickerItem` is never loaded as `Data`, never converted to a `MessageAttachment`, and `onSend` always passes `nil` for attachments.

| Property | Value |
|----------|-------|
| Type | input / service |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatInputBar.swift` |
| Swift line(s) | 9-10, 40-55, 75-77 |
| HTML location | line ~1081 in `preview/chat.html` |
| API endpoint | `/files/upload` then `/chat/vision` |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `ChatInputBar.swift:9` — Add `@State private var loadedImageData: Data?` and `@State private var loadedFileName: String?`
- [ ] `ChatInputBar.swift:49-55` — In the `.onChange(of: selectedItem)` handler, after setting `attachmentActive = true`, load the photo data: `Task { if let data = try? await newItem?.loadTransferable(type: Data.self) { loadedImageData = data; loadedFileName = "photo.jpg" } }`
- [ ] `ChatInputBar.swift:75-77` — Change the send button action: if `loadedImageData != nil`, create a `MessageAttachment(type: .image, fileName: loadedFileName ?? "photo.jpg", fileSize: loadedImageData?.count, thumbnailData: loadedImageData)` and pass it in the attachments array to `onSend`
- [ ] `ChatInputBar.swift:24-28` — In the cancel button action, also clear `loadedImageData = nil; loadedFileName = nil`
- [ ] `ChatView.swift:63-65` — In the `onSend` closure, detect if attachments contain images. If so, call a new `viewModel.sendWithVision()` method instead of `viewModel.send()`
- [ ] `ChatViewModel.swift` — Add `sendWithVision(_ text:, mode:, attachments:)` method that uploads the image via `TradeGuruAPI.uploadFile()`, then calls `TradeGuruAPI.chatVision()` with the image data in the messages array

**Changes needed (HTML):**
- [ ] `chat.html:~1081` — Add `onclick="alert('Photo picker — simulated in HTML preview')"` to the add-btn

**Note:** The `TradeGuruAPI.chatVision()` and `TradeGuruAPI.uploadFile()` methods already exist and are fully implemented.

---

### 5. Voice Input Button

**Pain Point:** There is no microphone button in the input bar. The API has `/audio/transcribe` and `TradeGuruAPI.transcribe()` is fully implemented, but there's no way for users to trigger voice input.

| Property | Value |
|----------|-------|
| Type | button / service |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatInputBar.swift` |
| Swift line(s) | 73-85 (next to send button area) |
| HTML location | line ~1081 in `preview/chat.html` |
| API endpoint | `/audio/transcribe` |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `ChatInputBar.swift:7` — Add `var onVoiceInput: ((String) -> Void)?` callback prop
- [ ] `ChatInputBar.swift:9` — Add `@State private var isRecording = false`
- [ ] `ChatInputBar.swift:73` — When `text.isEmpty && !isRecording`, show a microphone button (`mic.fill` SF Symbol) instead of the send button. When tapped, start recording with AVAudioRecorder.
- [ ] `ChatInputBar.swift` — Add recording logic: start/stop AVAudioRecorder, on stop send audio data to `TradeGuruAPI.transcribe()`, inject returned text into the text field via `onVoiceInput` callback
- [ ] `ChatView.swift:60-70` — Pass `onVoiceInput` closure that sets `inputText` to the transcribed text
- [ ] Add `NSMicrophoneUsageDescription` to Info.plist keys (via `INFOPLIST_KEY_NSMicrophoneUsageDescription` in build settings)

**Changes needed (HTML):**
- [ ] `chat.html:~1081` — Add a mic button SVG next to the send button area that shows when input is empty
- [ ] `chat.html:~838` — Add `block-voice-input` option to component picker dropdown (Mandatory Rule #2)

**Note:** Audio recording requires microphone permission. Use AVAudioRecorder for recording, capture as m4a format, send via `TradeGuruAPI.transcribe()`.

---

### 6. Rating UI on Assistant Messages

**Pain Point:** `ChatViewModel.rateLastResponse()` exists and calls `TradeGuruAPI.rate()`, but there is no UI element to trigger it. Users cannot rate responses.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Views/MessageBubble.swift` |
| Swift line(s) | 44-53 (assistant bubble area) |
| HTML location | inside `htmlMessage()` ~1204 |
| API endpoint | `/rating` |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `MessageBubble.swift:3` — Add `var onRate: ((Int) -> Void)?` callback and `var isLastAssistantMessage: Bool = false`
- [ ] `MessageBubble.swift:44-53` — After the assistant bubble content, if `isLastAssistantMessage`, show a row of 1-5 star buttons (SF Symbol `star` / `star.fill`). On tap, call `onRate?(stars)`
- [ ] `ChatView.swift:37-39` — When creating `MessageBubble` for assistant messages, pass `isLastAssistantMessage` (true if it's the last message and role is assistant) and `onRate` closure that calls `Task { await viewModel.rateLastResponse(stars: stars) }`

**Changes needed (HTML):**
- [ ] `chat.html:~1204` — In `htmlMessage()` for assistant messages, add a star rating row below the meta line: 5 star spans with onclick handlers

**Note:** Only show rating on the last assistant message to keep UI clean. `lastResponseId` is already tracked in ChatViewModel.

---

### 7. Feedback/Flag UI on Assistant Messages

**Pain Point:** `TradeGuruAPI.feedback()` is fully implemented but there's no UI to flag problematic responses.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Views/MessageBubble.swift` |
| Swift line(s) | 44-53 (assistant bubble area) |
| HTML location | inside `htmlMessage()` ~1204 |
| API endpoint | `/feedback` |
| Blocked by | #6 (rating UI — share the same action row) |

**Changes needed (Swift):**
- [ ] `MessageBubble.swift` — Next to the star rating row, add a flag button (SF Symbol `flag`). On tap, show an action sheet with feedback reasons: "Incorrect", "Harmful", "Unhelpful", "Other"
- [ ] `ChatViewModel.swift` — Add `flagLastResponse(reason:detail:)` method that calls `TradeGuruAPI.feedback()`
- [ ] `ChatView.swift` — Pass `onFlag` closure to MessageBubble

**Changes needed (HTML):**
- [ ] `chat.html:~1204` — Add a flag icon button next to the star rating row

**Note:** Bundle with #6 in the same action row. Keep it unobtrusive — small icon, sheet for details.

---

### 8. Link Block Tap Action

**Pain Point:** Link blocks in assistant messages show styled underlined text but are not tappable. Users see links but can't open them.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Views/MessageBubble.swift` |
| Swift line(s) | 101-105 |
| HTML location | line ~1187 in `preview/chat.html` |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `MessageBubble.swift:101-105` — Replace the static `Text` with a `Link` view or a `Button` that opens the URL via `UIApplication.shared.open()`. Use `if let urlString = block.url, let url = URL(string: urlString)` guard.

**Changes needed (HTML):**
- [ ] Already working — HTML uses `<a>` tag with `href`. No changes needed.

**Note:** HTML is ahead of Swift here — the `<a>` tag already works.

---

### 9. Code Block Copy Button

**Pain Point:** Code blocks display content in monospace but users cannot copy the text. Electrical formulas, cable sizing tables, and code references need to be easily copyable.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Views/Blocks/CodeBlockView.swift` |
| Swift line(s) | 7-27 |
| HTML location | line ~1156 in `preview/chat.html` |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `CodeBlockView.swift:8-15` — Add a copy button (SF Symbol `doc.on.doc`) in the top-right corner (aligned with the language label). On tap, copy `content` to `UIPasteboard.general.string` and briefly show a "Copied" confirmation.

**Changes needed (HTML):**
- [ ] `chat.html:~1156` — Add a copy button in the `.block-code` div: `<button class="code-copy-btn" onclick="navigator.clipboard.writeText(this.parentElement.textContent)">Copy</button>` with appropriate styling

**Note:** Simple clipboard copy. No framework needed.

---

### 10. Scroll to Bottom on New Message

**Pain Point:** When new messages arrive (user sends or assistant responds), the ScrollView doesn't scroll to the latest message. Users must manually scroll down.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatView.swift` |
| Swift line(s) | 35-43 |
| HTML location | N/A (HTML is static) |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] `ChatView.swift:35` — Add `ScrollViewReader` wrapping the `ScrollView`. Add an anchor `EmptyView` with `.id("bottom")` after the `ForEach` in `LazyVStack`
- [ ] `ChatView.swift:35-43` — Add `.onChange(of: viewModel.activeConversation?.messages.count)` that scrolls to "bottom" with animation
- [ ] `ChatView.swift:35-43` — Also scroll to bottom when `viewModel.streamingBlocks` changes (during streaming)

**Changes needed (HTML):**
- [ ] No changes needed — HTML preview is static

**Note:** Use `scrollTo("bottom", anchor: .bottom)` with `.easeOut` animation.

---

### 11. Photo Attachment Preview

**Pain Point:** When a photo is selected via PhotosPicker, `attachmentActive` toggles the plus icon to an X, but the user never sees the selected photo. There's no thumbnail preview.

| Property | Value |
|----------|-------|
| Type | component |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ChatInputBar.swift` |
| Swift line(s) | 22-56 |
| HTML location | N/A |
| API endpoint | N/A |
| Blocked by | #4 (photo data loading) |

**Changes needed (Swift):**
- [ ] `ChatInputBar.swift:22-56` — When `attachmentActive && loadedImageData != nil`, show a small thumbnail preview (40x40) of the selected image between the cancel button and the text field. Use `if let data = loadedImageData, let uiImage = UIImage(data: data) { Image(uiImage: uiImage).resizable().scaledToFill().frame(width: 40, height: 40).clipShape(.rect(cornerRadius: 8)) }`

**Changes needed (HTML):**
- [ ] No changes needed

**Note:** Depends on #4 for the `loadedImageData` state to exist.

---

### 12. Conversation Persistence

**Pain Point:** Conversations exist only in memory. When the app restarts, all conversations are lost. The sidebar shows an empty list for new users.

| Property | Value |
|----------|-------|
| Type | service / state |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` |
| Swift line(s) | 6, 16-19 |
| HTML location | N/A |
| API endpoint | N/A (local storage) |
| Blocked by | nothing |

**Changes needed (Swift):**
- [ ] Create `ios/Tradeguruelectrical/Services/ConversationStore.swift` — simple JSON file persistence using `FileManager`. Methods: `save([Conversation])`, `load() -> [Conversation]`, using app's documents directory
- [ ] `ChatViewModel.swift:16-19` — In `init()`, load conversations from `ConversationStore.load()` instead of starting empty
- [ ] `ChatViewModel.swift:138-145` — In `updateConversation()`, after updating the array, call `ConversationStore.save(conversations)` to persist
- [ ] `ChatViewModel.swift:45-52` — In `newConversation()`, after inserting, persist

**Changes needed (HTML):**
- [ ] No changes — HTML uses hardcoded MOCK data

**Note:** Use simple JSON file storage, not SwiftData. Keep it minimal — `Conversation` and its nested types are already `Codable`.

---

### 13. New Chat Button (HTML)

**Pain Point:** The new chat (compose) button in the HTML nav bar has no onclick handler. Swift version works, HTML doesn't match.

| Property | Value |
|----------|-------|
| Type | button |
| Status | empty |
| Swift file | N/A |
| Swift line(s) | N/A |
| HTML location | `htmlNavBar()` ~line 1097 |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- None — already wired

**Changes needed (HTML):**
- [ ] `chat.html:~1097` — Add `onclick="newChat()"` to the compose button
- [ ] `chat.html:~1296` — Add `newChat()` function that resets messages to empty, dismisses sidebar, shows empty state with watermark

---

### 14. Sidebar Conversation Selection (HTML)

**Pain Point:** Clicking a conversation in the HTML sidebar only closes the sidebar. It doesn't load that conversation's messages into the chat view.

| Property | Value |
|----------|-------|
| Type | button |
| Status | partial |
| Swift file | N/A |
| Swift line(s) | N/A |
| HTML location | `htmlSidebar()` ~line 1115 |
| API endpoint | N/A |
| Blocked by | nothing |

**Changes needed (Swift):**
- None — already wired

**Changes needed (HTML):**
- [ ] `chat.html:~1115` — Change sidebar row onclick to `selectConversation('${c.id}')`
- [ ] `chat.html:~993` — Add `let currentConversation = 0;` state variable
- [ ] `chat.html:~1296` — Add `selectConversation(id)` function that sets `currentConversation` to the matching index and re-renders
- [ ] `chat.html:~1221` — In `renderFullChat()`, use `MOCK.conversations[currentConversation]` instead of hardcoded `[0]`

---

### 15. Send Button Action (HTML)

**Pain Point:** The HTML send button shows/hides based on input content but doesn't simulate sending a message. Swift sends to real API.

| Property | Value |
|----------|-------|
| Type | button |
| Status | empty |
| Swift file | N/A |
| Swift line(s) | N/A |
| HTML location | `htmlInputBar()` ~line 1085 |
| API endpoint | N/A |
| Blocked by | #14 (need conversation selection to know where to add message) |

**Changes needed (Swift):**
- None — already wired

**Changes needed (HTML):**
- [ ] `chat.html:~1085` — Add `onclick="sendMessage()"` to send button
- [ ] `chat.html:~1296` — Add `sendMessage()` function that reads input value, adds a user message object to the current mock conversation's messages array, clears input, re-renders, and optionally shows a "waiting for API" placeholder

---

### 16. Text-to-Speech Playback

**Pain Point:** `TradeGuruAPI.speak()` is implemented but there's no UI to trigger text-to-speech on assistant message content.

| Property | Value |
|----------|-------|
| Type | component / service |
| Status | needs-backend |
| Swift file | `ios/Tradeguruelectrical/Views/MessageBubble.swift` |
| Swift line(s) | 44-53 |
| HTML location | N/A |
| API endpoint | `/audio/speech` |
| Blocked by | #6 (share the action row) |

**Changes needed (Swift):**
- [ ] `MessageBubble.swift` — Add a speaker button (SF Symbol `speaker.wave.2`) in the assistant message action row. On tap, extract all text content from blocks, call `TradeGuruAPI.speak()`, play returned audio data with `AVAudioPlayer`
- [ ] `ChatViewModel.swift` — Add `speakMessage(_ message: ChatMessage)` method that concatenates text blocks and calls the speak API

**Changes needed (HTML):**
- [ ] No changes needed for MVP

**Note:** Lower priority — nice-to-have for accessibility. Bundle with #6/#7 action row.

---

## Wired Items (no changes needed)

These items are fully functional:

| # | Item | File | Status |
|---|------|------|--------|
| W1 | Hamburger menu button | `ChatView.swift:113` | wired |
| W2 | New chat button (Swift) | `ChatView.swift:126` | wired |
| W3 | Mode card dismiss | `ModeInfoCard.swift:29` | wired |
| W4 | Fault Finder mode tab | `ModeSelector.swift:9` | wired |
| W5 | Learn mode tab | `ModeSelector.swift:9` | wired |
| W6 | Research mode tab | `ModeSelector.swift:9` | wired |
| W7 | Send button (Swift) | `ChatInputBar.swift:75` | wired — calls TradeGuruAPI.chat() via ViewModel |
| W8 | Attachment cancel | `ChatInputBar.swift:24` | wired |
| W9 | Message text field | `ChatInputBar.swift:58` | wired |
| W10 | ModeInfoCard component | `ModeInfoCard.swift` | wired |
| W11 | ModeSelector component | `ModeSelector.swift` | wired |
| W12 | ChatInputBar component | `ChatInputBar.swift` | wired (partial — attachment incomplete) |
| W13 | MessageBubble component | `MessageBubble.swift` | wired |
| W14 | All 8 block renderers | `Views/Blocks/*.swift` | wired |
| W15 | SidebarView component | `SidebarView.swift` | wired |
| W16 | TradeGuruAPI service | `TradeGuruAPI.swift` | wired — all 10 endpoints |
| W17 | APIConfig service | `APIConfig.swift` | wired |
| W18 | StreamParser service | `StreamParser.swift` | wired |
| W19 | DeviceManager service | `DeviceManager.swift` | wired |
| W20 | Device registration | `ChatViewModel.swift:110` | wired |
| W21 | Rate API method | `ChatViewModel.swift:125` | wired (no UI trigger) |

---

## Implementation Order

Items must be implemented in this order (respects dependencies):

| Phase | Items | Description | Dependency |
|-------|-------|-------------|------------|
| 1 | #1 | Mock toggle flag in APIConfig + ViewModel + HTML | None |
| 2 | #2, #3 | Streaming progressive render + error display | None |
| 3 | #4, #11 | Photo attachment loading + thumbnail preview | None (then #11 depends on #4) |
| 4 | #10 | Scroll to bottom | None |
| 5 | #8, #9 | Link tappable + code copy | None |
| 6 | #6, #7, #16 | Rating + feedback + TTS action row | None |
| 7 | #5 | Voice input button + recording | None |
| 8 | #12 | Conversation persistence | None |
| 9 | #13, #14, #15 | HTML parity fixes | #1 (mock toggle) |

---

## Verification

After all items are wired:
- [ ] Every `Button(action:)` in ChatView.swift has a non-empty closure
- [ ] Every `Button(action:)` in ChatInputBar.swift has a non-empty closure
- [ ] Every `Button(action:)` in SidebarView.swift has a non-empty closure
- [ ] ChatViewModel.send() calls TradeGuruAPI.chat() (not mock) when `useMockData = false`
- [ ] ChatViewModel.send() returns MockData blocks when `useMockData = true`
- [ ] Mock mode toggle works in both Swift and HTML
- [ ] Photo picker -> loads Data -> creates MessageAttachment -> sends via TradeGuruAPI.chatVision()
- [ ] Photo thumbnail preview shows in input bar when image selected
- [ ] Speak button -> records audio -> TradeGuruAPI.transcribe() -> text injected
- [ ] Rating stars can be submitted on last assistant message -> TradeGuruAPI.rate()
- [ ] Feedback flag can be submitted on last assistant message -> TradeGuruAPI.feedback()
- [ ] New chat creates empty conversation (not mock) when `useMockData = false`
- [ ] Sidebar loads real conversation history from persistence
- [ ] Error states display as dismissible banner when API fails
- [ ] Streaming blocks render progressively during response with typing indicator
- [ ] Link blocks open URLs in Safari when tapped
- [ ] Code blocks have a copy button that copies to clipboard
- [ ] ScrollView auto-scrolls to bottom on new messages
- [ ] Conversations persist across app restarts via JSON file storage
- [ ] HTML preview matches Swift for every component
- [ ] HTML new chat button creates empty conversation
- [ ] HTML sidebar row selection loads conversation messages
- [ ] HTML send button simulates message sending
