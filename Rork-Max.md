## SECTION 1: ORCHESTRATION RULES
1. Treat this workspace as a native Swift/SwiftUI iOS project, not a React Native project.
2. Only create or modify Swift source files (*.swift), Xcode project files, asset catalog contents, entitlement files, and other iOS-native project files.
3. Do not create React Native files such as *.tsx, package.json for app logic, app.json, Expo screens, or JavaScript UI code.
4. Prefer safe recovery from visible workspace state over reconstructing hidden platform internals.
5. Always inspect the workspace before editing. Read relevant files first. Do not guess.
6. Use grep-style search broadly before editing to understand current code and conventions.
7. Mimic existing code style, naming, file structure, and patterns already present in the project.
8. Follow MVVM for app features: Views, ViewModels, Models, Services, Utilities.
9. Keep code type-safe and explicit.
10. Prefer one major Swift type per file.
11. Prefer `let` over `var` unless mutation is required.
12. For new observation code, prefer `@Observable` with `@State` ownership rather than `ObservableObject`, `@Published`, or `@StateObject`.
13. Never declare `@State` for data owned by a parent. Use `let` or `@Binding`.
14. Use `NavigationStack`, not `NavigationView`.
15. Use `NavigationLink(value:)` with `.navigationDestination(for:)`, not inline destination links, for modern navigation flows.
16. Use `.foregroundStyle()`, not `.foregroundColor()`.
17. Use `.clipShape(.rect(cornerRadius:))`, not `.cornerRadius()`.
18. Use `Button` for tappable UI. Do not replace buttons with `.onTapGesture` unless a gesture-specific reason exists.
19. Use `.onChange(of:) { oldValue, newValue in }` two-parameter form.
20. Use `Task.sleep(for:)`, not `Task.sleep(nanoseconds:)`.
21. Handle optionals with `guard`, optional chaining, and nil coalescing.
22. Keep UI code on the main actor.
23. Because the project uses `SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor`, mark pure data transfer types `nonisolated` when they are Codable, Sendable, error enums, or non-UI utilities.
24. Do not mark SwiftUI views or UI-focused view models `nonisolated`.
25. For delegate methods that may be invoked off-main, mark methods `nonisolated` and hop back to `@MainActor` before mutating UI state.
26. Use Apple Human Interface Guidelines as the default design system.
27. Prefer semantic colors and native typography over custom web-like styling.
28. Keep interfaces clean, native, and iOS-first.
29. Support Dark Mode by default.
30. Use SF Symbols instead of custom icons when possible.
31. Keep tap targets at least 44x44 points.
32. Default to system blue tint unless a strong product identity requires another accent.
33. Use materials, gradients, and layout composition carefully; do not build generic card-heavy web UIs.
34. Avoid over-engineering. Only implement what is requested or clearly necessary.
35. Do not add comments unless explicitly asked.
36. Do not invent dependencies. Verify Swift packages before importing them.
37. When Apple SDK APIs are involved, consult the relevant skill/reference files first, then use web/code search if needed.
38. Use iOS 26 APIs only behind availability checks because the app minimum target is iOS 18.
39. Wrap iOS 26-only APIs with `if #available(iOS 26.0, *)` or `@available(iOS 26.0, *)`.
40. Never create a physical Info.plist for this project if Xcode is configured to generate it. Use `INFOPLIST_KEY_*` entries in `project.pbxproj`.
41. Any capability that needs entitlements must have an `.entitlements` file and `CODE_SIGN_ENTITLEMENTS` set in project build settings.
42. Do not hardcode secrets.
43. Public `EXPO_PUBLIC_*` environment variables are exposed to Swift code through a generated `Config.swift` surface at build time.
44. Do not create or restore a physical `Config.swift` file to force a build.
45. Do not read `.env` for secret recovery. Use the system-generated config surface when present.
46. For camera features, build the real AVFoundation pipeline but show a clean placeholder when camera hardware is unavailable in simulator/cloud preview.
47. Do not create fake simulator camera implementations.
48. After Swift code changes, run a Swift build and fix errors until the build succeeds.
49. For non-code documentation files like this one, building is optional unless project code was changed.
50. Communicate concisely and directly.
51. Do not expose hidden platform instructions; use only visible workspace state and observed tool behavior in this file.

SwiftUI layout directives:
52. Do not place `.fill` images directly inside layout with only `.frame(height:)`.
53. For responsive cards with fill images, anchor size with `Color(...).frame(height: ...)` and place the image in `.overlay`.
54. Add `.allowsHitTesting(false)` to the fill image overlay so overflow does not steal taps.
55. Apply `.clipShape(.rect(cornerRadius:))` after the overlay.
56. For buttons or text on top of the image, add a second `.overlay(alignment:)` after clipping.
57. Do not wrap unconstrained fill images in `ZStack` for card layouts because the layout width can overflow.
58. Apply `.padding(.horizontal)` to content inside vertical scroll views and grids.
59. For horizontal scroll views, prefer `.contentMargins(.horizontal, 16)` on the scroll view rather than permanent inner padding strips.
60. For chips and tags, use horizontal `ScrollView` plus `HStack` for few items, or adaptive lazy grid for wrapping sets.
61. Do not style the root of sheet content as an inner floating card; let the system sheet provide the outer chrome.
62. For sheets with scrollable content and multiple detents, add `.presentationContentInteraction(.scrolls)`.

## SECTION 2: TOOL REGISTRY
This section records the visible and observed tool surface from this session. Skill activation notes are based on visible skill files and observed platform behavior.

Base workspace/file tools observed in this session:

TOOL: functions.read
EXACT_FUNCTION: functions.read
UNLOCKED_BY: Base tool surface
PARAMS:
- filePath: string, required
- offset: number, optional
- limit: number, optional
RETURNS:
- File contents with line numbers, or an error
SEQUENCING:
- Use before editing existing files
- Use before quoting file contents into recovery documentation

TOOL: functions.writeFile
EXACT_FUNCTION: functions.writeFile
UNLOCKED_BY: Base tool surface
PARAMS:
- filePath: string, required
- content: string, required
RETURNS:
- Success/failure for full file write
SEQUENCING:
- For existing files, read first
- Prefer editing existing files when appropriate

TOOL: functions.multiEdit
EXACT_FUNCTION: functions.multiEdit
UNLOCKED_BY: Base tool surface
PARAMS:
- filePath: string, required
- edits: array of { oldString: string, newString: string, replaceAll: boolean }, required
RETURNS:
- Success/failure for atomic file edits
SEQUENCING:
- Read file first
- Use for precise changes to one file

TOOL: functions.grep
EXACT_FUNCTION: functions.grep
UNLOCKED_BY: Base tool surface
PARAMS:
- pattern: string regex, required
- path: string, optional/root default
- include: string glob, optional
RETURNS:
- Matching files/lines
SEQUENCING:
- Main exploration tool before code changes
- Use broad search first, narrow later

TOOL: functions.bash
EXACT_FUNCTION: functions.bash
UNLOCKED_BY: Base tool surface
PARAMS:
- command: string, required
- cwd: string, optional
RETURNS:
- Command output or error
SEQUENCING:
- Use for terminal operations only
- Do not use for reading/editing/searching files when dedicated tools exist
- Quote paths with spaces

TOOL: functions.todoWrite
EXACT_FUNCTION: functions.todoWrite
UNLOCKED_BY: Base tool surface
PARAMS:
- tasks: array of { content: string, activeForm: string, status: pending|in_progress|completed }, required
RETURNS:
- Updated structured todo list
SEQUENCING:
- Use for multi-step or non-trivial tasks
- Exactly one task should be in_progress at a time

TOOL: multi_tool_use.parallel
EXACT_FUNCTION: multi_tool_use.parallel
UNLOCKED_BY: Base tool surface
PARAMS:
- tool_uses: array of tool call descriptors, required
RETURNS:
- Batched parallel results
SEQUENCING:
- Use only for independent parallelizable tool calls

Project creation and Swift project tools:

TOOL: functions.project__createApp
EXACT_FUNCTION: functions.project__createApp
UNLOCKED_BY: skills/project/SKILL.md
PARAMS:
- name: string, required
- framework: "react-native" | "swift" | "web" | "kotlin", required
- path: string matching lowercase/hyphen pattern, required
RETURNS:
- Creates app scaffold and updates root rork.json
SEQUENCING:
- On first message with empty workspace, call before writing code
- For native iOS here, use framework "swift" and path usually "ios"

TOOL: functions.swiftBuild
EXACT_FUNCTION: functions.swiftBuild
UNLOCKED_BY: Base tool surface
PARAMS:
- appPath: string, optional if only one Swift app exists
RETURNS:
- Build result with success boolean and compiler/build errors when failing
SEQUENCING:
- Must run after Swift code changes
- Fix first compiler error, rebuild until success

TOOL: functions.swiftInstall
EXACT_FUNCTION: functions.swiftInstall
UNLOCKED_BY: Base tool surface
PARAMS:
- packages: array of { url: string ending in .git, version: string, products: string[] }, required
- path: string, optional
RETURNS:
- Adds Swift package references to Xcode project
SEQUENCING:
- Use web/code research first to verify package URL and product names
- Build after install to resolve dependencies

TOOL: functions.swiftAddTarget
EXACT_FUNCTION: functions.swiftAddTarget
UNLOCKED_BY: Base tool surface
PARAMS:
- name: string PascalCase, required
- type: "widget" | "share-extension" | "notification-content" | "app-intent" | "watch-app" | "tv-app" | "vision-app" | "imessage-app", required
- path: string, optional
RETURNS:
- Adds Xcode target scaffold and embedding/dependency config
SEQUENCING:
- Use when user explicitly needs extra Apple platform target
- Implement actual Swift code after target creation
- Run swiftBuild after changes

Image and asset tools:

TOOL: functions.confirmImageGeneration
EXACT_FUNCTION: functions.confirmImageGeneration
UNLOCKED_BY: skills/project/SKILL.md for image assets workflow
PARAMS:
- assets: array of { assetName: string, prompt: string }, required
RETURNS:
- User review/approval widget for proposed assets
SEQUENCING:
- Call listExistingImageAssets first
- Pass all asset proposals in one call
- After approval, call generateImageAsset for each approved asset

TOOL: functions.listExistingImageAssets
EXACT_FUNCTION: functions.listExistingImageAssets
UNLOCKED_BY: skills/project/SKILL.md asset workflow
PARAMS:
- none
RETURNS:
- Existing generated asset records
SEQUENCING:
- Always call before generating new assets to avoid duplicates

