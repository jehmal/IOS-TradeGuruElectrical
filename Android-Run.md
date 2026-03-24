# Android-Run.md — TradeGuru Android Build Process

## Overview

Complete process for building the TradeGuru Android native app from the existing Swift iOS app, achieving 1:1 feature and visual parity.

## Prerequisites Completed

- Swift iOS app (55 files) as source of truth
- iOS HTML preview at `preview/chat.html`
- Android HTML preview at `android/preview/chat.html`
- GitHub CI/CD for Android builds (`.github/workflows/android-build.yml`)
- `gh` CLI authenticated

## Run 1 — Initial Build (2026-03-24)

### Phase 1: Foundation Setup
1. Created `android/` directory with Gradle build system (AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01)
2. Created Room database schema matching SwiftData exactly (5 entities, 5 DAOs, converters, cascade deletes)
3. Created all model enums matching Swift raw values (ThinkingMode, ContentBlockType, MessageRole, PipelineStage, AttachmentType, UserTier, AuthState, AuthUser, AuthTokens)
4. Created Compose theme (Color.kt, Theme.kt, Type.kt) with all hex values matching iOS
5. Created MockData.kt with 3 conversations matching Swift mock data
6. Ran structural parity audit → Report #12 (100% match)

### Phase 2: Visual Parity
1. Created Android HTML preview (`android/preview/chat.html`) with Material Design 3 styling
2. Rewrote Android HTML to match iOS HTML pixel-for-pixel (same radii, fonts, colors, spacing)
3. Ran visual parity audit → Report #13 (100% match after 29 fixes)

### Phase 3: Code Reader Autoresearch
1. Set up code reader agent using `gh` CLI (no cloning)
2. Read PRs from: android/nowinandroid, chrisbanes/tivi, square/okhttp, android/architecture-samples, android/compose-samples, JetBrains/compose-multiplatform
3. Extracted 98 patterns across 14 expertise files (220KB total)

### Phase 4: Spec Creation + Review
1. Created `specs/android-swift-parity-sculpting-flow.md` — full implementation spec
2. Ran 5 rounds of adversarial review (10 independent hostile reviewers)
3. Applied 45+ fixes across rounds until both reviewers approved
4. Key architecture decisions documented: AD1-AD11

### Phase 5: Multi-Agent Build (7 tasks, 5 waves)
1. **Wave 1 (parallel):** builder-services (15 files), builder-viewmodels (3 files), builder-blocks (12 files)
2. **Wave 2 (parallel):** builder-chat-ui (10 files), builder-secondary (9 files)
3. **Wave 3:** builder-wiring (connected everything, updated MainActivity/NavGraph)
4. **Wave 4:** validator-parity → Report #14 (93% → 97% after gap fixes)

### Phase 6: CI/CD + Build Loop
1. Created `.github/workflows/android-build.yml`
2. Build 1: FAIL — XML Material3 theme (fixed: use platform theme)
3. Build 2: FAIL — 5 compilation errors (fixed: types, imports, params)
4. Build 3: FAIL — ensureActive() needs coroutine context (fixed)
5. Build 4: SUCCESS

### Phase 7: Code Hardening
1. Set up code-hardener autoresearch agent
2. Swept 74 Kotlin files against all 14 expertise libraries (1,036 pattern checks)
3. Found 3 violations, fixed all, converged
4. Build 5: SUCCESS — APK downloaded to desktop

### Final Artifacts
- 74 Kotlin files (27 foundation + 47 new)
- 14 expertise files (98 patterns)
- 4 parity reports (#12, #13, #14)
- Reviewed spec with 11 architecture decisions
- Debug APK (21 MB)

---

### Run-Additions: Variant 1 Bug Fixes + Feature Additions

**Crash Bugs (must fix):**
1. Camera capture crashes the app — likely CameraX permission or lifecycle issue with `TakePicturePreview`
2. Sending a message with a photo attachment crashes — likely null/type mismatch when constructing the vision API request from attachment data

**Feature Additions (must add):**
1. Settings page — port from Swift SettingsView.swift (account section, data section, about section, sign out)
2. WorkOS auth integration — connect Chrome Custom Tabs OAuth flow to real WorkOS endpoints, enable sign-in via Google/Apple/Email, persist tokens, refresh on foreground
3. New chat button — top-right of nav bar (pen/compose icon), matching iOS and Android HTML preview
4. Full discrepancy audit — compare every Swift screen behavior to Android, fix remaining gaps

**Expertise Research Needed:**
1. Study how production Android apps handle CameraX in Compose (lifecycle, permissions, preview cleanup)
2. Study how production Android apps handle image attachment → API upload → streaming response
3. Add expertise files for these crash patterns to prevent recurrence

**Build Steps:**
1. Run code reader focused on camera/attachment crash patterns
2. Fix both crash bugs
3. Add settings page matching iOS
4. Wire WorkOS auth end-to-end
5. Add new chat button to ChatNavBar
6. Run discrepancy audit
7. Code harden with new expertise
8. Push → CI build → download APK
