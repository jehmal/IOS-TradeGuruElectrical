# Plan: Add Unified Relay Endpoints to TradeGuru

## Task Description
Add new `/api/v1/*` relay endpoints to the existing TradeGuru Expo app at `C:\Users\jehma\Desktop\TradeGuru\expo-chatgpt-clone`. These endpoints centralise AI chat for iOS, Android, and Web clients behind a single API surface at `tradeguru.com/api/v1/*`. The relay proxies requests to OpenAI (using the Responses API), adds analytics logging, response caching, ratings collection, and platform segmentation. Existing endpoints (`assistants-*`, `stripe-*`, `user-*`, etc.) must NOT be modified or broken.

## Objective
When this plan is complete, the TradeGuru backend will have:
1. A new `/api/v1/chat` endpoint that proxies to OpenAI Responses API with SSE streaming
2. A new `/api/v1/chat/vision` endpoint for photo analysis
3. A new `/api/v1/audio/transcribe` endpoint proxying to `gpt-4o-transcribe`
4. A new `/api/v1/audio/speech` endpoint proxying to `gpt-4o-mini-tts`
5. A new `/api/v1/files/upload` endpoint proxying to OpenAI Files API
6. A new `/api/v1/rating` and `/api/v1/feedback` endpoint for response quality data
7. A new `/api/v1/device/register` endpoint for anonymous device registration
8. Analytics logging on every request (question, mode, tokens, category, region, platform, latency)
9. Response caching via existing Upstash Redis
10. An admin stats endpoint at `/api/v1/admin/stats`
11. A new `/api/webhook/revenuecat` endpoint for RevenueCat server-to-server subscription events
12. Unified tier checking that works for both Stripe (web) and RevenueCat (iOS/Android) users
13. All existing endpoints untouched and working

## Problem Statement
The iOS Swift app and future Android app need a centralised API that:
- Hides the OpenAI API key (server-side only)
- Returns the standardised `{ blocks: [...] }` structured JSON contract
- Collects analytics per user, region, electrical category, and platform
- Caches frequent answers to reduce costs
- Allows swapping the AI engine without client changes
- Reuses existing auth (WorkOS), rate limiting (Upstash Redis), and infrastructure (Vercel)

## Solution Approach
Add new Vercel Serverless Functions under `api/v1/` that sit alongside (not replace) existing endpoints. Reuse the existing `utils/` infrastructure:
- `utils/auth/workOSTokenVerifier.ts` — JWT auth (already works)
- `utils/vercel-kv.ts` — Redis for caching + analytics counters (already works)
- `utils/api/rateLimit.ts` — Rate limiting (already works)
- `utils/api/corsHeaders.ts` — CORS (needs iOS origin added)

New utilities are added under `utils/v1/` to avoid touching existing code. New types go in `types/v1/`.

## Security Model

