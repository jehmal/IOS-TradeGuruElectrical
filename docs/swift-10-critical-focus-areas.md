# Swift & SwiftUI: 10 Critical Focus Areas — Pitfalls, Runtime Traps, and Authoritative Fixes

## 1. SwiftUI State Management Pitfalls

### @State vs @StateObject: The Initialization Trap

The most dangerous pitfall when migrating to iOS 17+ is treating `@State` with `@Observable` as a drop-in replacement for `@StateObject` with `ObservableObject`. `@StateObject` uses an `@autoclosure` initializer — it only creates the wrapped object **once** across the view's entire lifetime. `@State` takes the value directly, meaning the initializer for your `@Observable` class runs **every single time SwiftUI rebuilds the view struct**.[^1]

SwiftUI preserves the original `@State` value internally (discarding new instances), but those extra instances linger in memory. If your `@Observable` object's `init()` registers for notifications, starts timers, or reads from `UserDefaults`, all those side effects fire repeatedly with phantom instances that never deallocate. Jesse Squires documented this extensively — the fix is to place app-level `@State` properties in your top-level `App` struct (which doesn't get rebuilt), or ensure `init()` is side-effect-free.[^1]

### @State Initialization Ordering Bugs

Setting `@State` variables in a custom initializer is fragile. SwiftUI caches the initial `@State` value on first render, and subsequent re-initializations are ignored. This means passing a value via initializer injection only works on the *first* creation — after that, the cached value takes over silently. The Swift Forums explicitly warn: "@State variables in SwiftUI should not be initialized from data you pass down through the initializer".[^2][^3][^4]

### Lazy Container State Loss

`@State` in lazy containers (`LazyVStack`, `List`) can be **lost** when views scroll offscreen. Despite WWDC21's "Demystify SwiftUI" session saying state persists based on identity, lazy containers discard and recreate views to save memory, resetting view-local state. A Swift Forums pitch to stabilize this confirms it remains an open issue. The workaround is centralizing state in a parent or `@Observable` model rather than relying on `@State` inside lazy-loaded child views.[^5][^6]

### @EnvironmentObject Runtime Crash

`@EnvironmentObject` crashes with a fatal error if the object hasn't been injected into the environment — with **no compile-time safety**. This is one of SwiftUI's worst "lies by omission".[^7][^8]

***

## 2. Swift 6 Concurrency Traps

### Runtime Crashes That Didn't Exist in Swift 5

Swift 6's strict concurrency mode introduces **runtime assertions** (`dispatch_assert_queue`) that **did not exist** in Swift 5. Code that compiled and ran fine under Swift 5 can crash at runtime under Swift 6 without any source changes.[^9][^10]

