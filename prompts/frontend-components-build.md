# Agent Prompt: Build All 15 Frontend Components — SwiftUI + HTML Component Viewer

You are building the complete frontend component set for TradeGuruElectrical, a native Swift/SwiftUI iOS app for electrical fault-finding with AI. You must produce TWO outputs for every component: real SwiftUI code AND an HTML preview with a multi-device component viewer.

## Why Two Outputs

This project runs on Windows/WSL with no local Xcode. The SwiftUI code is the real app code that will compile on a Mac or cloud build. The HTML preview lets the developer instantly see every component in a browser across iPhone, iPad, and Apple Watch frames — without needing Xcode. Both outputs must be pixel-identical representations of the same design. All components use **mock data** since the backend is not yet deployed.

## Reference Materials — READ ALL BEFORE WRITING CODE

You MUST read every one of these files before writing a single line:

- `CLAUDE.md` — Project rules, SwiftUI conventions, anti-patterns, decision trees. FOLLOW ALL RULES.
- `rork.json` — App name: TradeGuruElectrical, framework: swift, path: ios
- `screens/chat/inventory.md` — Complete inventory of the chat screen: every button, icon, input, component, state variable, and what needs building. THIS IS YOUR CHECKLIST.
- `mocks/chat/chatmock.md` — Mock analysis with spacing, fonts, colors
- `mocks/chat/chatmock.png` — Visual reference mock
- `assets/branding/tradeguru-logo.png` — TradeGuru logo (transparent)
- `ios/Tradeguruelectrical/ChatView.swift` — Existing chat screen (read to understand current structure)
- `ios/Tradeguruelectrical/ChatInputBar.swift` — Existing input bar
- `ios/Tradeguruelectrical/ModeInfoCard.swift` — Existing mode card
- `ios/Tradeguruelectrical/ModeSelector.swift` — Existing mode selector
- `ios/Tradeguruelectrical/ThinkingMode.swift` — Existing ThinkingMode enum
- `ios/Tradeguruelectrical/TradeGuruColors.swift` — Existing color tokens
- `specs/add-relay-endpoints-to-tradeguru.md` — Backend plan (read to understand the `{ blocks: [...] }` JSON contract your models must match)

## The 15 Components to Build

Cross-reference each item against `screens/chat/inventory.md` Priority 1/2/3. Every item below maps to an inventory need.

### Models (4 files — nonisolated, Codable)

**1. `ios/Tradeguruelectrical/Models/ContentBlock.swift`**
The structured JSON contract — must match `types/v1/blocks.ts` from the backend plan.

```swift
nonisolated enum ContentBlockType: String, Codable, CaseIterable {
    case text
    case heading
    case stepList = "step_list"
    case warning
    case code
    case partsList = "parts_list"
    case regulation
    case diagramRef = "diagram_ref"
    case toolCall = "tool_call"
    case table
    case callout
    case link
}

nonisolated struct ContentBlock: Codable, Identifiable {
    let id: UUID
    let type: ContentBlockType
    var content: String?
    var title: String?
    var steps: [String]?
    var items: [PartsItem]?
    var language: String?
    var code: String?         // for regulation
    var clause: String?       // for regulation
    var summary: String?      // for regulation
    var url: String?          // for link
    var rows: [[String]]?     // for table
    var headers: [String]?    // for table
    var level: Int?           // for heading (1-3)
    var style: String?        // for callout: "tip", "info", "important"
}

nonisolated struct PartsItem: Codable, Identifiable {
    let id: UUID
    let name: String
    let spec: String
    let qty: Int
}
```

**2. `ios/Tradeguruelectrical/Models/ChatMessage.swift`**
Maps to inventory Priority 1 item #1.

```swift
nonisolated enum MessageRole: String, Codable {
    case user
    case assistant
}

nonisolated struct ChatMessage: Codable, Identifiable {
    let id: UUID
    let role: MessageRole
    let blocks: [ContentBlock]
    let timestamp: Date
    let mode: ThinkingMode
    var attachments: [MessageAttachment]?
}

nonisolated struct MessageAttachment: Codable, Identifiable {
    let id: UUID
    let type: AttachmentType
    let fileName: String
    let fileSize: Int?
    let thumbnailData: Data?
}

nonisolated enum AttachmentType: String, Codable {
    case image
    case video
    case document
}
```

