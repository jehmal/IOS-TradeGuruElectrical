# Agent Prompt: Inventory Audit + Full Wiring Spec for TradeGuruElectrical

You are an inventory auditor and spec writer. You have THREE sequential jobs:

1. Run a complete inventory of the chat screen (Swift + HTML)
2. Produce a wiring spec for every inventory item that isn't fully functional
3. Convert mock data from hardcoded to a toggleable testing mode

## Why This Matters

TradeGuruElectrical has a complete frontend (27 Swift files + HTML preview) and a live backend at `tradeguru.com/api/v1`. The frontend was built with mock data. Now we need a precise, line-level spec that tells an implementer exactly what to change in every file to make the entire screen fully functional against the real API — while keeping mock data available as a toggle for testing.

## Step 1: Run Inventory

### 1A: Screen Inventory (Swift)

Read ALL Swift files in `ios/Tradeguruelectrical/` — every single one. For each file, catalogue:
- Every `Button(action:)` — is the action empty `{}` or wired?
- Every `@State` / `@Binding` — is it read and written, or orphaned?
- Every function call — does it call a real service or mock data?
- Every view — does it display real data or placeholder content?

Cross-reference against the existing inventory at `screens/chat/inventory.md` — read it first to see what was previously catalogued.

### 1B: HTML Inventory

Read `preview/chat.html` completely. Catalogue:
- Every interactive element (buttons, dropdowns, inputs)
- Every component in the component picker dropdown
- What mock data it displays
- Whether it matches the current Swift code

### 1C: Write Updated Inventories

Update `screens/chat/inventory.md` with the fresh scan using the format from `/get-screen-inventory`. Use the standardised status values: `wired`, `empty`, `needs-backend`, `decorative`, `removed`.

Also write `screens/chat/html-inventory.md` with the HTML component inventory using the same format structure.

## Step 2: Generate the Wiring Spec

For EVERY inventory item that is NOT fully `wired`, create a spec entry. The spec goes to: `specs/chat-full-wiring-spec.md`

The document MUST follow this exact structure:

```markdown
# Chat Screen Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Screen | Chat |
| Total inventory items | N |
| Already wired | N |
| Needs wiring | N |
| Target | 100% functional against live API |
| Mock mode | Toggleable via `USE_MOCK_DATA` flag |
| Backend base URL | `https://tradeguru.com/api/v1` |
| API reference | `specs/api-integration-reference.md` |
| Date | YYYY-MM-DD |

---

## Objective

Wire every button, input, state variable, and component on the chat screen to the live TradeGuru API at `tradeguru.com/api/v1/*`. Convert hardcoded mock data into a toggleable testing mode so developers can switch between real API and mock responses. After implementation, every element on the screen must be fully functional — zero empty action closures, zero placeholder data, zero dead code paths.

---

## Pain Point

The chat screen has N items that are visual-only — buttons that do nothing, inputs that go nowhere, state that isn't persisted. The app looks complete but doesn't work. Users tap send and nothing happens. The sidebar shows mock conversations that aren't real. Photo attachments are selected but never uploaded. Voice input is a dead button. This spec fixes every single one of these gaps.

---

## Mock Data Toggle

Before wiring individual items, implement a global mock toggle:

### Swift
- File: `ios/Tradeguruelectrical/Services/APIConfig.swift`
- Add: `static var useMockData = false` (default false for production, true for testing)
- ChatViewModel checks this flag: if true, use MockData responses; if false, call TradeGuruAPI

### HTML
- Add a "Mock Mode" toggle in the workbench controls bar (next to dark/light toggle)
- When enabled, all API-dependent components show mock data
- When disabled, show "Connect to API" placeholder states

### Spec Entry Format:

(Every item below follows this format)

---

## Inventory Item Checklist

### 1. <Item Name>

**Pain Point:** <What doesn't work and why it matters>

| Property | Value |
|----------|-------|
| Type | button / input / component / state / service |
| Status | empty / needs-backend / partial |
| Swift file | `<exact file path>` |
| Swift line(s) | <exact line numbers> |
| HTML location | line ~N in `preview/chat.html` |
| API endpoint | `<endpoint path>` or N/A |
| Blocked by | <dependency item # or "nothing"> |

