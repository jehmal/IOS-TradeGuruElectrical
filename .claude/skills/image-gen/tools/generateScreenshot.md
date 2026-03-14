---
name: image-gen__generateScreenshot
description: Generates App Store screenshot mockups for marketing
unlocked_by: skills/image-gen/SKILL.md
category: image-generation
---

# image-gen__generateScreenshot

Generates App Store screenshot mockups with marketing-style composition.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `prompt` | object | yes | Describe the screenshot (generate) or what to change (edit with `inputImages`) |
| `appPath` | string | yes | App folder path (e.g. `"ios"`, `"expo"`) |
| `device` | enum | yes | `"iphone"` (auto-resized to 1320x2868) \| `"ipad"` (auto-resized to 2048x2732) |
| `runInBackground` | boolean | no | Default `false`. Set `true` for background scheduling |

## Returns

Screenshot generation result or generation ID for background tasks.

## Sequencing Rules

1. **Prefer real screenshot references** — if user hasn't attached one, use `requestAsset` to ask
2. Default to **marketing-style composition**: device mockup + styled background + headline text
3. Only use plain fullscreen screenshot if user explicitly asks
4. Read the app's code first to understand design, colors, and best features

## Auto-Sizing

| Device | Output Size |
|--------|-------------|
| iPhone 6.9" | 1320 x 2868 |
| iPad 13" | 2048 x 2732 |
