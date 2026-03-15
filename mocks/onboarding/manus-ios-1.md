# Mock Analysis: manus-ios-1

**Source:** `mocks/onboarding/manus-ios-1.png`
**Analysed:** 2026-03-14

---

## Mock Type

Onboarding / sign-in screen with social OAuth buttons (Google, Microsoft, Apple) and email fallback, centered branding with animated hand-cursor icon.

---

## Spacing

| Property | Value (est.) | Notes |
|----------|-------------|-------|
| Screen horizontal padding | 24pt | Content inset from screen edges |
| Section vertical spacing | 40pt | Between logo area and button group |
| Item gap (horizontal rows) | — | N/A |
| Item gap (vertical lists) | 12pt | Between sign-in buttons |
| Card internal padding | 16pt | Button group card has inner padding |
| Input field padding | 12pt | Vertical padding inside each button row |
| Tab bar height | ~44pt | Thin bottom bar with branding |
| Nav bar height | 0pt | No navigation bar; status bar only |

---

## Text & Font

| Style | Size (est.) | Weight | Color | Usage |
|-------|------------|--------|-------|-------|
| Title | 28pt | bold | primary (#000) | "Welcome to Manus" |
| Button Label | 16pt | medium | primary (#000) | "Continue with Google", etc. |
| Separator | 14pt | regular | tertiary (gray) | "OR" divider text |
| Legal | 11pt | regular | secondary (dark gray) | Terms/Privacy footer text |
| Legal Link | 11pt | regular | primary (#000) underlined | "Terms of Service", "Privacy Policy" |
| Tab Label | 12pt | medium | primary (#000) | "Manus" bottom-left |
| Tab Subtitle | 10pt | regular | secondary (gray) | "curated by" bottom-center |

---

## Components (Left to Right, Top to Bottom)

### Region: Status Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | text | "9:41" | left | caption | System time, bold, black |
| 2 | icon | cellular signal bars | right | small | System indicator, black |
| 3 | icon | Wi-Fi indicator | right | small | System indicator, black |
| 4 | icon | battery (full) | right | small | System indicator, black |

### Region: Background

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | pattern | Subtle dot grid | full | full | Light gray dots on white/off-white background, evenly spaced ~20pt grid |

### Region: Hero / Branding (upper center)

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | Hand cursor with click lines (custom) | center | ~64pt | Black line art, hand with index finger pointing, 3 emanating lines above |
| 2 | text | "Welcome to Manus" | center | title (28pt) | Bold, black, centered below icon |

### Region: Sign-In Card (lower half)

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | card | Sign-in container | center | full-width | White/light background, large rounded corners (~24pt), subtle shadow/border, contains all buttons |

### Region: Sign-In Card > Button Row 1

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | Google "G" logo | left | 20pt | Multicolor Google icon |
| 2 | text | "Continue with Google" | center | 16pt medium | Black text, centered |

### Region: Sign-In Card > Button Row 2

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | Microsoft logo (4 colored squares) | left | 20pt | Multicolor Microsoft icon |
| 2 | text | "Continue with Microsoft" | center | 16pt medium | Black text, centered |

### Region: Sign-In Card > Button Row 3

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | Apple logo | left | 20pt | Black Apple icon |
| 2 | text | "Continue with Apple" | center | 16pt medium | Black text, centered |

### Region: Sign-In Card > Divider

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | divider | Horizontal line | left | thin | Light gray, extends to "OR" |
| 2 | text | "OR" | center | 14pt regular | Gray/tertiary color |
| 3 | divider | Horizontal line | right | thin | Light gray, extends from "OR" |

### Region: Sign-In Card > Button Row 4 (Email)

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | envelope (SF: `envelope.fill`) | left | 20pt | Black/dark gray envelope icon |
| 2 | text | "Continue with Email" | center | 16pt medium | Black text, centered |

### Region: Sign-In Card > Legal Footer

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | text | "By continuing, you agree to our" | center | 11pt regular | Dark gray, inline |
| 2 | text | "Terms of Service" | center | 11pt regular | Black, underlined, tappable link |
| 3 | text | "and have read our" | center | 11pt regular | Dark gray, inline |
| 4 | text | "Privacy Policy" | center | 11pt regular | Black, underlined, tappable link |
| 5 | text | "." | center | 11pt regular | Dark gray, punctuation |

### Region: Bottom Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | Manus logo (circular, cursor-like) | far-left | ~16pt | Small black icon matching hero branding |
| 2 | text | "Manus" | left | 12pt medium | Black, app name |
| 3 | text | "curated by" | center | 10pt regular | Gray/secondary text |
| 4 | icon | Mobbin logo (two overlapping ovals) | right | ~16pt | Dark gray custom logo |
| 5 | text | "Mobbin" (implied by logo) | far-right | — | Part of Mobbin branding watermark |

---

## Component Summary

| Component Type | Count | Examples |
|---------------|-------|---------|
| icon | 9 | Google logo, Microsoft logo, Apple logo, envelope, hand-cursor hero, cellular, Wi-Fi, battery, Manus logo |
| button | 4 | "Continue with Google", "Continue with Microsoft", "Continue with Apple", "Continue with Email" |
| text | 8 | "Welcome to Manus", "OR", button labels, legal text, bottom bar text |
| card | 1 | Sign-in container with rounded corners |
| divider | 1 | "OR" separator with horizontal lines |
| pattern | 1 | Dot grid background |

---

## SwiftUI Implementation Notes

- **View Structure:** `ZStack` with dot-grid background, `VStack` containing hero section (icon + title) at top, sign-in card pushed toward bottom using `Spacer()`.
- **Sign-in Card:** Use a `VStack(spacing: 12)` inside a `.background(.regularMaterial)` or white `RoundedRectangle` with `cornerRadius: 24`. Each button is an `HStack` with icon + label inside a `Button`.
- **Buttons:** Each auth button is full-width with left-aligned brand icon and centered text. Use `HStack { icon; Spacer(); text; Spacer() }` or `.frame(maxWidth: .infinity)` centering with leading icon overlay.
- **Recommended SF Symbols:**
  - Email: `envelope.fill`
  - Apple: Use `Image(systemName: "apple.logo")` or the `ASAuthorizationAppleIDButton` for Sign in with Apple
  - Google/Microsoft: Custom asset images (no SF Symbol equivalents)
- **Color Tokens:**
  - Background: `Color(.systemBackground)` or custom off-white
  - Primary text: `Color(.label)`
  - Secondary text: `Color(.secondaryLabel)`
  - Tertiary text: `Color(.tertiaryLabel)`
  - Card background: `Color(.systemBackground)` or `.regularMaterial`
- **Dot Grid Background:** Custom `Canvas` or `Path` drawing small circles in a grid pattern, or a repeating tile image asset.
- **Layout:** No `NavigationStack` needed for this screen. Present as root view or first screen in onboarding flow. Bottom bar is likely a Mobbin watermark and NOT part of the app — omit in implementation.
- **Legal Text:** Use `Text` with `AttributedString` for inline underlined links, or a custom `Text` concatenation with `.underline()` for tappable terms.
- **Sign in with Apple:** Must use `SignInWithAppleButton` from `AuthenticationServices` for App Store compliance rather than a custom button.
