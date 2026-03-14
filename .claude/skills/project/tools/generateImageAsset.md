---
name: generateImageAsset
description: Generates a custom image asset using AI (OpenAI gpt-image-1.5) and returns URL or generation ID
unlocked_by: skills/project/SKILL.md
category: image-assets
---

# generateImageAsset

Generates a custom image asset using AI and uploads to R2. Returns image URL or generation ID for background tasks.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `prompt` | string | yes | Detailed description of the desired image |
| `assetName` | string | yes | Short snake_case identifier (e.g. `"hero_banner"`) |
| `size` | enum | yes | `"1024x1024"` (square) \| `"1024x1536"` (portrait) \| `"1536x1024"` (landscape) |
| `background` | enum | yes | `"transparent"` (cutouts/mascots) \| `"opaque"` (scenes/photos) \| `"auto"` (model decides) |
| `inputImages` | string[] | yes | URLs of existing images for edit/composite mode. Pass empty array `[]` for new generation |
| `runInBackground` | boolean | no | Default `true`. Set to run generation in background |

## Returns

- **Background mode** (`runInBackground: true`): Returns generation ID immediately
- **Foreground mode** (`runInBackground: false`): Returns asset URL on completion

## Sequencing Rules

1. **Confirm with user first** via `confirmImageGeneration`
2. Continue building the app while generation runs in background
3. **NEVER retry failed generations** — user sees the error and can retry manually
4. Use returned URL to reference images in code
