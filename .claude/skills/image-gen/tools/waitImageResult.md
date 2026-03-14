---
name: image-gen__waitImageResult
description: Waits for and retrieves the result of a background image generation
unlocked_by: skills/image-gen/SKILL.md
category: image-generation
---

# image-gen__waitImageResult

Waits for a background image generation to complete and returns the final result.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `generationId` | string | yes | The generation ID returned from a background image generation call |

## Returns

Final image result including the generated image URL and metadata.

## Sequencing Rules

1. Use **only after** a background image generation call (`runInBackground: true`)
2. Call at the end of the workflow after other work is done
3. The `generationId` comes from `generateImage`, `generateIcon`, `generateScreenshot`, or `generateImageAsset` when run in background mode