### Device Authentication
- Device registration is rate-limited to **5 registrations per IP per hour** via existing `rateLimit.ts` (key: `device-reg:{ip}`)
- Device fingerprint hash: `SHA-256(platform + "|" + os_version + "|" + app_version + "|" + locale + "|" + random_salt)` where `random_salt` is a 16-byte crypto-random hex string generated at registration time. The salt is stored with the device record but NOT returned to the client. This ensures two users with identical devices get different device_ids (fingerprint is NOT used for dedup — it's for audit/abuse detection only)
- Each registration always creates a new device_id (UUID v4). Note: Because the fingerprint includes a random salt, each registration produces a unique fingerprint. Abuse detection relies on **IP-based rate limiting** (5 registrations per IP per hour) and **behavioral analysis** (e.g., many device_ids making identical queries), NOT on fingerprint matching. The fingerprint is stored for forensic/audit purposes only.
- Note: `app_version` is included in fingerprint for abuse detection only — it does NOT affect device_id generation (each registration creates a new UUID regardless of fingerprint). App updates do not invalidate existing device_ids.
- Device-id auth is a **lower trust tier** than WorkOS JWT. Device-only users get Free tier limits only. To unlock paid tiers, a device must be linked to a WorkOS user via `/api/v1/device/link`
- Device TTL (365 days) is **refreshed on every authenticated API call** via `EXPIRE` command to prevent active users from losing their device

### Device-to-User Linking
- New endpoint: `/api/v1/device/link` — POST `{ device_id }` with WorkOS JWT in Authorization header
- Links the device_id to the authenticated WorkOS user_id in Redis: `device:user:{device_id}` → `{user_id}`
- After linking, the device inherits the user's subscription tier
- A user can link up to 5 devices. A device can only be linked to one user at a time
- Re-linking to a different user requires unlinking first via `/api/v1/device/unlink`

### Tier Enforcement
| Tier | Chat/day | Vision/day | Audio/day | File uploads/day | Max file size |
|------|----------|------------|-----------|------------------|---------------|
| Free | 10 | 3 | 5 | 2 | 5 MB |
| Pro | 100 | 30 | 50 | 20 | 25 MB |
| Unlimited | 1000 | 200 | 500 | 100 | 50 MB |

Tier limits are enforced in `auth-middleware.ts` using Redis counters: `usage:{identity}:{endpoint}:{YYYY-MM-DD}` where date is UTC. TTL is set via `EXPIREAT` to midnight UTC of the NEXT day (e.g., key `usage:uid:chat:2026-03-14` expires at `2026-03-15T00:00:00Z`). This ensures daily quotas reset at a consistent time regardless of when the first request was made. The `EXPIREAT` is set on first `INCR` only (subsequent INCRs don't reset TTL).

### RevenueCat Security
- Webhook auth uses `REVENUECAT_WEBHOOK_AUTH_KEY` env var (shared secret set in RevenueCat dashboard)
- All tier change operations use Redis Lua scripts for atomicity: dedup key check + tier SET + timestamp SET execute as a single atomic operation
- Event dedup key is `revenuecat:event:{transaction_id}:{event_type}` if `transaction_id` is present, or `revenuecat:event:{app_user_id}:{event_type}:{event_timestamp_ms}:{product_id || 'none'}` if `transaction_id` is null (product_id adds uniqueness for same-millisecond events). Uses **90-day TTL** (matching analytics retention) to balance replay protection with Redis memory management — no cleanup job needed. Events older than 90 days that replay are extremely unlikely (RevenueCat retries for max ~3 hours).
- Webhook events are processed with `event_timestamp_ms` ordering: if a stored `last_event_ts` for the user is newer than the incoming event, the event is skipped (prevents out-of-order processing)
- `REVENUECAT_SKIP_SANDBOX` env var (default: `true` in production) controls whether SANDBOX events are processed

### Entitlement-to-Tier Mapping
| RevenueCat Entitlement ID | Tier |
|---------------------------|------|
| `pro_access` | `pro` |
| `unlimited_access` | `unlimited` |
| (no active entitlement) | `free` |

This mapping is defined as a constant in `utils/v1/revenuecat-webhook.ts`. Adding new tiers requires a code deploy.

If a user has multiple active entitlements, **highest tier wins**: `unlimited` > `pro` > `free`. The mapping function iterates `entitlement_ids` array, resolves each to a tier, and returns the highest.

### Redis Key Namespace
All new v1 keys use these prefixes to avoid collision with existing endpoints:
| Prefix | Example | TTL | Purpose |
|--------|---------|-----|---------|
| `cache:` | `cache:{hash}:{mode}:usr:{uid}` | 24h | Response cache |
| `tts:` | `tts:{hash}` | 24h | TTS audio cache |
| `device:` | `device:{device_id}` | 365d (refreshed) | Device record |
| `device:user:` | `device:user:{device_id}` | 365d (refreshed) | Device-to-user link |
| `user:devices:` | `user:devices:{user_id}` | none | User's linked device SET |
| `user:subscription:tier:stripe:` | `user:subscription:tier:stripe:{uid}` | none | Stripe subscription tier |
| `user:subscription:tier:revenuecat:` | `user:subscription:tier:revenuecat:{uid}` | none | RevenueCat subscription tier |
| `user:subscription:last_event_ts:` | `user:subscription:last_event_ts:{uid}` | none | Last RevenueCat event timestamp (integer) |
| `revenuecat:event:` | `revenuecat:event:{txn_id}:{type}` | 90d | Webhook dedup |
| `usage:` | `usage:{uid}:chat:2026-03-14` | 24h | Tier usage counters |
| `analytics:` | `analytics:2026-03-14:{req_id}` | 90d | Request analytics |
| `idempotency:` | `idempotency:{key}` | 1h | Request idempotency |
| `device-reg:` | `device-reg:{ip}` | 1h | Registration rate limit |
| `rating:` | `rating:{response_id}:{user_or_device_id}` | none | Per-user response ratings |
| `feedback:` | `feedback:{response_id}:{user_or_device_id}` | none | Per-user response feedback |

All keys above use prefixes that do NOT conflict with existing `assistants-*`, `stripe-*`, `user-*` key patterns.

## Error Handling

### Streaming Error Protocol
All streaming endpoints follow this error contract:

1. **Pre-stream errors** (auth failure, validation, rate limit): Return standard HTTP error response `{ error: { code, message } }` with appropriate status code (401, 400, 429)
2. **Mid-stream errors** (OpenAI failure, timeout, connection drop): Send an SSE error event before closing:
   ```
   event: error\ndata: {"code": "STREAM_INTERRUPTED", "message": "...", "partial": true}\n\n
   ```
3. **Stream completion**: Send a final SSE event:
   ```
   event: done\ndata: {"response_id": "...", "usage": {"input_tokens": N, "output_tokens": N}, "cached": false}\n\n
   ```
4. **Client detection**: Client knows stream is complete when it receives `event: done`. If connection drops without `done`, client should retry with same `idempotency_key`

### OpenAI Proxy Error Handling
- **429 (Rate Limit)**: Return 429 to client with `Retry-After` header from OpenAI response
- **500/503 (Server Error)**: Retry once with 1s delay. If still failing, return 502 to client
- **Timeout (no response in 30s)**: Abort, return 504 to client
- **Invalid API Key**: Return 503 with `"AI service temporarily unavailable"` (don't leak internal details)
- **Partial stream failure**: Do NOT cache partial responses. If stream fails mid-way, discard buffer. Client receives `event: error` and must retry. Only fully completed responses (after `event: done`) are cached.
- **Cache write timing**: Cache write happens ONLY inside the `event: done` handler, after the done event is successfully sent to the client and `finish_reason` is `"stop"`. Responses with `finish_reason: "length"` (truncated) or `"content_filter"` (filtered) are NOT cached.

### Category Classifier Fallback
- Keyword match → if score >= 0.6, use result. Score is a float in range [0.0, 1.0] representing keyword match confidence. Type: `number` in CategoryClassification type.
- If keyword match fails or score < 0.6, classify as `"other"` for the **synchronous response** (do NOT block on GPT-4o-mini)
- **Background reclassification**: after the response is sent AND cached, if category is `"other"`, fire-and-forget a GPT-4o-mini classification call. Implementation: use Vercel Edge Runtime's `waitUntil()` API to run the classification after the response stream ends. The function stays alive until the background work completes, but the client connection is already closed. On success, update the analytics entry's category field. Do NOT update pre-aggregated `stats:category:*` counters (they reflect initial classification only). Do NOT update the cache entry's category (cache is keyed by content, not category). Hard timeout: 5 seconds. If GPT-4o-mini does not respond within 5s, abort the classification and leave category as "other". This prevents `waitUntil()` from keeping Edge Functions alive under load. Add counter `stats:category_fallback_errors:{date}` (90-day TTL) incremented on timeout/failure for observability in admin stats.
- If background classification fails (timeout, rate limit, error), log to console.error and leave category as `"other"`. No retry.
- This means: GPT-4o-mini IS called for `"other"` categories, but only as a non-blocking background job for analytics enrichment — it never delays the user response.

### Analytics Durability
- Analytics logger uses `request_id` (UUID generated per request) as idempotency key
- Redis key: `analytics:{date}:{request_id}` — if key exists, skip logging (prevents double-count on retry)
- Analytics entries have 90-day TTL in Redis
- If Redis write fails, log to `console.error` for Vercel log drain — analytics are best-effort, never block responses

### Request Idempotency
- Clients SHOULD send `X-Idempotency-Key` header (UUID format, max 64 chars) on mutating requests (chat, vision, audio, file upload)
- Server stores `idempotency:{key}` → `{response_json}` in Redis with **1-hour TTL**
- If a request arrives with an idempotency key that already exists, return the stored response immediately (no OpenAI call, no analytics log)
- If `X-Idempotency-Key` header is present but not a valid string (empty or >64 chars), return 400. Any non-empty string ≤64 chars is accepted (UUID format recommended but not enforced).
- If no `X-Idempotency-Key` header is provided, request is processed normally (no dedup)
- Analytics dedup uses the `X-Idempotency-Key` if present, otherwise falls back to server-generated `request_id`. This means: retries WITH idempotency key are properly deduped; retries WITHOUT idempotency key may double-count (acceptable for MVP)
- Maximum stored response size: 256KB. If response exceeds this, do NOT store in idempotency cache (retry will re-execute the request). This prevents Redis memory exhaustion from large vision/chat responses.
- For concurrent requests with the same idempotency key (before first completes): server stores `idempotency:{key}` → `"pending"` immediately on first request. Subsequent requests with same key that find `"pending"` return HTTP 409 `{ error: { code: "REQUEST_IN_PROGRESS" } }`. On completion, value is replaced with response JSON. On error, key is deleted (allows retry).
- For responses exceeding 256KB (common with vision), store a **minimal idempotency record**: `{ response_id, status: "completed", size: N }` (< 1KB). On retry with same key, return `{ error: { code: "ALREADY_PROCESSED", response_id: "resp_..." } }` with HTTP 409 Conflict. Client uses response_id to confirm the original request succeeded. This prevents double-billing for expensive vision calls without storing the full response. The 409 response includes the response_id. If the client needs the actual response content, it should NOT use idempotency keys for vision requests (since vision responses are unique and uncacheable). Idempotency for vision is opt-in — clients that don't send `X-Idempotency-Key` get normal retry behavior.

## Relevant Files

### Existing Files (DO NOT MODIFY unless noted)

- `api/assistants-runs.ts` — Existing streaming endpoint. Reference for SSE patterns. **DO NOT MODIFY.**
- `api/assistants-threads.ts` — Existing thread creation. **DO NOT MODIFY.**
- `utils/auth/workOSTokenVerifier.ts` — JWT verification. Reuse as-is.
- `utils/vercel-kv.ts` — Redis client + helpers. Reuse as-is.
- `utils/api/rateLimit.ts` — Rate limiting. Reuse as-is.
- `utils/api/corsHeaders.ts` — CORS headers. **MODIFY: add iOS app origin to allowlist.**
- `utils/api/threadOwnership.ts` — Thread ownership. Reference pattern for device ownership.
- `vercel.json` — **MODIFY: add function config for new streaming endpoints.**
- `package.json` — **MODIFY: add `@vercel/postgres` dependency if using Vercel Postgres for analytics.**

### New Files

#### Types (`types/v1/`)
- `types/v1/blocks.ts` — ContentBlock type definitions (the cross-platform JSON contract)
- `types/v1/analytics.ts` — ChatLogEntry, Rating, Feedback, Device types
- `types/v1/request.ts` — V1ChatRequest, V1VisionRequest, V1RatingRequest, V1FeedbackRequest
- `types/v1/category.ts` — ElectricalCategory enum + CategoryClassification
- `types/v1/revenuecat.ts` — RevenueCat webhook event types, payload shape, subscription tier mapping

#### Utilities (`utils/v1/`)
- `utils/v1/openai-responses.ts` — OpenAI Responses API proxy (streams SSE, extracts tokens)
- `utils/v1/openai-audio.ts` — OpenAI Audio API proxy (STT + TTS)
- `utils/v1/openai-files.ts` — OpenAI Files API proxy
- `utils/v1/openai-images.ts` — OpenAI Images API proxy
- `utils/v1/system-prompt-builder.ts` — Builds system prompt per ThinkingMode
- `utils/v1/category-classifier.ts` — Auto-classifies questions into electrical categories
- `utils/v1/category-keywords.ts` — Keyword map for fast classification
- `utils/v1/response-cache.ts` — Redis-based response cache. Cache key: `cache:{question_hash}:{mode}:{identity_key}` where `identity_key` is always `usr:{user_id}` for JWT-authenticated requests or `dev:{device_id}` for device-only requests. The prefix (`usr:` / `dev:`) prevents ID collisions. After device linking, requests using that device's ID resolve to `usr:{linked_user_id}` for cache key construction (so linked devices share cache with user). Stores `{ blocks: [...], token_usage: {...}, created_at, category }`. Never caches partial/errored responses. Provides `invalidateByPattern(pattern)` for admin cache purge.
**Cache invalidation pattern validation:**
- Pattern MUST start with `cache:` prefix
- Pattern is split by `:` into segments. Segments after `cache:` are evaluated
- At least ONE segment must be a literal (does not contain `*` or `?` wildcards)
- Examples:
  - `cache:*:fault_finder:usr:abc123` → literals: `fault_finder`, `usr`, `abc123` → ALLOWED
  - `cache:*:learn:*` → literal: `learn` → ALLOWED
  - `cache:*:*:usr:abc123` → literal: `usr`, `abc123` → ALLOWED (clears all modes for one user)
  - `cache:abc123hash:*:*` → literal: `abc123hash` → ALLOWED (clears one question hash)
  - `cache:*` → no literals → REJECTED
  - `cache:*:*` → no literals → REJECTED
  - `cache:*:*:*` → no literals → REJECTED
  - `cache:*:*:*:*` → no literals → REJECTED
- Maximum 10,000 keys deleted per request. If pattern matches more, operation returns `{ deleted: 10000, truncated: true }`.
- `utils/v1/analytics-logger.ts` — Async analytics logging to Redis (non-blocking)
- `utils/v1/cost-calculator.ts` — Token → USD cost calculation
- `utils/v1/question-hasher.ts` — SHA-256 hash for cache key generation. For single-message requests: hash normalised question text. For multi-message conversations: hash a canonical JSON representation of the messages array. Each message is reduced to `{role, content}` (strip extra fields), keys are sorted alphabetically, then `JSON.stringify()` produces a deterministic string. Messages maintain their array order (do NOT sort messages — order matters for context). Normalisation per message: lowercase, trim whitespace, collapse multiple spaces, remove trailing punctuation. Combined with mode to form cache key.
- `utils/v1/region-detector.ts` — Extract country/region from Vercel headers
- `utils/v1/device-manager.ts` — Anonymous device registration + tracking
- `utils/v1/auth-middleware.ts` — Unified auth for v1 routes (WorkOS JWT OR device-id header)
- `utils/v1/revenuecat-webhook.ts` — RevenueCat webhook verification + event processing
- `utils/v1/subscription-tier.ts` — Unified tier resolver: checks Redis for tier from either Stripe (web) or RevenueCat (iOS/Android)

#### API Routes (`api/v1/`)
- `api/v1/chat.ts` — Main chat relay (streaming SSE via Responses API)
- `api/v1/chat/vision.ts` — Photo analysis relay
- `api/v1/audio/transcribe.ts` — STT proxy
- `api/v1/audio/speech.ts` — TTS proxy
- `api/v1/files/upload.ts` — File upload proxy
- `api/v1/rating.ts` — Submit response rating (1-5 stars)
- `api/v1/feedback.ts` — Flag response (wrong/unsafe/unhelpful)
- `api/v1/device/register.ts` — Anonymous device registration
- `api/v1/device/link.ts` — Link device to WorkOS user (requires JWT)
- `api/v1/device/unlink.ts` — Unlink device from user (requires JWT)
- `api/v1/admin/stats.ts` — Analytics JSON endpoint (admin only)
- `api/webhook/revenuecat.ts` — RevenueCat server-to-server webhook endpoint

## Implementation Phases

### Phase 1: Foundation
- Create type definitions (blocks, analytics, request shapes, categories)
- Create utility functions (hasher, cost calculator, region detector, category keywords)
- Add iOS origin to CORS allowlist
- Update vercel.json for new streaming routes

### Phase 2: Core Implementation
- Build the OpenAI Responses API proxy (streaming + non-streaming)
- Build the system prompt builder (per-mode prompts)
- Build response cache layer
- Build analytics logger
- Build category classifier
- Build device manager
- Build auth middleware for v1 routes
- Create all 9 API route files

### Phase 3: Integration & Polish
- Wire caching into chat route
- Wire analytics into all routes
- Add admin stats endpoint
- Test streaming with curl
- Test caching behaviour
- Verify existing endpoints still work

## Team Orchestration

- You operate as the team lead and orchestrate the team to execute the plan.
- Two teams work in parallel: Team Backend-Foundation builds types + utilities, Team Backend-Routes builds API routes + wiring.
- You NEVER write code directly. You use `Task` and `Task*` tools to deploy team members.

### Team Members

- Builder
  - Name: foundation-builder
  - Role: Creates all type definitions, utility functions, and shared infrastructure under `types/v1/` and `utils/v1/`. Does NOT create API routes.
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: routes-builder
  - Role: Creates all API route files under `api/v1/`, updates `vercel.json`, updates CORS. Depends on foundation-builder completing types and utilities first.
  - Agent Type: general-purpose
  - Resume: true

- Builder
  - Name: validator
  - Role: Validates all new files compile, existing endpoints still work, and streaming functions correctly.
  - Agent Type: general-purpose
  - Resume: false

## Step by Step Tasks

### 1. Create Type Definitions
- **Task ID**: create-types
- **Depends On**: none
- **Assigned To**: foundation-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can start immediately)
- Create `types/v1/blocks.ts` — define all 12 ContentBlock types matching the JSON contract in `todo/inventory/1-ai-chat-endpoints.md`
- Create `types/v1/category.ts` — `ElectricalCategory` enum (wiring, rcd, breaker, earthing, lighting, switchboard, motor, solar, ev_charger, compliance, tools, theory, other) + `CategoryClassification` type
- Create `types/v1/analytics.ts` — `ChatLogEntry`, `Rating`, `Feedback`, `Device`, `TokenUsage`, `CostBreakdown`, `ResponseMetadata` types
- Create `types/v1/request.ts` — `V1ChatRequest` (messages, mode, device_id, platform), `V1VisionRequest`, `V1RatingRequest` (response_id, stars: integer 1-5, comment), `V1FeedbackRequest` (response_id, reason: 'wrong' | 'unsafe' | 'unhelpful' | 'outdated', detail), `V1DeviceRegisterRequest`, `V1DeviceLinkRequest`. Platform enum: `'ios' | 'android' | 'web'`. ThinkingMode enum: `'fault_finder' | 'learn' | 'research'`. ResponseId format: `resp_{timestamp}_{random8chars}`.

### 2. Create Core Utilities
- **Task ID**: create-utilities
- **Depends On**: create-types
- **Assigned To**: foundation-builder
- **Agent Type**: general-purpose
- **Parallel**: false (needs types first)
- Create `utils/v1/question-hasher.ts` — SHA-256 of normalised question + mode
- Create `utils/v1/cost-calculator.ts` — token count → USD. Pricing table: gpt-4o $2.50/$10 per 1M, gpt-4o-transcribe, gpt-4o-mini-tts, gpt-image-1.5
- Create `utils/v1/region-detector.ts` — extract country/region from Vercel `x-vercel-ip-country`, `x-vercel-ip-city`, `x-vercel-ip-timezone` headers. If Vercel geo headers are missing (local dev, proxy), defaults to `country: 'XX'`, `region: 'unknown'`.
- Create `utils/v1/category-keywords.ts` — keyword map for 13 electrical categories
- Create `utils/v1/category-classifier.ts` — keyword match first (free, fast), GPT-4o-mini fallback for ambiguous queries (async, cheap)
- Create `utils/v1/response-cache.ts` — Redis GET/SET with composite key `cache:{question_hash}:{mode}:{identity_key}` where `identity_key` is `usr:{user_id}` or `dev:{device_id}`, 24h TTL, hit counting. Stores `{ blocks: [...], token_usage: {...}, created_at, category }`. Never cache partial/errored responses. Provides `invalidateByPattern(pattern)` for admin cache purge (pattern validation: must start with `cache:` AND must contain at least one literal segment after `cache:`. Maximum 10,000 keys deleted per request).
- **Cache key migration on device link**: When a device is linked to a user, existing cache entries under `dev:{device_id}` are NOT migrated to `usr:{user_id}`. These entries naturally expire (24h TTL). This means the first requests after linking may be cache misses. This is an acceptable MVP trade-off — migrating cache keys adds complexity for minimal benefit since cache has only 24h TTL anyway.
- Create `utils/v1/analytics-logger.ts` — async fire-and-forget logging to Redis. Non-blocking to response. Uses `request_id` as idempotency key (`analytics:{date}:{request_id}`). Each entry stores: question (truncated to 200 chars), mode, tokens_in, tokens_out, cost_usd, category, region, platform, device_id, user_id (if linked), latency_ms, cached (bool), response_id, timestamp. Analytics keys have 90-day TTL. If Redis write fails, log to console.error only — never block response.
- Create `utils/v1/device-manager.ts` — register device (store in Redis), track first/last seen, query count
- Create `utils/v1/auth-middleware.ts` — accept WorkOS JWT (web) OR device-id header (iOS/Android). The device ID header name is `X-Device-ID` (case-insensitive header matching per HTTP spec). Value must be a valid UUID v4 format. Validate device exists in Redis. Rate limit per user/device. On every device-authenticated request (X-Device-ID header present), auth-middleware calls `EXPIRE device:{device_id} 31536000` AND `EXPIRE device:user:{device_id} 31536000` (if link exists) to refresh both TTLs simultaneously. Note: Device TTL is refreshed ONLY on successful requests (non-429, non-error responses). If a request is rejected by tier limits (429), the device TTL is NOT refreshed. This prevents abuse where an attacker spams rate-limited requests to keep expired devices alive indefinitely. Implementation: move the `EXPIRE` call from auth-middleware to the success path (after Lua quota check returns 'ok').

### 3. Create System Prompt Builder
- **Task ID**: create-prompt-builder
- **Depends On**: create-types
- **Assigned To**: foundation-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside create-utilities)
- Create `utils/v1/system-prompt-builder.ts`
- Three mode prompts: Fault Finder (diagnosis-focused, safety-first, structured steps), Learn (educational, theory-focused, progressive), Research (reference-focused, specs, regulations)
- All prompts include the structured JSON output instruction: return `{ blocks: [...] }` with the 12 block types
- Include the block type schema definitions in the prompt so the AI knows the contract

### 4. Create OpenAI Proxy Utilities
- **Task ID**: create-openai-proxies
- **Depends On**: create-types
- **Assigned To**: foundation-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside create-utilities)
- Create `utils/v1/openai-responses.ts` — POST to `/v1/responses` with streaming. Transform SSE stream. Extract token usage. Handle errors.
- Create `utils/v1/openai-audio.ts` — POST multipart to `/v1/audio/transcriptions` (model: gpt-4o-transcribe) and POST to `/v1/audio/speech` (model: gpt-4o-mini-tts, voice: nova, format: aac)
- Create `utils/v1/openai-files.ts` — POST multipart to `/v1/files` (purpose: `user_data` for model inputs per current OpenAI guidance; fallback to `assistants` if `user_data` is rejected)
- Create `utils/v1/openai-images.ts` — POST to `/v1/images/generations` (model: gpt-image-1.5)
- All proxies read `OPENAI_API_KEY` from env (already exists on Vercel)
- All OpenAI proxy functions have explicit timeouts: chat/vision 30s, transcription 60s, TTS 30s, file upload 60s, image generation 60s.

