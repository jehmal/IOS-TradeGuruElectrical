---
name: confirmImageGeneration
description: Presents proposed image assets to user for review and approval before generation
unlocked_by: skills/project/SKILL.md
category: image-assets
---

# confirmImageGeneration

Presents a user review/approval widget for proposed image assets. The user can edit prompts, change size/background, or skip individual assets.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `assets` | array | yes | Array of asset proposal objects |

### Asset Object Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `assetName` | string | yes | Short snake_case identifier (e.g. `"hero_banner"`, `"onboarding_bg"`) |
| `prompt` | string | yes | Description of the desired image |

## Returns

User review/approval widget. User can approve, edit, or skip each asset.

## Sequencing Rules

1. **Call `listExistingImageAssets` first** to check for duplicates
2. Pass **all** asset proposals in a single call
3. After user approval, call `generateImageAsset` for each approved asset