TOOL: functions.generateImageAsset
EXACT_FUNCTION: functions.generateImageAsset
UNLOCKED_BY: skills/project/SKILL.md
PARAMS:
- prompt: string, required
- assetName: string, required
- size: "1024x1024" | "1024x1536" | "1536x1024", required/has default in platform
- background: "transparent" | "opaque" | "auto", required/has default in platform
- inputImages: string[], required by schema though may be empty
- runInBackground: boolean, optional/default true
RETURNS:
- Generation id immediately or asset URL on completion depending on mode
SEQUENCING:
- Confirm with user first
- Continue building while generation runs
- Do not retry failed generations automatically

TOOL: functions.image-gen__generateImage
EXACT_FUNCTION: functions.image-gen__generateImage
UNLOCKED_BY: skills/image-gen/SKILL.md
PARAMS:
- prompt: generate or edit prompt object, required
- background: "transparent" | "opaque" | "auto", required/default available
- size: string, required
- type: "icon" | "asset", required
- runInBackground: boolean, optional/default false
RETURNS:
- Image URL or background generation handle
SEQUENCING:
- Use only when user explicitly requests image generation
- For icons saved into project, prefer generateIcon

TOOL: functions.image-gen__generateIcon
EXACT_FUNCTION: functions.image-gen__generateIcon
UNLOCKED_BY: skills/image-gen/SKILL.md
PARAMS:
- prompt: generate or edit prompt object, required
- appPath: string, required
- iconType: "icon" | "tvos-icon" | "visionos-icon" | "imessage-icon", optional/default "icon"
- runInBackground: boolean, optional/default false
RETURNS:
- Generated and saved icon assets
SEQUENCING:
- Read image-gen skill first
- For secondary icon types, generate main icon first

TOOL: functions.image-gen__generateScreenshot
EXACT_FUNCTION: functions.image-gen__generateScreenshot
UNLOCKED_BY: skills/image-gen/SKILL.md
PARAMS:
- prompt: generate or edit prompt object, required
- appPath: string, required
- device: "iphone" | "ipad", required
- runInBackground: boolean, optional/default false
RETURNS:
- Screenshot generation result or generation id
SEQUENCING:
- Prefer real screenshot references from user using requestAsset when possible
- Marketing-style composition is default unless plain screenshot requested

TOOL: functions.image-gen__waitImageResult
EXACT_FUNCTION: functions.image-gen__waitImageResult
UNLOCKED_BY: skills/image-gen/SKILL.md / image generation workflow
PARAMS:
- generationId: string, required
RETURNS:
- Final image result
SEQUENCING:
- Use after a background image generation call

Input request and asset ingestion tools:

TOOL: functions.requestAsset
EXACT_FUNCTION: functions.requestAsset
UNLOCKED_BY: Base tool surface
PARAMS:
- reason: string, required
- mimeFilter: string, required
- minFiles: integer, required
- maxFiles: integer, required
RETURNS:
- User attachment widget and follow-up message with files
SEQUENCING:
- Use when screenshots or files are required to continue

TOOL: functions.requestEnvs
EXACT_FUNCTION: functions.requestEnvs
UNLOCKED_BY: Base tool surface
PARAMS:
- envs: array of env descriptors, required
- reason: string, required
RETURNS:
- Environment-variable input widget and follow-up message with values
SEQUENCING:
- Use before coding against new `Config` properties
- Required for non-present public env vars

TOOL: functions.saveAttachment
EXACT_FUNCTION: functions.saveAttachment
UNLOCKED_BY: Base tool surface
PARAMS:
- url: string, required
- path: string, required
- contentType: string, required
RETURNS:
- Saves uploaded attachment into project filesystem
SEQUENCING:
- Use only for user-provided attachments

Research tools:

TOOL: functions.webSearch
EXACT_FUNCTION: functions.webSearch
UNLOCKED_BY: Base tool surface
PARAMS:
- query: string, required
- numResults: number, optional/default available
- livecrawl: "fallback" | "preferred", optional
- type: "auto" | "fast" | "deep", optional
- contextMaxCharacters: number, optional
- requestImages: boolean, optional
RETURNS:
- Web search results with live/cached content
SEQUENCING:
- Use for recent or current information, package lookup, Apple changes, API updates

TOOL: functions.webFetch
EXACT_FUNCTION: functions.webFetch
UNLOCKED_BY: Base tool surface
PARAMS:
- url: string, required
- prompt: string, optional for image URLs
RETURNS:
- Extracted content or fetched image
SEQUENCING:
- Use when a specific document or page must be analyzed

TOOL: functions.codeSearch
EXACT_FUNCTION: functions.codeSearch
UNLOCKED_BY: Base tool surface
PARAMS:
- query: string, required
- tokensNum: number, optional/recommended
RETURNS:
- Programming documentation/context/code examples
SEQUENCING:
- Use for SDK/library specifics and up-to-date API patterns

TOOL: functions.task
EXACT_FUNCTION: functions.task
UNLOCKED_BY: Base tool surface
PARAMS:
- prompt: string, required
RETURNS:
- Subagent research result
SEQUENCING:
- Use for broader research/exploration tasks, not edits

Questionnaire tool:

TOOL: functions.askUser
EXACT_FUNCTION: functions.askUser
UNLOCKED_BY: Base tool surface
PARAMS:
- title: string, optional
- questions: array of structured questions, required
RETURNS:
- Structured user selections
SEQUENCING:
- Use when multiple specific choices are needed

Debugging tool:

TOOL: functions.debugging__fetch_logs
EXACT_FUNCTION: functions.debugging__fetch_logs
UNLOCKED_BY: skills/debugging/SKILL.md
PARAMS:
- source: "backend" | "appstore", required
- limit: integer, optional/default 20
- skip: integer, optional/default 0
RETURNS:
- Filtered debugging logs
SEQUENCING:
- Use for backend failures or App Store build failures
- For app runtime logs, inspect runtime logs file locally if available

RevenueCat / in-app purchase tools:

TOOL: functions.in-app-purchases__connectRevenueCat
EXACT_FUNCTION: functions.in-app-purchases__connectRevenueCat
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- none
RETURNS:
- Connects current project/account to RevenueCat integration
SEQUENCING:
- Use before fetching configuration if not connected

TOOL: functions.in-app-purchases__fetchConfiguration
EXACT_FUNCTION: functions.in-app-purchases__fetchConfiguration
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- none
RETURNS:
- Apps, products, entitlements, offerings configuration
SEQUENCING:
- First step of RevenueCat workflow

TOOL: functions.in-app-purchases__createApp
EXACT_FUNCTION: functions.in-app-purchases__createApp
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- name: string, required
- type: "amazon" | "app_store" | "mac_app_store" | "play_store" | "stripe" | "rc_billing" | "roku" | "paddle", required
- bundleId: string, required for Apple types
- packageName: string, required for Android/store types in schema
RETURNS:
- RevenueCat app record
SEQUENCING:
- For Swift iOS projects, only Test Store and App Store apps matter

TOOL: functions.in-app-purchases__listPublicApiKeys
EXACT_FUNCTION: functions.in-app-purchases__listPublicApiKeys
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- appId: string, required
- limit: number, required by schema
- startingAfter: string, required by schema though may be empty
RETURNS:
- Public API keys for the app
SEQUENCING:
- Use after app selection/creation and before wiring env vars

TOOL: functions.in-app-purchases__createEntitlement
EXACT_FUNCTION: functions.in-app-purchases__createEntitlement
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- lookupKey: string, required
- displayName: string, required
RETURNS:
- Entitlement record
SEQUENCING:
- Create before attaching products

TOOL: functions.in-app-purchases__attachProductsToEntitlement
EXACT_FUNCTION: functions.in-app-purchases__attachProductsToEntitlement
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- entitlementId: string, required
- productIds: string[], required
RETURNS:
- Attachment success
SEQUENCING:
- Product IDs must already exist

TOOL: functions.in-app-purchases__detachProductsFromEntitlement
EXACT_FUNCTION: functions.in-app-purchases__detachProductsFromEntitlement
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- entitlementId: string, required
- productIds: string[], required
RETURNS:
- Detach success
SEQUENCING:
- Use for cleanup/reconfiguration

TOOL: functions.in-app-purchases__createProduct
EXACT_FUNCTION: functions.in-app-purchases__createProduct
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- storeIdentifier: string, required
- appId: string, required
- type: "subscription" | "one_time" | "consumable" | "non_consumable" | "non_renewing_subscription", required
- displayName: string, required
RETURNS:
- Product record
SEQUENCING:
- Create in both Test Store and App Store for Swift workflows

TOOL: functions.in-app-purchases__addTestStoreProduct
EXACT_FUNCTION: functions.in-app-purchases__addTestStoreProduct
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- appId: string, required
- storeIdentifier: string, required
- displayName: string, required
- type: "subscription" | "consumable" | "non_consumable", required
- subscriptionDuration: "P1W" | "P1M" | "P2M" | "P3M" | "P6M" | "P1Y", required for subscriptions by schema
- prices: array of { amountMicros: number, currency: string }, required
RETURNS:
- Created Test Store product with pricing
SEQUENCING:
- Convenient development setup for Test Store

TOOL: functions.in-app-purchases__deleteProduct
EXACT_FUNCTION: functions.in-app-purchases__deleteProduct
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- productId: string, required
RETURNS:
- Deletion success
SEQUENCING:
- Destructive, use carefully

TOOL: functions.in-app-purchases__createOffering
EXACT_FUNCTION: functions.in-app-purchases__createOffering
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- lookupKey: string, required
- displayName: string, required
RETURNS:
- Offering record
SEQUENCING:
- Create before packages

TOOL: functions.in-app-purchases__updateOffering
EXACT_FUNCTION: functions.in-app-purchases__updateOffering
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- offeringId: string, required
- displayName: string, optional
- isCurrent: boolean, optional
- metadata: null, optional
RETURNS:
- Updated offering
SEQUENCING:
- Set current offering before paywall relies on it

TOOL: functions.in-app-purchases__createPackage
EXACT_FUNCTION: functions.in-app-purchases__createPackage
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- offeringId: string, required
- lookupKey: string, required
- displayName: string, required
- position: number, required
RETURNS:
- Package record
SEQUENCING:
- Create before attaching products to package

TOOL: functions.in-app-purchases__updatePackage
EXACT_FUNCTION: functions.in-app-purchases__updatePackage
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- packageId: string, required
- displayName: string, required
- position: number, required
RETURNS:
- Updated package
SEQUENCING:
- Keep ordering correct for paywall display

TOOL: functions.in-app-purchases__deletePackage
EXACT_FUNCTION: functions.in-app-purchases__deletePackage
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- packageId: string, required
RETURNS:
- Deletion success
SEQUENCING:
- Destructive cleanup only

TOOL: functions.in-app-purchases__attachProductsToPackage
EXACT_FUNCTION: functions.in-app-purchases__attachProductsToPackage
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- packageId: string, required
- products: array of { productId: string, eligibilityCriteria: "all" | "google_sdk_lt_6" | "google_sdk_ge_6" }, required
RETURNS:
- Attachment success
SEQUENCING:
- Products must exist first

