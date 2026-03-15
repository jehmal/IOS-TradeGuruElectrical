# SwiftData Migration Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Target | SwiftData persistence layer (replaces ConversationStore JSON) |
| Total inventory items | 16 |
| Already wired | 0 |
| Needs wiring | 16 |
| Outcome | Search, delete, pagination, auto-save, future CloudKit sync |
| Mock mode | Preserved — `APIConfig.useMockData` continues to work |
| Date | 2026-03-15 |

---

## Objective

Replace the flat JSON persistence layer (`ConversationStore.swift`) with SwiftData `@Model` classes. After implementation: conversations auto-persist, search via `#Predicate`, delete with `modelContext.delete()`, and conversations are capped/paginated. Zero manual save calls. Zero JSON encode/decode for storage. ConversationStore.swift deleted.

---

## Pain Point

Every conversation mutation requires a manual `ConversationStore.save(conversations)` call (6 call sites across ChatViewModel). If any call site is missed, data is silently lost. Conversations grow unbounded with no deletion UI, no search, and no pagination. The JSON file is a single monolithic blob — one corrupt write loses everything.

---

## Pre-Migration: File Extraction (Phase 0)

Two models currently share files with other types. SwiftData requires one `@Model` per file for clean macro expansion.

### 0A. Extract MessageAttachment to own file

| Property | Value |
|----------|-------|
| Type | model extraction |
| Status | needs-wiring |
| Source file | `ios/Tradeguruelectrical/Models/ChatMessage.swift:33-53` |
| Target file | `ios/Tradeguruelectrical/Models/MessageAttachment.swift` (new) |

**Changes needed:**
- [ ] Cut `MessageAttachment` struct (lines 33-53) and `AttachmentType` enum (lines 55-59) from `ChatMessage.swift`
- [ ] Create `MessageAttachment.swift` with those types
- [ ] Verify `ChatMessage.swift` still compiles (it references `MessageAttachment` but Swift resolves across files in same target)

### 0B. Extract PartsItem to own file

| Property | Value |
|----------|-------|
| Type | model extraction |
| Status | needs-wiring |
| Source file | `ios/Tradeguruelectrical/Models/ContentBlock.swift:70-82` |
| Target file | `ios/Tradeguruelectrical/Models/PartsItem.swift` (new) |

**Changes needed:**
- [ ] Cut `PartsItem` struct (lines 70-82) from `ContentBlock.swift`
- [ ] Create `PartsItem.swift` with that type

---

## Phase 1: Convert Models (bottom-up)

### 1A. PartsItem → @Model

| Property | Value |
|----------|-------|
| Type | model conversion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Models/PartsItem.swift` (new from Phase 0) |
| Blocked by | Phase 0B |

**Changes needed:**
- [ ] Replace `import Foundation` with `import Foundation` + `import SwiftData`
- [ ] Remove `nonisolated` keyword
- [ ] Change `struct PartsItem: Codable, Identifiable` → `@Model class PartsItem`
- [ ] Change `let` properties to `var` (SwiftData requires mutability)
- [ ] Keep `init()` but remove `Codable` conformance (SwiftData handles persistence)
- [ ] Add inverse: `var contentBlock: ContentBlock?` for relationship back-link

### 1B. MessageAttachment → @Model

| Property | Value |
|----------|-------|
| Type | model conversion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Models/MessageAttachment.swift` (new from Phase 0) |
| Blocked by | Phase 0A |

**Changes needed:**
- [ ] Add `import SwiftData`
- [ ] Remove `nonisolated` from struct and enum
- [ ] Change `struct MessageAttachment: Codable, Identifiable` → `@Model class MessageAttachment`
- [ ] Change `let` to `var`
- [ ] `AttachmentType` enum stays as-is (Codable enum works with SwiftData)
- [ ] Add inverse: `var message: ChatMessage?`
- [ ] `thumbnailData: Data?` stays — SwiftData stores `Data` as external blob automatically

### 1C. ContentBlock → @Model

| Property | Value |
|----------|-------|
| Type | model conversion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Models/ContentBlock.swift` |
| Blocked by | Phase 1A (PartsItem must be @Model first) |

**Changes needed:**
- [ ] Add `import SwiftData`
- [ ] Remove `nonisolated` (line 18)
- [ ] Change `struct ContentBlock: Codable, Identifiable` → `@Model class ContentBlock`
- [ ] Change `let id` / `let type` to `var`
- [ ] `items: [PartsItem]?` → `@Relationship(deleteRule: .cascade, inverse: \PartsItem.contentBlock) var items: [PartsItem]?`
- [ ] `steps: [String]?` stays (SwiftData handles array of primitives)
- [ ] `rows: [[String]]?` stays (SwiftData stores via Codable backing)
- [ ] `ContentBlockType` enum stays as-is (line 3-16)
- [ ] Add inverse: `var message: ChatMessage?`

### 1D. ChatMessage → @Model

| Property | Value |
|----------|-------|
| Type | model conversion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Models/ChatMessage.swift` |
| Blocked by | Phase 1B, 1C |

