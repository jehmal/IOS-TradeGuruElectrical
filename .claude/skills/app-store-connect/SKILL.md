---
name: app-store-connect
description: Manage App Store Connect via the `asc` CLI — builds, TestFlight, app metadata, submissions, signing, analytics, and more.
---

# App Store Connect CLI Skill

You can help users manage their App Store Connect account using the `asc` CLI tool installed in the sandbox.

## Setup

**Before running any `asc` command, you MUST call the `setupAsc` tool first.** This fetches the user's API credentials from the database and configures the CLI.

- If `setupAsc` returns `{status: "ready"}`, you're good — run `asc` commands via `bash`.
- If `setupAsc` returns `{status: "no_api_key"}`, the user has no App Store Connect API key. Call the `connectAppleDeveloper` tool to prompt the user to set one up.
- If `setupAsc` returns `{status: "error"}`, something went wrong (e.g., backend not deployed, network issue). Tell the user about the error — do NOT call `connectAppleDeveloper` for errors.

You only need to call `setupAsc` once per conversation. After it succeeds, all `asc` commands will work.

## App Context

When `setupAsc` succeeds, it returns an `app` field with the project's App Store Connect app information (if the user has previously published this project):

- `app.ascAppId` — the numeric App Store Connect app ID (use this with `--app` flags)
- `app.bundleId` — the app's bundle identifier
- `app.appName` — the app name

If `app` is `null`, the user hasn't published this project yet. In that case, run `asc apps --output table` to list all apps in their account and ask which one they want to work with.

## CLI Basics

- **All output is JSON by default** (minified, one line). Use `jq` for parsing.
- Add `--output table` for human-readable display when showing results to the user.
- Add `--pretty` for indented JSON when debugging.
- Use `--paginate` to automatically fetch all pages of results.
- Use `--sort` to sort results (prefix `-` for descending).
- The CLI never prompts interactively — everything is flag-based.
- Use `--confirm` for destructive operations (expire, delete, submit).

## Resolving IDs

Most commands need IDs. Always resolve them explicitly — never assume or guess.

```bash
# App ID — by bundle ID or name
asc apps list --bundle-id "com.example.app"
asc apps list --name "My App"

# Build ID — latest for a version
asc builds latest --app "APP_ID" --version "1.2.3" --platform IOS

# Build ID — recent builds sorted by date
asc builds list --app "APP_ID" --sort -uploadedDate --limit 5

# Version ID
asc versions list --app "APP_ID" --paginate

# Beta group IDs
asc testflight beta-groups list --app "APP_ID" --paginate

# Beta tester IDs
asc testflight beta-testers list --app "APP_ID" --paginate
```

Always use `--paginate` on list commands to avoid missing results.

## Key Commands Reference

### Apps & Builds

```bash
# List all apps
asc apps --output table

# Get build details and processing state
asc builds info --build "BUILD_ID"

# Get the latest build for a version
asc builds latest --app "APP_ID" --version "1.2.3" --platform IOS

# Expire old builds (preview first with --dry-run)
asc builds expire-all --app "APP_ID" --older-than 90d --dry-run
asc builds expire-all --app "APP_ID" --older-than 90d --confirm
```

### End-to-End Release (Preferred)

Use `asc publish` for single-command release workflows instead of manual multi-step sequences.

```bash
# Upload + distribute to TestFlight in one step
asc publish testflight --app "APP_ID" --ipa "app.ipa" --group "GROUP_ID" --wait --notify

# Upload + submit to App Store in one step
asc publish appstore --app "APP_ID" --ipa "app.ipa" --version "1.2.3" --wait --submit --confirm
```

### Manual Release Steps (When More Control Is Needed)

```bash
# 1. Upload build
asc builds upload --app "APP_ID" --ipa "app.ipa" --wait

# 2. Find the build ID
asc builds latest --app "APP_ID"

# 3a. TestFlight: add build to group
asc builds add-groups --build "BUILD_ID" --group "GROUP_ID"

# 3b. App Store: attach build to version and submit
asc versions attach-build --version-id "VERSION_ID" --build "BUILD_ID"
asc submit create --app "APP_ID" --version "1.0.0" --build "BUILD_ID" --confirm
```

### TestFlight

```bash
# List beta groups
asc testflight beta-groups list --app "APP_ID"

# List beta testers
asc testflight beta-testers list --app "APP_ID"

# Add a tester to a group
asc testflight beta-testers add --app "APP_ID" --email "tester@example.com" --group "Beta"

# Remove a tester
asc testflight beta-testers remove --app "APP_ID" --email "tester@example.com"

# Create a beta group
asc testflight beta-groups create --app "APP_ID" --name "Beta Testers"

# Get beta feedback
asc feedback --app "APP_ID" --output table

# Get crash reports
asc crashes --app "APP_ID" --output table
```

### App Store Versions