The root cause: when Swift 6 infers a closure as `@MainActor`-isolated (because it's called from a `@MainActor` context), it inserts a runtime assertion that the closure actually executes on the main thread. If a framework calls that closure from a background thread, the app crashes.[^11][^10]

### Combine + Swift 6: The Silent Killer

A Combine chain created from a `@MainActor` context will have its `map`/`sink` closures inherit `@MainActor` isolation via implicit inference. If a publisher emits values off the main thread, the runtime assertion fires and crashes the app. The fix is to either mark closures `@Sendable` explicitly, or place `receive(on: DispatchQueue.main)` **before** `map`/`sink` rather than after.[^12][^13]

### Cross-Module Swift 5/6 Boundary Crashes

When Module A is compiled with Swift 5 (no concurrency checking) and Module B uses Swift 6, closures passed from Module B to Module A can be inferred as `@MainActor`-isolated. If Module A calls them on a background thread (which was perfectly legal in Swift 5), the app crashes at runtime with `dispatch_assert_queue` failures. This is tracked as a known issue in the Swift compiler (GitHub issue #75453).[^11]

### Timer Closures Escape Actor Isolation

Timer closures are **not** `@MainActor`-isolated even if the enclosing class is. Touching `@Published` properties inside `Timer.scheduledTimer` triggers a Swift 6 error. The fix: wrap in `Task { @MainActor [weak self] in ... }`.[^14][^15]

### Default Actor Isolation: The Hidden Build Setting

Swift 6.2 / Xcode 26 introduces `SWIFT_DEFAULT_ACTOR_ISOLATION` (SE-0466). New projects default to `MainActor`, meaning **every type** in your module is implicitly `@MainActor` unless marked `nonisolated`. This causes cascading `Codable`/`Sendable` errors because `init(from: Decoder)` becomes `@MainActor`-isolated and can't run on background threads where `JSONDecoder` operates. Either switch to the `nonisolated` default or mark model types `nonisolated` explicitly.[^16][^17][^18][^14]

***

## 3. SwiftUI Navigation Mistakes

### Duplicate Push with Identical IDs

`NavigationStack` with `.navigationDestination(for:)` uses the **identity** of items in `ForEach` to determine navigation. If your data contains duplicate identifiers, tapping one item can push **two or three** detail views simultaneously. Alexander Weiss documented this: the fix is ensuring all items in your navigation list have truly unique identifiers.[^19]

### .navigationDestination(isPresented:) Skip-Back Bug

Using `.navigationDestination(isPresented:)` is unreliable — it can skip the originating view entirely when navigating back, especially when the parent view receives updates (like location changes) during navigation. Stick to value-based navigation with `.navigationDestination(for:)` for predictability.[^20]

### Placement of .navigationDestination

`.navigationDestination` must be placed **inside** the `NavigationStack`, ideally on the `NavigationStack`'s root view or directly on child views within it. Placing it outside or on the wrong level produces the runtime warning: "The navigationDestination modifier only works inside a NavigationStack or NavigationSplitView".[^21]

### NavigationStack Freezes with Sheets

Users report that `NavigationStack` inside a `Sheet` with multiple navigation levels can cause random app freezes, especially when passing bindings between sheet content and the parent view.[^20]

***

## 4. SwiftData/CoreData Surprises

### Cascade Delete Is Broken with Inverse Relationships

One of SwiftData's most frustrating bugs: `.cascade` delete rules **silently fail** when you define an `inverse` relationship. Multiple developers on Apple Developer Forums confirmed that removing the explicit `inverse` parameter makes cascade deletion work again. Separately, explicitly calling `modelContext.save()` before the cascade has also been reported to prevent cascade from working correctly.[^22][^23]

### Thread Safety: @Model Objects Are Not Sendable

`@Model` objects are bound to their `ModelContext` and **must not be passed across threads**. Accessing a `@Model` object from a different thread than where its context lives produces `EXC_BAD_ACCESS` crashes. Pass `persistentModelID` instead and re-fetch in the target context.[^24][^25]

### @Query Performance and Main Thread Blocking

`@Query` runs on the main thread and produces performance warnings. With datasets exceeding ~1,000 items, it becomes noticeably slow. Relationship fetches compound this problem significantly. Community consensus (notably Geoff Pado's "@Query Considered Harmful") argues that `@Query` violates separation of concerns by coupling persistence logic directly into views.[^26][^27][^28]

### Migration Without VersionedSchema = Crash

If you ship a SwiftData model without wrapping it in `VersionedSchema` and later change the model, users upgrading will experience crashes because SwiftData has no migration path. You cannot retroactively version an unversioned schema cleanly. The WWDC23 session "Model your schema with SwiftData" emphasizes defining `VersionedSchema` from the start and using `SchemaMigrationPlan` for ordered migrations.[^29][^30][^31]

### @Model Contaminates File Scope

SwiftData's `@Model` macro makes the class `@MainActor`-isolated, and this isolation can **leak to other types in the same file**. Codable helpers or enums defined alongside your `@Model` class may unexpectedly inherit `@MainActor` isolation. Put nonisolated helpers in separate files.[^14]

***

## 5. Memory Management in SwiftUI

### When You Need [weak self]

SwiftUI views are **value types** — they're recreated constantly and don't cause retain cycles by themselves. Memory issues come from **reference-type objects** you own. You need `[weak self]` in:[^32]

- **Escaping closures** in `ObservableObject`/`@Observable` classes (e.g., callbacks, completion handlers)[^33][^34]
- **Timer closures**: `Timer.scheduledTimer` retains self strongly unless you use `[weak self]`[^33]
- **Task closures inside class init**: `Task { await self.loadData() }` creates a retain cycle — use `Task { [weak self] in await self?.loadData() }`[^33]
- **Combine subscriptions**: `.sink { self.doSomething() }` retains self for the subscription's lifetime[^34]

### Navigation Is the #1 Leak Source

Navigation stacks retain the view, the ViewModel, and all captured dependencies. If a ViewModel references navigation state, global services, or closures capturing `self`, it may never be released as long as it's in the navigation path. Rule: if it stays in the path, it stays alive.[^32]

### @State Does Not Cause Retain Cycles Directly

`@State` wraps value types and is managed by SwiftUI's internal storage. It doesn't create retain cycles by itself. However, if you store a closure in `@State` that captures a reference type (like a ViewModel), you can create a retain cycle indirectly. The fix is resetting `@State` closure properties to `nil` after execution.[^35]

### .task {} Is Safe (With Caveats)

SwiftUI's `.task` modifier automatically cancels when the view disappears, which prevents most leaks. However, if the async function inside `.task` loops indefinitely or awaits a long-lived operation that never completes, it retains the view model for that duration.[^32]

***

## 6. Performance Killers That Look Innocent

### VStack Inside ScrollView = Instant Performance Death

Putting a regular `VStack` (not `LazyVStack`) inside a `ScrollView` for large datasets forces SwiftUI to initialize **every single row** immediately, spiking memory and destroying launch time. `LazyVStack` and `List` only render visible rows. One developer discovered their `ForEach` was loading all views at once because it was inside a `VStack` — switching to `LazyVStack` made it load "almost instantly".[^36][^37]

### AnyView Destroys the Diffing Engine

`AnyView` erases the view type, forcing SwiftUI to **destroy and rebuild** the entire view hierarchy rather than efficiently diffing it. Apple explicitly warned against using `AnyView` inside `ForEach` at WWDC. Use `@ViewBuilder` or `Group` to maintain type transparency.[^37][^38][^39][^40]

### AsyncImage: No Caching, Full-Resolution Memory Bombs

`AsyncImage` has **no built-in image cache**. Every time a cell scrolls offscreen and back, it re-fetches the image from the network. Worse, it loads the full-resolution image into memory without downsampling — on older devices (iPhone 11/12), this causes memory crashes. Use a custom caching solution (e.g., `URLCache` or a third-party library like Kingfisher).[^41][^42][^43]

### Unstable IDs Force Full View Recreation

Using `.indices` or `self` as the `id` in `ForEach` means any data change causes SwiftUI to lose track of which view is which, leading to broken animations, redundant re-renders, and state loss. Use stable, unique identifiers (like UUID). The `WWDC23: Demystify SwiftUI performance` session covers this: "Identity helps SwiftUI manage view lifetime — a change to the identity means the view changed".[^44][^45][^37]

### The .id(UUID()) Anti-Pattern

```swift
Text(title).id(UUID()) // Forces full recreation every update
```

This forces SwiftUI to treat the view as brand new on **every** render, destroying all state and animations. Only use `.id()` for explicit reset scenarios.[^44]

### Heavy body Computation

`body` can be called dozens of times per second. Initializing `DateFormatter`, `JSONDecoder`, or doing complex logic inside `body` is a guaranteed performance killer. Keep `body` for layout only; move expensive logic to ViewModels or background `.task` modifiers.[^37]

***

## 7. App Store Review Common Rejections

### Privacy Manifests (Enforced Since May 1, 2024)

The single biggest new rejection reason. Apps **must** include a `PrivacyInfo.xcprivacy` file declaring all Required Reason APIs used (including common ones like `UserDefaults`). Apps without proper privacy manifests are rejected outright. Third-party SDKs must also include their own manifests and valid signatures.[^46][^47][^48]

### Missing Restore Purchases

Apps with non-consumable in-app purchases **must** include a visible "Restore Purchases" button. This catches developers every cycle — even if StoreKit 2 handles restoration automatically, reviewers expect an explicit button. One developer was rejected despite having `AppStore.sync()` working because the restoration required an app restart.[^49][^50]

### Crashes During Review

Any crash, freeze, or unresponsive screen during review is immediate rejection (Guideline 2.1). This includes crashes caused by API timeouts — if your backend is down during review, the app gets rejected. Always implement fallback mechanisms and proper error handling for network requests.[^51][^52][^53]

### Privacy Policy and Data Disclosure

Guideline 5.1.1 requires a privacy policy URL in App Store Connect and accurate data collection disclosures. Undisclosed third-party SDKs (Firebase, Facebook SDK, analytics) that collect data are a common cause of rejection. Every permission request (`NSCameraUsageDescription`, `NSLocationWhenInUseUsageDescription`, etc.) must clearly explain **why** the data is needed.[^54][^55]

### Deprecated APIs

Using deprecated APIs or outdated SDKs can trigger rejection, especially when Apple flags them in new review guidelines.[^53]

***

## 8. Xcode Build System Mysteries

### "Unable to type-check expression in reasonable time"

This is a **compiler limitation**, not technically a bug. The Swift type checker uses exponential time with complex expressions involving operator overloading and type inference. Common triggers: long arithmetic chains with `CGFloat`/`SIMD` types, complex `onChange` blocks, deeply nested string interpolations.[^56][^57][^58]

Fixes:
- Break expressions into intermediate `let` bindings
- Add explicit type annotations (e.g., `let x: CGFloat = ...`)
- Extract complex closures into named functions[^58][^56]

This can **regress** between Xcode versions — code that compiled in Xcode 15.2 may fail in 15.3 due to type-checker changes.[^56]

### SWIFT_STRICT_CONCURRENCY

Controls the level of concurrency checking. Values: `minimal`, `targeted`, `complete`. In Xcode 16, even `minimal` reports warnings that weren't present before. There's no way to fully turn it off — `minimal` is the floor. Frank Rupprecht found that workarounds like assigning closures to variables can suppress spurious warnings.[^59]

### SWIFT_DEFAULT_ACTOR_ISOLATION

New in Xcode 26 / Swift 6.2 (SE-0466). Set to `MainActor` by default in new projects, `nonisolated` for existing projects. This setting is configured at the **target** level, and project-level settings can be overridden per-target — a common source of confusion. In `Package.swift`, use `.swiftSettings([.defaultIsolation(MainActor.self)])` to set it.[^60][^61][^18][^16]

### Unnecessary Rebuilds

Common causes of excessive recompilation:[^62][^63]
- **Compilation Mode** set to "Whole Module" instead of "Incremental" for debug builds
- Heavy Obj-C/Swift bridging headers that force cascading recompiles
- SPM package resolution triggering full rebuilds
- Changing "core" files that many others depend on
- Build system sometimes ignores incremental settings entirely — some developers report `xcodebuild` doesn't do true incremental builds even when configured to[^64]

***

## 9. Testing Without a Mac

### What Simulators Can't Test

iOS simulators run on desktop hardware and fundamentally cannot validate:[^65]

| Category | Simulator | Real Device |
|----------|-----------|-------------|
| Push notifications | ❌ (limited support since Xcode 14) | ✅ |
| Camera/biometrics | ❌ | ✅ |
| GPS accuracy | Simulated only | ✅ |
| Memory pressure/thermal throttling | ❌ | ✅ |
| Real network conditions | Desktop networking | ✅ |
| Touch/gesture accuracy | Mouse approximation | ✅ |
| System alerts/permission prompts | Behave differently | ✅ |
| Background app lifecycle | Simplified | ✅ |

### Cloud Simulator Platforms

Services like BrowserStack, Appetize, and MacStadium provide remote access to real iOS devices or Mac environments running Xcode simulators. BrowserStack explicitly notes: "Tests may pass consistently on simulators while failing on real devices, creating a gap between test success and actual user experience".[^66][^67][^65]

### What Will ONLY Break on a Real Device

- **Metal/GPU rendering bugs**: Simulators use the Mac's GPU, masking iOS-specific rendering issues
- **Memory limits**: Simulators don't enforce iOS memory limits — apps that work in simulator may be terminated by iOS for excessive memory use on real hardware[^65]
- **Concurrency timing**: Race conditions and threading bugs may only manifest under real device CPU scheduling
- **Entitlements and provisioning**: Push notifications, HealthKit, and certain capabilities require a real device with proper provisioning profiles
- **Performance**: Scroll hitches, animation jank, and thermal throttling are invisible in simulators[^65]

### CI Without a Mac

You **cannot** build iOS apps without macOS — Xcode only runs on macOS. CI options: GitHub Actions macOS runners, Bitrise, CircleCI with macOS executors, or cloud Mac services like MacStadium. You can run unit tests and UI tests on simulators in CI, but for release confidence, always validate on real devices through cloud device farms.[^68][^69][^70]

***

## 10. The Swift/SwiftUI APIs That Lie

### Deprecated APIs Still Everywhere in Tutorials

| Deprecated API | Replacement | Since | Notes |
|---------------|-------------|-------|-------|
| `.foregroundColor(_:)` | `.foregroundStyle(_:)` | iOS 17[^71][^72] | `foregroundStyle` accepts any `ShapeStyle`, not just colors |
| `.cornerRadius(_:)` | `.clipShape(.rect(cornerRadius:))` | iOS 18.1[^73][^74] | `cornerRadius` clips content; `clipShape` gives more control over shape |
| `NavigationView` | `NavigationStack` / `NavigationSplitView` | iOS 16[^75] | `NavigationView` has persistent bugs Apple won't fix |
| `@StateObject` | `@State` (with `@Observable`) | iOS 17[^1] | **Not a drop-in replacement** — different initialization semantics |
| `@ObservedObject` | `@Bindable` | iOS 17[^76] | For passing `@Observable` objects to child views |
| `@EnvironmentObject` | `.environment(_:)` (with `@Observable`) | iOS 17[^7] | New `@Environment` supports custom observable types directly |

### @StateObject → @State: The Most Dangerous Migration

Apple's official migration guide presents this as straightforward, but it's not. `@StateObject` initializes once (via `@autoclosure`); `@State` re-runs the initializer on every view rebuild. If your observable class does anything in `init()` — reads UserDefaults, registers notifications, starts network requests — migration to `@State` introduces phantom instances and non-deterministic behavior. The Swift Forums community has repeatedly flagged that the migration guide should carry a warning.[^77][^1]

### @EnvironmentObject: Crash Without Warning

`@EnvironmentObject` accesses are **not checked at compile time**. If a parent view forgets to inject `.environmentObject(myObject)`, the app crashes at runtime with no useful error message. There's no optional access — it's a force-unwrap under the hood.[^8][^7]

### .cornerRadius vs .clipShape: Not Just a Rename

`.cornerRadius` clips the **entire view content** to rounded corners, which can unexpectedly clip child views that extend beyond bounds (like shadows or overlapping elements). `.clipShape(RoundedRectangle(cornerRadius:))` gives explicit control over what gets clipped. The deprecation also enables `UnevenRoundedRectangle` for asymmetric corner radii.[^78][^74]

### NavigationView: Still in Every Tutorial

`NavigationView` was deprecated in iOS 16 but remains the top result in most Google/Stack Overflow searches. It has known bugs with `List` selection, column collapse on iPad, and programmatic navigation that Apple has no plans to fix — all development has moved to `NavigationStack`.[^75][^79]

---

## References

1. [SwiftUI's Observable macro is not a drop-in replacement for ...](https://www.jessesquires.com/blog/2024/09/09/swift-observable-macro/) - The new @Observable macro was introduced last year with iOS 17 and macOS 14. It was advertised as (m...

2. [SwiftUI @State var initialization issue](https://stackoverflow.com/questions/56691630/swiftui-state-var-initialization-issue) - SwiftUI doesn't allow you to change @State in the initializer but you can initialize it. Remove the ...

3. [@State messing with initializer flow - Using Swift](https://forums.swift.org/t/state-messing-with-initializer-flow/25276) - @State variables in SwiftUI should not be initialized from data you pass down through the initialize...

4. [SwiftUI @State PW exact same code different result if another ...](https://forums.swift.org/t/swiftui-state-pw-exact-same-code-different-result-if-another-optional-is-added/62789) - This code both a and b get the initialize value in the init: struct FooFooFoo: View { @State var a: ...

5. [[Pitch] [SwiftUI] Stabilize State in Lazy Containers](https://forums.swift.org/t/pitch-swiftui-stabilize-state-in-lazy-containers/79926) - Introduction In SwiftUI, @State and @StateObject are designed to persist values across view updates,...

6. [Lazy-loading views with LazyVStack in SwiftUI](https://www.createwithswift.com/lazy-loading-views-with-lazyvstack-in-swiftui/) - If you're seeing memory warnings or crashes when displaying lists, especially on older devices, your...

7. [@Environment vs @EnvironmentObject](https://stackoverflow.com/questions/58061910/environment-vs-environmentobject/64215425) - What's the difference between @Environment and @EnvironmentObject in SwiftUI? From what I found from...

8. [swift - @Environment vs @EnvironmentObject](https://stackoverflow.com/questions/58061910/environment-vs-environmentobject) - @Environment is a key/value pair, whereas @EnvironmentObject is just a value identified by its type....

9. [Objective-C framework callbacks in Swift 6 · aplus.rs](https://aplus.rs/2024/objective-c-callback-crashes-swift6/) - The simplest solution? Mark the callback Sendable so compiler knows that whatever is done inside is ...

10. [Mysterious Runtime Crashes in Swift 6 Language Mode](https://philz.blog/mysterious-runtime-crashes-in-swift-6-language-mode/) - The sendability of a closure affects how the compiler infers isolation for its body. A callback clos...

11. [Incorrect actor isolation assumption across Swift 5/6 module boundary leads to `dispatch_assert_queue` crashes · Issue #75453 · swiftlang/swift](https://github.com/swiftlang/swift/issues/75453) - Reproduction Consider the following code, split in 2 modules: // Module 1, compiled with Swift 5 and...

12. [Crash in Combine Related to Swift 6 Concurrency](https://forums.swift.org/t/crash-in-combine-related-to-swift-6-concurrency/75178) - I was writing a test for a custom Publisher, and to simplify the asynchrony inherent in the test I m...

13. [Do runtime actor-isolation crashes still happen in real apps?](https://www.reddit.com/r/swift/comments/1pgoxvi/swift_6_strict_concurrency_do_runtime/) - This will crash when a value is published off of main. You can either mark that closure explicitly @...

14. [Swift 6 Concurrency: Overcoming Actor Isolation and Sendable Issues](https://www.linkedin.com/posts/pardip-bhatti_swift6-concurrency-mainactor-activity-7425729637542084608-fgGJ) - Taming Swift 6 Concurrency: My Battle with @MainActor, Sendable, and nonisolated I switched my macOS...

15. [Taming Swift 6 Concurrency: My Battle with @MainActor, Sendable ...](https://pardipbhatti.pro/blog/taming-swift-6-concurrency-my-battle-with-mainactor-sendable-and-nonisolated) - I switched my macOS app to Swift 6 strict concurrency and got 40+ compiler errors.

16. [Default Actor Isolation：好初衷带来的新问题](https://fatbobman.com/zh/posts/default-actor-isolation/) - Swift 6.2 推出 Default Actor Isolation，让开发者可为模块设置默认隔离域（如 MainActor），大幅减少并发相关的样板代码，但也引入了跨隔离域访问限制及宏开发的新挑...

17. [Swift Concurrency Just Got Approachable - SwiftMo - Substack](https://moelnaggar14.substack.com/p/swift-concurrency-just-got-approachable) - How Swift 6.2's default actor isolation changes everything

18. [swift-evolution/proposals/0466-control-default-actor-isolation.md at main · swiftlang/swift-evolution](https://github.com/swiftlang/swift-evolution/blob/main/proposals/0466-control-default-actor-isolation.md) - This maintains proposals for changes and user-visible enhancements to the Swift Programming Language...

19. [Accidental Multiple Push with SwiftUI NavigationStack - Teabyte](https://alexanderweiss.dev/blog/2023-01-24-accidental-double-push-with-swiftui-navigationstack) - I experienced a weird behavior where a simple detail navigation from a list of entries causes two de...

20. [Several issues with NavigationStack : r/SwiftUI](https://www.reddit.com/r/SwiftUI/comments/1axsmx4/several_issues_with_navigationstack/) - Using .navigationDestination(isPresented:) is not very reliable as it can produce a bug where when n...

21. [Problem with NavigationLink navigating to same location](https://www.reddit.com/r/SwiftUI/comments/1f36zfg/problem_with_navigationlink_navigating_to_same/) - Problem with NavigationLink navigating to same location

22. [SwiftData does not cascade delete](https://forums.developer.apple.com/forums/thread/740649)

23. [SwiftData does not cascade delete relationship entries](https://stackoverflow.com/questions/77355653/swiftdata-does-not-cascade-delete-relationship-entries) - I'm trying to delete a parent along with its array of children. What happens is that the parent is d...

24. [SwiftData thread-safety: passing models between threads](https://forums.developer.apple.com/forums/thread/766973)

25. [Is it a bit weird that all SwiftData operations require you to be in the main thread?](https://www.reddit.com/r/SwiftUI/comments/1fpk5mn/is_it_a_bit_weird_that_all_swiftdata_operations/) - Is it a bit weird that all SwiftData operations require you to be in the main thread?

26. [Separating Concerns in SwiftData Models, or: @Query ...](https://pado.name/blog/2025/02/swiftdata-query/) - Personal website for Geoff Pado: iOS, macOS, and backend server developer.

27. [SwiftData @Query runs in main thread? : r/iOSProgramming](https://www.reddit.com/r/iOSProgramming/comments/1egeim7/swiftdata_query_runs_in_main_thread/) - I am just using "@Query" property wrapper to get list of SwiftData data models into the view. But wh...

28. [SwiftData fetch performance problem with relationships](https://stackoverflow.com/questions/79316512/swiftdata-fetch-performance-problem-with-relationships) - Trying to solve performance issues with SwiftData. When I add one-to-many relationship to my model, ...

29. [SwiftData Migration: V1 to V3 with Custom and Lightweight Options](https://www.linkedin.com/posts/nadeem-ahmad-2314b4112_swiftdata-migration-v1-v3-in-swiftdata-activity-7426853293152231424-lwvr) - 📊 SwiftData Migration (V1 → V3) ⸻ In SwiftData, migration has become much easier. To perform a migra...

30. [Never use SwiftData without VersionedSchema - Mert Bulan](https://mertbulan.com/programming/never-use-swiftdata-without-versionedschema) - No versioned schema in SwiftData? Prepare for crashes—learn from my mistake.

31. [Model your schema with SwiftData - WWDC23 - Videos - Apple Developer](https://developer.apple.com/videos/play/wwdc2023/10195/) - Learn how to use schema macros and migration plans with SwiftData to build more complex features for...

32. [SwiftUI Memory Management & Retain Cycle Pitfalls (Production ...](https://dev.to/sebastienlato/swiftui-memory-management-retain-cycle-pitfalls-production-guide-2kii) - SwiftUI hides a lot of memory complexity — until it doesn’t. At scale, teams run into: ViewModels...

33. [Fixing Memory Leaks in a SwiftUI App](https://www.linkedin.com/pulse/fixing-memory-leaks-swiftui-app-mustafa-bekirov-5k4le) - A practical deep-dive into tracking retain cycles and optimizing memory in SwiftUI apps using Xcode ...

34. [SwiftUI Memory Leaks: 10 Critical Mistakes Developers Make](https://drcodes.com/posts/swiftui-memory-leaks-10-critical-mistakes-developers-make) - SwiftUI Memory Leaks: 10 Critical Mistakes Developers Make

35. [How to Fix Memory Leaks in SwiftUI with Closures](https://www.youtube.com/watch?v=2UJh2ERrM1k) - Learn how to resolve complex memory leaks in SwiftUI caused by nested closures and improper handling...

36. [ForEach within List affecting performance at scale?](https://www.reddit.com/r/SwiftUI/comments/1cwtfom/foreach_within_list_affecting_performance_at_scale/) - ForEach within List affecting performance at scale?

37. [SwiftUI Performance Pitfalls: 5 Common Issues to Avoid](https://www.linkedin.com/posts/vinaylakshakar_iosdevelopment-swiftui-swiftlang-activity-7407670936901943296-D1mn) - 🚀 Stop Chasing SwiftUI "Hitches": 5 Common Pitfalls & How to Fix Them Is your SwiftUI app feeling a ...

38. [Performance Battle: AnyView vs Group](https://nalexn.github.io/anyview-vs-group/) - AnyView brings in serious performance implications to the SwiftUI render engine, due to the fact tha...

39. [SwiftUI AnyView vs Group: type erasure in practice](https://gist.github.com/perlguy99/b6bf73fa366421f0bd8f4e65758bb86d) - Type erasure is the process of hiding the underlying type of some data. In SwiftUI we have AnyView f...

40. [AnyView's impact on SwiftUI performance - martinmitrevski](https://martinmitrevski.com/2024/01/02/anyviews-impact-on-swiftui-performance/) - Introduction AnyView is a type-erased view, that can be handy in SwiftUI containers consisting of he...

41. [Why AsyncImage in SwiftUI can crash your app | Anton Marchanka posted on the topic | LinkedIn](https://www.linkedin.com/posts/anthonyby_swiftui-im-actually-surprised-thatasyncimage-activity-7329529538638434304-uAEp) - SwiftUI. I’m actually surprised that AsyncImage doesn’t have a built-in way to resize images — my fr...

42. [How to cache images in SwiftUI for better performance](https://www.linkedin.com/posts/jacobmartinbartlett_swiftui-performance-image-caching-the-activity-7329159290374471680-S-Df) - This is a weakness in the SwiftUI AsyncImage ... This simple detective work can save your app from t...

43. [SwiftUI's Dirty Secret: Why AsyncImage Fails You & How to Fix It with Caching!](https://www.youtube.com/watch?v=HO1jOqbnkBA) - SwiftUI's Dirty Secret: Why AsyncImage Fails You & How to Fix It with Caching!

Mentoring 👉 https://...

44. [SwiftUI Performance Deep Dive: Rendering, Identity & Invalidations](https://dev.to/sebastienlato/swiftui-performance-deep-dive-rendering-identity-invalidations-elm) - SwiftUI performance problems rarely come from “slow code”. They come from misunderstanding how...

45. [Demystify SwiftUI performance - WWDC23 - Videos - Apple Developer](https://developer.apple.com/videos/play/wwdc2023/10160/) - Learn how you can build a mental model for performance in SwiftUI and write faster, more efficient c...

46. [iOS SDK - Privacy manifest FAQ](https://support.singular.net/hc/en-us/articles/24045392537243-iOS-SDK-Privacy-manifest-FAQ) - Apple Privacy Manifest Comply with Apple's privacy requirements by including a privacy manifest file...

47. [Enforcement of Apple Privacy Manifest starting from May 1, ...](https://bitrise.io/blog/post/enforcement-of-apple-privacy-manifest-starting-from-may-1-2024) - May 1st, 2024: Starting this date, new apps that don't describe their use of required reasons API in...

48. [Privacy requirement for app submissions starts May 1](https://developer.apple.com/news/?id=pvszzano) - Starting May 1, 2024, new or updated apps that have a newly added third-party SDK that's on the list...

49. [iOS app rejected due to lack of "restore purchases"](https://www.reddit.com/r/iOSProgramming/comments/1hexm3f/ios_app_rejected_due_to_lack_of_restore_purchases/) - I got this rejection as well. I put a "Restore Purchases" button at the bottom of my in-app store pa...

50. [Apple reject because of In app purchase not implement ...](https://stackoverflow.com/questions/11200460/apple-reject-because-of-in-app-purchase-not-implement-restore) - To restore previously purchased In-App Purchase products, it would be appropriate to provide a "Rest...

51. [14 Common Apple App Store Rejections and How To Avoid Themonemobile.ai › common-app-store-rejections-and-how-to-avoid-them](https://onemobile.ai/common-app-store-rejections-and-how-to-avoid-them/) - Leave no gaps that could lead to your app being rejected on the Apple App Store with 14 common rejec...

52. [7 Common App Store Rejection Reasons - Adalo](https://www.adalo.com/posts/common-app-store-rejection-reasons) - Seven common reasons apps get rejected on Apple and Google—privacy gaps, crashes, poor performance, ...

53. [How Do You Fix Common App Store Rejection Problems?](https://thisisglance.com/learning-centre/how-do-you-fix-common-app-store-rejection-problems) - Expert guide to fixing app store rejection problems, covers technical issues, design flaws, privacy ...

54. [Fix Apple Rejection: App Store Guideline 5.1.1 Privacy Issues](https://shopapper.com/fix-apple-rejection-app-store-guideline-5-1-1-privacy-issues/) - Got rejected under App Store Guideline 5.1.1? Learn how to fix privacy policy and data disclosure is...

55. [iOS App Store rejection: Privacy Policy URL](https://stackoverflow.com/questions/44969836/ios-app-store-rejection-privacy-policy-url) - My app (version 1.13) was rejected last night from the Apple review team. The reason: Guideline 5.1....

56. [After latest update: The compiler is unable to type-check this expression in reasonable time](https://developer.apple.com/forums/thread/748729)

57. [No longer able to type check expressions with Xcode 10.2 (caused by new SIMD API?)](https://forums.swift.org/t/no-longer-able-to-type-check-expressions-with-xcode-10-2-caused-by-new-simd-api/22873) - I know for certain that the three lines with i, j and k in test.swift did type check (quickly) prior...

58. [The compiler is unable to type-check this expression Swift 4?](https://stackoverflow.com/questions/52382645/the-compiler-is-unable-to-type-check-this-expression-swift-4) - After updating Xcode I am getting this error into my code: The compiler is unable to type-check this...

59. [Unwanted Swift Concurrency Checking](https://mjtsai.com/blog/2024/09/20/unwanted-swift-concurrency-checking/)

60. [Setting default actor isolation in Xcode 26 - Donny Wals](https://www.donnywals.com/setting-default-actor-isolation-in-xcode-26/) - With Swift 6.2, Apple has made a several improvements to Swift Concurrency and its approachability. ...

61. [Help with approachable-concurrency sample code - Using Swift](https://forums.swift.org/t/help-with-approachable-concurrency-sample-code/82634) - The code below, from here: Swift 6.2 Released- Approachable Concurrency, won't compile for me. // In...

62. [Is there a way to stop Xcode from recompiling all the packages all the time, even when they don't have any change?](https://www.reddit.com/r/iOSProgramming/comments/1c0lfg0/is_there_a_way_to_stop_xcode_from_recompiling_all/) - Is there a way to stop Xcode from recompiling all the packages all the time, even when they don't ha...

63. [Xcode 11 recompiles too much](https://stackoverflow.com/questions/60854743/xcode-11-recompiles-too-much) - I would advise: Make sure you are doing debug builds with incremental building, not whole module opt...

64. [xcodebuild not doing incremental builds](https://stackoverflow.com/questions/55376810/xcodebuild-not-doing-incremental-builds) - I recently checked out a fresh version of our iOS app from git and built from command line via xcode...

65. [Appium iOS Testing: Simulator vs Real Devices [2026] | BrowserStack](https://www.browserstack.com/guide/appium-ios-simulator-vs-real-device-testing) - Choose between Appium for iOS testing (both simulator and real device) because of its cross-browser ...

66. [How to test an iOS App without iPhone? - BrowserStack](https://www.browserstack.com/guide/how-to-test-ios-app-without-iphone) - Learn how to test an iOS app without an iPhone. Discover tools and methods for efficient iOS testing...

67. [Any affordable & accurate way I can test iOS app local development without Apple devices?](https://www.reddit.com/r/developersIndia/comments/1khow7z/any_affordable_accurate_way_i_can_test_ios_app/) - Any affordable & accurate way I can test iOS app local development without Apple devices?

68. [How to Test iOS Apps on Non-iPhone Devices - TestMu AI](https://www.testmuai.com/blog/ios-app-testing-non-iphone/) - Learn practical methods to test iOS apps on simulators, iPads, Macs, and cloud device farms, includi...

69. [Device testing for iOS - Bitrise Docs](https://docs.bitrise.io/en/bitrise-ci/testing/device-testing-with-firebase/device-testing-for-ios.html) - With Bitrise’s iOS device testing solution, you can run iOS tests on physical devices without having...

70. [Simulate iOS on Windows: Here's What Actually Works - Rent a Mac](https://rentamac.io/simulate-ios-on-windows/) - Simulate iOS on Windows? Discover the legit method developers are using to preview and test iOS apps...

71. [New and Deprecated APIs in iOS 17](https://www.youtube.com/watch?v=Y1e5h0aLpVE) - In this video I'd like to share with you some things that were either deprecated or added in iOS 17 ...

72. [SwiftUI foregroundColor is deprecated - Xcode IOS ...](https://www.youtube.com/watch?v=MV55CvlPb2M) - Stay up-to-date with SwiftUI in iOS Development 2024! In this quick tutorial, discover how to handle...

73. [[SwiftUI] Deprecated 되어버린 cornerRadius를 대체할 방법](https://semin1127.tistory.com/entry/SwiftUI-Deprecated-%EB%90%98%EC%96%B4%EB%B2%84%EB%A6%B0-cornerRadius%EB%A5%BC-%EB%8C%80%EC%B2%B4%ED%95%A0-%EB%B0%A9%EB%B2%95) - [머릿말]테두리를 부드럽게 설정할 수 있는 메서드, cornerRadius는 iOS 18.1부터 deprecated 되었다... 왜 deprecated 되었을까관련해서 StackO...

74. [Whoever deprecated corner radius should be fired and ...](https://www.reddit.com/r/SwiftUI/comments/1drs4h6/whoever_deprecated_corner_radius_should_be_fired/) - Whoever deprecated corner radius should be fired and what is the new best practice for radiating cor...

75. [NavigationStack with .navigationDestination doesn't works ...](https://stackoverflow.com/questions/78309322/navigationstack-with-navigationdestination-doesnt-works-for-me) - I'm new with SwiftUI and I have a problem with the navigation with NavigationStack and .navigationDe...

76. [Why @Bindable and not @Binding in SwiftUI for iOS 17](https://developer.apple.com/forums/thread/735416)

77. [Observable init() called multiple times by @State, different behavior ...](https://forums.swift.org/t/observable-init-called-multiple-times-by-state-different-behavior-to-stateobject/70811) - 👋 I am trying to adapt @Observation in my app, but some behavioral differences confuse me. Here is a...

78. [CornerRadius issue with GeometryReader in SwiftUI](https://stackoverflow.com/questions/66040003/cornerradius-issue-with-geometryreader-in-swiftui) - If you only want the yellow to have the corner radius and to not crop the red square, give the corne...

79. [Taking control of your navigation in SwiftUI with NavigationPath](https://www.createwithswift.com/taking-control-of-your-navigation-in-swiftui-with-navigationpath/) - SwiftUI's NavigationStack and NavigationPath provide a powerful and flexible way to perform programm...

