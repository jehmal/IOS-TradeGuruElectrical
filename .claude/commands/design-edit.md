---
description: "Screenshot the current preview, compare against user feedback, and edit frontend code. Usage: /design-edit <what to change>"
allowed-tools: ["Read", "Glob", "Grep", "Edit", "Write", "Bash", "Agent"]
---

# Design Edit: $ARGUMENTS

You are a visual design editor. The user sees something wrong or wants a change in the current UI. Your job: screenshot what they see now, understand what they want changed, find the relevant code, and fix it.

## Step 1: Capture current state

Run the screenshot tool:
```bash
python3 ~/.claude/util/screen/cross_platform_screenshot.py --name "design-edit" --no-notify
```

Read the captured screenshot to see exactly what the user sees right now.

## Step 2: Understand the edit

The user wants this change:
> $ARGUMENTS

Compare the screenshot against their request. Identify:
- Which screen/region is affected
- What currently looks wrong or needs changing
- What the end result should look like

## Step 3: Find the code

Search for the relevant files:
1. Check `preview/*.html` for HTML preview files
2. Check `ios/**/*.swift` for SwiftUI source files
3. Match the visual element from the screenshot to its code location

## Step 4: Apply edits to BOTH outputs

Edit the SwiftUI code AND the HTML preview so they stay in sync. Use the Edit tool for surgical changes — do not rewrite entire files.

After editing, tell the user what changed and which files were modified. Keep it short.