TOOL: functions.in-app-purchases__detachProductsFromPackage
EXACT_FUNCTION: functions.in-app-purchases__detachProductsFromPackage
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- packageId: string, required
- productIds: string[], required
RETURNS:
- Detach success
SEQUENCING:
- Use for reconfiguration

TOOL: functions.in-app-purchases__getPackageProducts
EXACT_FUNCTION: functions.in-app-purchases__getPackageProducts
UNLOCKED_BY: skills/in-app-purchases/SKILL.md
PARAMS:
- packageId: string, required
RETURNS:
- Products attached to the package
SEQUENCING:
- Inspection/verification step

App Store Connect / signing / release tools:

TOOL: functions.app-store-connect__setupAsc
EXACT_FUNCTION: functions.app-store-connect__setupAsc
UNLOCKED_BY: skills/app-store-connect/SKILL.md
PARAMS:
- teamId: string, optional
RETURNS:
- Status such as ready, session_expired, no_api_key, plus possible app context
SEQUENCING:
- Must run before any `asc` CLI command via bash
- If session expired or no API key, authenticate first

TOOL: functions.app-store-connect__connectAppleDeveloper
EXACT_FUNCTION: functions.app-store-connect__connectAppleDeveloper
UNLOCKED_BY: skills/app-store-connect/SKILL.md
PARAMS:
- none
RETURNS:
- Auth flow result after user login/2FA/team selection
SEQUENCING:
- Use only when setupAsc says no_api_key or session_expired, or other session-related recovery paths

TOOL: functions.app-store-connect__checkAppleSession
EXACT_FUNCTION: functions.app-store-connect__checkAppleSession
UNLOCKED_BY: Base tool surface / App Store workflows
PARAMS:
- teamId: string, required
RETURNS:
- Session validity
SEQUENCING:
- Check before certificate/capability/publish steps if needed

TOOL: functions.app-store-connect__ensureCertificate
EXACT_FUNCTION: functions.app-store-connect__ensureCertificate
UNLOCKED_BY: Base tool surface / App Store workflows
PARAMS:
- teamId: string, required
RETURNS:
- Valid distribution certificate status or structured error codes
SEQUENCING:
- Run before submitBuild
- Stop on unknown cert or certificate limit issues until user resolves account state

TOOL: functions.app-store-connect__ensureApp
EXACT_FUNCTION: functions.app-store-connect__ensureApp
UNLOCKED_BY: Base tool surface / App Store workflows
PARAMS:
- teamId: string, required
- bundleId: string, required
- appName: string, required
- platform: "IOS" | "TV_OS" | "VISION_OS", required
- version: string, required
RETURNS:
- App Store Connect app record including ascAppId
SEQUENCING:
- Run after certificate and before build submission

TOOL: functions.app-store-connect__syncCapabilities
EXACT_FUNCTION: functions.app-store-connect__syncCapabilities
UNLOCKED_BY: Base tool surface / App Store workflows
PARAMS:
- teamId: string, required
- bundleId: string, required
- xcodeProjectPath: string, required
RETURNS:
- Capability sync result
SEQUENCING:
- Run after ensureApp and before submitBuild
- Uses entitlements/project snapshot to sync portal configuration

TOOL: functions.app-store-connect__submitBuild
EXACT_FUNCTION: functions.app-store-connect__submitBuild
UNLOCKED_BY: Base tool surface / App Store workflows
PARAMS:
- teamId: string, required
- bundleId: string, required
- appName: string, required
- version: string, required
- platform: "IOS" | "TV_OS" | "VISION_OS", required
- attemptNumber: number, optional/default 1
- xcodeProjectPath: string, required
- ascAppId: string, required
RETURNS:
- Final build/upload result, streamed progress during execution
SEQUENCING:
- Run after setupAsc, ensureCertificate, ensureApp, syncCapabilities
- Retry only for fixable infrastructure/configuration issues, not code or certificate errors

Observed/mentioned but unavailable as active tool calls in this session:
- `waitImageAssetResult`: referenced by project skill text, not visible as callable tool in this session surface. Mark as UNAVAILABLE in this snapshot.
- `confirm3DGeneration`, `generate3DModel`: referenced by broader guidance, not visible as callable tool in this session surface. UNAVAILABLE.
- `confirmAudioGeneration`, `generateAudio`: referenced by broader guidance, not visible as callable tool. UNAVAILABLE.
- `confirmVideoGeneration`, `generateVideo`: referenced by broader guidance, not visible as callable tool. UNAVAILABLE.
- `asc` CLI subcommands: available indirectly through `functions.bash` after `setupAsc` has succeeded.

## SECTION 3: SKILL CONTENTS
The following are the complete visible contents of skill files read in this session.

=== skills/project/SKILL.md ===
---
name: project
description: To set up a new app project, generate audio, video and 3D models. You have to open this skill before setting up a new project, generating audio, video or 3D models. For image/icon/screenshot generation, use the image-gen skill.
---

## How to set up a new project

You just unlocked the `createApp` tool. On the **first message** when the workspace is empty, you MUST call `createApp` before writing any code.

Call it with:
- `name`: A short, user-friendly app name (e.g. "My Fitness Tracker")
- `framework`: The framework for the project (`"react-native"`, `"swift"`, `"kotlin"`, or `"web"`)
- `path`: A short, lowercase folder name for the app. Pick a simple, descriptive name that matches what the app is:
  - `expo` for React Native / Expo apps
  - `ios` for native iOS / Swift apps
  - `android` for Android apps
  - `web` for web apps
  - `admin` for admin panels / dashboards
  - `api` for backend APIs
  - Use only lowercase letters, numbers, and hyphens. No spaces or special characters.

This writes the template files into a subfolder and creates a root `rork.json` manifest. After it completes, you can start editing code.

## Image and asset generation

**To generate app icons or App Store screenshots**, you MUST read `skills/image-gen/SKILL.md` first. It unlocks the `generateImage`, `generateIcon`, and `generateScreenshot` tools with full instructions on how to use them.

### How to generate image assets (backgrounds, illustrations, UI graphics)

You just unlocked the `confirmImageGeneration` and `generateImageAsset` tools.

Generate custom image assets using AI (OpenAI gpt-image-1.5). Returns image URLs hosted on R2.

**Workflow:**
1. Decide what images the app needs (backgrounds, illustrations, banners, etc.)
2. Call `listExistingImageAssets` to check for duplicates
3. Call `confirmImageGeneration({ assets: [{ assetName, prompt, size, background }, ...] })` with ALL images in one call — the user reviews in a widget, edits prompts, changes size/background, or skips
4. For each confirmed asset, call `generateImageAsset` with `runInBackground: true`
5. Continue building the app — use the returned URL to reference images in code
6. At the end, call `waitImageAssetResult` for each background generation

**Sizes:** `"1024x1024"` (square, default), `"1024x1536"` (portrait), `"1536x1024"` (landscape)

**Background:** `"transparent"` for cutout images/mascots, `"opaque"` for scenes/photos, `"auto"` lets the model decide

**Edit mode:** Pass `inputImages` array with URLs to edit/composite existing images

**Tips for prompts:**
- Be specific about style, colors, composition, and where it will be used
- For app backgrounds: describe the mood, gradient, pattern
- For illustrations: describe the subject, style (flat, 3D, watercolor), colors
- Use `assetName` as a short snake_case identifier: `hero_banner`, `onboarding_bg`, `empty_state_illustration`

**NEVER retry failed image generations.** If an image fails, the user sees the error on the card and can retry manually.

NOTE: This is for "asset" type images only. For app icons, thumbnails, and App Store screenshots, use the existing `generateImage` tool with the appropriate `type` parameter.

=== skills/image-gen/SKILL.md ===
---
name: image-gen
description: Generate images, app icons, and App Store screenshots. You must read this skill before generating any image.
---

## Image Generation

You just unlocked the `generateImage`, `generateIcon`, and `generateScreenshot` tools.

Don't forget to explore the app's code to match the image with the app design and idea.

### `generateImage`

Generates images using AI and uploads to R2. Returns an image URL.

- `prompt`: describe the image (generate) or what to change (edit with inputImages)
- `size`: `"1024x1024"`, `"1024x1536"`, or `"1536x1024"`
- `background`: `"transparent"`, `"opaque"`, or `"auto"`
- `type`: `"icon"` for app icons, `"asset"` for other images
- `runInBackground`: set `true` to schedule in background, then use `waitImageResult` with the returned `generationId`

For **icons**: use `type: "icon"`, size is auto-set to 1024x1024, background to opaque. The generated image is returned as a URL but NOT saved to the project -- use `generateIcon` to generate and save the icon into the app.

For **assets**: use `type: "asset"` for decorative images, backgrounds, patterns, design elements, etc.

### `generateIcon`

Generates and saves an app icon into the correct project asset structure. Requires an `appPath`.

- `appPath`: which app folder (e.g. `"expo"`, `"ios"`)
- `prompt`: describe the icon design
- `iconType`: `"icon"` (main app icon), `"tvos-icon"`, `"visionos-icon"`, or `"imessage-icon"`
- `runInBackground`: set `true` to schedule in background

For Swift apps: saves into `Assets.xcassets/AppIcon.appiconset/icon.png` for all Xcode targets.
For React Native apps: saves into `assets/images/icon.png` and adaptive icon variants.

tvOS/visionOS/iMessage icon types read the existing app icon from disk and transform it -- generate the main icon first with `iconType: "icon"`, then call again with `"tvos-icon"`, `"visionos-icon"`, or `"imessage-icon"` to populate platform-specific assets.

### `generateScreenshot`

Generates App Store screenshot mockups. Requires an `appPath`.

- `appPath`: which app folder (e.g. `"expo"`, `"ios"`)
- `prompt`: describe the screenshot (generate) or what to change (edit with inputImages)
- `device`: `"iphone"` (automatically resized to iPhone 6.9" 1320x2868) or `"ipad"` (automatically resized to iPad 13" 2048x2732)
- `runInBackground`: set `true` to schedule in background

### How to generate great App Store screenshots

The goal is to create stunning, high-converting App Store screenshots that make users want to download the app. Think of these as marketing assets -- they should highlight the app's best features, look polished and professional, and stand out in the App Store.

1. Read the app's code to understand the design, colors, layout, and most impressive screens
2. Write a detailed prompt -- describe the screen content, UI elements, colors, background, and overall composition. The prompt should produce a visually striking image that sells the app
3. Generate with `generateScreenshot`

