---
description: "Audit a screen's Swift + HTML files and produce a full wiring spec for every unwired item. Usage: /make-wiring-spec <screen-slug or description>"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Edit", "Bash", "Agent"]
---

# Full Wiring Spec: $ARGUMENTS

You are a wiring spec agent. You audit a screen's Swift + HTML code, catalogue every element, and produce a line-level spec that tells an implementer exactly what to change to make everything functional against the real API — with mock data available as a toggle.

You have THREE sequential jobs:
1. Run a complete inventory of the screen (Swift + HTML)
2. Produce a wiring spec for every inventory item that isn't fully functional
3. Convert mock data from hardcoded to a toggleable testing mode

**CRITICAL: You MUST output the ASCII workflow diagram as chat output at the end.**

---

## Phase 1: Identify Target

The user specified: `$ARGUMENTS`

1. Set `SCREEN_SLUG` to this value (lowercase, hyphenated)
2. Read `CLAUDE.md` for project rules
3. Read `rork.json` if it exists to get project name and app path
4. Find the primary view file: search `ios/**/*View.swift` and `ios/**/*Screen.swift` for a struct matching the screen slug
5. If no match, glob `ios/**/*.swift` and grep for `$ARGUMENTS` case-insensitively
6. Check for existing inventory at `screens/<SCREEN_SLUG>/inventory.md`
7. Check for API reference at `specs/api-integration-reference.md`

---

## Phase 2: Read ALL Files

**Read EVERY file before writing anything.**

Use parallel reads. Batch files into groups of 10-18 simultaneous reads.

### Batch 1 — Core files (read in parallel):
- The primary view file and all Swift files it references
- All ViewModels referenced
- All Services files (`Services/*.swift`)
- All Models files (`Models/*.swift`)
- The API integration reference spec
- Any existing inventory files

### Batch 2 — Views + HTML (read in parallel):
- All view files in `Views/` and `Views/Blocks/`
- `preview/chat.html` or equivalent HTML preview file
- Any other Swift files found in the screen's directory
- Color extensions, mode enums, config files

### Batch 3 — HTML pagination (if needed):
- If HTML file exceeds 2000 lines, read in chunks of 500 lines
- Cover the entire file — CSS, HTML body, JS logic, workbench tools

---

## Phase 3: Analyse

With all files in context, classify EVERY element:

### For every Swift file:
- Every `Button(action:)` — is the action empty `{}` or wired?
- Every `@State` / `@Binding` / `@Observable` property — read+written or orphaned?
- Every function call — real service or mock data?
- Every view — real data or placeholder?

### For every HTML element:
- Every interactive element (buttons, inputs, selects)
- Every component in the picker dropdown
- What mock data it displays
- Whether it matches current Swift code

### Cross-reference:
- Every API endpoint in the reference spec against every `TradeGuruAPI` method
- Every API method against every ViewModel call site
- Every ViewModel method against every UI trigger
- Every HTML component against its Swift equivalent

### Classify each item:
- `wired` — does something real
- `empty` — action is `{}` or value goes nowhere
- `needs-backend` — requires API call, processing, or persistence
- `decorative` — visual only

---

## Phase 4: Write Inventories

### 4A: Swift Inventory

Write or update `screens/<SCREEN_SLUG>/inventory.md` following the format from `/get-screen-inventory`. Include these categories:
- Summary table
- Buttons
- Icons
- Inputs
- Components
- Text Elements
- State Variables
- Services (NEW — catalogue API clients, parsers, managers)
- Missing Features (NEW — features not present in any file but referenced by API)
- Action Items (prioritised)
- Color Tokens
- StockTake

### 4B: HTML Inventory