### 5. Update CORS and Vercel Config
- **Task ID**: update-config
- **Depends On**: none
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can start immediately)
- In `utils/api/corsHeaders.ts`: add `https://tradeguru.com` to `ALLOWED_ORIGINS` array. Keep all existing origins. Note: native iOS/Android apps using URLSession/OkHttp do NOT send Origin headers, so CORS is only relevant for web clients. Do NOT add `capacitor://` or `ionic://` origins — this is a native Swift app, not a webview app. Also ensure `Access-Control-Allow-Headers` includes `X-Device-ID` and `X-Idempotency-Key` custom headers.
- In `vercel.json`: add `functions` entries for new streaming endpoints with `maxDuration: 120`:
  - `"api/v1/chat.ts": { "maxDuration": 120 }`
  - `"api/v1/chat/vision.ts": { "maxDuration": 120 }`
  - `"api/v1/audio/transcribe.ts": { "maxDuration": 60 }`
  - `"api/v1/audio/speech.ts": { "maxDuration": 60 }`
- In `vercel.json`: add CSP `connect-src` entry for the iOS app if needed

### 6. Create Device Registration Route
- **Task ID**: create-device-route
- **Depends On**: create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: false
- Create `api/v1/device/register.ts`
- POST handler: accepts `{ platform, os_version, app_version, locale, timezone }`
- Rate limit: 5 registrations per IP per hour (key: `device-reg:{ip}`)
- Generate `device_fingerprint` hash: `SHA-256(platform + "|" + os_version + "|" + app_version + "|" + locale + "|" + random_salt)` with 16-byte crypto-random salt
- Always generate a new UUID v4 device_id (fingerprint is NOT used for dedup — stored for abuse detection only)
- Store device record in Redis via device-manager with 365-day TTL (salt stored in record, not returned to client)
- Returns `{ device_id }` — client persists to Keychain
- No auth required (this IS the registration step)
- Extract region from Vercel headers automatically
- Validate `platform` is one of: `ios`, `android`, `web` (reject unknown platforms)