**Changes needed:**
- [ ] Add `import SwiftData`
- [ ] Remove `nonisolated` (line 8)
- [ ] Change `struct ChatMessage: Codable, Identifiable` → `@Model class ChatMessage`
- [ ] Change `let` to `var` for all stored properties
- [ ] `blocks: [ContentBlock]` → `@Relationship(deleteRule: .cascade, inverse: \ContentBlock.message) var blocks: [ContentBlock]`
- [ ] `attachments: [MessageAttachment]?` → `@Relationship(deleteRule: .cascade, inverse: \MessageAttachment.message) var attachments: [MessageAttachment]?`
- [ ] `MessageRole` enum stays (line 3-6)
- [ ] Add inverse: `var conversation: Conversation?`

### 1E. Conversation → @Model

| Property | Value |
|----------|-------|
| Type | model conversion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Models/Conversation.swift` |
| Blocked by | Phase 1D |

**Changes needed:**
- [ ] Add `import SwiftData`
- [ ] Remove `nonisolated` (line 3)
- [ ] Change `struct Conversation: Codable, Identifiable` → `@Model class Conversation`
- [ ] Change `let id` / `let createdAt` to `var`
- [ ] `messages: [ChatMessage]` → `@Relationship(deleteRule: .cascade, inverse: \ChatMessage.conversation) var messages: [ChatMessage]`
- [ ] Keep `init()` with default parameter values

---

## Phase 2: Wire App Entry Point

### 2A. TradeguruelectricalApp — add modelContainer

| Property | Value |
|----------|-------|
| Type | app configuration |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/TradeguruelectricalApp.swift` |
| Swift lines | 1-10 (entire file) |
| Blocked by | Phase 1 (all models must be @Model) |

**Changes needed:**
- [ ] Line 1: Add `import SwiftData`
- [ ] Line 7: Add `.modelContainer(for: Conversation.self)` modifier on `WindowGroup`

**Target state:**
```swift
import SwiftUI
import SwiftData

@main
struct TradeguruelectricalApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(for: Conversation.self)
    }
}
```

---

## Phase 3: Rewire ChatViewModel

### 3A. ChatViewModel — replace ConversationStore with ModelContext

| Property | Value |
|----------|-------|
| Type | viewmodel rewrite |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` |
| Lines affected | 1, 7, 19-27, 44-54, 72-82, 102-112, 117-125, 431-439 |
| Blocked by | Phase 2A |

**Changes needed:**

**Imports and properties:**
- [ ] Line 1: Add `import SwiftData`
- [ ] Line 7: Change `var conversations: [Conversation] = []` to computed property or fetched array
- [ ] Add `private var modelContext: ModelContext` property
- [ ] Change `init()` to accept `ModelContext` parameter

**Init replacement (lines 19-29):**
- [ ] Replace `ConversationStore.load()` with `modelContext.fetch(FetchDescriptor<Conversation>(sortBy: [SortDescriptor(\.updatedAt, order: .reverse)], fetchLimit: 50))`
- [ ] Keep mock data branch unchanged

**Remove all ConversationStore.save() calls:**
- [ ] Line 53: Remove `ConversationStore.save(conversations)` — SwiftData auto-saves on insert
- [ ] Line 81: Remove `ConversationStore.save(conversations)` — same
- [ ] Line 111: Remove `ConversationStore.save(conversations)` — same
- [ ] Line 124: Remove `ConversationStore.save(conversations)` — same
- [ ] Line 438: Remove `ConversationStore.save(conversations)` — auto-tracked

**New conversation creation (lines 49-53, 77-81, 107-111):**
- [ ] Replace `conversations.insert(newConvo, at: 0)` with `modelContext.insert(newConvo)` + re-fetch
- [ ] `newConversation()` (line 117): Same — insert into modelContext

**updateConversation helper (lines 431-439):**
- [ ] Remove `ConversationStore.save(conversations)` — SwiftData tracks mutations automatically
- [ ] Simplify to just update `activeConversation` reference

**New capabilities to add:**
- [ ] `func deleteConversation(_ conversation: Conversation)` — `modelContext.delete(conversation)`
- [ ] `func searchConversations(_ query: String) -> [Conversation]` — `FetchDescriptor` with `#Predicate { $0.title.localizedStandardContains(query) }`
- [ ] `func refreshConversations()` — re-fetch from modelContext with sort + limit

### 3B. ChatView — pass ModelContext to ViewModel