```bash
# List versions
asc versions list --app "APP_ID"

# Create a new version
asc versions create --app "APP_ID" --version "2.0.0" --platform IOS

# Attach a build to a version
asc versions attach-build --version-id "VERSION_ID" --build "BUILD_ID"

# Release a pending version
asc versions release --version-id "VERSION_ID" --confirm
```

### App Metadata & Info

```bash
# Get app info
asc app-info get --app "APP_ID"

# Update app metadata for a locale
asc app-info set --app "APP_ID" --locale "en-US" --whats-new "Bug fixes and improvements"
asc app-info set --app "APP_ID" --locale "en-US" --description "My app description" --keywords "app,tool"

# Set categories
asc categories set --app "APP_ID" --primary GAMES --secondary ENTERTAINMENT

# Download/upload localizations
asc localizations download --version "VERSION_ID" --path "./localizations"
asc localizations upload --version "VERSION_ID" --path "./localizations"
```

### Customer Reviews

```bash
# List reviews
asc reviews --app "APP_ID" --output table

# Filter by rating
asc reviews --app "APP_ID" --stars 1

# Respond to a review
asc reviews respond --review-id "REVIEW_ID" --response "Thanks for your feedback!"
```

### Pre-submission Checks

Before submitting, always verify these:

```bash
# 1. Check build processing state — must be VALID
asc builds info --build "BUILD_ID"

# 2. Check encryption compliance
asc encryption declarations list --app "APP_ID"

# 3. Validate version metadata, screenshots, age rating
asc validate --app "APP_ID" --version-id "VERSION_ID"

# 4. Check submission status
asc submit status --version-id "VERSION_ID"
```

If `usesNonExemptEncryption` is true on the build, create an encryption declaration:

```bash
asc encryption declarations create \
  --app "APP_ID" \
  --app-description "Uses standard HTTPS/TLS" \
  --contains-proprietary-cryptography=false \
  --contains-third-party-cryptography=true \
  --available-on-french-store=true

asc encryption declarations assign-builds --id "DECLARATION_ID" --build "BUILD_ID"
```

### Submissions

```bash
# Submit for review
asc submit create --app "APP_ID" --version "1.0.0" --build "BUILD_ID" --confirm

# Check submission status
asc submit status --version-id "VERSION_ID"

# Cancel a submission
asc submit cancel --version-id "VERSION_ID" --confirm
```

### Screenshots & Previews

```bash
# List screenshots
asc screenshots list --version-localization "LOC_ID"

# Upload screenshots
asc screenshots upload --version-localization "LOC_ID" --path "./screenshots/" --device-type IPHONE_65

# List supported sizes
asc screenshots sizes
```

### Signing (Certificates, Profiles, Bundle IDs)

```bash
# List certificates
asc certificates list

# List provisioning profiles
asc profiles list

# List bundle IDs
asc bundle-ids list

# Fetch signing files
asc signing fetch --bundle-id "com.example.app" --profile-type IOS_APP_STORE --output "./signing"
```

### Subscriptions & In-App Purchases

```bash
# List subscription groups
asc subscriptions groups list --app "APP_ID"

# List subscriptions in a group
asc subscriptions list --group "GROUP_ID"

# List in-app purchases
asc iap list --app "APP_ID"

# Get pricing info
asc subscriptions pricing --app "APP_ID"
asc iap prices --app "APP_ID"
```

### Analytics & Sales

```bash
# Download sales report
asc analytics sales --vendor "VENDOR_NUMBER" --type SALES --subtype SUMMARY --frequency DAILY --date "2024-01-20" --decompress

# Create analytics report request
asc analytics request --app "APP_ID" --access-type ONGOING

# Get analytics data
asc analytics get --request-id "REQUEST_ID"
```

### Devices

```bash
# List registered devices
asc devices list --output table

# Register a new device
asc devices register --name "My iPhone" --udid "UDID" --platform IOS
```

### Xcode Cloud

```bash
# List workflows
asc xcode-cloud workflows --app "APP_ID"

# Trigger a build
asc xcode-cloud run --app "APP_ID" --workflow "CI Build" --branch "main" --wait

# Check build run status
asc xcode-cloud status --run-id "BUILD_RUN_ID"
```

## Publishing to App Store

When the user asks to publish their app, follow this flow. All data (appName, bundleId, version, platform, teamId) comes from the chat message payload — do NOT read app.json, Info.plist, or project.pbxproj for this data.

### Standard publishing flow

1. **Setup App Store Connect** — call `setupAsc` with the teamId from the publish payload.
   - If `status: "ready"` → proceed (App Store Connect configured, session is valid).
   - If `status: "session_expired"` → App Store Connect is configured but the Apple Developer session has expired. Call `connectAppleDeveloper` to re-authenticate, then call `setupAsc` again.
   - If `status: "no_api_key"` → call `connectAppleDeveloper` to set up authentication.

