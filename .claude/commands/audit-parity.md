---
description: "Audit every Swift behaviour against the HTML preview, fix all gaps, and output a detailed parity report. Usage: /audit-parity <screen-slug>"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Edit", "Bash", "Agent"]
---

# Behaviour Parity Audit + Fix: $ARGUMENTS

You are a behaviour parity auditor AND fixer. You read every Swift file and the matching HTML preview, compare EVERY behaviour 1-for-1, fix every gap in the HTML, and output a detailed audit report — both as chat output AND as a file.

**Mandatory Rule #5:** All behaviours the Swift code has must also be mimicked 1 for 1. The HTML is how we test what the Swift looks like — that includes behaviour.

---

## Phase 1: Identify Target

The user specified: `$ARGUMENTS`

1. Set `SCREEN_SLUG` to this value (lowercase, hyphenated). If empty, default to `chat`.
2. Find the primary view file: glob `ios/**/*View.swift` and `ios/**/*Screen.swift` matching the slug
3. Find the matching HTML preview: `preview/<slug>.html`
4. Find ALL related Swift files: ViewModels, Services, Models, Views/Blocks, color extensions, mode enums

---

## Phase 2: Read ALL Files

Read EVERY file before analysing. Use parallel reads, batched 10-18 at a time.

### Batch 1 — Swift core:
- Primary view file + all referenced views
- ViewModels
- Services (API client, config, parsers, managers)
- Models

### Batch 2 — Swift components + HTML:
- All block views in `Views/Blocks/`
- Sidebar, MessageBubble, ModeSelector, ModeInfoCard, etc.
- The full HTML file (in chunks if > 2000 lines)

---

## Phase 3: Behaviour Extraction

For EVERY Swift file, extract EVERY behaviour into a numbered checklist:

### Button Behaviours
For every `Button(action:)`:
- What happens when tapped?
- Does it call an API?
- Does it update state?
- Does it trigger animation?
- Does it navigate?

### State Change Behaviours
For every `@State`, `@Binding`, `@Observable`:
- What triggers a change?
- What UI updates when it changes?
- Are there `.onChange` handlers?

### Input Behaviours
- What happens on focus?
- What happens on text change?
- What happens on submit?
- Conditional visibility (send vs mic vs stop)

### API Behaviours
- What triggers each API call?
- How is the response handled?
- What happens on error?
- How does streaming work?
- What UI updates during streaming?

### Interaction Behaviours
- Sidebar open/close/select/new
- Mode switching + card lifecycle
- Attachment pick/preview/cancel/send
- Voice record start/stop/transcribe
- Rating/flag/speak actions
- Scroll behaviour
- Error display/dismiss
- Empty state conditions
- Conversation title updates
- Message count updates

---

## Phase 4: Compare Against HTML

For EVERY extracted behaviour, check the HTML:
- Is the JS handler present?
- Does it do the SAME thing?
- Does it call the SAME API endpoint?
- Does it update the SAME state?
- Does the visual result match?

Classify each as:
- `MATCH` — identical behaviour
- `PARTIAL` — similar but not exact
- `MISSING` — behaviour not implemented in HTML
- `EXTRA` — HTML has behaviour Swift doesn't (flag as stale)

---

## Phase 5: Fix Every Gap

For every `PARTIAL` or `MISSING` item:
1. Edit `preview/<slug>.html` directly
2. Add the missing JS function or fix the existing one
3. Match the Swift behaviour EXACTLY

**CRITICAL: Do NOT modify workbench tools** (pen, notes, drawing canvas — everything below `// WORKBENCH TOOLS` comment).

---

## Phase 6: Determine Report Number

- Glob `parity/*-Parity-status.md`
- Find highest number N
- New report is `(N+1)-Parity-status.md`

---

## Phase 7: Write the Parity Report File

Write to: `parity/<N>-Parity-status.md`

