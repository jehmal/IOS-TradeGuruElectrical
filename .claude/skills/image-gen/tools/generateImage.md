---
name: image-gen__generateImage
description: Generates images using AI and uploads to R2, returns image URL
unlocked_by: skills/image-gen/SKILL.md
category: image-generation
---

# image-gen__generateImage

Generates images using AI and uploads to R2. Returns an image URL.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `prompt` | object | yes | Generate prompt (describe image) or edit prompt (describe changes with `inputImages`) |
| `background` | enum | yes | `"transparent"` \| `"opaque"` \| `"auto"` |
| `size` | string | yes | `"1024x1024"`, `"1024x1536"`, or `"1536x1024"` |
| `type` | enum | yes | `"icon"` for app icons \| `"asset"` for other images |
| `runInBackground` | boolean | no | Default `false`. Set `true` to schedule in background, then use `waitImageResult` |

## Returns

- **Foreground**: Image URL
- **Background**: Generation handle / `generationId` for use with `waitImageResult`

## Sequencing Rules

1. Use **only when user explicitly requests** image generation
2. For icons saved into project, **prefer `generateIcon`** instead
3. For `type: "icon"`: size auto-set to 1024x1024, background to opaque
4. For `type: "asset"`: decorative images, backgrounds, patterns, design elements
