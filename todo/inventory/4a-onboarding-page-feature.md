# Sub-Inventory: Onboarding Feature Pages (1-3)

**Parent:** `todo/inventory/4-onboarding-screens.md`
**Covers:** OnboardingPageView.swift — the reusable template for pages 1, 2, 3

---

## OnboardingPageView — Element Inventory

### Layout (top to bottom)

| # | Element | SwiftUI | Size/Style | Notes |
|---|---------|---------|------------|-------|
| 1 | Spacer (top) | `Spacer()` | flexible | Pushes content to vertical center |
| 2 | Icon container | `Image(systemName:)` | 80pt, `.semibold` weight | SF Symbol tinted with mode color |
| 3 | Icon background | `.frame(120,120).background(color.opacity(0.12)).clipShape(Circle())` | 120x120 circle | Subtle tinted circle behind icon |
| 4 | Spacing | fixed | 32pt | Between icon and title |
| 5 | Title | `Text(title)` | `.title`, `.bold` | Mode name — 1 line, centered |
| 6 | Spacing | fixed | 12pt | Between title and body |
| 7 | Body | `Text(body)` | `.body`, `.tradeTextSecondary`, `.multilineTextAlignment(.center)` | fullDescription text — max 3 lines, centered |
| 8 | Spacer (bottom) | `Spacer()` | flexible | Balances top spacer |

### Properties

| # | Property | Type | Source |
|---|----------|------|--------|
| 1 | icon | String | SF Symbol name from ThinkingMode.icon |
| 2 | title | String | ThinkingMode.name |
| 3 | body | String | ThinkingMode.fullDescription |
| 4 | color | Color | ThinkingMode.color |

### Behaviour

| # | Behaviour | Implementation |
|---|-----------|---------------|
| 1 | No buttons on feature pages | Pure display — user swipes to navigate |
| 2 | Adapts to dark mode | Uses semantic colors (tradeText, tradeTextSecondary, tradeBg) |
| 3 | Icon color matches mode | Pass ThinkingMode.color directly |
| 4 | Content vertically centered | Dual Spacer() pattern |
| 5 | Horizontal padding | `.padding(.horizontal, 40)` for body text readability |

### Data per page instance

| Page | icon | title | body | color |
|------|------|-------|------|-------|
| 1 | `bolt.fill` | Fault Finder | Diagnose electrical faults, trace circuits, and identify issues with AI-assisted troubleshooting. | .modeFaultFinder |
| 2 | `book.fill` | Learn | Study electrical theory, code compliance, and best practices with interactive AI tutoring. | .modeLearn |
| 3 | `magnifyingglass` | Research | Research products, specifications, regulations, and technical documentation. | .modeResearch |

---

## File Plan

```swift
// ios/Tradeguruelectrical/Views/Onboarding/OnboardingPageView.swift

struct OnboardingPageView: View {
    let icon: String
    let title: String
    let body: String
    let color: Color

    // VStack centered layout
    // No state, no actions, no side effects
    // Pure presentational component
}
```

**Estimated lines:** ~35
**Dependencies:** TradeGuruColors (existing)
**Status:** todo
