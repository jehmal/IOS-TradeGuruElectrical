---
name: listExistingImageAssets
description: Lists all previously generated image assets to prevent duplicates
unlocked_by: skills/project/SKILL.md
category: image-assets
---

# listExistingImageAssets

Returns records of all previously generated image assets for the current project.

## Parameters

None.

## Returns

Array of existing generated asset records with names, URLs, and generation metadata.

## Sequencing Rules

1. **Always call before generating new assets** to avoid duplicates
2. Use the results to decide which assets still need to be created