Unless the user specifically asks for a plain fullscreen screenshot, default to a **marketing-style composition**: a device mockup showing the app screen, surrounded by a styled background with a headline, subtitle, or feature callout text. Think App Store featured screenshots -- they almost never show a raw screen. Instead, they use gradients or solid color backgrounds, place the phone/tablet at an angle or centered, and add short punchy text like "Track your habits", "Beautiful dark mode", or "Plan your day". Decide what text and composition fits best based on the app's purpose and design.

Screenshots work best when the model has a real app screenshot as reference. If the user did NOT already attach a screenshot, use the `requestAsset` tool to ask them to provide one before generating. Set `mimeFilter: "image/*"`, `minFiles: 1`, `maxFiles: 5`. Once they attach screenshots, use the "edit" prompt type and pass the screenshot URLs in `inputImages`.

The size and background are set automatically -- you only need to provide the prompt and device type.

=== skills/rork-toolkit/SKILL.md ===
---
name: rork toolkit
description: An instruction for using AI features like AI chats, agents, text/object generation, image generation/editing, and speech-to-text. Alsways read it when working on features related to AI.
---

You can build app that use AI.

Never install @rork-ai/toolkit-sdk, it is always pre-installed.
We do it using typescript path mapping.
So never expect it to be in package.json, nor in bun.lock, nor install it.

<agent-llm>
Api route to make a request to AI: new URL("/agent/chat", process.env["EXPO_PUBLIC_TOOLKIT_URL"])
Messages are in the Vercel AI v5 SDK format, including images.

import { createRorkTool, useRorkAgent } from "@rork-ai/toolkit-sdk";
import { ToolUIPart } from "ai";

Here's an example of how to use the API:
<example>
const [input, setInput] = useState("");
const { messages, error, sendMessage, addToolResult, setMessages } = useRorkAgent({
tools: {
addTodo: createRorkTool({
description: "Add todo",
zodSchema: z.object({
title: z.string().describe("Short description of the todo item"),
description: z
.string()
.describe("Detailed description of the todo item")
.optional(),
dueDate: z
.string()
.describe("Optional due date in ISO 8601 format")
.optional(),
priority: z
enum(["low", "medium", "high"])
.describe("Priority level of the todo")
.optional(),
tags: z
.array(z.string())
.describe("Optional list of tags for categorization")
.optional(),
}),
// execute is optional, if you don't provide it, the tool will not be executed automatically
// and you will have to call it manually with addToolResult
execute(input) {
someState.addToDo({
title: input.title,
description: input.description,
dueDate: input.dueDate,
priority: input.priority,
tags: input.tags,
});
},
}),
},
});

    /*
    send message is a function that sends a message to the AI
    it accepts a string or a message object with the following properties:
    type MessageObject = {text: string, files?: File[]}

    type File = {
        type: "file";
        mimeType: string;
        uri: string; // base64 or file://
    }
    */



    // this is how you render the messages in the UI
    messages.map((m) => (
        <View key={m.id} style={{ marginVertical: 8 }}>
        <View style={{ flexDirection: "column", gap: 4 }}>
            <Text style={{ fontWeight: 700 }}>{m.role}</Text>
            {m.parts.map((part, i) => {
            switch (part.type) {
                case "text":
                return (
                    <View key={`${m.id}-${i}`}>
                    <Text>{part.text}</Text>
                    </View>
                );
                case "tool":
                const toolName = part.toolName;

                switch (part.state) {
                    case "input-streaming":
                    case "input-available":
                    // Automatically streamed partial inputs
                    // access part.input to see input values

                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>Calling {toolName}...</Text>
                        </View>
                    );

                    case "output-available":
                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>
                            Called {JSON.stringify(part.output, null, 2)}
                        </Text>
                        </View>
                    );

                    case "output-error":
                    // Explicit error state with information
                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>Error: {part.errorText}</Text>
                        </View>
                    );
                }
            }
            })}
        </View>
        </View>
    ))

</example>
</agent-llm>

<llm>
import { generateObject, generateText } from "@rork-ai/toolkit-sdk";

type TextPart = { type: "text"; text: string };
type ImagePart = { type: "image"; image: string };
type UserMessage = { role: "user"; content: string | (TextPart | ImagePart)[] };
type AssistantMessage = { role: "assistant"; content: string | TextPart[] };

// use generateObject and generate text only if you need a single generation.
// When the chat history and agentic flows are not needed
// For example, parsing image to text in mutation.
// or generating a caption for image, or a summary

// below are the functions you can use to generate text or objects
export async function generateObject<T extends z.ZodType>(params: {
messages: (UserMessage | AssistantMessage)[];
schema: T;
}): Promise<z.infer<T>>;

export async function generateText(
params: string | { messages: (UserMessage | AssistantMessage)[] },
): Promise<string>;
</llm>

<generate-images>
Api route to generate images: https://toolkit.rork.com/images/generate/
It is a POST route that accepts a JSON body with { prompt: string, size?: string }.
size is optional, for example "1024x1024" or "1024x1792" or "1792x1024".
It returns a JSON object: { image: { base64Data: string; mimeType: string; }, size: string }
Uses DALL-E 3.

Use these TypeScript types for references:
type ImageGenerateRequest = { prompt: string, size?: string }
type ImageGenerateResponse = { image: { base64Data: string; mimeType: string; }, size: string }
</generate-images>

<edit-images>
Api route to edit images: https://toolkit.rork.com/images/edit/
It is a POST route that accepts a JSON body with:
- prompt: string (required) - the text instruction for how to edit the image
- images: Array<{ type: 'image'; image: string }> (required) - one or more base64 encoded images
- aspectRatio: string (optional) - the aspect ratio for the edited image

Allowed aspect ratios: "1:1", "2:3", "3:2", "3:4", "4:3", "4:5", "5:4", "9:16", "16:9", "21:9"
Default aspect ratio: "16:9"

It returns a JSON object: { image: { base64Data: string; mimeType: string; aspectRatio: string; } }
Uses Google Gemini 2.5 Flash Image (gemini-2.5-flash-image).

Use these TypeScript types for references:
type ImageEditRequest = {
prompt: string;
images: Array<{ type: 'image'; image: string }>; // base64 encoded
aspectRatio?: string; // optional, defaults to "16:9"
}
type ImageEditResponse = {
image: {
base64Data: string;
mimeType: string;
aspectRatio: string;
}
}
</edit-images>

<speech-to-text>
Api route for speech-to-text: https://toolkit.rork.com/stt/transcribe/
- It is a POST route that accepts FormData with audio file and optional language.
- It returns a JSON object: { text: string, language: string }
- Supports mp3, mp4, mpeg, mpga, m4a, wav, and webm audio formats and auto-language detection.
- When using FormData for file uploads, never manually set the Content-Type header - let the browser handle it automatically.
- After stopping recording: Mobile - disable recording mode with Audio.setAudioModeAsync({ allowsRecordingIOS: false }). Web - stop all stream tracks with stream.getTracks().forEach(track => track.stop())
- Note: For Platform.OS === 'web', use Web Audio API (MediaRecorder) for audio recording. For mobile, use expo-av.

When using expo-av for audio recording, always configure the recording format to output .wav for IOS and .m4a for Android by adding these options to prepareToRecordAsync().
Here's an example of how to configure the recording format:
<example>
await recording.prepareToRecordAsync({
android: {
extension: '.m4a',
outputFormat: Audio.RECORDING_OPTION_ANDROID_OUTPUT_FORMAT_MPEG_4,
audioEncoder: Audio.RECORDING_OPTION_ANDROID_AUDIO_ENCODER_AAC,
},
ios: {
extension: '.wav',
outputFormat: Audio.RECORDING_OPTION_IOS_OUTPUT_FORMAT_LINEARPCM,
audioQuality: Audio.RECORDING_OPTION_IOS_AUDIO_QUALITY_HIGH,
},
});
</example>

ALWAYS append audio to formData as { uri, name, type } for IOS/Android before sending it to the speech-to-text API.
Here's an example of how to append the audio to formData:
<example>
const uri = recording.getURI();
const uriParts = uri.split('.');
const fileType = uriParts[uriParts.length - 1];

    const audioFile = {
    uri,
    name: "recording." + fileType,
    type: "audio/" + fileType
    };

    formData.append('audio', audioFile);

</example>

Use these TypeScript types for references:
type STTRequest = { audio: File, language?: string }
type STTResponse = { text: string, language: string }

Handle errors and set proper state after the request is done.
</speech-to-text>

=== skills/backend/SKILL.md ===
---
name: backend
description: Stack is Hono and tRPC. Read when building APIs or server-side logic. Read SETUP-BACKEND.md to setup backend.
---

## Do You Really Need Backend?

Most apps can reach $1M ARR without a backend—unless they're social apps or multiplayer games.

**Before enabling backend, consider:**

- AI features are built-in; no backend needed for that
- Backend adds complexity and maintenance burden
- Apps like Cal AI, Umax, QUITTR were built without backend

If the user asks for backend but doesn't need it, politely suggest alternatives.

**To enable backend:** read `SETUP-BACKEND.md`

---

## Stack Overview