### 6b. Create Device Link/Unlink Routes
- **Task ID**: create-device-link
- **Depends On**: create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside device registration)
- Create `api/v1/device/link.ts` — POST `{ device_id }` with WorkOS JWT
  - Verify device_id exists in Redis
  - Extract user_id from JWT
  - Check user has < 5 linked devices
  - Store `device:user:{device_id}` → `{user_id}` in Redis with same TTL as device record (365 days, refreshed on use)
  - Store `user:devices:{user_id}` → SET of device_ids in Redis (no TTL — user account is permanent)
  - All device linking operations happen in a SINGLE Lua script (no MULTI/EXEC):
    1. `EXISTS device:{device_id}` → if false, return `"device_not_found"`
    2. `GET device:user:{device_id}` → if exists and != user_id, return `"linked_to_other_user"`. If exists and == user_id, return `"already_linked"` (idempotent)
    3. `SMEMBERS user:devices:{user_id}` → get all device IDs in SET
    4. For each member: `EXISTS device:{member_id}` → if false, `SREM user:devices:{user_id} {member_id}` (clean up expired devices)
    5. `SCARD user:devices:{user_id}` → if >= 5 (after cleanup), return `"device_limit_exceeded"` (HTTP 400)
    6. `SET device:user:{device_id} {user_id}`
    7. `EXPIRE device:user:{device_id} 31536000` (365 days)
    8. `SADD user:devices:{user_id} {device_id}`
    9. Return `"linked"`
  - Redis Lua scripts execute atomically — no concurrent requests can interleave. This guarantees device limit enforcement is race-free.
  - Hard cap: if `SCARD > 50` before cleanup, return `"device_set_corrupted"` (prevents unbounded SMEMBERS iteration in Lua).
  - If device is already linked to a different user, return `{ error: { code: 'DEVICE_LINKED_TO_OTHER', message: 'Device is linked to another account. Unlink it from that account first.' } }` with HTTP 409 Conflict.
  - Returns `{ linked: true, tier: "pro" }` with current subscription tier
