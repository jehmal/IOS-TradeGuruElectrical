# tool Inventory — tradeguru

## Instructions

This document tracks all **tool** items in the tradeguru project.

### How to use this document
- Items are numbered sequentially within each sub-type section
- Each item shows: number, name, path, status, and dependencies
- Run `/inventory-get tool` to refresh this inventory
- Manually add items by appending to the appropriate sub-type section
- **pending** = tool definition exists but CLI implementation not yet built
- **active** = fully implemented and callable
- **unavailable** = referenced in docs but no API surface exists yet

### Document sections
1. **Summary** — Quick counts per sub-type
2. **Inventory** — Complete numbered list segmented by sub-type
3. **StockTake** — Audit trail of when inventory was checked and what changed

---

## Summary

| Sub-type | Count | Active | Pending | Unavailable | Deprecated |
|----------|-------|--------|---------|-------------|------------|
| cli | 12 | 12 | 0 | 0 | 0 |
| utility | 12 | 12 | 0 | 0 | 0 |
| mcp-tool (implemented) | 11 | 11 | 0 | 0 | 0 |
| input-request | 3 | 0 | 3 | 0 | 0 |
| debugging | 1 | 0 | 1 | 0 | 0 |
| in-app-purchases | 16 | 0 | 16 | 0 | 0 |
| app-store-connect | 7 | 0 | 7 | 0 | 0 |
| unavailable | 5 | 0 | 0 | 5 | 0 |
| **Total** | **67** | **35** | **27** | **5** | **0** |

---

## Inventory

### cli

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | tradeguru (CLI entrypoint) | `.claude/tools/tradeguru:1` | active | project_tools, image_tools, argparse, json |
| 2 | cmd_create_app | `.claude/tools/tradeguru:33` | active | project_tools.create_app |
| 3 | cmd_swift_build | `.claude/tools/tradeguru:38` | active | project_tools.swift_build |
| 4 | cmd_swift_install | `.claude/tools/tradeguru:42` | active | project_tools.swift_install |
| 5 | cmd_swift_add_target | `.claude/tools/tradeguru:56` | active | project_tools.swift_add_target |
| 6 | cmd_list_assets | `.claude/tools/tradeguru:61` | active | image_tools.list_existing_image_assets |
| 7 | cmd_confirm_images | `.claude/tools/tradeguru:66` | active | image_tools.confirm_image_generation |
| 8 | cmd_generate_asset | `.claude/tools/tradeguru:70` | active | image_tools.generate_image_asset |
| 9 | cmd_generate_image | `.claude/tools/tradeguru:82` | active | image_tools.generate_image |
| 10 | cmd_generate_icon | `.claude/tools/tradeguru:93` | active | image_tools.generate_icon |
| 11 | cmd_generate_screenshot | `.claude/tools/tradeguru:103` | active | image_tools.generate_screenshot |
| 12 | cmd_wait_result | `.claude/tools/tradeguru:114` | active | image_tools.wait_image_result |

### utility

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | find_project_root | `.claude/tools/utils.py:30` | active | pathlib, os |
| 2 | get_app_path | `.claude/tools/utils.py:46` | active | find_project_root |
| 3 | read_json | `.claude/tools/utils.py:74` | active | json, pathlib |
| 4 | write_json | `.claude/tools/utils.py:85` | active | json, pathlib |
| 5 | load_assets | `.claude/tools/utils.py:99` | active | read_json, get_assets_registry |
| 6 | save_asset_record | `.claude/tools/utils.py:106` | active | read_json, write_json, get_assets_registry |
| 7 | scan_xcassets | `.claude/tools/utils.py:161` | active | read_json, pathlib |
| 8 | save_job | `.claude/tools/utils.py:124` | active | read_json, write_json, get_jobs_file |
| 9 | get_job | `.claude/tools/utils.py:139` | active | read_json, get_jobs_file |
| 10 | update_job | `.claude/tools/utils.py:145` | active | read_json, write_json, get_jobs_file |
| 11 | generate_job_id | `.claude/tools/utils.py:154` | active | hashlib, time |
| 12 | to_pascal | `.claude/tools/project_tools.py:79` | active | (none) |

### mcp-tool (implemented)

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | project__createApp | `.claude/skills/project/tools/createApp.md:1` | active | create_app (project_tools.py) |
| 2 | swiftBuild | `.claude/skills/project/tools/swiftBuild.md:1` | active | swift_build (project_tools.py) |
| 3 | swiftInstall | `.claude/skills/project/tools/swiftInstall.md:1` | active | swift_install (project_tools.py) |
| 4 | swiftAddTarget | `.claude/skills/project/tools/swiftAddTarget.md:1` | active | swift_add_target (project_tools.py) |
| 5 | confirmImageGeneration | `.claude/skills/project/tools/confirmImageGeneration.md:1` | active | confirm_image_generation (image_tools.py) |
| 6 | listExistingImageAssets | `.claude/skills/project/tools/listExistingImageAssets.md:1` | active | list_existing_image_assets (image_tools.py) |
| 7 | generateImageAsset | `.claude/skills/project/tools/generateImageAsset.md:1` | active | generate_image_asset (image_tools.py) |
| 8 | image-gen__generateImage | `.claude/skills/image-gen/tools/generateImage.md:1` | active | generate_image (image_tools.py) |
| 9 | image-gen__generateIcon | `.claude/skills/image-gen/tools/generateIcon.md:1` | active | generate_icon (image_tools.py) |
| 10 | image-gen__generateScreenshot | `.claude/skills/image-gen/tools/generateScreenshot.md:1` | active | generate_screenshot (image_tools.py) |
| 11 | image-gen__waitImageResult | `.claude/skills/image-gen/tools/waitImageResult.md:1` | active | wait_image_result (image_tools.py) |

