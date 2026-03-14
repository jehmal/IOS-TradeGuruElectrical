---
description: "Generate a detailed agent build prompt from a natural language description. Usage: /build-prompt <what you want built>"
allowed-tools: ["Read", "Glob", "Grep", "Write"]
---

# Build Prompt Generator

The user wants to build something. They described it casually:

> $ARGUMENTS

Your job is to transform this into a **precise, self-contained agent prompt** that a code agent with ZERO prior context can execute perfectly. The agent receiving this prompt has never seen this project before.

## Step 1: Gather project context

Read these files to understand the project state. You MUST read all of them:

1. `CLAUDE.md` — project rules, SwiftUI conventions, anti-patterns, decision trees
2. `rork.json` — app name, framework, path
3. The mock analysis for the relevant screen (search `mocks/**/*.md` for the closest match)
4. The mock image itself (if a `.md` analysis references one)
5. `ios/` directory — scan existing Swift files to understand current code structure
6. `assets/branding/` — check for logos, icons, brand assets available

Build a mental model of:
- What the app is called
- What exists already (files, views, models)
- What design references are available
- What rules the code must follow

## Step 2: Figure out what's being asked

Parse the user's description and identify:
- **Screen/feature name** — what is being built (e.g. "chat screen", "settings page", "onboarding flow")
- **Components described** — every UI element mentioned
- **Components explicitly removed** — things the user said to NOT include
- **Modifications from mock** — where the user deviates from any reference mock
- **Interactions** — tappable elements, state changes, mode switching, navigation
- **Data models needed** — enums, structs, view models implied by the description
- **Assets referenced** — logos, images, icons mentioned

## Step 3: Generate the agent prompt

Write the prompt to: `prompts/<screen-slug>-build.md`

The prompt MUST follow this exact structure:

```markdown
# Agent Prompt: Build <ScreenName> — SwiftUI + HTML Preview

You are building the <screen> for <AppName>, a native Swift/SwiftUI iOS app. You must produce TWO outputs: real SwiftUI code AND a browser-viewable HTML/CSS preview.

## Why Two Outputs

This project runs on Windows/WSL with no local Xcode. The SwiftUI code is the real app code that will compile on a Mac or cloud build. The HTML preview lets the developer instantly see the layout in a browser on their local machine without needing Xcode. Both outputs must be pixel-identical representations of the same design.

## Reference Materials

Before writing any code, read these files:
- <list every file the agent needs to read, with paths>
- <mock analysis .md if one exists>
- <mock image .png if one exists>
- <brand assets>
- `CLAUDE.md` — project rules (MUST follow all SwiftUI conventions and anti-patterns)

## Design Spec

<Detailed specification of every element, region by region, top to bottom, left to right. Include:>

### <Region 1 Name>
- Element-by-element description
- Exact text content in quotes
- SF Symbol names for icons
- Sizing in points
- Colors as semantic tokens
- Interaction behavior

### <Region 2 Name>
- ...continue for every region...

### REMOVED (do NOT include)
- <Bullet list of everything the user explicitly said to remove>
- <Things from the mock that are NOT in the user's description>

### State & Interactions
- <What is tappable>
- <What changes on tap>
- <What data flows where>
- <Default states>

## Output 1: SwiftUI Code

Write these files inside `ios/<AppSourceDir>/`:

<List every Swift file to create with:>
1. **`FileName.swift`** — One-line description of what it contains. Note key patterns: @State ownership, @Binding, nonisolated for data types.

<If updating existing files, say which ones and what changes>

### SwiftUI Rules (from CLAUDE.md)
<Extract and list the specific rules that apply to THIS screen. Don't dump all 62 — pick the 10-15 that matter. For example:>
- Use `.foregroundStyle()` not `.foregroundColor()`
- Use `.clipShape(.rect(cornerRadius: X))` not `.cornerRadius()`
- Use `NavigationStack` not `NavigationView`
- Use `Button` for all tappable elements, not `.onTapGesture`
- Mark pure data types `nonisolated`
- Keep 44pt minimum tap targets
- Support Dark Mode with semantic colors
- Use `@Observable` with `@State` ownership for shared state
- <...other relevant rules>

## Output 2: HTML Preview

Write a single self-contained file at `preview/<screen-slug>.html` that:

- Renders inside an iPhone 15 Pro device frame (393x852pt CSS viewport)
- Pixel-matches the SwiftUI layout: same spacing, fonts, colors, corner radii
- Uses `-apple-system` font for SF Pro appearance
- Embeds any images/logos as base64 data URIs (read the files and encode them)
- All interactive elements work (tapping modes, typing in inputs, visual state changes)
- Uses CSS variables for color tokens (list them)
- Fully self-contained — no external dependencies, opens in any browser
- Includes a device frame with rounded corners and notch/dynamic island

## Verification Checklist

After writing all files, confirm:
<Numbered list of specific things to verify — extracted from the design spec and CLAUDE.md rules. e.g.:>
1. No .foregroundColor() usage
2. No .cornerRadius() usage
3. NavigationStack used (not NavigationView)
4. All data enums marked nonisolated
5. HTML preview mode selector is interactive
6. <...specific to this screen>
```

## Rules for generating the prompt

- The prompt must be SELF-CONTAINED. An agent with no project knowledge must be able to execute it.
- Include EXACT file paths — never say "the relevant directory", say `ios/Tradeguruelectrical/ChatView.swift`
- Include EXACT text content — if the user said the placeholder should say "Ask TradeGuru", write `"Ask TradeGuru"` in the spec
- Include EXACT SF Symbol names — don't say "a search icon", say `magnifyingglass`
- Pull spacing/font values from the mock analysis if one exists
- List REMOVED items explicitly — agents hallucinate extra UI if you don't tell them what to exclude
- Extract the 10-15 most relevant CLAUDE.md rules for this specific screen
- Always explain WHY we need both SwiftUI and HTML (no Xcode locally)
- The verification checklist should catch the most common mistakes for this type of screen

## Step 4: Tell the user

After writing the prompt file, tell the user:
- Where the prompt was saved
- A quick summary of what it instructs (screen name, file count, key interactions)
- How to use it: "Hand this to a code-scribe agent or paste it as a prompt"