```markdown
# Parity Status Report #<N>

**Project:** tradeguruelectrical
**Date:** <YYYY-MM-DD>
**Screen:** <Screen Name>
**Overall parity:** <X>% (<PASS / WARN / FAIL>)
**Behaviours audited:** <N>
**Matched:** <N>
**Fixed:** <N>
**Remaining gaps:** <N>

Thresholds: PASS >= 95% | WARN >= 80% | FAIL < 80%

---

## Behaviour Audit

### Button Behaviours

| # | Button | Swift Behaviour | HTML Behaviour | Status | Fix Applied |
|---|--------|----------------|----------------|--------|-------------|

### State Change Behaviours

| # | Trigger | Swift Effect | HTML Effect | Status | Fix Applied |
|---|---------|-------------|-------------|--------|-------------|

### Input Behaviours

| # | Action | Swift Effect | HTML Effect | Status | Fix Applied |
|---|--------|-------------|-------------|--------|-------------|

### API Behaviours

| # | Trigger | Endpoint | Swift | HTML | Status | Fix Applied |
|---|---------|----------|-------|------|--------|-------------|

### Interaction Behaviours

| # | Interaction | Swift | HTML | Status | Fix Applied |
|---|-------------|-------|------|--------|-------------|

### Component Rendering Parity

| # | Component | Swift File | HTML Renderer | Visual Match | Behaviour Match |
|---|-----------|-----------|---------------|-------------|-----------------|

---

## Fixes Applied This Run

| # | Gap | What Was Wrong | What Was Fixed | File | Line |
|---|-----|---------------|----------------|------|------|

---

## Remaining Gaps (if any)

| # | Gap | Reason Not Fixed | Workaround |
|---|-----|-----------------|------------|

---

## API Endpoint Coverage

| Endpoint | Swift Method | HTML Function | Wired |
|----------|-------------|---------------|-------|
| POST /device/register | TradeGuruAPI.registerDevice() | registerDevice() | YES/NO |
| POST /chat | TradeGuruAPI.chat() | apiChat() | YES/NO |
| POST /chat/vision | TradeGuruAPI.chatVision() | ? | YES/NO |
| POST /audio/transcribe | TradeGuruAPI.transcribe() | ? | YES/NO |
| POST /audio/speech | TradeGuruAPI.speak() | ? | YES/NO |
| POST /files/upload | TradeGuruAPI.uploadFile() | ? | YES/NO |
| POST /rating | TradeGuruAPI.rate() | apiRate() | YES/NO |
| POST /feedback | TradeGuruAPI.feedback() | apiFlag() | YES/NO |

---

## Parity History

| Report # | Date | Overall | Audited | Matched | Fixed | Gaps |
|----------|------|---------|---------|---------|-------|------|
```

---

## Phase 8: Output the Chat Report

**AFTER writing the file AND making all fixes, output this as chat text — filled in with actual values:**

```
BEHAVIOUR PARITY AUDIT — <SCREEN NAME>
══════════════════════════════════════

Report: parity/<N>-Parity-status.md
Date: <YYYY-MM-DD>
Overall: <X>% (<STATUS>)

 # │ Behaviour                          │ Swift            │ HTML            │ Status
───┼────────────────────────────────────┼──────────────────┼─────────────────┼────────
 1 │ <description>                      │ <swift impl>     │ <html impl>     │ ✓ MATCH
 2 │ <description>                      │ <swift impl>     │ <html impl>     │ ✓ FIXED
 3 │ <description>                      │ <swift impl>     │ (was missing)   │ ✓ FIXED
...
(continue for EVERY behaviour)

API ENDPOINT COVERAGE:
 ✓ POST /device/register → registerDevice()
 ✓ POST /chat → apiChat() (SSE streaming)
 ✗ POST /chat/vision → not implemented in HTML
 ...

INTERACTION BEHAVIOUR:
 ✓ <description>
 ✓ <description>
 ...

FIXES APPLIED THIS RUN (<N> total):
 1. <what was fixed> — <file>:~<line>
 2. <what was fixed> — <file>:~<line>
 ...

REMAINING GAPS (<N>):
 1. <gap> — <reason>
 ...

FILES:
  parity/<N>-Parity-status.md ... created
  preview/<slug>.html .......... <N> edits applied
```

---

## Rules

- Read ACTUAL code — real line numbers, not guesses
- EVERY Swift behaviour must be checked — do not skip any
- Fix gaps directly in the HTML — don't just report them
- Do NOT modify workbench tools (pen, notes, drawing)
- Do NOT modify the controls-bar workbench section
- The chat output table is MANDATORY — it's the primary deliverable
- The file report is the secondary deliverable for future reference
- Use the CURRENT date for all timestamps
- Parity % = matched behaviours / total behaviours * 100
- A `MATCH` means the user experience is identical in both platforms
- A `PARTIAL` counts as a gap — fix it to `MATCH`
- API calls that exist in Swift but not HTML are gaps (except where browser limitations prevent it, e.g., AVAudioRecorder — note these as "browser limitation")
- Test mentally: for every user action, ask "would the same thing happen in both?"
- Be exhaustive — the goal is 100% behaviour parity