| Property | Value |
|----------|-------|
| Type | view modification |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/ChatView.swift` |
| Swift line | 4 |
| Blocked by | Phase 3A |

**Changes needed:**
- [ ] Line 1: Add `import SwiftData`
- [ ] Line 4: Change `@State private var viewModel = ChatViewModel()` to init with environment modelContext
- [ ] Add `@Environment(\.modelContext) private var modelContext`
- [ ] Pass modelContext to ViewModel in `.onAppear` or init

---

## Phase 4: Data Migration Helper

### 4A. JSON → SwiftData one-time migration

| Property | Value |
|----------|-------|
| Type | migration service |
| Status | pending |
| Swift file | `ios/Tradeguruelectrical/Services/DataMigrator.swift` (new) |
| Blocked by | Phase 3 |

**Changes needed:**
- [ ] Create `DataMigrator.swift` with `migrate(context: ModelContext)` static method
- [ ] Check `UserDefaults.standard.bool(forKey: "swiftdata_migrated")`
- [ ] If false: read old `conversations.json`, decode with legacy Codable structs
- [ ] Convert each struct instance to @Model class instance
- [ ] Insert into ModelContext, save
- [ ] Delete `conversations.json` file
- [ ] Set migration flag to `true`

**Legacy struct copies needed:**
- [ ] Keep old struct definitions (prefixed `Legacy`) inside DataMigrator for JSON decoding only

### 4B. Delete ConversationStore.swift

| Property | Value |
|----------|-------|
| Type | file deletion |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Services/ConversationStore.swift` |
| Blocked by | Phase 4A (migration must work first) |

**Changes needed:**
- [ ] Delete `ConversationStore.swift` entirely
- [ ] Verify no remaining references: `grep -r "ConversationStore" ios/`

---

## Phase 5: Package.swift Test Target + WSL Tests

### 5A. Add test target to Package.swift

| Property | Value |
|----------|-------|
| Type | build configuration |
| Status | pending |
| Swift file | `ios/Package.swift` |
| Swift lines | 11-13 |
| Blocked by | Nothing |

**Changes needed:**
- [ ] Line 12: Add `.testTarget(name: "TradeguruelectricalTests", dependencies: ["Tradeguruelectrical"], path: "Tests")`
- [ ] Create `ios/Tests/` directory

### 5B. Write WSL-compatible tests

| Property | Value |
|----------|-------|
| Type | test files |
| Status | pending |
| Directory | `ios/Tests/` (new) |
| Blocked by | Phase 5A + Phase 1 |

**Test files to create:**
- [ ] `ModelCompilationTests.swift` — verify @Model classes instantiate
- [ ] `EnumCodingTests.swift` — verify ContentBlockType raw values round-trip ("step_list" ↔ .stepList)
- [ ] `RelationshipTests.swift` — verify cascade relationships compile and resolve
- [ ] `FetchDescriptorTests.swift` — verify sort/filter predicates compile

---

## Wired Items (no changes needed)

| # | Item | File | Status |
|---|------|------|--------|
| 1 | DeviceManager | Services/DeviceManager.swift | wired (Keychain, not SwiftData) |
| 2 | TradeGuruAPI | Services/TradeGuruAPI.swift | wired (network only) |
| 3 | StreamParser | Services/StreamParser.swift | wired (SSE parsing only) |
| 4 | APIConfig | Services/APIConfig.swift | wired (useMockData flag preserved) |
| 5 | MockData | Models/MockData.swift | wired (test data, unaffected) |
| 6 | ThinkingMode | ThinkingMode.swift | wired (enum, no change) |
| 7 | PipelineStage | Models/PipelineStage.swift | wired (enum, no change) |
| 8 | MessageRole | Models/ChatMessage.swift:3 | wired (enum, no change) |
| 9 | AttachmentType | Models/ChatMessage.swift:55 | wired (enum, no change) |
| 10 | ContentBlockType | Models/ContentBlock.swift:3 | wired (enum, no change) |
| 11 | SidebarView | Views/SidebarView.swift | wired (receives array, no persistence) |
| 12 | All Block Views | Views/Blocks/*.swift | wired (render only) |

---

## Implementation Order

| Phase | Items | Description | Dependency | Files Touched |
|-------|-------|-------------|------------|---------------|
| 0 | 0A, 0B | Extract MessageAttachment + PartsItem to own files | None | 4 files (2 edit, 2 create) |
| 1 | 1A-1E | Convert 5 structs to @Model classes | Phase 0 | 5 files |
| 2 | 2A | Add .modelContainer to app entry | Phase 1 | 1 file |
| 3 | 3A, 3B | Rewire ChatViewModel + ChatView | Phase 2 | 2 files |
| 4 | 4A, 4B | Migration helper + delete ConversationStore | Phase 3 | 2 files (1 create, 1 delete) |
| 5 | 5A, 5B | Package.swift test target + WSL tests | Phase 1 | 5 files (1 edit, 4 create) |

**Total: 6 phases, 16 items, ~19 file operations**

---

## Verification

- [ ] `swift build` succeeds in `ios/` directory on WSL
- [ ] No references to `ConversationStore` remain: `grep -r "ConversationStore" ios/`
- [ ] No `nonisolated` on any @Model class: `grep -r "nonisolated.*@Model\|nonisolated.*class.*:" ios/Tradeguruelectrical/Models/`
- [ ] `APIConfig.useMockData = true` still produces mock conversations
- [ ] `APIConfig.useMockData = false` creates real conversations via API that persist across app restarts
- [ ] Deleting a conversation cascades to messages, blocks, attachments, parts
- [ ] Existing users' conversations.json migrates on first launch
- [ ] `swift test` runs in `ios/` directory on WSL (compilation tests)
- [ ] HTML preview is unaffected (no persistence in HTML)
