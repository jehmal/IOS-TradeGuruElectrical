# TradeGuru Swift — Project Rules

## Mandatory

1. **CI build verification loop.** After pushing Swift changes, wait 60 seconds, then check the iOS build GitHub Action. If the build failed, retrieve the error logs and consult GitHub Copilot with: `"Please find a solution for this failing job. Use the logs, job definition, and any referenced files where the failure occurred. Keep your response focused on the solution and include code suggestions when appropriate. ###Extra needed info"`. Apply the fix, push again, and repeat the check → fix → push loop until the build succeeds. Never consider a Swift change complete until CI is green.
2. **Swift-first development.** The Swift/SwiftUI iOS app is the PRIMARY product. All features, logic, and architecture decisions are made for Swift FIRST. The HTML preview exists solely as a visual testing tool because the developer is on Windows/WSL with no Mac or Xcode. Never prioritise HTML over Swift. Never build features in HTML that don't exist in Swift. The Swift code is the source of truth — the HTML follows.
3. When making changes to the HTML chat preview, do NOT modify or remove workbench utilities (device selector, light/dark mode toggle, pen/drawing tool, notes panel). These are dev workspace tools, not part of TradeGuru.
4. Every new SwiftUI component that is created must also be added to the HTML component picker dropdown so it can be previewed in the browser viewer.
5. This project is developed on Windows/WSL with no local Mac or Xcode. Every frontend SwiftUI view MUST have a matching pixel-identical HTML preview in `preview/chat.html` so the developer can see the design in a browser. Always produce both SwiftUI code and HTML output together for any frontend work.
6. Before finalising any file that runs on Vercel Edge Runtime (`export const config = { runtime: 'edge' }` or imported by an Edge function), inspect it for Node.js-only APIs that cannot run on Edge Runtime. Prohibited APIs include: `Buffer`, `crypto` (Node module import), `fs`, `path`, `child_process`, `require()`, `process.cwd()`, `__dirname`, `__filename`. Use Web API equivalents instead (`TextEncoder`, `crypto.subtle`, `crypto.getRandomValues`, `fetch`, etc.).
7. All behaviours the swift code has must also be mimicked 1 for 1, remember the html is how we test what the swft looks like, that includes behaviour
8. **Swift-before-HTML execution order.** When making changes that affect both Swift and HTML, the Swift file MUST be completed and finalised first. Only after the Swift code is done should the HTML preview be updated. The HTML teammate/agent must re-read the finished Swift file before editing HTML so it mirrors the Swift implementation exactly. Never start HTML changes in parallel with Swift — always sequential: Swift first, then HTML mirrors it.
9. **Swift 6 Strict Concurrency Compliance.** This project builds with Xcode 26 and Swift 6 strict concurrency. Every new or modified Swift file MUST comply with these rules before being considered complete:
   - **Sendable conformance:** Every `struct`, `enum`, and `class` that crosses actor boundaries must conform to `Sendable`. Pure value types (structs with only value-type stored properties) add `: Sendable`. Enums with Sendable associated values add `: Sendable`. Types containing non-Sendable types (like `@Model` classes) use `: @unchecked Sendable`.
   - **@Model classes are NOT Sendable:** Never add `Sendable` to `@Model` classes. When they appear inside enums or are passed across actors, mark the container as `@unchecked Sendable`.
   - **`nonisolated` types with Color properties:** If a `nonisolated` enum or struct has a computed property returning `Color` (non-Sendable), either remove `nonisolated` from the type (let default MainActor isolation apply) or mark the Color property as `@MainActor`.
   - **Static properties:** `static var` in `nonisolated` types must be `nonisolated(unsafe) static var`. `static let` holding non-Sendable types (arrays of `@Model` classes, closures) must be `nonisolated(unsafe) static let`. `static let` holding Sendable types (String, Int, Bool) are safe without annotation.
   - **AVFoundation / UIKit types:** These are non-Sendable. Classes that hold `AVCaptureSession`, `AVAudioRecorder`, `AVAudioPlayer` etc. must be `@MainActor` or use `@preconcurrency import AVFoundation` at the file level.
   - **`required convenience init(from decoder:)` on @Model classes:** When adding `Decodable` to an `@Model class`, the `required convenience init(from:)` MUST be inside the class body, NOT in an extension. The class declaration must include `: Decodable` directly (e.g., `@Model class Foo: Decodable`). CodingKeys enum also goes inside the class body.
   - **Argument ordering:** When calling an initializer, arguments MUST match the exact parameter order of the `init` declaration. Swift 6 enforces this strictly — `rows` before `headers` if the init declares them in that order.
   - **AsyncStream and continuations:** The element type yielded by `continuation.yield()` must be `Sendable`. If it contains non-Sendable types, use `@unchecked Sendable`.
   - **`for await` on throwing sequences:** Use `for try await` when iterating sequences that can throw (e.g., `URLSession.AsyncBytes.lines`). Use `for await` only on non-throwing `AsyncStream`.
   - **`@preconcurrency import`:** Use `@preconcurrency import AVFoundation`, `@preconcurrency import AuthenticationServices`, or any other Apple framework that has non-Sendable types you need to store as properties.
   - **Delegate methods:** Delegate methods called by Apple frameworks off-main-thread must be `nonisolated`. Inside them, hop back to MainActor with `Task { @MainActor in ... }` to access MainActor-isolated properties.