2. **Ensure certificate** — call `ensureCertificate({ teamId })`.
   - If `status: "ok"` → certificate is ready (stored server-side, you don't need to pass it to submitBuild).
   - If `status: "error"` with `code: "UNKNOWN_CERT_EXISTS"` → explain to user: a distribution certificate exists in their Apple account but we don't have its private key. They need to revoke it at https://developer.apple.com/account/resources/certificates/list and then try again. **STOP here.**
   - If `status: "error"` with `code: "CERT_LIMIT_REACHED"` → explain: their account has too many distribution certificates. They need to revoke one at https://developer.apple.com/account/resources/certificates/list. **STOP here.**
   - If `status: "error"` with `code: "NO_SESSION"` → explain: no valid Apple Developer session. They need to authenticate first.

3. **Ensure app** — call `ensureApp({ teamId, bundleId, appName, platform, version })`.

4. **Sync capabilities** — call `syncCapabilities({ teamId, bundleId, xcodeProjectPath })`. This reads the project entitlements and enables the matching capabilities in the Apple Developer Portal.
   - **For Swift projects**: same as `submitBuild` — if `rork.json` specifies a non-root `path` for the Swift app, pass it as `xcodeProjectPath`.

5. **Enable additional capabilities** (if needed) — use `asc` commands for standard capabilities:

   ```bash
   asc bundle-ids capabilities add --bundle-id "BUNDLE_ID" --capability CAPABILITY_TYPE
   ```

6. **Submit build** — call `submitBuild({ teamId, bundleId, appName, version, platform, xcodeProjectPath, ascAppId })`. The certificate is looked up server-side.
   - Pass `ascAppId` from the `ensureApp` result (step 3). This is needed for the automatic TestFlight invite after the build succeeds.
   - **For Swift projects**: before calling, read `rork.json` at the project root. If the Swift app entry has a `path` value other than `"."` (e.g. `"ios"`), pass it as `xcodeProjectPath`. This tells the build where to find the `.xcodeproj`/`.xcworkspace`.
   - `submitBuild` triggers the build and **waits for it to complete**. Build progress is streamed to the user in real-time. The tool returns the final result (success or failure) directly.
   - If the tool returns `{ status: "ok" }`, tell the user their app is on TestFlight.
   - If the tool returns `{ status: "error" }`, analyze the `message` and `details` fields and decide:
     - **Capability mismatch** → fix via `syncCapabilities` or `asc` commands, retry
     - **Metadata error** → fix the issue, retry
     - **Compilation error** → explain to user, do NOT retry (code issue)
     - **Certificate error** → explain to user, do NOT retry (must resolve manually)
   - `submitBuild` enforces a retry limit (default 3 attempts per version). If the limit is reached, it returns `RETRY_LIMIT_REACHED` — explain the persistent failure and stop.

7. **TestFlight invite** — after `submitBuild` returns `{ status: "ok" }`, do **not** manually create groups or add testers by default. The backend automatically sends the TestFlight invite using the `ascAppId` passed to `submitBuild`.
   - Treat the backend invite as the source of truth.
   - Tell the user the build was uploaded and the TestFlight invite is being handled automatically.
   - Only use `asc` CLI as a fallback if the user explicitly says the invite did not arrive or asks you to debug it.
   - If you need the fallback flow, first check for an existing internal group named `Internal Testing` and reuse it. Never create a new group blindly.

### Session errors

If ANY tool returns a session-related error (e.g., "Failed to restore session", "session expired", "NO_SESSION"), the Apple Developer session has expired. In this case:

1. Call `connectAppleDeveloper` to re-authenticate (the user will see a login widget).
2. After the user completes re-auth, retry the failed step.

This can happen even if `checkAppleSession` previously said "valid" — Apple's cookies expire independently of our records.

### Error recovery

- Only retry for infrastructure/configuration errors that you can fix (capabilities, metadata, session re-auth).
- Never retry for code compilation errors or certificate issues.
- After fixing an issue, call `submitBuild` again — it tracks attempt count automatically.
- If you've exhausted retries, explain what went wrong and suggest manual steps.

## Tips

- The `setupAsc` response includes the app ID for the current project — use it directly with `--app` flags instead of listing apps first.
- If the app context is not available, run `asc apps --output table` to find the right app ID.
- Use `--output table` when presenting results to the user for readability.
- Use JSON output (default) when you need to extract specific fields with `jq`.
- Always use `--paginate` on list commands to avoid missing results.
- Prefer `asc publish testflight` and `asc publish appstore` over manual multi-step sequences.
- Before submitting, always check build state is `VALID` (not processing) and run `asc validate`.
- When the user asks about "my app" and you have the app ID from `setupAsc`, use it directly. If they have multiple apps or no app context, list apps and confirm.
- For sorting: prefix field with `-` for descending order (e.g., `--sort -uploadedDate`).
- For long operations (upload, build processing): use `--wait`, `--poll-interval`, and `--timeout`.


## Current Status

No App Store Connect API key configured. The user needs to connect their Apple Developer account first.
