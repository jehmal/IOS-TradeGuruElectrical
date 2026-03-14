---
name: project__createApp
description: Creates a new app project scaffold and updates root rork.json manifest
unlocked_by: skills/project/SKILL.md
category: project-creation
---

# project__createApp

Creates app scaffold and updates root `rork.json`.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `name` | string | yes | A short, user-friendly app name (e.g. "My Fitness Tracker") |
| `framework` | enum | yes | `"react-native"` \| `"swift"` \| `"web"` \| `"kotlin"` |
| `path` | string | yes | Lowercase folder name matching `[a-z0-9-]+` pattern (e.g. `"ios"`, `"expo"`, `"web"`) |

## Returns

Creates the app scaffold files in a subfolder and updates the root `rork.json` manifest.

## Sequencing Rules

1. On **first message** with empty workspace, call this **before** writing any code
2. For native iOS projects, use `framework: "swift"` and `path: "ios"`
3. After completion, start editing code in the created subfolder

## Example

```json
{
  "name": "TradeGuru",
  "framework": "swift",
  "path": "ios"
}
```
