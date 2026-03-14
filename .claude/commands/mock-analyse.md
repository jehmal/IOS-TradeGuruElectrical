---
description: "Analyse a UI mock image and produce a structured component breakdown. Usage: /mock-analyse <path-to-image>"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Bash", "Agent"]
---

# Mock Analyse: $ARGUMENTS

You are a UI mock analysis agent. Your job is to visually inspect a mock image and produce a detailed, structured breakdown of every element in it.

## Step 1: Locate and organise the mock

The user provided this path: `$ARGUMENTS`

1. Read the image file at the given path to visually inspect it.
2. Determine a **mock-slug** from the filename (lowercase, hyphens, no extension). Example: `chatmock`, `login-screen`, `settings-panel`.
3. Determine the **mock-type** from visual inspection (e.g. `chat`, `login`, `settings`, `dashboard`, `profile`, `onboarding`, `paywall`, `list`, `detail`, `modal`, `sheet`).
4. The correct destination structure is: `mocks/<mock-type>/<mock-slug>.png` and `mocks/<mock-type>/<mock-slug>.md`
5. If the source image is NOT already at `mocks/<mock-type>/<mock-slug>.png`:
   - Create the directory `mocks/<mock-type>/` if needed
   - Copy (not move) the image to `mocks/<mock-type>/<mock-slug>.png`
   - Report the reorganisation to the user
6. If the image IS already in the correct location, leave it as-is.

## Step 2: Visually analyse the mock

Read the image file and perform a thorough visual analysis. Identify:

### Layout Regions
Scan the mock **top to bottom**, and within each horizontal band **left to right**. Identify every distinct region:
- Status bar
- Navigation bar / top bar
- Content area (scrollable body)
- Action areas (button rows, toolbars)
- Input areas (text fields, compose bars)
- Tab bar / bottom navigation
- Overlays, sheets, modals

### For EVERY element found, record:
- **Type**: icon, text, button, image, card, input, toggle, badge, divider, spacer, avatar, tab, etc.
- **Label/Content**: the visible text or icon name (use SF Symbol names where recognisable)
- **Position**: which region it belongs to, and its left-to-right order within that region
- **Approximate sizing**: small/medium/large, or estimated point values
- **Visual style**: color, weight, opacity, fill vs outline, rounded corners

### Spacing Analysis
- Estimate horizontal padding (screen edges to content)
- Estimate vertical spacing between major sections
- Estimate gaps between items in rows/groups
- Note any asymmetric spacing

### Typography Analysis
- Identify distinct text styles used (headline, body, caption, footnote, etc.)
- Estimate font weights (regular, medium, semibold, bold)
- Estimate font sizes in points
- Note text colors (primary, secondary, tertiary, accent)

## Step 3: Write the analysis document

Write the output file to: `mocks/<mock-type>/<mock-slug>.md`

The document MUST follow this EXACT structure:

```markdown
# Mock Analysis: <mock-slug>

**Source:** `<relative-path-to-image>`
**Analysed:** <YYYY-MM-DD>

---

## Mock Type

<One-line description of what this mock is. e.g. "Chat interface with AI agent, featuring message history, action buttons, and text input.">

---

## Spacing

| Property | Value (est.) | Notes |
|----------|-------------|-------|
| Screen horizontal padding | Xpt | |
| Section vertical spacing | Xpt | |
| Item gap (horizontal rows) | Xpt | |
| Item gap (vertical lists) | Xpt | |
| Card internal padding | Xpt | |
| Input field padding | Xpt | |
| Tab bar height | Xpt | |
| Nav bar height | Xpt | |

---

## Text & Font

| Style | Size (est.) | Weight | Color | Usage |
|-------|------------|--------|-------|-------|
| Title | Xpt | bold | primary | Screen titles |
| Headline | Xpt | semibold | primary | Card titles |
| Body | Xpt | regular | primary | Main content |
| Caption | Xpt | regular | secondary | Subtitles, descriptions |
| Footnote | Xpt | medium | tertiary | Timestamps, metadata |
| Button Label | Xpt | medium | primary | Action buttons |

---

## Components (Left to Right, Top to Bottom)

### Region: <region-name> (e.g. "Status Bar")

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | text | "9:41" | left | caption | system time |
| 2 | icon | signal bars | right | small | system indicator |

### Region: <next-region>

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | ... | ... | ... | ... | ... |

(Continue for EVERY region, top to bottom)

---

## Component Summary

| Component Type | Count | Examples |
|---------------|-------|---------|
| icon | N | list of names |
| button | N | list of labels |
| text | N | list of styles used |
| image | N | description |
| input | N | description |
| card | N | description |

---

## SwiftUI Implementation Notes

- Suggested view structure (NavigationStack, VStack, HStack, etc.)
- Recommended SF Symbols for identified icons
- Suggested color tokens (semantic colors)
- Layout approach recommendations
```

## Rules

- Number components sequentially WITHIN each region (restart at 1 per region)
- Scan strictly **left to right, top to bottom** — this ordering is mandatory
- Use SF Symbol names where the icon is recognisable (e.g. `house.fill`, `magnifyingglass`, `square.and.pencil`)
- For unrecognisable icons, describe them (e.g. "circular icon with lightning bolt")
- Estimate sizes in iOS points (pt), not pixels
- Reference Apple HIG conventions for standard element sizes (44pt tap targets, 49pt tab bar, etc.)
- Keep the analysis factual — describe what you SEE, don't invent elements
- The date should be the CURRENT date
- If the mock contains text, transcribe it exactly as shown