- Create `api/v1/device/unlink.ts` — POST `{ device_id }` with WorkOS JWT
  - Verify the device is linked to the authenticated user (not someone else's device)
  - Remove `device:user:{device_id}` and remove from `user:devices:{user_id}` SET
  - Returns `{ unlinked: true }`
  - If device is not linked to any user, return `{ unlinked: true }` (idempotent, no error)
  - If device is linked to a different user than the authenticated one, return 403 Forbidden
  - After unlinking, the device reverts to Free tier on next request. Usage counters are keyed by identity (`usr:{user_id}` when linked, `dev:{device_id}` when unlinked). After unlinking, new requests use `dev:{device_id}` counter (fresh or existing). The old `usr:{user_id}` counter is not affected — if user re-links, they continue from their existing user quota. This prevents quota reset exploits (link → use 100 → unlink → re-link for another 100).

### 7. Create Chat Relay Route
- **Task ID**: create-chat-route
- **Depends On**: create-openai-proxies, create-prompt-builder, create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: false
- Create `api/v1/chat.ts` — Edge Runtime for streaming
- POST handler flow:
  1. Generate `request_id` (UUID) and read `X-Idempotency-Key` header if present
  2. Authenticate via auth-middleware (WorkOS JWT or device-id). Extract user_id or device_id. Resolve subscription tier via `getSubscriptionTier()`. **Read-only check**: `GET usage:{identity}:chat:{date}` — if count >= tier limit, return 429 immediately (no increment). This is a fast pre-check to reject obvious over-limit requests.
  3. Parse and validate `V1ChatRequest` (messages, mode, device_id, platform). Validate `platform` enum, `mode` enum, messages array non-empty. Maximum 100 messages in the messages array. Return 400 if exceeded. Maximum 10,000 characters per individual message content field. These limits prevent context window exhaustion and excessive hashing costs.
  4. Check response cache (question hash + mode + user_or_device_id)
  5. If cache hit: check tier limits via atomic Lua script: `local count = redis.call('INCR', usage_key); if count > limit then redis.call('DECR', usage_key); return 'exceeded' end; return 'ok'`. This is ONE atomic Redis operation — concurrent requests cannot bypass the limit because Lua scripts block other commands during execution. If limit exceeded, return 429. Otherwise stream cached blocks as SSE: emit each block as `event: block\ndata: {...}\n\n`, then `event: done` with `cached: true`. This ensures clients use a single SSE parser for all responses.
     Note: If the request is interrupted after INCR but before SSE completes, the user loses one quota unit. This is acceptable for MVP — the alternative (two-phase commit) adds complexity for a rare edge case. Free tier users losing 1 of 10 daily requests to network interruption is tolerable.
     Cache hits MUST go through auth-middleware first (step 2 in chat flow) — do NOT short-circuit auth for cached responses. This ensures device TTL refresh and tier enforcement on every request.
  6. If cache miss: increment usage counter atomically via same Lua script as step 5 (`INCR` → check limit → `DECR` if exceeded → return 429 or 'ok'). If ok, build system prompt for mode, call OpenAI Responses API via proxy
  7. Stream SSE back to client. Collect full response in memory buffer simultaneously. Streaming response buffer for cache storage is capped at 1MB (1,048,576 bytes measured in UTF-8 encoding via `Buffer.byteLength(text, 'utf8')`). If the response exceeds 1MB, stop buffering for cache but CONTINUE streaming to the client. The client receives the full response, but it is NOT cached (too large for Redis). This is a silent cache skip, not an error — the user gets their full answer.
  8. On stream complete: send `event: done` with response_id and token usage
  9. After done event: store complete blocks array in cache (only if finish_reason is "stop" and buffer < 1MB), classify category (keyword match >= 0.6 or "other"), log analytics with request_id/idempotency_key. Analytics does NOT increment usage counter (already done in step 5 or 6).
  10. On stream error: send `event: error` with error details, do NOT cache partial response
- Response ID format: `resp_{timestamp}_{random8chars}` (generated server-side, included in done event). Use `crypto.randomBytes(4).toString('hex')` for the 8-char suffix — NOT `Math.random()`. This ensures cryptographic randomness.

### SSE Event Format
All streaming endpoints use a **TradeGuru app-level SSE contract**, NOT the raw OpenAI Responses stream. The relay's `openai-responses.ts` proxy transforms OpenAI's native SSE events into this app-level format before forwarding to clients. This decouples clients from OpenAI's wire format — if we swap to a custom model later, only the transform layer changes.

**App-level SSE contract (what clients receive):**
```
event: block\ndata: {"type": "text", "content": "..."}\n\n
event: block\ndata: {"type": "code", "language": "python", "content": "..."}\n\n
event: done\ndata: {"response_id": "resp_1710000000_a1b2c3d4", "usage": {"input_tokens": 150, "output_tokens": 420}, "cached": false, "category": "wiring"}\n\n
```

On error mid-stream:
```
event: error\ndata: {"code": "STREAM_INTERRUPTED", "message": "AI service error", "partial": true}\n\n
```

**Transform responsibility:** `openai-responses.ts` receives OpenAI's native `response.*` SSE events, accumulates the JSON `content` string, parses the completed `{ blocks: [...] }` object, and re-emits each block as an individual `event: block` SSE frame. The `event: done` frame is emitted after OpenAI's stream completes with usage data extracted from the final response object.

Client parses `event:` field to distinguish block data from control events. Clients NEVER see raw OpenAI SSE — only the app-level contract above.

### 8. Create Vision Relay Route
- **Task ID**: create-vision-route
- **Depends On**: create-openai-proxies, create-prompt-builder, create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true (can run alongside chat route)
- Create `api/v1/chat/vision.ts` — Edge Runtime for streaming
- Same flow as chat but with image content part
- No caching (images are unique)
- Log `has_image: true` in analytics

### 9. Create Audio Routes
- **Task ID**: create-audio-routes
- **Depends On**: create-openai-proxies, create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true
- Create `api/v1/audio/transcribe.ts` — accepts multipart audio, proxies to OpenAI STT, returns `{ text }`. Max audio file size: 25MB (OpenAI limit). Validate MIME type: `audio/flac`, `audio/mp3`, `audio/mp4`, `audio/mpeg`, `audio/mpga`, `audio/m4a`, `audio/ogg`, `audio/wav`, `audio/webm`. Return 415 for unsupported types.
- Create `api/v1/audio/speech.ts` — accepts `{ text, voice?, response_format? }`, proxies to OpenAI TTS. Default voice: `nova`, default format: `aac`. Voice parameter is normalised to lowercase before validation and cache key generation. Valid built-in voices: `alloy`, `ash`, `ballad`, `coral`, `echo`, `fable`, `nova`, `onyx`, `sage`, `shimmer` — do NOT reject unknown voices (OpenAI may add more or support custom voices; pass through to API and let OpenAI validate). Valid response_format: `aac`, `mp3`, `wav`, `flac`, `opus`, `pcm16` — reject others with 400. Text normalisation: lowercase, trim, collapse whitespace (same rules as question-hasher). Cache key: `tts:{SHA256(normalised_text + "|" + resolved_voice + "|" + resolved_format)}`. `resolved_voice` = explicit voice or `nova` if omitted. `resolved_format` = explicit format or `aac` if omitted. 24h TTL.

### 10. Create File Upload Route
- **Task ID**: create-files-route
- **Depends On**: create-openai-proxies, create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true
- Create `api/v1/files/upload.ts` — accepts multipart file, proxies to OpenAI Files API
- Validate file size against tier limits (Free: 5MB, Pro: 25MB, Unlimited: 50MB). Return 413 if exceeded
- Validate MIME type against allowlist: `image/jpeg`, `image/png`, `image/webp`, `application/pdf`, `text/plain`, `text/csv`. Return 415 for unsupported types. MIME type comparison is **case-insensitive** (normalise to lowercase before matching).
- Log file_size, mime_type in analytics

### 11. Create Rating and Feedback Routes
- **Task ID**: create-rating-routes
- **Depends On**: create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true
- Create `api/v1/rating.ts` — POST `{ response_id, stars, comment?, mode, category? }`. Store in Redis hash `rating:{response_id}:{user_or_device_id}`. Rate limit: 5 ratings per user/device per hour. Ratings are last-write-wins per response_id per user (not append-only). Max `comment` length: 500 chars.
- Validate `stars`: must be integer in range [1, 5]. Return 400 for invalid values.
- Storage key: `rating:{response_id}:{user_or_device_id}` (per-user rating, not global last-write-wins). Multiple users can rate the same response. Aggregation reads all `rating:{response_id}:*` keys.
- Create `api/v1/feedback.ts` — POST `{ response_id, reason, detail?, mode }`. Store in Redis hash `feedback:{response_id}:{user_or_device_id}`. Rate limit: 5 feedback submissions per user/device per hour. Max `detail` length: 1000 chars.
- Validate `reason`: must be one of `'wrong' | 'unsafe' | 'unhelpful' | 'outdated'`. Return 400 for invalid values.
- Validate `detail`: max 1000 chars, trimmed.

### 12. Create Admin Stats Route
- **Task ID**: create-admin-route
- **Depends On**: create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true
- Create `api/v1/admin/stats.ts`
- GET handler: requires `ADMIN_TOKEN` bearer auth + IP allowlist (configurable via `ADMIN_ALLOWED_IPS` env var, comma-separated IPv4 addresses, e.g., `"1.2.3.4,5.6.7.8"`. No CIDR support for MVP. **Defaults to deny-all if not set** — admin endpoint returns 403 until explicitly configured. This prevents accidental exposure.) Both ADMIN_TOKEN AND IP allowlist must match (AND logic). If `ADMIN_ALLOWED_IPS` is not set, admin endpoint returns 403 for all requests until configured. IPv6 addresses are NOT supported for MVP. If admin connects via IPv6, the request is rejected (403). Document in deployment notes that admin should use IPv4 network. Use Vercel's `x-real-ip` header for IP extraction (not `req.socket.remoteAddress`).
- Returns aggregated stats: total queries, unique devices, tokens used, cost estimate, cache hit rate, top categories (top 20), ratings distribution, platform breakdown
- **Primary approach: pre-aggregated counters** — analytics logger increments atomic Redis counters on each request: `stats:total_queries`, `stats:tokens:{date}`, `stats:cache_hits:{date}`, `stats:platform:{platform}:{date}`, `stats:category:{category}:{date}`. Admin stats endpoint reads these counters directly (O(1) per counter, no SCAN). Counters have 90-day TTL matching analytics entries. Cache hit rate = `stats:cache_hits:{date}` / `stats:total_queries:{date}`. Vision requests are included in total_queries but never in cache_hits, which correctly shows the actual hit rate across all request types.
- **Detailed breakdowns only**: SCAN is used ONLY for `top_questions` query (requires iterating entries). Limited to SCAN COUNT 200, max 500 iterations, 30s timeout. Returns `"truncated": true` if incomplete. Detailed breakdowns are optional — omitted if SCAN times out.
- DELETE `/api/v1/admin/cache` — purge cache by pattern (e.g., `?pattern=cache:*:wiring:*` to clear all wiring-related cache). Requires `ADMIN_TOKEN`.
- Rate limited to 10 requests per minute per IP

### 13. Create RevenueCat Webhook + Unified Tier System
- **Task ID**: create-revenuecat-webhook
- **Depends On**: create-utilities
- **Assigned To**: routes-builder
- **Agent Type**: general-purpose
- **Parallel**: true
- Create `types/v1/revenuecat.ts` — Type definitions for RevenueCat webhook events:
  - `RevenueCatWebhookPayload`: top-level `{ api_version, event }` envelope
  - `RevenueCatEvent`: the event object with fields: `type`, `app_user_id`, `product_id`, `entitlement_ids`, `event_timestamp_ms`, `transaction_id`, `store` (APP_STORE/PLAY_STORE/STRIPE), `environment` (PRODUCTION/SANDBOX), `subscriber_attributes`, `price_in_purchased_currency`, `currency`, `expiration_at_ms`, `period_type`
  - Event types enum: `INITIAL_PURCHASE`, `RENEWAL`, `CANCELLATION`, `UNCANCELLATION`, `EXPIRATION`, `BILLING_ISSUE_DETECTED`, `PRODUCT_CHANGE`, `SUBSCRIBER_ALIAS`, `TRANSFER`, `TEST`
  - `SubscriptionTier` type: `'free' | 'pro' | 'unlimited'`
- Create `utils/v1/revenuecat-webhook.ts`:
  - `verifyRevenueCatAuth(req)` — checks `Authorization` header matches `REVENUECAT_WEBHOOK_AUTH_KEY` env var (set in RevenueCat dashboard → Webhooks → Authorization header)
  - `processRevenueCatEvent(event)` — maps event type to tier change:
    - `INITIAL_PURCHASE` / `RENEWAL` / `UNCANCELLATION` → set tier to `pro` (or `unlimited` based on `entitlement_ids`)
    - `CANCELLATION` / `EXPIRATION` → set tier to `free`
    - `BILLING_ISSUE_DETECTED` → log warning, keep current tier (grace period)
    - `PRODUCT_CHANGE` → update tier based on new `entitlement_ids`
    - `TEST` → log only, no tier change
  - Stores tier in Redis: `user:subscription:tier:revenuecat:{app_user_id}` (separate from Stripe key `user:subscription:tier:stripe:{userId}` — merged at read time by `getSubscriptionTier()`)
  - Idempotent: dedup key is `revenuecat:event:{transaction_id}:{event_type}` if `transaction_id` is present, or `revenuecat:event:{app_user_id}:{event_type}:{event_timestamp_ms}:{product_id || 'none'}` if `transaction_id` is null (product_id adds uniqueness for same-millisecond events; e.g., BILLING_ISSUE_DETECTED, SUBSCRIBER_ALIAS). Dedup keys have **90-day TTL** (matching analytics retention). This balances replay protection with Redis memory management — no cleanup job needed. Events older than 90 days that replay are extremely unlikely (RevenueCat retries for max ~3 hours).
  - Tier mutations use a Redis Lua script (atomic, no race conditions). Script execution order:
    1. `GET dedup_key` → if exists, return `"duplicate"` (already processed)
    2. Validate incoming timestamp: `local incoming = tonumber(ARGV[timestamp_idx]); if not incoming then return "invalid_timestamp" end`
    3. `GET last_event_ts_key` → `local stored = tonumber(result); if stored and incoming < stored then return "skipped" end` (out-of-order protection; `<` not `<=` to allow same-millisecond events of different types)
    4. `SET dedup_key value NX` → if returns nil (race: another request set it between step 1 and now), return `"duplicate"` (safety net)
    5. `EXPIRE dedup_key 7776000` (90-day TTL)
    6. `SET tier_key new_tier`
    7. `SET last_event_ts_key incoming_timestamp`
    8. Return `"processed"`
  - If stored timestamp is corrupted (tonumber returns nil), treat as if no timestamp exists — allow the event and overwrite with valid timestamp.
  - Stores `user:subscription:last_event_ts:{app_user_id}` — if incoming `event_timestamp_ms` < stored timestamp, skip event (prevents out-of-order processing)
- Create `utils/v1/subscription-tier.ts`:
  - `getSubscriptionTier(userId)` — reads BOTH `user:subscription:tier:stripe:{userId}` and `user:subscription:tier:revenuecat:{userId}` from Redis, returns the **highest** tier across both sources (unlimited > pro > free). Returns `'free'` if neither key exists. This prevents tier loss when one payment source cancels while the other remains active.
  - Stripe webhooks write to `user:subscription:tier:stripe:{userId}`, RevenueCat webhooks write to `user:subscription:tier:revenuecat:{userId}` — separate keys, merged at read time.
  - Replaces direct Redis reads in auth-middleware with a single function
- Create `api/webhook/revenuecat.ts`:
  - POST handler, NO auth middleware (RevenueCat sends its own auth header)
  - Verify auth header via `verifyRevenueCatAuth()`
  - Parse `RevenueCatWebhookPayload` from request body
  - Skip `SANDBOX` events in production (configurable)
  - Call `processRevenueCatEvent(event)`
  - Return `200` ONLY after tier update (or dedup skip) completes successfully (RevenueCat requires 200, retries on anything else — up to 5 retries at 5/10/20/40/80 minute intervals)
  - Return `500` if `processRevenueCatEvent()` throws or Redis write fails — this triggers RevenueCat retry (dedup key prevents double-processing on successful retry)
  - Return `401` on auth failure
  - Log all events to analytics (event type, app_user_id, product_id, store, timestamp)

### 14. Validate Everything
- **Task ID**: validate-all
- **Depends On**: create-chat-route, create-vision-route, create-audio-routes, create-files-route, create-rating-routes, create-admin-route, create-device-route, create-revenuecat-webhook, update-config
- **Assigned To**: validator
- **Agent Type**: general-purpose
- **Parallel**: false
- Run `npx tsc --noEmit` to verify all TypeScript compiles
- Verify existing endpoints are untouched: `git diff --name-only -- api/assistants-*.ts api/stripe-*.ts api/user-*.ts` should show NO changes
- Verify new files exist: `ls api/v1/` should show all 9 route files
- Verify `vercel.json` has new function configs
- Verify CORS has new origins
- Count total new files created vs expected

## Acceptance Criteria
1. All 12 new API route files exist under `api/v1/` + `api/webhook/`
2. All 16 new utility files exist under `utils/v1/`
3. All 5 new type files exist under `types/v1/`
4. `npx tsc --noEmit` passes with zero errors
5. `git diff --name-only -- api/assistants-*.ts api/stripe-*.ts api/user-*.ts` shows NO changes (existing endpoints untouched)
6. `vercel.json` has `maxDuration` entries for new streaming endpoints
7. `utils/api/corsHeaders.ts` has iOS origins in allowlist (only change to existing files)
8. The chat endpoint accepts `{ messages, mode, device_id, platform }` and returns SSE streaming `{ blocks: [...] }`
9. Response cache checks Redis before calling OpenAI
10. Analytics logger fires async on every request
11. RevenueCat webhook at `api/webhook/revenuecat.ts` verifies auth header, processes events, updates tier in Redis
12. `getSubscriptionTier()` returns correct tier by reading both `stripe:` and `revenuecat:` tier keys and returning the highest
13. Device registration is rate-limited (5/IP/hour), always creates a new device_id, and stores fingerprint for abuse detection
14. `/api/v1/device/link` correctly links device_id to WorkOS user_id with 5-device limit
15. Tier enforcement limits are checked on every authenticated request
16. All file upload endpoints validate size and MIME type before proxying
17. SSE streams send `event: done` on completion and `event: error` on failure
18. RevenueCat webhook uses Redis Lua scripts for atomic tier changes
19. Cache keys include user/device context to prevent cross-user data leakage
20. Analytics entries use request_id idempotency key to prevent double-counting

## Validation Commands
Execute these commands to validate the task is complete:

- `cd /mnt/c/Users/jehma/Desktop/TradeGuru/expo-chatgpt-clone && npx tsc --noEmit` — Verify TypeScript compiles
- `ls api/v1/` — Verify all route files exist
- `ls utils/v1/` — Verify all utility files exist
- `ls types/v1/` — Verify all type files exist
- `git diff --name-only -- api/assistants-*.ts api/stripe-*.ts api/user-*.ts` — Verify existing endpoints untouched
- `git diff -- vercel.json` — Verify function configs added
- `git diff -- utils/api/corsHeaders.ts` — Verify only CORS change to existing utils

## Notes
- `REVENUECAT_WEBHOOK_AUTH_KEY` env var needs to be added to Vercel — this is the shared secret you set in RevenueCat dashboard → Integrations → Webhooks → Authorization header. RevenueCat sends this in every webhook POST so you can verify it's really from RevenueCat.
- The webhook URL to configure in RevenueCat dashboard is: `https://tradeguru.com/api/webhook/revenuecat`
- RevenueCat webhooks require your endpoint to return HTTP 200. Any other status triggers retries (5 retries at 5/10/20/40/80 min intervals, then stops).
- RevenueCat delivers "at least once" — your handler must be idempotent (dedup by event ID in Redis).
- Stripe writes to `user:subscription:tier:stripe:{userId}`, RevenueCat writes to `user:subscription:tier:revenuecat:{userId}`. `getSubscriptionTier()` reads both and returns the highest tier.
- RevenueCat `app_user_id` must be set to the WorkOS user ID when configuring the SDK on iOS/Android. Device-only users (not logged in) cannot purchase subscriptions — the iOS app must require WorkOS login before showing the paywall. This ensures every RevenueCat subscriber has a WorkOS user ID for tier mapping.
- User account deletion cleanup is handled by existing WorkOS webhook. When a user is deleted, a cleanup function removes: `user:devices:{userId}` SET, all `device:user:{deviceId}` links for that user's devices, and `user:subscription:tier:stripe:{userId}` + `user:subscription:tier:revenuecat:{userId}` keys.
- The existing `OPENAI_API_KEY` env var on Vercel is reused — no new secrets needed for the AI proxy
- The existing Upstash Redis (`KV_REST_API_URL`, `KV_REST_API_TOKEN`) is reused for caching and analytics
- No new database needed for MVP — Redis hashes store analytics. Migrate to Vercel Postgres later if volume requires it.
- The `api/v1/chat.ts` route MUST use Edge Runtime for streaming (same as existing `assistants-runs.ts`)
- The web app continues using existing `assistants-*` endpoints. Migration to `/api/v1/*` is a separate future task.
- `ADMIN_TOKEN` env var needs to be added to Vercel for the admin stats endpoint
- Device registration is anonymous — no PII. The device_id is a UUID stored in Redis with a 365-day TTL.
