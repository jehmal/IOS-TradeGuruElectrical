---
name: swiftAddTarget
description: Adds a new Xcode target scaffold with embedding and dependency configuration
unlocked_by: base
category: swift-project
---

# swiftAddTarget

Adds a new Xcode target scaffold and configures embedding/dependency settings.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `name` | string | yes | Target name in PascalCase (e.g. `"MyWidget"`, `"ShareExtension"`) |
| `type` | enum | yes | Target type (see options below) |
| `path` | string | no | Path to the Swift app |

### Target Types

| Value | Description |
|-------|-------------|
| `"widget"` | Home Screen / Lock Screen widget |
| `"share-extension"` | Share sheet extension |
| `"notification-content"` | Rich notification UI |
| `"app-intent"` | App Intents / Shortcuts |
| `"watch-app"` | watchOS companion app |
| `"tv-app"` | tvOS app |
| `"vision-app"` | visionOS app |
| `"imessage-app"` | iMessage sticker/app |

## Returns

Creates the target scaffold files and configures:
- Target membership in Xcode project
- Embedding configuration
- Dependency linking

## Sequencing Rules

1. Use **only** when user explicitly requests an extra Apple platform target
2. After target creation, **implement actual Swift code** for the target
3. Run `swiftBuild` after making changes to verify

## Example

```json
{
  "name": "TradeWidget",
  "type": "widget",
  "path": "ios"
}
```
