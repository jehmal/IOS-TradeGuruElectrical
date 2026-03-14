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
