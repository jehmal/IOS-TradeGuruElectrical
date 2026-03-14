---
name: swiftInstall
description: Adds Swift package dependencies to the Xcode project
unlocked_by: base
category: swift-project
---

# swiftInstall

Adds Swift Package Manager (SPM) package references to the Xcode project.

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `packages` | array | yes | Array of package objects (see schema below) |
| `path` | string | no | Path to the Swift app |

### Package Object Schema

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `url` | string | yes | Package repository URL (must end in `.git`) |
| `version` | string | yes | Minimum version string (e.g. `"5.0.0"`) |
| `products` | string[] | yes | Array of product names to import |

## Returns

Adds the specified Swift package references to the Xcode project configuration.

## Sequencing Rules

1. **Research first** — use web/code search to verify package URL and product names before calling
2. **Build after install** — run `swiftBuild` to resolve dependencies
3. Do not invent package URLs — verify they exist

## Example

```json
{
  "packages": [
    {
      "url": "https://github.com/RevenueCat/purchases-ios-spm.git",
      "version": "5.0.0",
      "products": ["RevenueCat"]
    }
  ],
  "path": "ios"
}
```