**3. `ios/Tradeguruelectrical/Models/Conversation.swift`**
Needed by sidebar for conversation history.

```swift
nonisolated struct Conversation: Codable, Identifiable {
    let id: UUID
    var title: String
    var messages: [ChatMessage]
    var mode: ThinkingMode
    let createdAt: Date
    var updatedAt: Date
}
```

**4. `ios/Tradeguruelectrical/Models/MockData.swift`**
Provides realistic mock conversations for all 3 modes so the UI can be demoed without a backend. Include at least:
- 1 Fault Finder conversation with: text blocks, a step_list, a warning, a parts_list, and a regulation block
- 1 Learn conversation with: text, heading, code block, callout, and a table
- 1 Research conversation with: text, heading, link, table, and a regulation
- Each conversation should have 3-4 message exchanges (user + assistant)
- Make the content realistic for an Australian electrician (AS/NZS 3000 references, RCD testing, cable sizing, etc.)

### ViewModel (1 file)

**5. `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift`**
Maps to inventory Priority 1 item #2. `@Observable` class.

```swift
@Observable
@MainActor
class ChatViewModel {
    var conversations: [Conversation] = []
    var activeConversation: Conversation?
    var isStreaming = false
    var streamingBlocks: [ContentBlock] = []

    init() {
        // Load mock data for demo
        conversations = MockData.allConversations
        activeConversation = conversations.first
    }

    func send(_ text: String, mode: ThinkingMode, attachments: [MessageAttachment]? = nil) {
        // Create user message
        // Append to active conversation
        // Simulate streaming response from mock data
        // Use Task.sleep(for:) to simulate streaming delay
    }

    func newConversation(mode: ThinkingMode) {
        // Create empty conversation, set as active
    }

    func selectConversation(_ conversation: Conversation) {
        activeConversation = conversation
    }
}
```

### Services (1 file)

**6. `ios/Tradeguruelectrical/Services/DeviceManager.swift`**
Stores device_id in Keychain for future `/api/v1/device/register`.

```swift
nonisolated struct DeviceManager {
    static func getOrCreateDeviceId() -> String
    // Check Keychain for existing device_id
    // If not found, generate UUID, store in Keychain, return it
    // Use Security framework (SecItemAdd/SecItemCopyMatching)
}
```

### Block Renderer Views (8 files)

All block views live in `ios/Tradeguruelectrical/Views/Blocks/`. Each takes a `ContentBlock` as input and renders it with the app's color tokens.

**7. `ios/Tradeguruelectrical/Views/Blocks/TextBlockView.swift`**
Renders `ContentBlock` where type == `.text`. Simple `Text(block.content)` with `.tradeText` color, 15pt regular.

**8. `ios/Tradeguruelectrical/Views/Blocks/StepListView.swift`**
Renders `.stepList`. Title in 16pt semibold, numbered steps with circle indicators, 12pt spacing. Use `.tradeSurface` background, 12pt corner radius.

**9. `ios/Tradeguruelectrical/Views/Blocks/WarningCardView.swift`**
Renders `.warning`. Yellow/amber card with `exclamationmark.triangle.fill` icon, `.modeFaultFinder` tint. Bold title "Warning" or "Danger" based on content keywords. 12pt corner radius, 1pt border.

**10. `ios/Tradeguruelectrical/Views/Blocks/CodeBlockView.swift`**
Renders `.code`. Monospace font (`.system(.body, design: .monospaced)`), dark background (`.tradeSurface`), 8pt corner radius, horizontal scroll for long lines. Show language label top-right if `block.language` is set.

**11. `ios/Tradeguruelectrical/Views/Blocks/PartsListView.swift`**
Renders `.partsList`. Table with 3 columns: Item, Spec, Qty. Header row in semibold. Alternating row backgrounds. 12pt corner radius wrapper.

**12. `ios/Tradeguruelectrical/Views/Blocks/RegulationView.swift`**
Renders `.regulation`. Styled reference card: code citation (e.g. "AS/NZS 3000:2018") in bold, clause number, and summary. Left accent border (4pt, `.modeResearch` color). `.tradeSurface` background.

**13. `ios/Tradeguruelectrical/Views/Blocks/TableBlockView.swift`**
Renders `.table`. Generic table with `headers` and `rows`. Horizontal scroll for wide tables. Header row bold with `.tradeSurface` bg. Grid lines using `.tradeBorder`.