### input-request

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | requestAsset | `(pending)` | pending | User attachment widget for screenshots/files. Params: reason, mimeFilter, minFiles, maxFiles |
| 2 | requestEnvs | `(pending)` | pending | Environment variable input widget. Params: envs[], reason. Required before coding against Config properties |
| 3 | saveAttachment | `(pending)` | pending | Save user-provided attachment to project filesystem. Params: url, path, contentType |

### debugging

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | debugging__fetch_logs | `(pending)` | pending | Fetch backend/appstore logs. Params: source (backend\|appstore), limit, skip. Skill: debugging/SKILL.md |

### in-app-purchases

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | in-app-purchases__connectRevenueCat | `(pending)` | pending | Connect project to RevenueCat. No params |
| 2 | in-app-purchases__fetchConfiguration | `(pending)` | pending | Get apps, products, entitlements, offerings. No params |
| 3 | in-app-purchases__createApp | `(pending)` | pending | Create RevenueCat app. Params: name, type, bundleId, packageName |
| 4 | in-app-purchases__listPublicApiKeys | `(pending)` | pending | List API keys. Params: appId, limit, startingAfter |
| 5 | in-app-purchases__createEntitlement | `(pending)` | pending | Create entitlement. Params: lookupKey, displayName |
| 6 | in-app-purchases__attachProductsToEntitlement | `(pending)` | pending | Attach products to entitlement. Params: entitlementId, productIds[] |
| 7 | in-app-purchases__detachProductsFromEntitlement | `(pending)` | pending | Detach products from entitlement. Params: entitlementId, productIds[] |
| 8 | in-app-purchases__createProduct | `(pending)` | pending | Create product. Params: storeIdentifier, appId, type, displayName |
| 9 | in-app-purchases__addTestStoreProduct | `(pending)` | pending | Create Test Store product with pricing. Params: appId, storeIdentifier, displayName, type, subscriptionDuration, prices[] |
| 10 | in-app-purchases__deleteProduct | `(pending)` | pending | Delete product. Params: productId. Destructive |
| 11 | in-app-purchases__createOffering | `(pending)` | pending | Create offering. Params: lookupKey, displayName |
| 12 | in-app-purchases__updateOffering | `(pending)` | pending | Update offering. Params: offeringId, displayName, isCurrent, metadata |
| 13 | in-app-purchases__createPackage | `(pending)` | pending | Create package. Params: offeringId, lookupKey, displayName, position |
| 14 | in-app-purchases__updatePackage | `(pending)` | pending | Update package. Params: packageId, displayName, position |
| 15 | in-app-purchases__deletePackage | `(pending)` | pending | Delete package. Params: packageId. Destructive |
| 16 | in-app-purchases__attachProductsToPackage | `(pending)` | pending | Attach products to package. Params: packageId, products[] |

### app-store-connect

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | app-store-connect__setupAsc | `(pending)` | pending | Setup ASC CLI credentials. Params: teamId (optional). Must run before any asc command |
| 2 | app-store-connect__connectAppleDeveloper | `(pending)` | pending | Apple Developer auth flow. No params. Use when setupAsc returns no_api_key/session_expired |
| 3 | app-store-connect__checkAppleSession | `(pending)` | pending | Check session validity. Params: teamId |
| 4 | app-store-connect__ensureCertificate | `(pending)` | pending | Ensure distribution certificate. Params: teamId. Stop on UNKNOWN_CERT/CERT_LIMIT errors |
| 5 | app-store-connect__ensureApp | `(pending)` | pending | Ensure ASC app record. Params: teamId, bundleId, appName, platform, version. Returns ascAppId |
| 6 | app-store-connect__syncCapabilities | `(pending)` | pending | Sync entitlements to portal. Params: teamId, bundleId, xcodeProjectPath |
| 7 | app-store-connect__submitBuild | `(pending)` | pending | Build, sign, upload, distribute. Params: teamId, bundleId, appName, version, platform, xcodeProjectPath, ascAppId, attemptNumber |

### unavailable

| # | Name | Path | Status | Dependencies |
|---|------|------|--------|--------------|
| 1 | waitImageAssetResult | `(unavailable)` | unavailable | Referenced in project skill but no API surface exists |
| 2 | confirm3DGeneration | `(unavailable)` | unavailable | Referenced in broader guidance, no callable tool |
| 3 | generate3DModel | `(unavailable)` | unavailable | Referenced in broader guidance, no callable tool |
| 4 | confirmAudioGeneration / generateAudio | `(unavailable)` | unavailable | Referenced in broader guidance, no callable tool |
| 5 | confirmVideoGeneration / generateVideo | `(unavailable)` | unavailable | Referenced in broader guidance, no callable tool |

---

## StockTake

| Date | Action | Items Added | Items Removed | Total |
|------|--------|-------------|---------------|-------|
| 2026-03-14 | initial scan | 35 | 0 | 35 |
| 2026-03-14 | refresh — added pending tools from Rork-Max registry | 32 | 0 | 67 |
