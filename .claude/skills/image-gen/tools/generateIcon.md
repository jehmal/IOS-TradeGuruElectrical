---
name: image-gen__generateIcon
description: Generates and saves an app icon into the correct project asset structure
unlocked_by: skills/image-gen/SKILL.md
category: image-generation
---

# image-gen__generateIcon

Generates and saves an app icon directly into the correct project asset catalog structure.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `prompt` | object | yes | Description of the icon design |
| `appPath` | string | yes | App folder path (e.g. `"ios"`, `"expo"`) |
| `iconType` | enum | no | `"icon"` (default, main app icon) \| `"tvos-icon"` \| `"visionos-icon"` \| `"imessage-icon"` |
| `runInBackground` | boolean | no | Default `false`. Set `true` for background scheduling |

## Returns

Generated and saved icon asset files in the correct project locations:
- **Swift**: `Assets.xcassets/AppIcon.appiconset/icon.png`
- **React Native**: `assets/images/icon.png` + adaptive variants

## Sequencing Rules

1. **Read image-gen skill first** before calling
2. For secondary icon types (`tvos-icon`, `visionos-icon`, `imessage-icon`): **generate main icon first** with `iconType: "icon"`, then call again for platform-specific variants
3. Platform-specific icons read the existing app icon from disk and transform it
