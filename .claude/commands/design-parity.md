---
description: "Check parity between SwiftUI code and HTML preview, output a status report with fixes. Usage: /design-parity"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Edit", "Bash"]
---

# Design Parity Check

You are a parity auditor. Your job is to compare every SwiftUI view against its HTML preview counterpart and report what matches, what doesn't, and how to fix mismatches.

## Step 1: Discover all screen pairs

Find all SwiftUI screens and their HTML previews:

1. Glob `ios/**/*View.swift` and `ios/**/*Screen.swift` to find all view files
2. Glob `preview/*.html` to find all HTML previews
3. Match them by name convention: `ChatView.swift` ↔ `preview/chat.html`, `SettingsView.swift` ↔ `preview/settings.html`
4. Read `rork.json` to get the project name

For each pair, also find subcomponent files:
- Grep the main view file for any imported/referenced custom views
- Find those subcomponent `.swift` files too

## Step 2: Compare each pair element-by-element

For every matched screen pair, read BOTH files completely. Compare:

### Layout Structure
- Does the HTML have the same region hierarchy as the SwiftUI? (nav bar, content, input, etc.)
- Are sections in the same order?
- Are any regions present in one but missing from the other?

### Components
For every UI element (buttons, text, icons, inputs, cards, images):
- **Present in both** → check if they match visually (same text, same icon, same position)
- **Present in Swift only** → parity gap, HTML needs updating
- **Present in HTML only** → parity gap, either Swift needs it or HTML has stale code

### Styling
- Font sizes: do they use equivalent values?
- Colors: do CSS variables match SwiftUI color tokens?
- Spacing: are padding/margin values equivalent?
- Corner radii: do they match?
- Opacity values: do they match?

### Interactions
- Does the HTML replicate SwiftUI interactions? (mode switching, button taps, input focus)
- Are JS event handlers present for every SwiftUI `Button` / `.onTapGesture`?

### Assets
- Are images/logos embedded in HTML as base64?
- Do both reference the same assets?

## Step 3: Determine the parity document number

Check the `parity/` directory for existing files:
- Glob `parity/*-Parity-status.md`
- Find the highest number N
- New file is `(N+1)-Parity-status.md`
- If no files exist, start at `1-Parity-status.md`

## Step 4: Write the parity report

Create: `parity/<N>-Parity-status.md`

The document MUST follow this exact structure:

```markdown
# Parity Status Report #<N>

**Project:** <project-name>
**Date:** <YYYY-MM-DD>
**Overall parity:** <X>% (<PASS / WARN / FAIL>)

Thresholds: PASS ≥ 95% | WARN ≥ 80% | FAIL < 80%

---

## Screen Summary

| Screen | Swift File | HTML File | Parity | Status |
|--------|-----------|-----------|--------|--------|
| <name> | `<path>` | `<path>` | X% | PASS/WARN/FAIL |
| <name> | `<path>` | (missing) | 0% | FAIL |

---

## <Screen Name> — Detailed Parity

### Matching (in parity)

| # | Element | Type | Swift | HTML | Notes |
|---|---------|------|-------|------|-------|
| 1 | <name> | <type> | `<file>:<line>` | line ~N | Identical |

### Mismatched (out of parity)

| # | Element | Issue | Swift Value | HTML Value | Fix |
|---|---------|-------|-------------|------------|-----|
| 1 | <name> | <what differs> | <swift value> | <html value> | <exact fix instruction> |

### Missing from HTML

| # | Element | Type | Swift File | Fix |
|---|---------|------|-----------|-----|
| 1 | <name> | <type> | `<path>:<line>` | Add <element> to HTML at <location> |

### Missing from Swift

| # | Element | Type | HTML Location | Fix |
|---|---------|------|--------------|-----|
| 1 | <name> | <type> | line ~N | Add <element> to <SwiftFile> or remove from HTML if stale |

### Style Mismatches

| # | Property | Swift | HTML | Fix |
|---|----------|-------|------|-----|
| 1 | font-size | 16pt | 14px | Change HTML to `font-size: 16px` |
| 2 | corner-radius | 12pt | 8px | Change HTML `border-radius` to `12px` |
| 3 | color | .secondary | #888 | Change HTML to `#666` (systemSecondaryLabel) |

---

(Repeat ## section for each screen)

---

## Fix Checklist

Numbered list of every fix needed to reach 100% parity, ordered by screen then priority:

- [ ] 1. <Screen>: <Fix description> — file: `<path>`, line ~N
- [ ] 2. <Screen>: <Fix description> — file: `<path>`, line ~N
- [ ] ...

---

## Parity History

| Report # | Date | Overall | Screens Checked | Fixes Needed |
|----------|------|---------|----------------|-------------|
| <N> | <date> | X% | N | N |
```

## Rules

- Parity percentage = (matching elements / total unique elements across both) * 100
- A "match" means same element type, same text/icon, same approximate position, same visual style
- Minor CSS differences (1-2px) count as matching — flag only meaningful visual differences
- If an HTML preview doesn't exist for a Swift screen, that's 0% parity for that screen
- If a Swift screen doesn't exist for an HTML preview, flag the HTML as orphaned
- Fix instructions must be SPECIFIC: exact file, approximate line, what to change
- Always check subcomponents, not just the main view file
- Color comparisons: map SwiftUI semantic colors to their CSS hex equivalents
- The fix checklist should be actionable — someone could follow it without reading the rest
