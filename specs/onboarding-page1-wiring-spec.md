# Onboarding Page 1 Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Screen | Onboarding — Page 1 (Fault Finder) |
| Total items | 7 |
| Already wired | 0 |
| Needs wiring | 7 |
| Target | Viewable in HTML preview, matches Swift pixel-for-pixel |
| No API calls | Pure UI — no backend needed |
| Date | 2026-03-15 |

---

## Objective

Build the first onboarding page showing the Fault Finder mode. Both Swift and HTML must render identically: amber bolt icon in a tinted circle, title, description, page dots, and Skip button. After implementation, selecting "Onboarding" in the HTML component picker shows the full page. ContentView gates on `@AppStorage` to show onboarding on first launch.

---

## Item Checklist

### 1. OnboardingPageView.swift (new file)

| Property | Value |
|----------|-------|
| Type | view (new) |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Views/Onboarding/OnboardingPageView.swift` |
| HTML equivalent | `renderOnboardingPage()` function |
| Blocked by | nothing |

**Swift implementation:**
```swift
import SwiftUI

struct OnboardingPageView: View {
    let icon: String
    let title: String
    let description: String
    let color: Color

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Image(systemName: icon)
                .font(.system(size: 80, weight: .semibold))
                .foregroundStyle(color)
                .frame(width: 120, height: 120)
                .background(color.opacity(0.12))
                .clipShape(Circle())

            Text(title)
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(Color.tradeText)
                .padding(.top, 32)

            Text(description)
                .font(.system(size: 16))
                .foregroundStyle(Color.tradeTextSecondary)
                .multilineTextAlignment(.center)
                .lineLimit(4)
                .padding(.top, 12)
                .padding(.horizontal, 40)

            Spacer()
        }
    }
}
```

### 2. OnboardingView.swift (new file)

| Property | Value |
|----------|-------|
| Type | view (new) |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Views/Onboarding/OnboardingView.swift` |
| HTML equivalent | `renderOnboarding()` function |
| Blocked by | OnboardingPageView |

**Swift implementation:**
```swift
import SwiftUI

struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var currentPage = 0

    private let pages: [(icon: String, title: String, description: String, color: Color)] = ThinkingMode.allCases.map {
        ($0.icon, $0.name, $0.fullDescription, $0.color)
    }

    var body: some View {
        ZStack {
            Color.tradeBg.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Spacer()
                    if currentPage < pages.count {
                        Button("Skip") {
                            hasCompletedOnboarding = true
                        }
                        .font(.system(size: 14))
                        .foregroundStyle(Color.tradeTextSecondary)
                        .padding(.trailing, 20)
                        .padding(.top, 8)
                    }
                }

                TabView(selection: $currentPage) {
                    ForEach(Array(pages.enumerated()), id: \.offset) { index, page in
                        OnboardingPageView(
                            icon: page.icon,
                            title: page.title,
                            description: page.description,
                            color: page.color
                        )
                        .tag(index)
                    }

                    OnboardingFinalPageView(
                        onGetStarted: { hasCompletedOnboarding = true },
                        onSignIn: { hasCompletedOnboarding = true }
                    )
                    .tag(pages.count)
                }
                .tabViewStyle(.page(indexDisplayMode: .always))
            }
        }
    }
}
```

### 3. OnboardingFinalPageView.swift (new file — stub for now)

| Property | Value |
|----------|-------|
| Type | view (new) |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/Views/Onboarding/OnboardingFinalPageView.swift` |
| Blocked by | nothing |

**Swift implementation (minimal — full version in later pages):**
```swift
import SwiftUI

struct OnboardingFinalPageView: View {
    let onGetStarted: () -> Void
    let onSignIn: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Image("TradeGuruLogo")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 100, height: 100)
                .foregroundStyle(Color.tradeGreen)

            Text("Ready to start?")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(Color.tradeText)
                .padding(.top, 32)

            Text("Your AI-powered electrical assistant. Ask anything about wiring, faults, standards, or theory.")
                .font(.system(size: 16))
                .foregroundStyle(Color.tradeTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 12)
                .padding(.horizontal, 40)

            Spacer()

            Button(action: onGetStarted) {
                Text("Get Started")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.tradeGreen)
                    .clipShape(.rect(cornerRadius: 14))
            }
            .padding(.horizontal, 40)

            Button(action: onSignIn) {
                Text("Already have an account? Sign In")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
            }
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
    }
}
```

### 4. ContentView.swift — gate on @AppStorage

| Property | Value |
|----------|-------|
| Type | modification |
| Status | needs-wiring |
| Swift file | `ios/Tradeguruelectrical/ContentView.swift` |
| Swift lines | 1-11 (entire file) |
| Blocked by | OnboardingView |

**Target state:**
```swift
import SwiftUI

struct ContentView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false

    var body: some View {
        if hasCompletedOnboarding {
            ChatView()
        } else {
            OnboardingView()
        }
    }
}
```

### 5. HTML — Component picker entry

| Property | Value |
|----------|-------|
| Type | html modification |
| Status | needs-wiring |
| File | `preview/chat.html` |
| Line | ~894 (after pipeline-status option) |

**Change:** Add `<option value="onboarding">Onboarding</option>` after line 894.

### 6. HTML — Onboarding renderer function

| Property | Value |
|----------|-------|
| Type | html addition |
| Status | needs-wiring |
| File | `preview/chat.html` |
| Location | Before `render()` function (~line 1960) |

**Add `renderOnboarding()` function** that renders:
- Full-screen onboarding page with current page state (`htmlOnboardingPage`)
- Pages 0-2: Icon circle (120x120, mode color bg at 12% opacity) + title (28px bold) + description (16px secondary, centered)
- Page 3: Logo + title + "Get Started" button + "Sign In" link
- Page dots at bottom (active dot = wider green pill, inactive = small gray circle)
- Skip button top-right on pages 0-2
- Swipe support via touch events
- Dot click to jump between pages

**Add JS state:** `let htmlOnboardingPage = 0;` in the state variables section.

### 7. HTML — Render switch case

| Property | Value |
|----------|-------|
| Type | html modification |
| Status | needs-wiring |
| File | `preview/chat.html` |
| Line | ~2033 (before `default:` in render() switch) |

**Change:** Add `case 'onboarding': sc.innerHTML = renderOnboarding(); break;` before default.

---

## Implementation Order

| Phase | Items | Description |
|-------|-------|-------------|
| 1 | #1, #3 | Create OnboardingPageView + OnboardingFinalPageView (leaf components) |
| 2 | #2, #4 | Create OnboardingView container + ContentView gate |
| 3 | #5, #6, #7 | HTML picker entry + renderer + render case |

---

## Verification

- [ ] Selecting "Onboarding" in HTML component picker shows page 1 (Fault Finder)
- [ ] Clicking dots navigates between all 4 pages
- [ ] Swiping left/right navigates between pages
- [ ] Skip button visible on pages 1-3, hidden on page 4
- [ ] Get Started on page 4 switches to Full Chat view
- [ ] Dark mode toggle works on all onboarding pages
- [ ] iPhone/iPad/Watch device frames all render correctly
- [ ] Page 1 shows: amber bolt icon, "Fault Finder" title, fullDescription text
- [ ] Swift OnboardingView has matching layout to HTML
- [ ] ContentView shows OnboardingView when hasCompletedOnboarding is false
