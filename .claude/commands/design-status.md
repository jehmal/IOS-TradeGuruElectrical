---
description: "Capture current screen state, inventory all design elements, and produce a status report. Usage: /design-status"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Edit", "Bash"]
---

# Design Status

You are a design status auditor. Your job is to capture the current screen, find its corresponding code files (SwiftUI + HTML preview), inventory every element, and produce a structured status document.

## Step 1: Capture and identify the current screen

Run the screenshot tool:
```bash
python3 ~/.claude/util/screen/cross_platform_screenshot.py --name "design-status" --no-notify
```

Read the captured screenshot. Determine:
- **Screen name** — what screen is visible (e.g. "chat", "settings", "onboarding")
- **Screen slug** — lowercase hyphenated (e.g. `chat`, `settings-main`, `onboarding-step1`)

## Step 2: Locate corresponding source files

Search for all files related to this screen:

**SwiftUI files:**
- Glob `ios/**/*.swift` and grep for view structs that match the screen name
- Identify: main view file, subcomponent files, view models, models/enums

**HTML preview files:**
- Glob `preview/*.html` for a matching preview file
- Read it to confirm it corresponds to the same screen

**Mock files:**
- Check `mocks/**/*.md` and `mocks/**/*.png` for related mock analyses

Record every file found with its path.

## Step 3: Inventory all visible elements

Scan the screenshot top-to-bottom, left-to-right. For each element record:
- **Name** — component identifier (e.g. "ModeSelector", "ChatInputBar")
- **Type** — view, button, icon, text, input, card, image, spacer, divider
- **SwiftUI status** — `implemented` (found in .swift), `missing` (not in code), `partial` (exists but incomplete)
- **HTML status** — `implemented` (found in .html), `missing`, `partial`
- **In sync** — YES if Swift and HTML match visually, NO if they differ, N/A if one is missing

## Step 4: Check backend connections

For each element that would need data or API calls, check:
- Does a service/API call exist in the Swift code?
- Is there a view model managing state?
- Are there any hardcoded/placeholder values that should come from an API?
- Are environment variables referenced (check for `Config.` or `ProcessInfo` or `.env` usage)?

Classify each connection as:
- `connected` — wired to a real service/API
- `mocked` — uses placeholder/hardcoded data
- `not-needed` — purely visual, no backend required
- `missing` — needs a backend connection but has none

## Step 5: Write or update the status file

The output file goes to: `inventory/<project-name>-screen-status-<screen-slug>.md`

Read `rork.json` to get the project name (lowercase, hyphenated). If no `rork.json`, derive from the working directory name.

**If the file already exists:** Read it, preserve the StockTake history, update all sections with fresh data, and append a new StockTake row with action `refresh`.

**If the file does not exist:** Create it fresh.

The document MUST follow this exact structure:

```markdown
# Screen Status: <screen-slug>

**Project:** <project-name>
**Last checked:** <YYYY-MM-DD>
**Screenshot:** `<path-to-latest-screenshot>`

---

## Screen Overview

<2-3 sentence description of what this screen does, its purpose in the app, and its current state of completion.>

| Property | Value |
|----------|-------|
| Screen name | <human readable> |
| Screen slug | <kebab-case> |
| Primary SwiftUI file | `<path>` or `(not created)` |
| View model | `<path>` or `(not created)` |
| Models/Enums | `<paths>` or `(none)` |
| Subcomponents | `<count>` files |
| Completion | <X>% (estimated from element inventory) |

---

## Connected HTML

| Property | Value |
|----------|-------|
| Preview file | `<path>` or `(not created)` |
| Interactive | YES / NO (does it have working JS interactions?) |
| In sync with Swift | YES / NO / PARTIAL |
| Last modified | <date or unknown> |
| Base64 assets embedded | YES / NO |

### Sync Diff

| Element | SwiftUI | HTML | Match |
|---------|---------|------|-------|
| <element name> | implemented | implemented | YES |
| <element name> | implemented | missing | NO |
| <element name> | missing | missing | N/A |

---

## Inventory

### Implemented

| # | Component | Type | Swift File | HTML | Notes |
|---|-----------|------|-----------|------|-------|
| 1 | <Name> | view | `<path>` | YES | |
| 2 | <Name> | button | `<path>` | YES | |

### Missing (in design but not in code)

| # | Component | Type | Seen In | Priority | Notes |
|---|-----------|------|---------|----------|-------|
| 1 | <Name> | <type> | screenshot | high/med/low | |

### Extra (in code but not in current design)

| # | Component | Type | Swift File | Notes |
|---|-----------|------|-----------|-------|
| 1 | <Name> | <type> | `<path>` | May be unused or for future use |

---

## Backend Connection Status

| # | Component | Data Source | Status | Details |
|---|-----------|-----------|--------|---------|
| 1 | <Name> | <API/service/local> | connected / mocked / missing / not-needed | <what it connects to or what's missing> |

### Environment Variables Required

| Variable | Used By | Status |
|----------|---------|--------|
| `<VAR_NAME>` | <component> | set / missing / placeholder |

### API Endpoints Referenced

| Endpoint | Method | Used By | Status |
|----------|--------|---------|--------|
| <url or path> | GET/POST | <component> | active / stub / not-implemented |

---

## StockTake

| Date | Action | Implemented | Missing | Sync | Completion |
|------|--------|------------|---------|------|------------|
| <YYYY-MM-DD> | initial scan | N | N | X% | X% |
```

## Rules

- Use the CURRENT date for all timestamps
- Completion percentage = (implemented elements / total elements seen in screenshot) * 100
- Sync percentage = (matching elements / total elements present in both) * 100
- If no HTML preview exists, note it clearly — don't invent one
- If no Swift code exists, note it clearly — every element is "missing"
- Keep file paths relative to project root
- Number items sequentially within each table
- Priority for missing items: `high` = core interaction, `med` = visible UI, `low` = decorative