**14. `ios/Tradeguruelectrical/Views/Blocks/CalloutView.swift`**
Renders `.callout`. Tip/info/important box. Icon varies: `lightbulb.fill` (tip), `info.circle.fill` (info), `exclamationmark.circle.fill` (important). Tinted left border (4pt). `.tradeSurface` bg, 12pt corner radius.

### Composite Views (2 files)

**15. `ios/Tradeguruelectrical/Views/MessageBubble.swift`**
Renders a single `ChatMessage`. User messages: right-aligned, `.tradeGreen` background, white text (simple text only, no blocks). Assistant messages: left-aligned, iterates `message.blocks` and renders each with the appropriate block view using a `switch block.type` statement. Show small timestamp below each bubble. Show mode icon next to assistant messages.

**16. `ios/Tradeguruelectrical/Views/SidebarView.swift`**
Slide-in drawer from left (triggered by hamburger button in ChatView). Shows:
- Header: "Conversations" title + close button (`xmark`)
- List of conversations: each row shows title, mode icon, date, message count
- Tapping a row calls `viewModel.selectConversation()`
- "New Conversation" button at bottom
- Animated slide-in/out with `.tradeBg` background, 300pt width

### Update Existing Files

**17. Update `ios/Tradeguruelectrical/ChatView.swift`**
- Add `@State private var viewModel = ChatViewModel()`
- Add `@State private var showSidebar = false`
- Wire hamburger button → `showSidebar = true`
- Wire new chat button → `viewModel.newConversation(mode: selectedMode)`
- Replace empty content area with `ScrollView` + `ForEach(viewModel.activeConversation.messages)` rendering `MessageBubble` for each
- Add sidebar overlay: `if showSidebar { SidebarView(...) }`
- Wire send in ChatInputBar to `viewModel.send()`
- Keep all existing layout structure — just fill in the empty areas

**18. Update `ios/Tradeguruelectrical/ChatInputBar.swift`**
- Add `var onSend: (String, [MessageAttachment]?) -> Void` parameter
- Wire send button → call `onSend(text, attachments)` then clear text
- Change placeholder from `"Message"` to `"Ask TradeGuru"`

## Output: HTML Component Viewer

Update `preview/chat.html` to become a **full component viewer** with these features:

### Device Frames
A dropdown at the top of the page (OUTSIDE any device frame) with options:
- **iPhone 15 Pro** (393×852 viewport)
- **iPad Pro 11"** (834×1194 viewport)
- **Apple Watch Series 9** (198×242 viewport)

Switching the dropdown re-renders the current view inside the selected device frame with appropriate scaling.

### Component Picker
A second dropdown: "View Component" with options:
- **Full Chat Screen** (default) — the complete chat with mock conversation
- **Message Bubble (User)** — isolated user message bubble
- **Message Bubble (Assistant)** — isolated assistant message with all block types
- **Text Block** — isolated text block
- **Step List** — isolated step list with mock steps
- **Warning Card** — isolated warning card
- **Code Block** — isolated code block with mock code
- **Parts List** — isolated parts list table
- **Regulation Card** — isolated regulation citation
- **Table Block** — isolated generic table
- **Callout (tip/info/important)** — all 3 callout variants
- **Sidebar** — the conversation history drawer
- **Mode Selector** — the 3 mode tabs
- **Chat Input Bar** — the input area

### Mock Data
The HTML must include the same mock data as `MockData.swift` — same conversations, same block content, same text. When viewing "Full Chat Screen", show a full mock conversation with all block types visible.

### Dark/Light Toggle
A button to toggle between light and dark mode using the same color tokens from `TradeGuruColors.swift`:

```css
:root {
    --trade-green: #20AB6E;
    --trade-surface: #F7F2F9;
    --trade-input: #EEE9F0;
    --trade-light: #FFFCFF;
    --trade-border: #B8B3BA;
    --trade-text: #242026;
    --trade-text-secondary: #6B7280;
    --trade-bg: #FFFFFF;
    --mode-fault-finder: #F59E0B;
    --mode-learn: #3B82F6;
    --mode-research: #8B5CF6;
}

[data-theme="dark"] {
    --trade-surface: #2F2D32;
    --trade-input: #3D3A40;
    --trade-light: #2F2D32;
    --trade-border: #6B7280;
    --trade-text: #FFFFFF;
    --trade-text-secondary: #9CA3AF;
    --trade-bg: #1A1A1C;
    --mode-fault-finder: #FBBF24;
    --mode-learn: #60A5FA;
    --mode-research: #A78BFA;
}
```

