# Mock Analysis: chatmock

**Source:** `mocks/chat/chatmock.png`
**Analysed:** 2026-03-14

---

## Mock Type

Chat interface with AI agent, featuring mode selector, announcement card, quick-action buttons, text input with voice, and bottom tab bar.

---

## Spacing

| Property | Value (est.) | Notes |
|----------|-------------|-------|
| Screen horizontal padding | 16pt | Content inset from screen edges |
| Section vertical spacing | 24pt | Between nav bar, card, actions, input |
| Item gap (horizontal rows) | 12pt | Between action buttons |
| Item gap (vertical lists) | 8pt | Between card text lines |
| Card internal padding | 12pt | Image + text within card |
| Input field padding | 16pt horizontal, 12pt vertical | "Ask anything" text field |
| Tab bar height | 49pt | Standard iOS tab bar |
| Nav bar height | 44pt | Below status bar |
| Status bar height | 54pt | Dynamic Island / notch area |

---

## Text & Font

| Style | Size (est.) | Weight | Color | Usage |
|-------|------------|--------|-------|-------|
| Nav Title | 16pt | semibold | primary (#000) | "Expert" mode label |
| Card Title | 15pt | bold | primary (#000) | "Introducing Grok 4.1" |
| Card Body | 13pt | regular | secondary (#666) | Card description text |
| Button Label | 12pt | medium | primary (#000) | "Create Images", "Edit Image", "Try Voice Mode" |
| Input Placeholder | 16pt | regular | tertiary (#999) | "Ask anything" |
| Speak Button | 14pt | semibold | white (#FFF) | "Speak" label on dark pill |
| Watermark | — | — | light gray (#E0E0E0) | Centered logo, ~60pt diameter |
| Attribution | 11pt | regular | secondary (#888) | "curated by" footer text |

---

## Components (Left to Right, Top to Bottom)

### Region: Status Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | text | "9:41" | left | 15pt semibold | System time, black |
| 2 | icon | cellular signal bars | right-1 | small | System indicator, black |
| 3 | icon | wifi signal | right-2 | small | System indicator, black |
| 4 | icon | battery (full) | right-3 | small | System indicator, black, filled |

### Region: Navigation Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | avatar | User profile photo | left | 32pt circle | Circular crop, man's face |
| 2 | icon | `location.fill` or pin icon | center-left | 14pt | Inline before "Expert" text, black |
| 3 | text | "Expert" | center | 16pt semibold | Mode selector label |
| 4 | icon | `chevron.down` | center-right | 10pt | Dropdown indicator, black |
| 5 | icon | `clock.arrow.circlepath` or history | right-1 | 22pt | Outline style, black, tappable |
| 6 | icon | `square.and.pencil` | right-2 | 22pt | Outline style, black, compose/new chat |

### Region: Announcement Card

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | image | Space/asteroid scene | left | ~56pt square | Rounded corners (~8pt), dark scene with bright streaks |
| 2 | text | "Introducing Grok 4.1" | right-top | 15pt bold | Primary color, single line |
| 3 | text | "A new standard in conversational intelligence, emotional understanding, and real-world helpfulness." | right-bottom | 13pt regular | Secondary gray, 3 lines, wrapping |

Card container: full-width, light gray background (#F5F5F5), rounded corners (~12pt), horizontal layout (image left, text right), ~80pt tall.

### Region: Content Area (Empty State)

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon/logo | Stylized bolt/slash watermark | center | ~60pt | Light gray (#E0E0E0), low opacity, centered vertically in empty space |

### Region: Action Buttons Row

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | button | "Create Images" | left | ~88pt wide | Light gray bg (#F2F2F2), rounded rect (~12pt), icon above text |
| 1a | icon | Overlapping squares / `photo.on.rectangle` | inside button 1 | 20pt | Outline, black, centered above label |
| 2 | button | "Edit Image" | center-left | ~88pt wide | Light gray bg, rounded rect, icon above text |
| 2a | icon | `pencil` or brush tool | inside button 2 | 20pt | Outline, black, centered above label |
| 3 | button | "Try Voice Mode" | center-right | ~88pt wide | Light gray bg, rounded rect, icon above text |
| 3a | icon | `waveform` or equalizer bars | inside button 3 | 20pt | Outline, black, centered above label |
| 4 | button | (partially visible, 4th button) | right (clipped) | ~88pt wide | Only left edge visible, implies horizontal scroll |
| 4a | icon | Expand/arrows icon | inside button 4 | 20pt | Barely visible at right edge |

Row layout: horizontal ScrollView with HStack, buttons are equal-width cards with icon + label stacked vertically. Row has ~56pt height per button.

### Region: Input Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | input | "Ask anything" | full-width top | 44pt height | Light gray bg (#F5F5F5), rounded rect (~22pt full radius), placeholder text left-aligned |
| 2 | icon | `paperclip` or attachment | bottom-left | 28pt circle | Dark gray bg, white icon, circular button |
| 3 | spacer | — | bottom-center | flexible | Empty space between attachment and speak |
| 4 | button | "Speak" | bottom-right | ~80pt x 36pt | Dark/black pill bg, white text, microphone icon left of text |
| 4a | icon | `mic.fill` or waveform | inside speak button | 14pt | Red/pink accent color, left of "Speak" text |

Input area has two rows: text field on top, toolbar (attachment + speak) below. Separated by ~8pt.

### Region: Tab Bar

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | tab | `house.fill` (Home) | position 1 | 24pt icon | Black/filled, active state |
| 2 | tab | `magnifyingglass` (Search) | position 2 | 24pt icon | Gray/outline, inactive |
| 3 | tab | `checkmark.seal` or verified badge | position 3 | 24pt icon | Gray/outline, inactive |
| 4 | tab | `bubble.left.fill` (Messages) | position 4 | 24pt icon | Gray/outline, inactive |
| 4a | badge | "5" | on tab 4 | 16pt circle | Blue bg, white text, notification count |
| 5 | tab | `envelope` (Mail/Inbox) | position 5 | 24pt icon | Gray/outline, inactive |

### Region: Footer Attribution

| # | Type | Label / Content | Position | Size | Style Notes |
|---|------|----------------|----------|------|-------------|
| 1 | icon | "X" logo | left | 12pt | Black, platform icon |
| 2 | text | "curated by" | center | 11pt regular | Gray secondary text |
| 3 | image | "Mobbin" logo | right | ~40pt wide | Third-party attribution |

---

## Component Summary

| Component Type | Count | Examples |
|---------------|-------|---------|
| icon | 17 | signal, wifi, battery, location, chevron, clock, pencil, waveform, paperclip, mic, house, magnifyingglass, seal, bubble, envelope, X logo, bolt watermark |
| button | 6 | Create Images, Edit Image, Try Voice Mode, (4th clipped), Speak, attachment |
| text | 8 | time, "Expert", card title, card body, button labels, placeholder, "Speak", attribution |
| image | 2 | Announcement card image (space scene), user avatar |
| input | 1 | "Ask anything" text field |
| card | 1 | Announcement card (image + title + description) |
| avatar | 1 | User profile photo (32pt circle) |
| badge | 1 | "5" notification count on messages tab |
| tab | 5 | Home, Search, Verified, Messages, Mail |
| logo | 1 | Centered watermark in empty state |

---

## SwiftUI Implementation Notes

### View Structure
```
NavigationStack {
    VStack(spacing: 0) {
        // Custom nav bar (not system)
        HStack { avatar, Spacer, modeSelector, Spacer, historyBtn, composeBtn }

        ScrollView {
            // Announcement card
            AnnouncementCard(image:, title:, body:)

            Spacer() // Empty state with watermark overlay
        }
        .overlay { WatermarkLogo() }  // Centered logo

        // Action buttons - horizontal scroll
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ActionButton(icon:, label:)  // x4
            }
        }
        .contentMargins(.horizontal, 16)

        // Input area
        VStack(spacing: 8) {
            TextField("Ask anything", text: $input)
                .padding(.horizontal, 16)
                .frame(height: 44)
                .background(Color(.systemGray6))
                .clipShape(.capsule)

            HStack {
                AttachmentButton()
                Spacer()
                SpeakButton()
            }
        }
        .padding(.horizontal, 16)
    }
}
// Custom tab bar (not TabView - uses custom implementation)
```

### Recommended SF Symbols
| Mock Element | SF Symbol |
|-------------|-----------|
| Mode pin icon | `location.fill` |
| Dropdown chevron | `chevron.down` |
| History | `clock.arrow.circlepath` |
| New chat | `square.and.pencil` |
| Create Images | `photo.on.rectangle.angled` |
| Edit Image | `paintbrush.fill` |
| Voice Mode | `waveform` |
| Attachment | `paperclip` |
| Microphone | `mic.fill` |
| Home tab | `house.fill` / `house` |
| Search tab | `magnifyingglass` |
| Verified tab | `checkmark.seal.fill` / `checkmark.seal` |
| Messages tab | `bubble.left.fill` / `bubble.left` |
| Inbox tab | `envelope.fill` / `envelope` |

### Suggested Color Tokens
| Token | Value | Usage |
|-------|-------|-------|
| `.primary` | System label | Text, icons |
| `.secondary` | System secondary label | Card body, inactive tabs |
| `.tertiary` | System tertiary label | Placeholder text |
| `Color(.systemGray6)` | #F2F2F2 | Button backgrounds, input bg, card bg |
| `Color(.systemBackground)` | #FFFFFF | Screen background |
| `.accent` (blue) | System blue | Notification badge |
| `Color.black` | #000000 | Speak button bg |
| `Color.white` | #FFFFFF | Speak button text |

### Layout Approach
- Custom navigation bar (not `.navigationTitle`) — avatar + mode picker + action icons
- Announcement card: `HStack` with fixed-size image + `VStack` for text
- Action buttons: horizontal `ScrollView` + `HStack`, each button is `VStack { icon, text }` with rounded rect background
- Input area pinned to bottom using `VStack(spacing: 0)` layout (not overlay)
- Tab bar: custom `HStack` with 5 equal-width items, badge overlay on messages
- Empty state: `Color.clear` with `.overlay` for centered watermark