Write `screens/<SCREEN_SLUG>/html-inventory.md` with:
- Workbench Controls (flag as DO NOT MODIFY)
- Interactive Elements (in-device)
- Component Picker Options (check Mandatory Rule #2 compliance)
- Content Block Renderers
- Mock Data catalogue
- Swift-HTML Parity Issues

---

## Phase 5: Write the Wiring Spec

Write to: `specs/<SCREEN_SLUG>-full-wiring-spec.md`

The document MUST follow this exact structure:

```markdown
# <Screen Name> Screen Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Screen | <Screen Name> |
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

Wire every button, input, state variable, and component on the <screen> screen to the live TradeGuru API. Convert hardcoded mock data into a toggleable testing mode. After implementation, every element must be fully functional — zero empty action closures, zero placeholder data, zero dead code paths.

---

## Pain Point

<Describe specifically what doesn't work. Name the broken items. Be concrete.>

---

## Mock Data Toggle

### Swift
- File: `ios/<app>/Services/APIConfig.swift`
- Add: `static var useMockData = false`
- ViewModel checks this flag: if true, use MockData; if false, call real API

### HTML
- Add "Mock Mode" toggle in workbench controls bar (next to dark/light toggle)
- When enabled, show mock data. When disabled, show "Connect to API" states.

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
| HTML location | line ~N in `preview/<file>.html` |
| API endpoint | `<endpoint path>` or N/A |
| Blocked by | <dependency item # or "nothing"> |

**Changes needed (Swift):**
- [ ] `<file>:<line>` — <exact change description>

**Changes needed (HTML):**
- [ ] `<file>:~<line>` — <exact change description>

**Note:** <caveats, edge cases, gotchas>

---

(continue for EVERY unwired item)

---

## Wired Items (no changes needed)

| # | Item | File | Status |
|---|------|------|--------|

---

## Implementation Order

| Phase | Items | Description | Dependency |
|-------|-------|-------------|------------|
| 1 | ... | ... | None |
| 2 | ... | ... | Phase 1 |

---

## Verification

- [ ] Every `Button(action:)` has a non-empty closure
- [ ] Mock mode toggle works in both Swift and HTML
- [ ] <screen-specific checks>
- [ ] HTML preview matches Swift for every component
```

---

## Phase 6: Output the Workflow Diagram

**AFTER writing all files, output this ASCII diagram as chat text, filled in with actual values from this run:**

```
┌─────────────────────────────────────────────────────────┐
│                   /make-wiring-spec                      │
│            Target: $ARGUMENTS                            │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│              STEP 1: READ THE SPEC                      │
│  Identified primary view: <PRIMARY_VIEW_FILE>           │
│  Found <N> related Swift files                          │
│  Found HTML preview: <HTML_FILE>                        │
│  API reference: <API_REF_FILE>                          │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│         STEP 2: PARALLEL FILE READS                     │
│                                                         │
│  Batch 1: <N> files (core Swift + services + models)    │
│  Batch 2: <N> files (views + blocks + HTML)             │
│  Batch 3: <N> HTML chunks (if paginated)                │
│  Total files read: <TOTAL>                              │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│             STEP 3: DEEP ANALYSIS                       │
│                                                         │
│  Buttons analysed: <N>                                  │
│  State variables tracked: <N>                           │
│  API endpoints cross-referenced: <N>                    │
│  HTML elements catalogued: <N>                          │
│                                                         │
│  KEY FINDING:                                           │
│  ┌─────────────────────────────────────────────┐        │
│  │ <ONE LINE SUMMARY OF MOST IMPORTANT FINDING>│        │
│  └─────────────────────────────────────────────┘        │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│          STEP 4: WRITE INVENTORIES                      │
│                                                         │
│  ┌──────────────────────┐  ┌──────────────────────┐     │
│  │ screens/<slug>/      │  │ screens/<slug>/      │     │
│  │ inventory.md         │  │ html-inventory.md    │     │
│  │ <STATUS>             │  │ <STATUS>             │     │
│  │                      │  │                      │     │
│  │ <N> items catalogued │  │ <N> HTML elements    │     │
│  │ <N> wired            │  │ <N> parity issues    │     │
│  │ <N> needs wiring     │  │                      │     │
│  └──────────────────────┘  └──────────────────────┘     │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│          STEP 5: WRITE WIRING SPEC                      │
│                                                         │
│  specs/<slug>-full-wiring-spec.md                       │
│                                                         │
│  Total items: <N>                                       │
│  Already wired: <N>                                     │
│  Needs wiring: <N> items across <N> phases              │
│                                                         │
│  Phase breakdown:                                       │
│  <list each phase with item count>                      │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                    DONE                                  │
│                                                         │
│  Files delivered:                                       │
│    screens/<slug>/inventory.md ......... <created/updated>│
│    screens/<slug>/html-inventory.md .... <created/updated>│
│    specs/<slug>-full-wiring-spec.md .... <created/updated>│
│                                                         │
│  Tool calls: <N> rounds                                 │
│  Files read: <N>                                        │
│  Files written: <N>                                     │
└─────────────────────────────────────────────────────────┘
```

---

## Rules

- Read ACTUAL code — report real line numbers, not guesses
- Every spec item must have EXACT file paths and line numbers
- If a button action is `{}`, status is `empty` — not "not yet implemented"
- The mock toggle must be a SIMPLE boolean flag, not a protocol/DI pattern
- HTML changes must respect Mandatory Rule #1: do NOT modify workbench utilities
- HTML changes must respect Mandatory Rule #2: every new Swift component must appear in the HTML picker
- Cross-reference EVERY item against the API integration reference to verify correct endpoints
- Items that are already fully wired should be listed in "Wired Items" — don't skip them
- Implementation order must respect real dependencies
- Use parallel file reads wherever possible — batch independent reads together
- Read ALL files before writing ANYTHING
- The ASCII workflow diagram is MANDATORY chat output — do not skip it
- Use the CURRENT date for all timestamps
- Keep file paths relative to project root
