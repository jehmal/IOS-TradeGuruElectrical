# Auth + Settings Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Feature | WorkOS OAuth + Settings Screen |
| Total items | 21 |
| Already wired | 3 (APIConfig jwt param, DeviceManager Keychain, device/link endpoint on backend) |
| Needs wiring | 18 |
| Target | Full OAuth sign-in with Google/Apple/Email, token management, Settings screen |
| Backend | All endpoints already deployed — zero backend changes |
| Auth proxy | `https://tradeguru.com.au/api/workos-auth-proxy` |
| Date | 2026-03-15 |

---

## Objective

Wire WorkOS OAuth authentication into the Swift app and HTML preview. Users can sign in via Google, Apple, or Email. After sign-in, the device links to their account, unlocking paid tiers. Settings screen shows account state, tier badge, and sign-out. All tokens stored in Keychain. Auto-refresh on app foreground. HTML preview shows settings component with mock auth state.

---

## Pain Point

The app only supports anonymous device-based auth (free tier). Users cannot sign in, link devices to accounts, or access paid tiers (Pro/Unlimited). The backend has full WorkOS integration with JWT validation, device linking, and tier-based quotas — but the Swift app never sends a JWT. There is no Settings screen.

---

## Implementation Checklist

### Phase 1 — Models (4 files, no dependencies)

#### 1. AuthState

| Property | Value |
|----------|-------|
| Type | model (new file) |
| File | `ios/Tradeguruelectrical/Models/AuthState.swift` |

```swift
import Foundation

nonisolated enum AuthState {
    case anonymous
    case authenticated(user: AuthUser)
}
```

#### 2. AuthUser

| Property | Value |
|----------|-------|
| Type | model (new file) |
| File | `ios/Tradeguruelectrical/Models/AuthUser.swift` |

```swift
import Foundation

nonisolated struct AuthUser: Codable {
    var id: String
    var email: String
    var name: String?
    var pictureURL: String?
}
```

#### 3. AuthTokens

| Property | Value |
|----------|-------|
| Type | model (new file) |
| File | `ios/Tradeguruelectrical/Models/AuthTokens.swift` |

```swift
import Foundation

nonisolated struct AuthTokens: Codable {
    var accessToken: String
    var refreshToken: String
    var expiresAt: Date
}
```

#### 4. UserTier

| Property | Value |
|----------|-------|
| Type | model (new file) |
| File | `ios/Tradeguruelectrical/Models/UserTier.swift` |

```swift
import Foundation
import SwiftUI

nonisolated enum UserTier: String, Codable {
    case free
    case pro
    case unlimited

    var displayName: String {
        switch self {
        case .free: "Free"
        case .pro: "Pro"
        case .unlimited: "Unlimited"
        }
    }

    var color: Color {
        switch self {
        case .free: .tradeTextSecondary
        case .pro: .modeLearn
        case .unlimited: .tradeGreen
        }
    }
}
```

---

### Phase 2 — Utilities (3 files)

#### 5. KeychainHelper

| Property | Value |
|----------|-------|
| Type | service (new file) |
| File | `ios/Tradeguruelectrical/Services/KeychainHelper.swift` |

Generic Keychain CRUD for any Codable type. Service name: `com.tradeguru.electrical.auth`.
Methods: `save<T: Codable>(_ value: T, forKey: String)`, `load<T: Codable>(forKey: String) -> T?`, `delete(forKey: String)`.
Uses `kSecAttrAccessibleAfterFirstUnlock`. Pattern matches existing DeviceManager Keychain code.

#### 6. PKCEHelper

| Property | Value |
|----------|-------|
| Type | utility (new file) |
| File | `ios/Tradeguruelectrical/Services/PKCEHelper.swift` |

```swift
import Foundation
import CryptoKit

nonisolated enum PKCEHelper {
    static func generateVerifier() -> String {
        // 43-128 char random string from [A-Z, a-z, 0-9, -._~]
    }
    static func generateChallenge(from verifier: String) -> String {
        // SHA-256 hash → base64url encoded (no padding)
    }
}
```

#### 7. JWTDecoder

| Property | Value |
|----------|-------|
| Type | utility (new file) |
| File | `ios/Tradeguruelectrical/Services/JWTDecoder.swift` |

Decodes JWT payload segment (base64url → JSON). Extracts `sub`, `email`, `name`, `picture`, `exp`. Returns `AuthUser` + expiration `Date`. No cryptographic validation (backend validates). Used to populate user profile without extra API call.

---

### Phase 3 — Core Auth Service (2 files)

#### 8. AuthManager

| Property | Value |
|----------|-------|
| Type | service (new file) |
| File | `ios/Tradeguruelectrical/Services/AuthManager.swift` |