### Requirements
- Fully self-contained HTML file — no external dependencies
- Embed TradeGuru logo as base64 (read `assets/branding/tradeguru-logo.png` and encode)
- Use `-apple-system` font family
- All interactive: mode selector cycles, input types, sidebar slides, dark/light toggles
- Device frame has rounded corners, notch/dynamic island, and subtle shadow
- Apple Watch frame shows simplified layout (no sidebar, compact blocks)

## SwiftUI Rules (MUST follow — from CLAUDE.md)

1. Use `.foregroundStyle()` not `.foregroundColor()`
2. Use `.clipShape(.rect(cornerRadius: X))` not `.cornerRadius()`
3. Use `NavigationStack` not `NavigationView`
4. Use `Button` for all tappable elements, not `.onTapGesture`
5. Mark ALL model structs and enums `nonisolated` (ContentBlock, ChatMessage, Conversation, etc.)
6. Do NOT mark views or view models `nonisolated`
7. Keep 44pt minimum tap targets
8. Support Dark Mode using the existing `TradeGuruColors.swift` semantic colors
9. Use `@Observable` with `@State` ownership — not `ObservableObject`/`@StateObject`
10. Never declare `@State` for data owned by a parent — use `let` or `@Binding`
11. Use `.onChange(of:) { oldValue, newValue in }` two-parameter form
12. Prefer `let` over `var` unless mutation is required
13. One major Swift type per file
14. No comments unless logic is non-obvious
15. Apply `.padding(.horizontal)` to content inside vertical scroll views

## REMOVED (do NOT include)

- No bottom tab bar
- No "Create Images", "Edit Image", "Try Voice Mode" buttons
- No footer attribution
- No real API calls — ALL data comes from MockData.swift
- No backend service files (TradeGuruAPI.swift, StreamParser.swift, etc.)
- No RevenueCat or IAP code
- No user authentication

## Execution Steps

1. Read ALL reference files listed above
2. Create the 4 model files (ContentBlock, ChatMessage, Conversation, MockData)
3. Create ChatViewModel
4. Create DeviceManager
5. Create all 8 block renderer views
6. Create MessageBubble and SidebarView
7. Update ChatView.swift and ChatInputBar.swift
8. Update `preview/chat.html` with the component viewer
9. Run `/design-parity` to verify Swift ↔ HTML match
10. Fix any parity issues found
11. Confirm all 15 inventory items are addressed

## Verification Checklist

After writing ALL files, verify:

- [ ] 1. No `.foregroundColor()` usage in any file
- [ ] 2. No `.cornerRadius()` usage in any file
- [ ] 3. No `NavigationView` — only `NavigationStack`
- [ ] 4. All 4 model files have `nonisolated` on structs/enums
- [ ] 5. ChatViewModel uses `@Observable` not `ObservableObject`
- [ ] 6. ChatViewModel uses `@State` in ChatView, not `@StateObject`
- [ ] 7. MockData contains realistic Australian electrical content
- [ ] 8. Every ContentBlockType has a corresponding block view in `Views/Blocks/`
- [ ] 9. MessageBubble switches on ALL 12 block types
- [ ] 10. SidebarView lists conversations from ChatViewModel
- [ ] 11. ChatView hamburger button opens sidebar
- [ ] 12. ChatView send button calls viewModel.send()
- [ ] 13. ChatInputBar placeholder says "Ask TradeGuru" (not "Message")
- [ ] 14. HTML component viewer has device dropdown (iPhone, iPad, Watch)
- [ ] 15. HTML component viewer has component picker with all 15 items
- [ ] 16. HTML has dark/light toggle with matching color tokens
- [ ] 17. HTML mock data matches Swift MockData content
- [ ] 18. All block views use TradeGuruColors tokens, not hardcoded colors
- [ ] 19. `/design-parity` has been run and all issues fixed
- [ ] 20. `screens/chat/inventory.md` Priority 1 items #1-#4 are addressed