**Changes needed (Swift):**
- [ ] `<file>:<line>` — <exact change description>
- [ ] `<file>:<line>` — <exact change description>

**Changes needed (HTML):**
- [ ] `chat.html:~<line>` — <exact change description>

**Note:** <Any caveats, edge cases, or gotchas>

---

### 2. <Next Item>

... (continue for EVERY inventory item that isn't fully wired)

---

## Implementation Order

Items must be implemented in this order (respects dependencies):

| Phase | Items | Dependency |
|-------|-------|------------|
| 1 | Mock toggle, APIConfig flag | None |
| 2 | <items> | Phase 1 |
| 3 | <items> | Phase 2 |
| ... | ... | ... |

---

## Verification

After all items are wired:
- [ ] Every `Button(action:)` in ChatView.swift has a non-empty closure
- [ ] Every `Button(action:)` in ChatInputBar.swift has a non-empty closure
- [ ] Every `Button(action:)` in SidebarView.swift has a non-empty closure
- [ ] ChatViewModel.send() calls TradeGuruAPI.chat() (not mock)
- [ ] Mock mode toggle works in both Swift and HTML
- [ ] Photo picker → uploads to /api/v1/files/upload
- [ ] Speak button → records audio → /api/v1/audio/transcribe
- [ ] Rating/feedback can be submitted on assistant messages
- [ ] New chat creates empty conversation (not mock)
- [ ] Sidebar loads real conversation history
- [ ] Error states display when API fails
- [ ] Streaming blocks render progressively during response
- [ ] HTML preview matches Swift for every component
```

## Step 3: Reference Files to Read

You MUST read ALL of these before writing anything:

- `CLAUDE.md` — project rules and mandatory items
- `rork.json` — project config
- `specs/api-integration-reference.md` — the API contract
- `screens/chat/inventory.md` — existing inventory
- `ios/Tradeguruelectrical/ChatView.swift`
- `ios/Tradeguruelectrical/ChatInputBar.swift`
- `ios/Tradeguruelectrical/Views/SidebarView.swift`
- `ios/Tradeguruelectrical/Views/MessageBubble.swift`
- `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift`
- `ios/Tradeguruelectrical/Services/TradeGuruAPI.swift`
- `ios/Tradeguruelectrical/Services/APIConfig.swift`
- `ios/Tradeguruelectrical/Services/StreamParser.swift`
- `ios/Tradeguruelectrical/Services/DeviceManager.swift`
- `ios/Tradeguruelectrical/Models/ContentBlock.swift`
- `ios/Tradeguruelectrical/Models/ChatMessage.swift`
- `ios/Tradeguruelectrical/Models/Conversation.swift`
- `ios/Tradeguruelectrical/Models/MockData.swift`
- `ios/Tradeguruelectrical/ThinkingMode.swift`
- `ios/Tradeguruelectrical/TradeGuruColors.swift`
- `ios/Tradeguruelectrical/ModeSelector.swift`
- `ios/Tradeguruelectrical/ModeInfoCard.swift`
- `preview/chat.html`
- ALL files in `ios/Tradeguruelectrical/Views/Blocks/`

## Rules

- Read ACTUAL code — report real line numbers, not guesses
- Every spec item must have EXACT file paths and line numbers
- If a button action is `{}`, status is `empty` — not "not yet implemented"
- The mock toggle must be a SIMPLE boolean flag, not a protocol/dependency injection pattern
- HTML changes must respect Mandatory rule #1: do NOT modify workbench utilities
- HTML changes must respect Mandatory rule #2: every new Swift component must appear in the HTML component picker
- Cross-reference EVERY item against `specs/api-integration-reference.md` to verify the correct endpoint
- Items that are already fully wired should be listed as `wired` with no changes needed — don't skip them, include them for completeness
- The implementation order must respect real dependencies — don't tell someone to wire send before the API client exists