`@Observable @MainActor class AuthManager`. Singleton pattern via static shared instance.

**Properties:**
- `var authState: AuthState = .anonymous`
- `var tier: UserTier = .free`
- `var currentUser: AuthUser?` (computed from authState)
- `var isAuthenticated: Bool` (computed)
- `private var tokens: AuthTokens?`

**Methods:**
- `signIn(provider: String?)` — Builds WorkOS authorize URL with PKCE. Opens `ASWebAuthenticationSession` with `tradeguru://auth-callback`. On callback: exchanges code for tokens via auth proxy, decodes JWT for user profile, stores tokens + user in Keychain, calls `linkDevice()`, sets authState to `.authenticated`.
- `signOut()` — Clears Keychain tokens + user. Calls backend session revoke if session_id available. Resets authState to `.anonymous`, tier to `.free`.
- `linkDevice()` — Calls `POST /device/link` with JWT + device_id. Extracts tier from response. Updates `self.tier`.
- `restoreSession()` — Called on app launch. Loads tokens from Keychain. If valid (not expired), restores authState. If expired, attempts refresh. If refresh fails, signs out.
- `refreshTokenIfNeeded()` — Checks expiresAt. If within 5 minutes, calls auth proxy with refresh_token grant. Updates stored tokens.

**OAuth URL format:**
```
https://api.workos.com/user_management/authorize
  ?client_id={WORKOS_CLIENT_ID}
  &redirect_uri=tradeguru://auth-callback
  &response_type=code
  &state={random}
  &code_challenge={challenge}
  &code_challenge_method=S256
  &provider={provider}  // GoogleOAuth, AppleOAuth, or omit for email
```

**Token exchange via proxy:**
```
POST https://tradeguru.com.au/api/workos-auth-proxy/user_management/authenticate
Content-Type: application/json
{
  "grant_type": "authorization_code",
  "client_id": "{WORKOS_CLIENT_ID}",
  "code": "{auth_code}",
  "code_verifier": "{verifier}"
}
```

#### 9. TokenRefresher (integrated into AuthManager)

Rather than a separate file, integrate refresh logic into AuthManager. Check on `scenePhase` change to `.active` in the app entry point.

**Add to TradeguruelectricalApp.swift:**
```swift
@Environment(\.scenePhase) private var scenePhase
// in body, add:
.onChange(of: scenePhase) { _, phase in
    if phase == .active {
        Task { await AuthManager.shared.refreshTokenIfNeeded() }
    }
}
```

---

### Phase 4 — Views (3 new files + 2 modifications)

#### 10. TierBadgeView

| Property | Value |
|----------|-------|
| Type | view (new file) |
| File | `ios/Tradeguruelectrical/Views/Settings/TierBadgeView.swift` |

Small pill: tier.displayName text, tier.color background at 15% opacity, tier.color text. Horizontal padding 10, vertical 4, cornerRadius 8. Font: .system(size: 12, weight: .semibold).

#### 11. SignInView

| Property | Value |
|----------|-------|
| Type | view (new file) |
| File | `ios/Tradeguruelectrical/Views/Settings/SignInView.swift` |

Sheet presented from SettingsView. Three buttons stacked:
- "Continue with Google" — `AuthManager.shared.signIn(provider: "GoogleOAuth")`
- "Continue with Apple" — `AuthManager.shared.signIn(provider: "AppleOAuth")`
- "Continue with Email" — `AuthManager.shared.signIn(provider: nil)` (WorkOS email flow)

Each button: full-width, 50pt height, rounded 12. Google = white bg + dark text, Apple = black bg + white text, Email = tradeGreen bg + white text. Loading state while OAuth in progress.

#### 12. SettingsView

| Property | Value |
|----------|-------|
| Type | view (new file) |
| File | `ios/Tradeguruelectrical/Views/Settings/SettingsView.swift` |

Presented as `.sheet` from ChatView nav bar gear button.

**Sections:**
1. **Account** — If signed in: avatar (AsyncImage from pictureURL, 60x60 circle), name, email, TierBadgeView, "Sign Out" button (red text). If anonymous: "Sign In" button → presents SignInView sheet.
2. **Data** — "Clear All Conversations" button (destructive, confirmation alert) → `viewModel.deleteAllConversations()` (new method, iterates modelContext delete).
3. **About** — App version (Bundle.main.infoDictionary), build number. "Terms of Service" and "Privacy Policy" as Link() to URLs.

#### 13. ChatView — Add gear button (modification)

| Property | Value |
|----------|-------|
| File | `ios/Tradeguruelectrical/ChatView.swift` |
| Location | navBar computed property |