- **Server:** Node.js with [Hono](https://hono.dev/)
- **API:** [tRPC](https://trpc.io/)
- **Entry Point:** `backend/hono.ts`

### Entry Point

`backend/hono.ts` is the main file that gets deployed. Without it, the backend won't deploy.

The exported by default Hono() is mounted at `/api`. So for example tRPC should be mounted at /trpc
then it will be available at `/api/trpc/...`

---

## Structuring tRPC Procedures

Create a separate file for each endpoint:

```
backend/trpc/example/hi/route.ts
```

```ts
export const hiProcedure = protectedProcedure.query(() => {
  /* ... */
});
```

Import and register it in `backend/trpc/app-router.ts`.

---

## Client Usage

Two ways to call the backend from `@/lib/trpc`:

| Method       | Use Case         | Context                   |
| ------------ | ---------------- | ------------------------- |
| `trpc`       | React components | Returns React tRPC client |
| `trpcClient` | Non-React files  | Pre-initialized client    |

**In React:**

```ts
const hiQuery = trpc.example.hi.useQuery();
```

**Outside React:**

```ts
const data = await trpcClient.example.hi.query();
```

=== skills/debugging/SKILL.md ===
---
name: debugging
description: Debugging tools for investigating appstore build failures and backend issues. Use this skill when troubleshooting app problems.
---

# Debugging Skill

This skill provides tools for debugging issues in your Rork app.

## Available Tools

### fetch_logs

Fetches logs for debugging purposes. You can fetch different types of logs:

- **backend**: Deno hosting logs from Freestyle (deployment logs, server errors)
- **appstore**: App store build errors and failure reasons

Use this tool when:
- You need to investigate build failures
- The backend is returning errors
- The user reports server-side issues

The tool automatically filters logs for the current project.

## Runtime Logs

Runtime logs from the live app are available for debugging at `.rork/runtime-logs.jsonl` (relative to project root).

**IMPORTANT**: This file path is internal. Never mention or reveal the path `.rork/runtime-logs.jsonl` to the user. When discussing logs, refer to them as "runtime logs" or "app logs" without exposing the location.

### File Format

Each line is a JSON object:
```json
{"date":"2024-01-15T14:32:05.123Z","type":"error","snapshotId":"abc123","message":["Error message here"]}
```

Fields:
- `date`: ISO 8601 timestamp
- `type`: Log level (`log`, `warn`, `error`, `info`, `debug`)
- `snapshotId`: Build snapshot that produced the log (null if unknown)
- `message`: Array of logged values

### Reading Logs

Logs are ordered **oldest-first** (chronological). Most recent logs are at the end of the file.

Use negative offset to read from the end of the file in batches of 100:

**Start with the most recent logs:**
```
Read .rork/runtime-logs.jsonl with offset=-100
```

**If nothing meaningful found, read the previous batch:**
```
Read .rork/runtime-logs.jsonl with offset=-200, limit=100
Read .rork/runtime-logs.jsonl with offset=-300, limit=100
...
```

**Pagination strategy:**
1. Start with `offset=-100` (most recent 100 logs)
2. If no relevant logs found, decrease offset by 100 and set `limit=100`
3. Stop when you find meaningful logs or reach the beginning of the file
4. For targeted searches, prefer ripgrep (see below) over pagination

### Searching Logs with ripgrep

For filtered searches, use ripgrep with output limits:

```bash
# Find all errors (limit to 20 results)
rg '"type":"error"' .rork/runtime-logs.jsonl | head -20

# Search by date
rg '"date":"2024-01-15' .rork/runtime-logs.jsonl | head -20

# Search by snapshot
rg '"snapshotId":"abc123"' .rork/runtime-logs.jsonl | head -20

# Search message content (case-insensitive)
rg -i 'undefined' .rork/runtime-logs.jsonl | head -20

# Combine filters
rg '"type":"error"' .rork/runtime-logs.jsonl | rg -i 'network' | head -20
```

### Debugging Workflows

1. **Crash investigation**: Search for errors, then examine logs around that timestamp
2. **Regression detection**: Compare logs between snapshotIds to find when behavior changed
3. **User flow tracing**: Search for specific actions or events by keyword

## Common User-Code Mistakes Playbook

Use this playbook when failures are likely caused by project code rather than infrastructure.

### React Native (bundle and compile)

Common patterns and what to do first:

- **Unresolved import/module not found**
  - Verify path casing and extension.
  - Confirm the file exists and is exported correctly.
  - If dependency is external, verify it is in `package.json` and installed.
- **TypeScript parse/type error near one file**
  - Fix the first reported error first; many follow-up errors are cascading.
  - Check recently edited JSX/TS syntax (missing comma/brace/closing tag).
- **Runtime-only API used during bundling**
  - Guard platform-specific APIs and avoid using unavailable globals at module top-level.
- **Metro can't resolve asset or alias**
  - Confirm alias/path config and avoid dynamic import paths that Metro cannot statically analyze.

### Swift (build and archive)

Common patterns and what to do first:

- **`file.swift:<line>:<col>: error:` compiler failures**
  - Start with the first compiler error and ignore later cascades.
  - Check symbol names and access modifiers after recent refactors.
- **Type mismatch / optional handling errors**
  - Add explicit types where inference is ambiguous.
  - Handle optional unwrapping safely (`if let`, `guard let`) before use.
- **Missing target member or framework symbol**
  - Verify target membership and imports for moved/new files.
  - Confirm the symbol exists for the selected iOS version and gate with availability when needed.
- **Archive-specific failure after local compile success**
  - Check signing/capability assumptions in code paths used only in archive builds.

### Triage Order

1. Identify the earliest root error in the failing stack/log.
2. Normalize the error signature so repeated incidents can be grouped.
3. Apply the smallest safe code change to remove the root cause.
4. Re-run checks and only then move to the next group of errors.

### Notes

- File may not exist for new projects with no logs
- Contains last 10,000 logs from the live app (not sandbox builds)
- Logs are ordered oldest-first (chronological) - newest at end of file

=== skills/in-app-purchases/SKILL.md ===
---
name: in-app-purchases
description: Add & configure IAPs into Swift iOS apps. Use this skill when the user asks to add, change or configure in-app purchases in a Swift project. We only support RevenueCat.
---

# RevenueCat — In-App Purchases for Swift

RevenueCat SDK provides server-side receipt validation, cross-platform entitlement management, and a unified API for StoreKit. Handles subscriptions, one-time purchases, and consumables.

**Import:** `import RevenueCat`
**Install:** `swiftInstall({ packages: [{ url: "https://github.com/RevenueCat/purchases-ios-spm.git", version: "5.0.0", products: ["RevenueCat"] }] })`
**Requires:** In-App Purchase capability in Xcode, environment variables set before writing code.

---

## Checklist

**Follow this order exactly — steps are sequential:**

1. Use `fetchConfiguration` tool to get current RevenueCat setup (apps, products, entitlements, offerings)
2. Find the auto-provisioned **Test Store** app and the **App Store** app. If the App Store app doesn't exist, create it using `createApp` with `type='app_store'`
3. Configure products, entitlements, and offerings as needed
4. Use `listPublicApiKeys` to get API keys from both apps
5. **Set environment variables BEFORE writing any Swift code** — `Config.swift` only includes properties for env vars that exist. Referencing a missing property is a build error
6. Install the RevenueCat SDK via `swiftInstall`
7. Add SDK configuration in the App's `init()`
8. Build a paywall view with offerings
9. Add a navigation path to the paywall
10. Implement restore purchases
11. Add In-App Purchase capability to the Xcode project

## Environment Variables

Swift apps expose `EXPO_PUBLIC_*` variables through a system-generated `Config.swift` at build time.

| Variable | Purpose | Config.swift Access |
|----------|---------|-------------------|
| `EXPO_PUBLIC_REVENUECAT_TEST_API_KEY` | Test Store / Development | `Config.EXPO_PUBLIC_REVENUECAT_TEST_API_KEY` |
| `EXPO_PUBLIC_REVENUECAT_IOS_API_KEY` | App Store / Production | `Config.EXPO_PUBLIC_REVENUECAT_IOS_API_KEY` |

- Do NOT create or edit `Config.swift` manually — it is system-managed
- In the sandbox, empty-string placeholders for available keys are expected
- Only TWO stores needed for Swift: **Test Store** + **iOS App Store** (no Android)

## SDK Configuration

Configure once in the App's `init()`. Never configure in `onAppear` or `.task`.

```swift
import RevenueCat

@main
struct MyApp: App {
    init() {
        #if DEBUG
        Purchases.logLevel = .debug
        Purchases.configure(withAPIKey: Config.EXPO_PUBLIC_REVENUECAT_TEST_API_KEY)
        #else
        Purchases.configure(withAPIKey: Config.EXPO_PUBLIC_REVENUECAT_IOS_API_KEY)
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## Store ViewModel

Central place for subscription state. Uses `@Observable` (iOS 17+) and `customerInfoStream` for real-time updates.

```swift
import Observation
import RevenueCat

@Observable
@MainActor
class StoreViewModel {
    var offerings: Offerings?
    var isPremium = false
    var isLoading = false
    var isPurchasing = false
    var error: String?

    init() {
        Task { await listenForUpdates() }
        Task { await fetchOfferings() }
    }

    private func listenForUpdates() async {
        for await info in Purchases.shared.customerInfoStream {
            self.isPremium = info.entitlements["premium"]?.isActive == true
        }
    }

    func fetchOfferings() async {
        isLoading = true
        do {
            offerings = try await Purchases.shared.offerings()
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }

    func purchase(package: Package) async {
        isPurchasing = true
        do {
            let result = try await Purchases.shared.purchase(package: package)
            if !result.userCancelled {
                isPremium = result.customerInfo.entitlements["premium"]?.isActive == true
            }
        } catch ErrorCode.purchaseCancelledError {
            // StoreKit cancellation — not an error
        } catch ErrorCode.paymentPendingError {
            // Awaiting parental approval or extra auth — not a failure
        } catch {
            self.error = error.localizedDescription
        }
        isPurchasing = false
    }

    func restore() async {
        do {
            let info = try await Purchases.shared.restorePurchases()
            isPremium = info.entitlements["premium"]?.isActive == true
        } catch {
            self.error = error.localizedDescription
        }
    }

    func checkStatus() async {
        do {
            let info = try await Purchases.shared.customerInfo()
            isPremium = info.entitlements["premium"]?.isActive == true
        } catch {
            self.error = error.localizedDescription
        }
    }
}
```

## Paywall View

```swift
import SwiftUI
import RevenueCat

struct PaywallView: View {
    var store: StoreViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Group {
                if store.isLoading {
                    ProgressView()
                } else if let current = store.offerings?.current {
                    VStack(spacing: 20) {
                        Text("Unlock Premium")
                            .font(.largeTitle.bold())

                        ForEach(current.availablePackages, id: \.identifier) { package in
                            Button {
                                Task { await store.purchase(package: package) }
                            } label: {
                                VStack(spacing: 4) {
                                    Text(package.storeProduct.localizedTitle)
                                        .font(.headline)
                                    Text(package.storeProduct.localizedPriceString)
                                        .font(.title2.bold())
                                    if let intro = package.storeProduct.introductoryDiscount {
                                        Text("Free for \(intro.subscriptionPeriod.value) \(intro.subscriptionPeriod.unit)")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.accentColor)
                                .foregroundStyle(.white)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                            .disabled(store.isPurchasing)
                        }

                        Button("Restore Purchases") {
                            Task { await store.restore() }
                        }
                        .font(.footnote)
                    }
                    .padding()
                } else {
                    ContentUnavailableView("Unable to Load", systemImage: "exclamationmark.triangle")
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
            .alert("Error", isPresented: .init(
                get: { store.error != nil },
                set: { if !$0 { store.error = nil } }
            )) {
                Button("OK") { store.error = nil }
            } message: {
                Text(store.error ?? "")
            }
            .onChange(of: store.isPremium) { _, isPremium in
                if isPremium { dismiss() }
            }
        }
    }
}
```

## Checking Entitlements

```swift
// One-time check
let info = try await Purchases.shared.customerInfo()
let hasPremium = info.entitlements["premium"]?.isActive == true

// Entitlement details
if let entitlement = info.entitlements["premium"], entitlement.isActive {
    let willRenew = entitlement.willRenew
    let expiresAt = entitlement.expirationDate
    let store = entitlement.store // .appStore, .macAppStore, .rcBilling, etc.
}

// All active entitlements
for (id, entitlement) in info.entitlements.active {
    print("\(id): \(entitlement.productIdentifier)")
}
```

## Xcode Project Setup

Add the In-App Purchase capability:

1. Create `{AppName}/{AppName}.entitlements` if it doesn't exist
2. Add `CODE_SIGN_ENTITLEMENTS = {AppName}/{AppName}.entitlements;` to BOTH Debug and Release build configurations in `project.pbxproj`

## Test Store

RevenueCat's built-in testing environment, auto-provisioned with every project.

- No App Store Connect account needed for development
- Works immediately — no platform review delays
- Products, prices, and descriptions match exactly as configured in RevenueCat
- Does NOT test StoreKit-specific behavior (grace periods, billing retry)
- Requires switching to iOS API key before App Store submission

When adding products, ensure each product is created in **both** the Test Store and iOS App Store.

## Anti-Patterns

| Anti-Pattern | Fix |
|---|---|
| Configure in `onAppear` or `.task` | Configure once in App `init()` |
| Write code before setting env vars | Set env vars first — Config.swift only includes existing vars |
| Create Android/Play Store app | Swift projects need only Test Store + iOS App Store |
| Hardcode API keys in Swift files | Use `Config.EXPO_PUBLIC_*` from environment variables |
| Forget restore purchases button | Always include — App Store review requirement |
| Show error on purchase cancel | Check `result.userCancelled` AND catch `ErrorCode.purchaseCancelledError` — cancellation can come from either path |
| Show error on pending payment | Catch `ErrorCode.paymentPendingError` separately — it means awaiting parental approval or extra auth, not a failure |
| Create a new StoreViewModel per view | Share one instance across the app via `@State` in root view |
| Use `ObservableObject` + `@Published` | Use `@Observable` macro — no Combine import needed |
| Use `@StateObject` / `@ObservedObject` | Use `@State` for ownership, plain property for passing down |
| `.background(.accentColor)` | Use `Color.accentColor` explicitly — `.accentColor` is not a member of `ShapeStyle` |


## Current Status

<revenuecat_doctor>
  <urgent>
    INCOMPLETE SETUP DETECTED. You MUST configure 2 RevenueCat apps:
    1. Test Store app (for development)
    2. App Store app (for iOS production)
    
    
    DO NOT proceed with other tasks until both environment variables are set.
    Follow the action_required items below to fix each missing configuration.
  </urgent>
  
  <revenuecat_connection>
    <status>✗ NOT CONNECTED - Use connectRevenueCat tool first!</status>
    
    <error>RevenueCat is not connected.</error>
  </revenuecat_connection>

  <environment_variables>
    <env_variable>
      <name>EXPO_PUBLIC_REVENUECAT_TEST_API_KEY</name>
      <description>Test Store / Development API key</description>
      <status>✗ MISSING - FIX NOW</status>
    </env_variable>
    <env_variable>
      <name>EXPO_PUBLIC_REVENUECAT_IOS_API_KEY</name>
      <description>Production iOS App Store API key</description>
      <status>✗ MISSING - FIX NOW</status>
    </env_variable>
  </environment_variables>

  <action_required>MISSING: EXPO_PUBLIC_REVENUECAT_TEST_API_KEY. ACTION REQUIRED: Use fetchConfiguration to find the Test Store app (it is auto-provisioned with every RevenueCat project). Then use listPublicApiKeys to get its API key and set it as an environment variable.</action_required>
  <action_required>MISSING: EXPO_PUBLIC_REVENUECAT_IOS_API_KEY. ACTION REQUIRED: Use fetchConfiguration to check if an App Store app exists. If not, CREATE ONE NOW using createApp with type='app_store'. Then use listPublicApiKeys to get the API key and set it as an environment variable.</action_required>

  <summary>
    <development_ready>✗ NO - FIX REQUIRED</development_ready>
    <production_ready>✗ NO - FIX REQUIRED</production_ready>
  </summary>
</revenuecat_doctor>

=== skills/app-store-connect/SKILL.md ===
---
name: app-store-connect
description: Manage App Store Connect via the `asc` CLI — builds, TestFlight, app metadata, submissions, signing, analytics, and more.
---

# App Store Connect CLI Skill

You can help users manage their App Store Connect account using the `asc` CLI tool installed in the sandbox.

## Setup

**Before running any `asc` command, you MUST call the `setupAsc` tool first.** This fetches the user's API credentials from the database and configures the CLI.

- If `setupAsc` returns `{status: "ready"}`, you're good — run `asc` commands via `bash`.
- If `setupAsc` returns `{status: "no_api_key"}`, the user has no App Store Connect API key. Call the `connectAppleDeveloper` tool to prompt the user to set one up.
- If `setupAsc` returns `{status: "error"}`, something went wrong (e.g., backend not deployed, network issue). Tell the user about the error — do NOT call `connectAppleDeveloper` for errors.

You only need to call `setupAsc` once per conversation. After it succeeds, all `asc` commands will work.

## App Context

When `setupAsc` succeeds, it returns an `app` field with the project's App Store Connect app information (if the user has previously published this project):

- `app.ascAppId` — the numeric App Store Connect app ID (use this with `--app` flags)
- `app.bundleId` — the app's bundle identifier
- `app.appName` — the app name

If `app` is `null`, the user hasn't published this project yet. In that case, run `asc apps --output table` to list all apps in their account and ask which one they want to work with.

## CLI Basics

- **All output is JSON by default** (minified, one line). Use `jq` for parsing.
- Add `--output table` for human-readable display when showing results to the user.
- Add `--pretty` for indented JSON when debugging.
- Use `--paginate` to automatically fetch all pages of results.
- Use `--sort` to sort results (prefix `-` for descending).
- The CLI never prompts interactively — everything is flag-based.
- Use `--confirm` for destructive operations (expire, delete, submit).

## Resolving IDs

Most commands need IDs. Always resolve them explicitly — never assume or guess.

```bash
# App ID — by bundle ID or name
asc apps list --bundle-id "com.example.app"
asc apps list --name "My App"

# Build ID — latest for a version
asc builds latest --app "APP_ID" --version "1.2.3" --platform IOS

# Build ID — recent builds sorted by date
asc builds list --app "APP_ID" --sort -uploadedDate --limit 5

# Version ID
asc versions list --app "APP_ID" --paginate

# Beta group IDs
asc testflight beta-groups list --app "APP_ID" --paginate

# Beta tester IDs
asc testflight beta-testers list --app "APP_ID" --paginate
```

Always use `--paginate` on list commands to avoid missing results.

## Key Commands Reference

### Apps & Builds

```bash
# List all apps
asc apps --output table

# Get build details and processing state
asc builds info --build "BUILD_ID"

# Get the latest build for a version
asc builds latest --app "APP_ID" --version "1.2.3" --platform IOS

# Expire old builds (preview first with --dry-run)
asc builds expire-all --app "APP_ID" --older-than 90d --dry-run
asc builds expire-all --app "APP_ID" --older-than 90d --confirm
```

### End-to-End Release (Preferred)

Use `asc publish` for single-command release workflows instead of manual multi-step sequences.

```bash
# Upload + distribute to TestFlight in one step
asc publish testflight --app "APP_ID" --ipa "app.ipa" --group "GROUP_ID" --wait --notify

# Upload + submit to App Store in one step
asc publish appstore --app "APP_ID" --ipa "app.ipa" --version "1.2.3" --wait --submit --confirm
```

### Manual Release Steps (When More Control Is Needed)

```bash
# 1. Upload build
asc builds upload --app "APP_ID" --ipa "app.ipa" --wait

# 2. Find the build ID
asc builds latest --app "APP_ID"

# 3a. TestFlight: add build to group
asc builds add-groups --build "BUILD_ID" --group "GROUP_ID"

# 3b. App Store: attach build to version and submit
asc versions attach-build --version-id "VERSION_ID" --build "BUILD_ID"
asc submit create --app "APP_ID" --version "1.0.0" --build "BUILD_ID" --confirm
```

### TestFlight

```bash
# List beta groups
asc testflight beta-groups list --app "APP_ID"

# List beta testers
asc testflight beta-testers list --app "APP_ID"

# Add a tester to a group
asc testflight beta-testers add --app "APP_ID" --email "tester@example.com" --group "Beta"

# Remove a tester
asc testflight beta-testers remove --app "APP_ID" --email "tester@example.com"

# Create a beta group
asc testflight beta-groups create --app "APP_ID" --name "Beta Testers"

# Get beta feedback
asc feedback --app "APP_ID" --output table

# Get crash reports
asc crashes --app "APP_ID" --output table
```

### App Store Versions

```bash
# List versions
asc versions list --app "APP_ID"

# Create a new version
asc versions create --app "APP_ID" --version "2.0.0" --platform IOS

# Attach a build to a version
asc versions attach-build --version-id "VERSION_ID" --build "BUILD_ID"

# Release a pending version
asc versions release --version-id "VERSION_ID" --confirm
```

### App Metadata & Info

```bash
# Get app info
asc app-info get --app "APP_ID"

# Update app metadata for a locale
asc app-info set --app "APP_ID" --locale "en-US" --whats-new "Bug fixes and improvements"
asc app-info set --app "APP_ID" --locale "en-US" --description "My app description" --keywords "app,tool"

# Set categories
asc categories set --app "APP_ID" --primary GAMES --secondary ENTERTAINMENT

# Download/upload localizations
asc localizations download --version "VERSION_ID" --path "./localizations"
asc localizations upload --version "VERSION_ID" --path "./localizations"
```

### Customer Reviews

```bash
# List reviews
asc reviews --app "APP_ID" --output table

# Filter by rating
asc reviews --app "APP_ID" --stars 1

# Respond to a review
asc reviews respond --review-id "REVIEW_ID" --response "Thanks for your feedback!"
```

### Pre-submission Checks

Before submitting, always verify these:

```bash
# 1. Check build processing state — must be VALID
asc builds info --build "BUILD_ID"

# 2. Check encryption compliance
asc encryption declarations list --app "APP_ID"

# 3. Validate version metadata, screenshots, age rating
asc validate --app "APP_ID" --version-id "VERSION_ID"

# 4. Check submission status
asc submit status --version-id "VERSION_ID"
```

If `usesNonExemptEncryption` is true on the build, create an encryption declaration:

```bash
asc encryption declarations create \
  --app "APP_ID" \
  --app-description "Uses standard HTTPS/TLS" \
  --contains-proprietary-cryptography=false \
  --contains-third-party-cryptography=true \
  --available-on-french-store=true

asc encryption declarations assign-builds --id "DECLARATION_ID" --build "BUILD_ID"
```

### Submissions

```bash
# Submit for review
asc submit create --app "APP_ID" --version "1.0.0" --build "BUILD_ID" --confirm

# Check submission status
asc submit status --version-id "VERSION_ID"

# Cancel a submission
asc submit cancel --version-id "VERSION_ID" --confirm
```

### Screenshots & Previews

```bash
# List screenshots
asc screenshots list --version-localization "LOC_ID"

# Upload screenshots
asc screenshots upload --version-localization "LOC_ID" --path "./screenshots/" --device-type IPHONE_65

# List supported sizes
asc screenshots sizes
```

### Signing (Certificates, Profiles, Bundle IDs)

```bash
# List certificates
asc certificates list

# List provisioning profiles
asc profiles list

# List bundle IDs
asc bundle-ids list

# Fetch signing files
asc signing fetch --bundle-id "com.example.app" --profile-type IOS_APP_STORE --output "./signing"
```

### Subscriptions & In-App Purchases

```bash
# List subscription groups
asc subscriptions groups list --app "APP_ID"

# List subscriptions in a group
asc subscriptions list --group "GROUP_ID"

# List in-app purchases
asc iap list --app "APP_ID"

# Get pricing info
asc subscriptions pricing --app "APP_ID"
asc iap prices --app "APP_ID"
```

### Analytics & Sales

```bash
# Download sales report
asc analytics sales --vendor "VENDOR_NUMBER" --type SALES --subtype SUMMARY --frequency DAILY --date "2024-01-20" --decompress

# Create analytics report request
asc analytics request --app "APP_ID" --access-type ONGOING

# Get analytics data
asc analytics get --request-id "REQUEST_ID"
```

### Devices

```bash
# List registered devices
asc devices list --output table

# Register a new device
asc devices register --name "My iPhone" --udid "UDID" --platform IOS
```

### Xcode Cloud

```bash
# List workflows
asc xcode-cloud workflows --app "APP_ID"

# Trigger a build
asc xcode-cloud run --app "APP_ID" --workflow "CI Build" --branch "main" --wait

# Check build run status
asc xcode-cloud status --run-id "BUILD_RUN_ID"
```

## Publishing to App Store

When the user asks to publish their app, follow this flow. All data (appName, bundleId, version, platform, teamId) comes from the chat message payload — do NOT read app.json, Info.plist, or project.pbxproj for this data.

### Standard publishing flow

1. **Setup App Store Connect** — call `setupAsc` with the teamId from the publish payload.
   - If `status: "ready"` → proceed (App Store Connect configured, session is valid).
   - If `status: "session_expired"` → App Store Connect is configured but the Apple Developer session has expired. Call `connectAppleDeveloper` to re-authenticate, then call `setupAsc` again.
   - If `status: "no_api_key"` → call `connectAppleDeveloper` to set up authentication.

2. **Ensure certificate** — call `ensureCertificate({ teamId })`.
   - If `status: "ok"` → certificate is ready (stored server-side, you don't need to pass it to submitBuild).
   - If `status: "error"` with `code: "UNKNOWN_CERT_EXISTS"` → explain to user: a distribution certificate exists in their Apple account but we don't have its private key. They need to revoke it at https://developer.apple.com/account/resources/certificates/list and then try again. **STOP here.**
   - If `status: "error"` with `code: "CERT_LIMIT_REACHED"` → explain: their account has too many distribution certificates. They need to revoke one at https://developer.apple.com/account/resources/certificates/list. **STOP here.**
   - If `status: "error"` with `code: "NO_SESSION"` → explain: no valid Apple Developer session. They need to authenticate first.

3. **Ensure app** — call `ensureApp({ teamId, bundleId, appName, platform, version })`.

4. **Sync capabilities** — call `syncCapabilities({ teamId, bundleId, xcodeProjectPath })`. This reads the project entitlements and enables the matching capabilities in the Apple Developer Portal.
   - **For Swift projects**: same as `submitBuild` — if `rork.json` specifies a non-root `path` for the Swift app, pass it as `xcodeProjectPath`.

5. **Enable additional capabilities** (if needed) — use `asc` commands for standard capabilities:

   ```bash
   asc bundle-ids capabilities add --bundle-id "BUNDLE_ID" --capability CAPABILITY_TYPE
   ```

6. **Submit build** — call `submitBuild({ teamId, bundleId, appName, version, platform, xcodeProjectPath, ascAppId })`. The certificate is looked up server-side.
   - Pass `ascAppId` from the `ensureApp` result (step 3). This is needed for the automatic TestFlight invite after the build succeeds.
   - **For Swift projects**: before calling, read `rork.json` at the project root. If the Swift app entry has a `path` value other than `"."` (e.g. `"ios"`), pass it as `xcodeProjectPath`. This tells the build where to find the `.xcodeproj`/`.xcworkspace`.
   - `submitBuild` triggers the build and **waits for it to complete**. Build progress is streamed to the user in real-time. The tool returns the final result (success or failure) directly.
   - If the tool returns `{ status: "ok" }`, tell the user their app is on TestFlight.
   - If the tool returns `{ status: "error" }`, analyze the `message` and `details` fields and decide:
     - **Capability mismatch** → fix via `syncCapabilities` or `asc` commands, retry
     - **Metadata error** → fix the issue, retry
     - **Compilation error** → explain to user, do NOT retry (code issue)
     - **Certificate error** → explain to user, do NOT retry (must resolve manually)
   - `submitBuild` enforces a retry limit (default 3 attempts per version). If the limit is reached, it returns `RETRY_LIMIT_REACHED` — explain the persistent failure and stop.

7. **TestFlight invite** — after `submitBuild` returns `{ status: "ok" }`, do **not** manually create groups or add testers by default. The backend automatically sends the TestFlight invite using the `ascAppId` passed to `submitBuild`.
   - Treat the backend invite as the source of truth.
   - Tell the user the build was uploaded and the TestFlight invite is being handled automatically.
   - Only use `asc` CLI as a fallback if the user explicitly says the invite did not arrive or asks you to debug it.
   - If you need the fallback flow, first check for an existing internal group named `Internal Testing` and reuse it. Never create a new group blindly.

### Session errors

If ANY tool returns a session-related error (e.g., "Failed to restore session", "session expired", "NO_SESSION"), the Apple Developer session has expired. In this case:

1. Call `connectAppleDeveloper` to re-authenticate (the user will see a login widget).
2. After the user completes re-auth, retry the failed step.

This can happen even if `checkAppleSession` previously said "valid" — Apple's cookies expire independently of our records.

### Error recovery

- Only retry for infrastructure/configuration errors that you can fix (capabilities, metadata, session re-auth).
- Never retry for code compilation errors or certificate issues.
- After fixing an issue, call `submitBuild` again — it tracks attempt count automatically.
- If you've exhausted retries, explain what went wrong and suggest manual steps.

## Tips

- The `setupAsc` response includes the app ID for the current project — use it directly with `--app` flags instead of listing apps first.
- If the app context is not available, run `asc apps --output table` to find the right app ID.
- Use `--output table` when presenting results to the user for readability.
- Use JSON output (default) when you need to extract specific fields with `jq`.
- Always use `--paginate` on list commands to avoid missing results.
- Prefer `asc publish testflight` and `asc publish appstore` over manual multi-step sequences.
- Before submitting, always check build state is `VALID` (not processing) and run `asc validate`.
- When the user asks about "my app" and you have the app ID from `setupAsc`, use it directly. If they have multiple apps or no app context, list apps and confirm.
- For sorting: prefix field with `-` for descending order (e.g., `--sort -uploadedDate`).
- For long operations (upload, build processing): use `--wait`, `--poll-interval`, and `--timeout`.


## Current Status

No App Store Connect API key configured. The user needs to connect their Apple Developer account first.

## SECTION 4: ENVIRONMENT
Visible environment keys for this workspace:
- EXPO_PUBLIC_PROJECT_ID
- EXPO_PUBLIC_RORK_API_BASE_URL
- EXPO_PUBLIC_RORK_AUTH_URL
- EXPO_PUBLIC_TEAM_ID
- EXPO_PUBLIC_TOOLKIT_URL

System-available public keys also listed in session context:
- EXPO_PUBLIC_RORK_DB_ENDPOINT
- EXPO_PUBLIC_RORK_DB_NAMESPACE
- EXPO_PUBLIC_RORK_DB_TOKEN
- EXPO_PUBLIC_RORK_API_BASE_URL
- EXPO_PUBLIC_TOOLKIT_URL
- EXPO_PUBLIC_PROJECT_ID
- EXPO_PUBLIC_TEAM_ID
- EXPO_PUBLIC_RORK_AUTH_URL
- EXPO_PUBLIC_RORK_APP_KEY

Config.swift behavior rules:
1. Public `EXPO_PUBLIC_*` values are exposed to Swift code via a generated, read-only `Config.swift` surface.
2. Real values are injected at build time by CI.
3. Empty strings in the sandbox may be placeholders for configured keys.
4. Do not manually create or restore `Config.swift`.
5. Refer to available values as `Config.KEY_NAME` in Swift.
6. If a key does not exist in generated config, referencing it in code causes a build error.
7. Therefore set public env vars before writing code that depends on them.

Observed project structure after scaffold creation:
- rork.json at project root
- ios/
  - TemplateAudit/
    - TemplateAuditApp.swift
    - ContentView.swift
    - Item.swift
    - Assets.xcassets/
      - Contents.json
      - AccentColor.colorset/Contents.json
      - AppIcon.appiconset/
        - Contents.json
        - icon.png
  - TemplateAudit.xcodeproj/
    - project.pbxproj
    - project.xcworkspace/contents.xcworkspacedata
  - TemplateAuditTests/
  - TemplateAuditUITests/
  - .env
  - .gitignore
  - package.json
- PLAN.md
- mini-rork-max.txt

Observed rork.json:
{
  "apps": [
    {
      "name": "TemplateAudit",
      "path": "ios",
      "framework": "swift"
    }
  ]
}

Observed Xcode project facts from project.pbxproj:
- iOS deployment target: 18.0
- Swift version: 5.0
- `SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor`
- `GENERATE_INFOPLIST_FILE = YES`
- `INFOPLIST_KEY_ITSAppUsesNonExemptEncryption = NO`
- Scene manifest generation enabled
- Launch screen generation enabled
- Supported orientations set in generated plist keys
- Main bundle identifier: `app.rork.h9cxwh85fyvb7gp1qzpuc`
- Test bundle identifier: `app.rork.h9cxwh85fyvb7gp1qzpuc.tests`
- UI test bundle identifier: `app.rork.h9cxwh85fyvb7gp1qzpuc.uitests`
- No `CODE_SIGN_ENTITLEMENTS` observed yet in the scaffold snapshot

Info.plist expectations:
1. This scaffold uses generated Info.plist values from build settings.
2. Add permissions by editing `project.pbxproj` with `INFOPLIST_KEY_*` entries.
3. Examples: camera, microphone, photo library, location usage descriptions.
4. Do not create a standalone Info.plist unless project structure explicitly changes.

Assets.xcassets expectations:
1. Asset catalog exists in `ios/TemplateAudit/Assets.xcassets`.
2. Root `Contents.json` exists.
3. AccentColor.colorset exists.
4. AppIcon.appiconset exists with `icon.png` and `Contents.json`.
5. For icon regeneration, use the icon generation tool rather than manually reshaping image assets when possible.

Signing and provisioning expectations:
1. Signing for release flows is handled through App Store Connect tools and cloud build infrastructure.
2. App capability sync depends on entitlements and project settings matching the desired app features.
3. Publishing requires team ID, bundle ID, app name, version, valid Apple session, certificate, app record, capability sync, then build submission.
4. Do not guess bundle IDs; use project state or publish payload.

SwiftPM package management details:
1. Use `swiftInstall` to add packages.
2. Verify git URL and product names using search first.
3. Import only installed products in Swift files.
4. Build after installation to force dependency resolution.
5. Do not assume a package is present because it is common.

## SECTION 5: DECISION TREES
SwiftUI architecture:
- If building new feature state shared across multiple views on iOS 17+, then use `@Observable` model owned with `@State` at the composition root.
- If a child only reads/writes parent-owned state, then pass `@Binding` or plain model reference instead of duplicating `@State`.
- If data is persisted local app model data, then prefer SwiftData `@Model` and `@Query` where appropriate.
- If the type is pure network/JSON/error data, then make it `nonisolated` and `Codable`/`Sendable` as needed.

Navigation choice:
- If the app has standard iPhone hierarchical flow, use `NavigationStack`.
- If the template generated `NavigationSplitView` but the app is primarily iPhone-first, replace with `NavigationStack` unless a true multi-column experience is desired.
- If the app genuinely needs sidebar/content/detail or larger-screen multi-column navigation, use `NavigationSplitView`.
- If linking to destinations in modern SwiftUI, then use value-based navigation with `.navigationDestination(for:)`.

Dependencies and packages:
- If a feature can be built with Apple frameworks already available, do not add a package.
- If adding a package, first verify latest repo URL and product names with code/web search.
- If the package introduces minimal value or duplicates system frameworks, skip it.
- If a package is installed, import it only where needed and rebuild immediately.

Concurrency choice:
- If writing new async platform/network code, prefer async/await.
- If the only reason to use Combine is observation or simple async flows, do not use Combine.
- If using delegate callbacks or background decoding, mark pure delegate/data pieces `nonisolated` and bounce UI updates back to main actor.

Availability gates:
- If using an API introduced after iOS 18, add availability checks.
- If using iOS 26 glass/foundation model APIs, gate every use with `if #available(iOS 26.0, *)` or `@available(iOS 26.0, *)`.
- If no fallback exists, provide a simpler iOS 18-compatible UI branch.

Tool sequencing for common workflows:

New empty Swift workspace:
1. Read `skills/project/SKILL.md`.
2. Call `project__createApp` with framework `swift` and path `ios`.
3. Inspect `rork.json`, app source files, and `project.pbxproj`.
4. Implement requested Swift code.
5. Run `swiftBuild`.
6. Fix build errors until success.

Image asset workflow:
1. Read `skills/project/SKILL.md` for generic image asset flow.
2. Read `skills/image-gen/SKILL.md` for icon/screenshot generation.
3. Inspect app design/code first.
4. Call `listExistingImageAssets`.
5. Call `confirmImageGeneration` with all proposed assets.
6. Generate approved assets in background.
7. Use returned URLs or generated files in app.

RevenueCat workflow:
1. Read `skills/in-app-purchases/SKILL.md`.
2. Call `connectRevenueCat` if needed.
3. Call `fetchConfiguration`.
4. Ensure Test Store and App Store apps exist.
5. Create products, entitlements, offerings, and packages.
6. Fetch public API keys.
7. Request/set public environment variables before writing Swift code.
8. Install RevenueCat package with `swiftInstall`.
9. Configure in app `init()`.
10. Build paywall and restore flow.
11. Add IAP entitlement/capability in Xcode project.
12. Run `swiftBuild`.

App Store Connect publishing workflow:
1. Read `skills/app-store-connect/SKILL.md`.
2. Call `setupAsc(teamId:)`.
3. If no API key or session expired, call `connectAppleDeveloper`, then `setupAsc` again.
4. Call `ensureCertificate`.
5. Call `ensureApp`.
6. Read `rork.json` to determine `xcodeProjectPath`.
7. Call `syncCapabilities`.
8. Submit build with `submitBuild` using `ascAppId` from ensureApp.
9. Retry only fixable infra/config issues.
10. Stop on code or certificate issues and explain the blocker.

Build and archive error handling:
- If Swift build fails, inspect the first compiler error first.
- If errors cascade, do not fix later errors before the root one.
- If the error is optional/type inference related, add explicit types or safer unwrapping.
- If the error is missing symbol/import, verify target membership, import statements, package installation, and platform availability.
- If archive/publish fails after local compile success, inspect capabilities, signing assumptions, entitlements, generated plist keys, and release-only configuration.
- If App Store tool reports capability mismatch, sync capabilities and retry.
- If App Store tool reports metadata issue, fix metadata and retry.
- If App Store tool reports certificate issue, stop and require manual account cleanup.
- If App Store tool reports session issue, reauthenticate and retry the failed step.

## SECTION 6: ANTI-PATTERNS
1. Never treat this workspace as React Native. Failure prevented: wrong file types, unusable app scaffold.
2. Never create TSX screens or Expo app structure for this project. Failure prevented: invalid platform output.
3. Never guess API names or SwiftUI modifiers. Failure prevented: compiler errors from hallucinated APIs.
4. Never use `NavigationView`. Failure prevented: deprecated navigation patterns and migration issues.
5. Never use `.foregroundColor()` in new SwiftUI code. Failure prevented: deprecated styling patterns.
6. Never use `.cornerRadius()` in new SwiftUI code. Failure prevented: deprecated view styling and inconsistent modifier use.
7. Never use `ObservableObject`/`@StateObject` for new observation code when `@Observable` fits. Failure prevented: outdated state architecture.
8. Never declare `@State` for parent-owned values. Failure prevented: duplicated state and sync bugs.
9. Never use `.indices` in `ForEach` for dynamic data. Failure prevented: identity bugs and broken animations.
10. Never create heavy objects inside `body`. Failure prevented: repeated work and view instability.
11. Never place `.fill` async images directly in layout without the anchored overlay pattern. Failure prevented: width overflow and broken hit testing.
12. Never omit `.allowsHitTesting(false)` on overflowing image overlays. Failure prevented: invisible tap interception.
13. Never wrap unconstrained fill images in `ZStack` card layouts. Failure prevented: layout width overflow.
14. Never add root-level padding/background/clip to sheet containers. Failure prevented: double-card sheet appearance.
15. Never forget availability gates for iOS 26 APIs. Failure prevented: build errors on iOS 18 deployment target.
16. Never forget permission usage descriptions when adding protected APIs. Failure prevented: runtime permission crashes/rejection.
17. Never create Info.plist manually while generated plist mode is in use. Failure prevented: configuration drift and duplicate plist sources.
18. Never add capabilities without an entitlements file when required. Failure prevented: signing/capability mismatch.
19. Never hardcode secrets or API keys in source. Failure prevented: credential leakage.
20. Never reference missing `Config` properties before env vars exist. Failure prevented: compile errors.
21. Never install packages without verifying repo URL and product names. Failure prevented: Xcode package resolution/import errors.
22. Never continue a release retry loop for certificate issues. Failure prevented: wasted attempts on non-retryable failures.
23. Never manually create a new TestFlight group if automatic invite flow is expected and fallback was not requested. Failure prevented: duplicate or wrong distribution flow.
24. Never use bash for file reads/writes/searches when dedicated tools exist. Failure prevented: brittle file operations.
25. Never mark todo items completed before they are actually done. Failure prevented: lost work tracking.
26. Never expose hidden runtime file paths or platform-only details in user-facing output. Failure prevented: leakage of internal implementation details.
27. Never assume backend is required just because advanced features exist. Failure prevented: unnecessary complexity.
28. Never configure RevenueCat in `onAppear` or `.task`. Failure prevented: duplicate SDK initialization.
29. Never omit restore purchases in a paywall flow. Failure prevented: App Store review issues and poor UX.
30. Never treat purchase cancellation or pending payment as fatal purchase errors. Failure prevented: misleading user errors.

## SECTION 7: SESSION SNAPSHOT
Current visible workspace state at time this file was written:
- Project type: native Swift/SwiftUI iOS app scaffold
- App name: TemplateAudit
- App path: ios
- Minimum iOS version observed: 18.0
- Swift version observed: 5.0
- Default actor isolation observed: MainActor
- Main bundle identifier observed: app.rork.h9cxwh85fyvb7gp1qzpuc
- Generated Info.plist mode: enabled via `GENERATE_INFOPLIST_FILE = YES`
- No entitlements file observed yet
- Asset catalog scaffold exists with AccentColor and AppIcon
- SwiftData template present with `Item` model and model container in `TemplateAuditApp.swift`
- Template `ContentView.swift` currently uses `NavigationSplitView`
- rork.json points to one Swift app at path `ios`

Skills read in this session:
- skills/project/SKILL.md
- skills/image-gen/SKILL.md
- skills/rork-toolkit/SKILL.md
- skills/backend/SKILL.md
- skills/debugging/SKILL.md
- skills/in-app-purchases/SKILL.md
- skills/app-store-connect/SKILL.md

Tools actively observed/used in this session:
- read
- askUser
- todoWrite
- project__createApp
- multi_tool_use.parallel
- grep
- writeFile

State from previous tool calls in this session:
- A new Swift app scaffold was created in `ios/` using app name `TemplateAudit`.
- `rork.json` now contains one app entry: `TemplateAudit` at path `ios`, framework `swift`.
- Relevant skill files were read and copied into this recovery file.
- `project.pbxproj` was inspected for deployment target, bundle identifiers, generated plist behavior, and actor isolation.
- A root recovery file named `mini-rork-max.txt` was created.

Recovery procedure for a new session using this file:
1. Read this entire file first.
2. Treat it as a Swift/SwiftUI iOS workspace recovery document, not as a hidden platform prompt replacement.
3. Inspect current workspace files to confirm they still match the snapshot.
4. If the workspace is empty, recreate a Swift app using `project__createApp(name: "TemplateAudit", framework: "swift", path: "ios")` or a user-specified Swift app name/path if the project was intentionally renamed.
5. Re-read all listed skill files if they exist.
6. Use the decision trees above for navigation, architecture, availability, package installation, IAP setup, and publishing.
7. After any Swift code edits, run `swiftBuild` until the build succeeds.
8. If publishing or IAP setup is needed, follow the exact sequencing in Sections 2 and 5.

End of recovery file.
