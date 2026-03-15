# Sub-Inventory: Onboarding HTML Parity

**Parent:** `todo/inventory/4-onboarding-screens.md`
**Covers:** HTML preview implementation in `preview/chat.html`

---

## HTML Elements Needed

### Component Picker Entry

| # | Element | Location | Change |
|---|---------|----------|--------|
| 1 | Dropdown option | `<select id="componentSelect">` | Add `<option value="onboarding">Onboarding</option>` after "pipeline-status" option |

### CSS (add to existing stylesheet)

| # | Class | Purpose | Properties |
|---|-------|---------|------------|
| 1 | `.onboarding-container` | Full-screen flex column | `flex:1; display:flex; flex-direction:column; overflow:hidden` |
| 2 | `.onboarding-page` | Single page layout | `flex:1; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:40px` |
| 3 | `.onboarding-icon-circle` | Tinted circle behind icon | `width:120px; height:120px; border-radius:50%; display:flex; align-items:center; justify-content:center` |
| 4 | `.onboarding-title` | Page title | `font-size:28px; font-weight:700; color:var(--trade-text); margin-top:32px` |
| 5 | `.onboarding-body` | Page description | `font-size:16px; color:var(--trade-text-secondary); text-align:center; line-height:1.5; margin-top:12px; max-width:300px` |
| 6 | `.onboarding-dots` | Page indicator dots | `display:flex; gap:8px; justify-content:center; padding:16px` |
| 7 | `.onboarding-dot` | Single dot | `width:8px; height:8px; border-radius:50%; background:var(--trade-border); transition:all 0.2s` |
| 8 | `.onboarding-dot.active` | Active dot | `background:var(--trade-green); width:24px; border-radius:4px` |
| 9 | `.onboarding-skip` | Skip button | `position:absolute; top:16px; right:16px; font-size:14px; color:var(--trade-text-secondary); cursor:pointer` |
| 10 | `.onboarding-cta` | Get Started button | `width:calc(100% - 80px); padding:14px; background:var(--trade-green); color:#fff; border:none; border-radius:14px; font-size:17px; font-weight:600; cursor:pointer` |
| 11 | `.onboarding-signin` | Sign In link | `font-size:14px; color:var(--trade-text-secondary); cursor:pointer; margin-top:12px` |

### JS State Variables

| # | Variable | Type | Default | Purpose |
|---|----------|------|---------|---------|
| 1 | `htmlOnboardingPage` | number | 0 | Current page index (0-3) |
| 2 | `htmlOnboardingComplete` | boolean | false | Whether "Get Started" was tapped |

### JS Functions

| # | Function | Purpose | Swift Equivalent |
|---|----------|---------|-----------------|
| 1 | `renderOnboarding()` | Renders the 4-page carousel into screenContent | OnboardingView.body |
| 2 | `setOnboardingPage(n)` | Sets htmlOnboardingPage, re-renders | TabView selection binding |
| 3 | `skipOnboarding()` | Sets htmlOnboardingComplete = true, switches to full-chat | Skip button action |
| 4 | `completeOnboarding()` | Same as skip — sets flag, shows chat | Get Started button action |

### Render Logic

```
case 'onboarding':
    sc.innerHTML = renderOnboarding();
    break;
```

`renderOnboarding()` returns:
- Pages 0-2: Icon circle + title + body + dots + Skip
- Page 3: Logo + title + body + Get Started + Sign In + dots (no Skip)
- Dots are clickable to jump between pages
- Swipe support via touch events (touchstart/touchend delta detection)

### Page Data (mirrors Swift)

| Page | Icon HTML | Color Var | Title | Body |
|------|----------|-----------|-------|------|
| 0 | bolt SVG | `--mode-fault-finder` | Fault Finder | (from MODES[0].fullDesc) |
| 1 | book SVG | `--mode-learn` | Learn | (from MODES[1].fullDesc) |
| 2 | search SVG | `--mode-research` | Research | (from MODES[2].fullDesc) |
| 3 | logo img | `--trade-green` | Ready to start? | Your AI-powered electrical assistant... |

### Touch/Swipe Support

| # | Event | Handler | Action |
|---|-------|---------|--------|
| 1 | touchstart | Store startX | Record swipe start position |
| 2 | touchend | Calculate deltaX | If delta > 50px: next page. If delta < -50px: previous page |
| 3 | dot click | `setOnboardingPage(n)` | Jump to page n |

### Watch Device Adaptations

| # | Element | Watch Override |
|---|---------|---------------|
| 1 | Icon circle | 60x60, icon 40pt |
| 2 | Title | font-size: 14px |
| 3 | Body | font-size: 10px |
| 4 | CTA button | padding: 8px, font-size: 12px |
| 5 | Skip | font-size: 10px |

---

## Parity Checklist

| # | Swift Behaviour | HTML Must Match |
|---|----------------|-----------------|
| 1 | TabView with .page style | Dot navigation + swipe |
| 2 | Skip on pages 1-3 | Skip button visible on pages 0-2 |
| 3 | No Skip on final page | Skip hidden on page 3 |
| 4 | Get Started sets @AppStorage | completeOnboarding() sets flag, shows chat |
| 5 | Sign In (future no-op) | Same as Get Started for now |
| 6 | Page dots show current | Active dot styled differently |
| 7 | Dark mode support | All CSS vars |
| 8 | Content vertically centered | Flexbox centering |
| 9 | Icon has tinted circle bg | .onboarding-icon-circle with opacity background |

**Status:** todo