Add `@State private var showSettings = false` state.
Add gear button in nav bar HStack between Spacer and new-chat button:
```swift
Button {
    showSettings = true
} label: {
    Image(systemName: "gearshape")
        .font(.system(size: 18))
        .foregroundStyle(Color.tradeText)
}
.frame(width: 44, height: 44)
```
Add `.sheet(isPresented: $showSettings) { SettingsView() }` on the VStack.

#### 14. ChatViewModel — Wire JWT into API calls (modification)

| Property | Value |
|----------|-------|
| File | `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` |

All `TradeGuruAPI.chat/chatVision/rate/feedback/speak/transcribe` calls currently pass `deviceId` but no JWT. Update `APIConfig.request()` calls to include `jwt: AuthManager.shared.tokens?.accessToken`. Since APIConfig.headers already handles the optional jwt param, this is a one-line change per call site — or add a helper:

```swift
private var currentJWT: String? {
    AuthManager.shared.isAuthenticated ? AuthManager.shared.tokens?.accessToken : nil
}
```

Then pass `jwt: currentJWT` in every API request builder.

---

### Phase 5 — Config + HTML (5 items)

#### 15. URL Scheme Registration

Add `tradeguru` URL scheme to the Xcode project. Since CLAUDE.md says no physical Info.plist, use `INFOPLIST_KEY_CFBundleURLTypes` in project.pbxproj build settings, or add the URL type entry to the existing entitlements/project config.

#### 16. WORKOS_CLIENT_ID

Add to APIConfig.swift:
```swift
static let workosClientId = "client_01..."  // From WorkOS Dashboard
static let authProxyURL = "https://tradeguru.com.au/api/workos-auth-proxy"
static let oauthRedirectURI = "tradeguru://auth-callback"
```

#### 17. WorkOS Dashboard — Register redirect URI

In WorkOS Dashboard → Redirects, add `tradeguru://auth-callback` as an allowed redirect URI. This is a manual step, not code.

#### 18. HTML — Settings component in picker

Add `<option value="settings">Settings</option>` to componentSelect dropdown.

#### 19. HTML — renderSettings() + nav bar gear

Add `renderSettings()` function showing:
- Mock signed-out state: "Sign In" button, app version
- Mock signed-in state: avatar placeholder, "Test User", "test@email.com", "Pro" badge, "Sign Out" button
- Toggle between states with a button in the settings view itself
- Add gear icon to `htmlNavBar()` function

Add `case 'settings': sc.innerHTML = renderSettings(); break;` to render() switch.

---

## Wired Items (no changes needed)

| # | Item | File | Status |
|---|------|------|--------|
| 1 | APIConfig.headers jwt param | Services/APIConfig.swift:14-21 | Already accepts optional jwt |
| 2 | DeviceManager Keychain | Services/DeviceManager.swift | Working Keychain pattern to reference |
| 3 | POST /device/link backend | Vercel (deployed) | Accepts JWT + device_id, returns tier |
| 4 | POST /device/unlink backend | Vercel (deployed) | Accepts JWT, unlinks device |
| 5 | Auth proxy backend | Vercel (deployed) | CORS bridge to WorkOS API |

---

## Implementation Order

| Phase | Items | Files | Dependency |
|-------|-------|-------|------------|
| 1 | Models (AuthState, AuthUser, AuthTokens, UserTier) | 4 new | None |
| 2 | Utilities (KeychainHelper, PKCEHelper, JWTDecoder) | 3 new | None |
| 3 | AuthManager | 1 new + 1 mod (TradeguruelectricalApp) | Phase 1 + 2 |
| 4 | Views (TierBadgeView, SignInView, SettingsView) + ChatView mod + ChatViewModel mod | 3 new + 2 mod | Phase 3 |
| 5 | Config (URL scheme, client ID, WorkOS dashboard) + HTML parity | 2 mod + manual | Phase 4 |

---

## Verification

- [ ] Tapping gear in nav bar opens Settings sheet
- [ ] Settings shows "Sign In" button when anonymous
- [ ] "Continue with Google" opens OAuth in system browser
- [ ] OAuth callback returns to app with JWT
- [ ] JWT stored in Keychain, survives app restart
- [ ] After sign-in, Settings shows user name, email, tier badge
- [ ] All API calls include `Authorization: Bearer {jwt}` header
- [ ] `POST /device/link` called after sign-in, tier updates
- [ ] "Sign Out" clears Keychain, returns to anonymous state
- [ ] Token auto-refreshes on app foreground when near expiry
- [ ] "Clear All Conversations" deletes all SwiftData conversations
- [ ] HTML preview: "Settings" component shows in picker
- [ ] HTML preview: gear icon in nav bar
- [ ] Mock mode (APIConfig.useMockData) still works for anonymous flow
