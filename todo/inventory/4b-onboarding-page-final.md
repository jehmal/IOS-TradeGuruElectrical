# Sub-Inventory: Onboarding Final Page (4)

**Parent:** `todo/inventory/4-onboarding-screens.md`
**Covers:** OnboardingFinalPageView.swift — the CTA page with Get Started + Sign In

---

## OnboardingFinalPageView — Element Inventory

### Layout (top to bottom)

| # | Element | SwiftUI | Size/Style | Notes |
|---|---------|---------|------------|-------|
| 1 | Spacer (top) | `Spacer()` | flexible | |
| 2 | App logo | `Image("TradeGuruLogo")` | `.renderingMode(.template)`, 100x100, `.tradeGreen` tint | Same logo used in empty chat state |
| 3 | Spacing | fixed | 32pt | |
| 4 | Title | `Text("Ready to start?")` | `.title`, `.bold`, `.tradeText` | |
| 5 | Spacing | fixed | 12pt | |
| 6 | Body | `Text(...)` | `.body`, `.tradeTextSecondary`, centered | "Your AI-powered electrical assistant..." |
| 7 | Spacer (bottom) | `Spacer()` | flexible | |
| 8 | Get Started button | `Button { onGetStarted() }` | Full-width, `.tradeGreen` bg, white text, 50pt height, cornerRadius 14 | Primary CTA |
| 9 | Spacing | fixed | 12pt | |
| 10 | Sign In link | `Button { onSignIn() }` | `.font(.subheadline)`, `.tradeTextSecondary`, no background | Secondary — "Already have an account? Sign In" |
| 11 | Spacing (bottom safe) | fixed | 16pt padding | Bottom safe area breathing room |

### Properties

| # | Property | Type | Purpose |
|---|----------|------|---------|
| 1 | onGetStarted | `() -> Void` | Sets @AppStorage to true, dismisses onboarding |
| 2 | onSignIn | `() -> Void` | Future: opens WorkOS OAuth. For now: same as Get Started |

### Behaviour

| # | Behaviour | Implementation |
|---|-----------|---------------|
| 1 | Get Started tapped | Calls onGetStarted closure — parent sets hasCompletedOnboarding = true |
| 2 | Sign In tapped | Calls onSignIn closure — for now identical to Get Started (WorkOS wired later) |
| 3 | No Skip button | This IS the final destination — Skip is not needed |
| 4 | Button has press state | Native SwiftUI Button provides this automatically |
| 5 | Dark mode | All semantic colors |

### Button Specifications

| Button | Width | Height | BG | Text Color | Font | Corner Radius |
|--------|-------|--------|-----|------------|------|---------------|
| Get Started | `.frame(maxWidth: .infinity)` | 50pt | `.tradeGreen` | `.white` | `.system(size: 17, weight: .semibold)` | 14 |
| Sign In | intrinsic | intrinsic | none | `.tradeTextSecondary` | `.system(size: 14)` | n/a |

---

## File Plan

```swift
// ios/Tradeguruelectrical/Views/Onboarding/OnboardingFinalPageView.swift

struct OnboardingFinalPageView: View {
    let onGetStarted: () -> Void
    let onSignIn: () -> Void

    // VStack with logo, text, two buttons
    // Pure presentational + two callback closures
}
```

**Estimated lines:** ~55
**Dependencies:** TradeGuruColors (existing), TradeGuruLogo asset (existing)
**Status:** todo