10. **SwiftUI view function size limit.** Any SwiftUI function returning `some View` must not exceed 60 lines of view-building code. When a view body or helper function grows beyond this, extract logical sections (lists, overlays, toolbars, input areas, error banners) into separate private functions that each return `some View`. This prevents Swift type-checker timeouts ("unable to type-check this expression in reasonable time") caused by deeply nested closures, conditionals, and generic modifier chains in a single expression.
11. **No ICU-dependent APIs.** This app is tested on Appetize cloud simulators where ICU data files may be missing. Never use `Text(date, style:)`, `Locale.current.identifier`, `TimeZone.current.identifier`, `localizedStandardContains`, `localizedCaseInsensitiveCompare`, or unguarded `DateFormatter`/`NumberFormatter`. Instead use static `DateFormatter` with `Locale(identifier: "en_US_POSIX")`, `Locale.preferredLanguages.first`, `TimeZone.current.secondsFromGMT()`, and `lowercased().contains()` for string comparison. Additionally, **never use bare `LocalizedStringKey`** in accessibility labels, Labels, or TextField placeholders — these trigger ICU localization lookups. Use `.accessibilityLabel(Text(verbatim: "string"))` instead of `.accessibilityLabel("string")`, `Label { Text(verbatim: "text") } icon: { Image(systemName: "icon") }` instead of `Label("text", systemImage: "icon")`, and `TextField(text: $binding, prompt: Text(verbatim: "placeholder"), axis: .vertical) {}` instead of `TextField("placeholder", text: $binding)`.
12. **No force unwraps or fatalError in production paths.** Never use `try!`, `fatalError()`, `preconditionFailure()`, `as!` force casts, or `optional!` force unwraps in code that can execute at runtime. Use `do/catch` with graceful fallbacks, `guard let`/`if let`, and `as?` safe casts. The only exception is a single last-resort `try!` for `ModelContainer` initialization where the App struct requires a non-optional value and all other fallbacks have been exhausted.
13. **No nested withAnimation blocks.** Never create a `withAnimation` block inside an `.onChange` handler that is triggered by a state change already wrapped in `withAnimation` elsewhere (e.g., a child view's button action). Two concurrent animation transactions on overlapping view hierarchies crash SwiftUI's layout engine. Animate at the source only — if a child already animates the binding change, the parent's `.onChange` must set state without `withAnimation`.
14. **AsyncStream continuations must always finish.** Every `AsyncStream` that iterates a throwing sequence (e.g., `for try await line in bytes.lines`) must wrap the loop in `do/catch` so that `continuation.finish()` is called on both success and error paths. An unfinished continuation causes the consumer to hang indefinitely.
15. **Filesystem and Keychain resilience.** Always pre-create `Library/Application Support/` before SwiftData `ModelContainer` init. Probe filesystem writability and fall back to `ModelConfiguration(isStoredInMemoryOnly: true)` on restricted environments. Use `DeviceManager.deviceIdOrFallback()` (Keychain → UserDefaults → in-memory) instead of `getOrCreateDeviceId()`. Never assume Keychain access succeeds on simulators. Guard all `FileManager` operations with `do/catch`.
16. **ViewModel size limit.** No ViewModel file may exceed 300 lines. When a ViewModel grows beyond this, extract domain responsibilities into focused components: streaming/SSE logic into an Engine class (e.g., `ChatEngine`), persistence CRUD into a Manager class (e.g., `ConversationManager`). The ViewModel remains as thin orchestration glue that delegates to these components.
17. **View file size limit.** No single View `.swift` file may exceed 200 lines. When a View file grows beyond this, extract private helper view functions into their own files (e.g., `ChatNavBar.swift`, `ChatMessageList.swift`, `ChatErrorBanner.swift`). Each extracted file should contain a single `struct` or private extension returning `some View`.

## Orchestration Rules

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

## SwiftUI Layout Directives

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

## Decision Trees

### SwiftUI Architecture
- If building new feature state shared across multiple views on iOS 17+, then use `@Observable` model owned with `@State` at the composition root.
- If a child only reads/writes parent-owned state, then pass `@Binding` or plain model reference instead of duplicating `@State`.
- If data is persisted local app model data, then prefer SwiftData `@Model` and `@Query` where appropriate.
- If the type is pure network/JSON/error data, then make it `nonisolated` and `Codable`/`Sendable` as needed.

### Navigation Choice
- If the app has standard iPhone hierarchical flow, use `NavigationStack`.
- If the template generated `NavigationSplitView` but the app is primarily iPhone-first, replace with `NavigationStack` unless a true multi-column experience is desired.
- If the app genuinely needs sidebar/content/detail or larger-screen multi-column navigation, use `NavigationSplitView`.
- If linking to destinations in modern SwiftUI, then use value-based navigation with `.navigationDestination(for:)`.

### Dependencies and Packages
- If a feature can be built with Apple frameworks already available, do not add a package.
- If adding a package, first verify latest repo URL and product names with code/web search.
- If the package introduces minimal value or duplicates system frameworks, skip it.
- If a package is installed, import it only where needed and rebuild immediately.

### Concurrency Choice
- If writing new async platform/network code, prefer async/await.
- If the only reason to use Combine is observation or simple async flows, do not use Combine.
- If using delegate callbacks or background decoding, mark pure delegate/data pieces `nonisolated` and bounce UI updates back to main actor.

### Availability Gates
- If using an API introduced after iOS 18, add availability checks.
- If using iOS 26 glass/foundation model APIs, gate every use with `if #available(iOS 26.0, *)` or `@available(iOS 26.0, *)`.
- If no fallback exists, provide a simpler iOS 18-compatible UI branch.

### Tool Sequencing for Common Workflows

**New empty Swift workspace:**
1. Read `skills/project/SKILL.md`.
2. Call `project__createApp` with framework `swift` and path `ios`.
3. Inspect `rork.json`, app source files, and `project.pbxproj`.
4. Implement requested Swift code.
5. Run `swiftBuild`.
6. Fix build errors until success.

**Image asset workflow:**
1. Read `skills/project/SKILL.md` for generic image asset flow.
2. Read `skills/image-gen/SKILL.md` for icon/screenshot generation.
3. Inspect app design/code first.
4. Call `listExistingImageAssets`.
5. Call `confirmImageGeneration` with all proposed assets.
6. Generate approved assets in background.
7. Use returned URLs or generated files in app.

**RevenueCat workflow:**
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

**App Store Connect publishing workflow:**
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

## Anti-Patterns

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

## CLI Toolkit

Project tools available at `.claude/tools/tradeguru`:

```bash
tradeguru create-app --name "App" --framework swift --path ios
tradeguru swift-build [--app-path ios]
tradeguru swift-install --packages '[{"url":"...git","version":"1.0.0","products":["Name"]}]'
tradeguru swift-add-target --name Widget --type widget
tradeguru list-assets
tradeguru generate-asset --prompt "..." --name hero_banner
tradeguru generate-icon --prompt "..." --app-path ios
tradeguru generate-screenshot --prompt "..." --app-path ios --device iphone
```
