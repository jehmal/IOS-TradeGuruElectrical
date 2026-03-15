---
description: "Analyse a wiring spec and produce an optimal agent assignment plan with zero file overlap. Usage: /assign-agents <path-to-spec>"
allowed-tools: ["Read", "Glob", "Grep"]
---

# Assign Agents: $ARGUMENTS

You are an agent orchestrator. Your job is to read a wiring spec, map every file each item touches, find the maximum safe parallelism with zero file overlap, and output an assignment plan.

## Step 1: Read the spec

Read the file at: `$ARGUMENTS`

If no path given, search for the most recent spec in `specs/` that contains an "Inventory Item Checklist" section.

## Step 2: Extract the file conflict map

For EVERY numbered item in the spec's checklist:
1. Read the "Changes needed (Swift)" and "Changes needed (HTML)" sections
2. Extract every file path mentioned (exact paths from the spec)
3. Record: Item # → [list of files it touches]

## Step 3: Build the conflict matrix

Create a matrix showing which files are touched by which items. Identify:
- **Bottleneck files** — touched by 3+ items (these CANNOT be parallelised)
- **Isolated files** — touched by only 1-2 items (safe for dedicated agents)
- **New files** — files that don't exist yet (safe, no conflicts)

## Step 4: Determine maximum parallelism

Rules:
- Two agents CANNOT write to the same file
- An agent owns ALL changes to its assigned files
- Bottleneck files must be handled by a single integration agent that runs AFTER parallel agents
- The HTML preview file (`preview/chat.html` or equivalent) should run LAST because it must mirror the final Swift code for parity (per Mandatory rule #3)
- Group related items that touch the same isolated file into one agent
- Minimise total agents — don't create an agent for a single 2-line change

## Step 5: Output the assignment plan

Output this EXACTLY in chat (not to a file — this is a decision aid, not a document):

```
## File Conflict Map

| File | Items | Touch Count |
|------|-------|-------------|
| `<file>` | #N, #N, #N | N |

Bottlenecks: <list files touched by 3+ items>
Isolated: <list files touched by 1-2 items>
New files: <list files that need creating>

## Agent Assignment

### Phase 1 (parallel — N agents)

| Agent | Name | Files (exclusive) | Spec Items | Est. Changes |
|-------|------|--------------------|------------|-------------|
| A | `<name>` | `<files>` | #N, #N | N lines |
| B | `<name>` | `<files>` | #N, #N | N lines |

### Phase 2 (sequential — after phase 1)

| Agent | Name | Files | Spec Items | Why Sequential |
|-------|------|-------|------------|----------------|
| C | `<name>` | `<bottleneck files>` | #N, #N, #N | Touches shared files, integrates callbacks from phase 1 |

### Phase 3 (sequential — after phase 2)

| Agent | Name | Files | Spec Items | Why Last |
|-------|------|-------|------------|----------|
| D | `html-parity` | `preview/*.html` | All HTML portions | Must read final Swift to ensure 1:1 parity |

## Summary

| Metric | Value |
|--------|-------|
| Total agents | N |
| Max concurrent | N |
| Phases | N |
| Total spec items | N |
| Items per agent (avg) | N |

## Launch Order

Phase 1: Launch A, B, C in parallel
Phase 2: After phase 1 completes, launch D
Phase 3: After phase 2 completes, launch E
```

## Rules

- NEVER assign two agents to the same file
- Always put HTML as the LAST phase (parity requirement)
- Always put bottleneck files (ChatView, ChatViewModel, etc.) in their own sequential phase
- If a spec item touches both an isolated file AND a bottleneck file, split it: the isolated file change goes to the parallel agent, the bottleneck file change goes to the integration agent
- Name agents descriptively based on what they own (e.g. `input-agent`, `bubble-agent`, not `agent-1`)
- Estimate changes as: small (< 20 lines), medium (20-80 lines), large (80+ lines)
- If the entire spec has fewer than 5 items, recommend a single agent instead of splitting
- The output goes to CHAT only — do not write a file. This is a planning tool, not a document.
