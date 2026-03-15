# Vector Search Pipeline Full Wiring Spec — TradeGuruElectrical

### Spec Metadata

| Property | Value |
|----------|-------|
| Screen | Chat (vector search pipeline integration) |
| Total inventory items | 15 |
| Already wired | 1 (OPENAI_API_KEY) |
| Needs wiring | 14 |
| Target | 100% functional vector search + synthesis + status UI |
| Mock mode | Toggleable via `APIConfig.useMockData` (already exists) |
| Backend base URL | `https://tradeguru.com.au/api/v1` |
| API reference | `todo/inventory/3-vector-search-pipeline.md` |
| Factcheck | `docs/openai-factcheck-3.md` |
| Date | 2026-03-14 |

---

## Objective

Wire the OpenAI Vector Store Search API into the v1 chat pipeline so that fault_finder and learn mode queries are enriched with the 127-page electrical knowledge base before generating responses. Add real-time pipeline status indicators (searching/synthesizing/streaming) to both Swift and HTML UIs.

---

## Pain Point

The v1 chat endpoint currently calls OpenAI Chat Completions with only a system prompt — it has **zero access** to the 127-page electrical fault-finding knowledge base that the original web app uses via the Assistants API. This means responses lack the deep domain knowledge (meter procedures, AS/NZS standards, RCD protocols, solar diagnostics) that make Trade Guru valuable. Users also have no visibility into what the backend is doing during the ~3-5 second search+synthesis delay.

---

## Critical Bug Found During Analysis

**`APIConfig.swift:5`** — `baseURL` is `"https://tradeguru.com/api/v1"` (wrong domain). Must be `"https://tradeguru.com.au/api/v1"`. This affects ALL Swift API calls.

---

## Mock Data Toggle

### Swift
- File: `ios/Tradeguruelectrical/Services/APIConfig.swift:4`
- Already exists: `static var useMockData = false`
- ChatViewModel already checks this flag (line 18)

### HTML
- File: `preview/chat.html`
- Mock toggle already exists in workbench controls
- When mock mode is on, skip API calls and show mock data

---

## Inventory Item Checklist

### 1. VectorSearchTypes

**Pain Point:** No TypeScript types for vector store search API — can't build services without types.

| Property | Value |
|----------|-------|
| Type | backend-type |
| Status | needs-backend |
| File (planned) | `types/v1/vector-search.ts` (Expo project) |
| Blocked by | nothing |

**Changes needed:**
- [ ] Create `types/v1/vector-search.ts` with:
  - `VectorStoreSearchParams` — `{ query: string, max_num_results?: number, rewrite_query?: boolean, ranking_options?: { ranker?: string, score_threshold?: number } }`
  - `VectorStoreSearchResult` — `{ file_id: string, filename: string, score: number, attributes: Record<string, any>, content: Array<{ type: 'text', text: string }> }`
  - `VectorStoreSearchResponse` — `{ object: string, search_query: string, data: VectorStoreSearchResult[], has_more: boolean, next_page: string | null }`
  - `VECTOR_STORE_ID` constant = `'vs_69017d88924081919fc6599a18ae2231'`
  - `VECTOR_SEARCH_CONFIG` = `{ max_num_results: 5, rewrite_query: true }`

**Note:** Per factcheck, `query` can be `string | string[]` but we only need `string` for MVP.

---

### 2. PipelineStatus Type

**Pain Point:** No SSE status event type defined — backend can't emit status events.

| Property | Value |
|----------|-------|
| Type | backend-type |
| Status | needs-backend |
| File | `types/v1/request.ts` (Expo project, line ~63, append) |
| Blocked by | nothing |

**Changes needed:**
- [ ] `types/v1/request.ts:63` — Add after `generateResponseId()`:
  ```typescript
  export type PipelineStage = 'searching' | 'synthesizing' | 'streaming';
  export interface PipelineStatusEvent {
    stage: PipelineStage;
    detail?: string;
  }
  ```

---

### 3. VectorStoreSearch Service

**Pain Point:** No way to query the 127-page knowledge base.

| Property | Value |
|----------|-------|
| Type | backend-service |
| Status | needs-backend |
| File (planned) | `utils/v1/vector-search.ts` (Expo project) |
| Blocked by | VectorSearchTypes (#1) |

**Changes needed:**
- [ ] Create `utils/v1/vector-search.ts` with:
  - `searchVectorStore(query: string, config?: Partial<SearchConfig>): Promise<VectorStoreSearchResponse>`
  - Uses raw `fetch('https://api.openai.com/v1/vector_stores/${VECTOR_STORE_ID}/search', ...)`
  - Authorization: `Bearer ${process.env.OPENAI_API_KEY}`
  - Default: `max_num_results: 5`, `rewrite_query: true`
  - Timeout: 10 seconds (vector search is fast)
  - On error: return empty results (don't block chat response)

**Note:** Must use raw fetch(), NOT OpenAI SDK — Edge Runtime compatibility per Mandatory Rule #4.

---

### 4. SearchResultFormatter Service

**Pain Point:** Raw vector results need formatting before injection into chat context.

| Property | Value |
|----------|-------|
| Type | backend-service |
| Status | needs-backend |
| File (planned) | `utils/v1/search-result-formatter.ts` (Expo project) |
| Blocked by | VectorSearchTypes (#1) |

**Changes needed:**
- [ ] Create `utils/v1/search-result-formatter.ts` with:
  - `formatSearchResults(results: VectorStoreSearchResult[]): string`
  - Output format (per OpenAI docs):
    ```
    <sources>
      <result file_id='file-abc' file_name='fault-finding-guide.pdf'>
        <content>relevant text chunk</content>
      </result>
      ...
    </sources>
    ```
  - Filter out results with `score < 0.3` (low relevance)
  - Truncate total formatted output to 8000 chars (prevent context overflow)

---

### 5. SynthesisStreamer (Modify openai-responses.ts)

**Pain Point:** `streamOpenAIResponses()` has no way to inject vector context into the prompt.

| Property | Value |
|----------|-------|
| Type | backend-modify |
| Status | needs-backend |
| File | `utils/v1/openai-responses.ts` (Expo project) |
| Lines | 17-22 (function signature), 40-48 (messages array) |
| Blocked by | nothing |

**Changes needed:**
- [ ] `openai-responses.ts:17-22` — Add optional `vectorContext?: string` parameter:
  ```typescript
  export async function streamOpenAIResponses(
    messages: Array<{ role: string; content: string }>,
    systemPrompt: string,
    onBlock: (block: ContentBlock) => void,
    onDone: (usage: TokenUsage, finish_reason: string) => void,
    onError: (error: string) => void,
    vectorContext?: string  // NEW
  ): Promise<void>
  ```
- [ ] `openai-responses.ts:40-48` — Inject vector context as `developer` message before system prompt:
  ```typescript
  messages: [
    ...(vectorContext ? [{
      role: 'developer',
      content: `Use the following knowledge base sources to inform your response. Cite source filenames when relevant.\n\n${vectorContext}`
    }] : []),
    { role: 'system', content: systemPrompt },
    ...messages,
  ],
  ```
- [ ] Same change in retry block (lines 74-82)

**Note:** Per factcheck, `developer` role is preferred over `system` for gpt-4o context injection.

---

### 6. Chat Route — Add Vector Search Step

**Pain Point:** Chat endpoint goes straight to OpenAI without querying knowledge base.

| Property | Value |
|----------|-------|
| Type | backend-modify |
| Status | needs-backend |
| File | `api/v1/chat.ts` (Expo project) |
| Lines | 186-197 (between quota check and stream creation) |
| Blocked by | VectorStoreSearch (#3), SearchResultFormatter (#4), SynthesisStreamer (#5), PipelineStatus (#2) |

**Changes needed:**
- [ ] `chat.ts:186` — After `buildSystemPrompt()`, before `new ReadableStream()`:
  ```typescript
  // Vector search for fault_finder and learn modes only
  let vectorContext: string | undefined;
  if (chatRequest.mode !== 'research') {
    // Emit status: searching
    const statusSearching = `event: status\ndata: ${JSON.stringify({ stage: 'searching', detail: 'Querying knowledge base...' })}\n\n`;
    // (emit before stream starts — see implementation note)

    const searchResults = await searchVectorStore(lastUserMessage);
    if (searchResults.data.length > 0) {
      vectorContext = formatSearchResults(searchResults.data);
    }
  }
  ```
- [ ] `chat.ts:197` — Pass `vectorContext` to `streamOpenAIResponses()`:
  ```typescript
  await streamOpenAIResponses(
    chatRequest.messages,
    systemPrompt,
    onBlock,
    onDone,
    onError,
    vectorContext  // NEW
  );
  ```
- [ ] Emit `event: status` SSE events within the ReadableStream start():
  - `{ stage: 'searching' }` before vector search call
  - `{ stage: 'synthesizing' }` after search, before OpenAI call
  - `{ stage: 'streaming' }` when first block arrives

**Note:** Status events must be emitted inside the ReadableStream's `start()` to work with SSE. The vector search call happens before streaming starts, so searching/synthesizing statuses are emitted, then streaming status arrives with first block.

---

### 7. Vision Route — Add Vector Search Step

**Pain Point:** Vision endpoint also lacks knowledge base context.

| Property | Value |
|----------|-------|
| Type | backend-modify |
| Status | needs-backend |
| File | `api/v1/chat/vision.ts` (Expo project) |
| Blocked by | Chat route modification (#6) |

**Changes needed:**
- [ ] Same vector search injection pattern as chat.ts (#6)
- [ ] Same status event emission
- [ ] Same `vectorContext` parameter pass-through

---

### 8. PipelineStage Swift Model

**Pain Point:** Swift app has no type to represent backend pipeline stages.

| Property | Value |
|----------|-------|
| Type | swift-model |
| Status | needs-backend |
| File (planned) | `ios/Tradeguruelectrical/Models/PipelineStage.swift` |
| Blocked by | nothing |

**Changes needed:**
- [ ] Create `ios/Tradeguruelectrical/Models/PipelineStage.swift`:
  ```swift
  nonisolated enum PipelineStage: String, Codable {
      case idle
      case searching
      case synthesizing
      case streaming
      case error
  }
  ```

---

### 9. PipelineStatusDots View

**Pain Point:** No animated indicator for pipeline activity.

| Property | Value |
|----------|-------|
| Type | swift-view |
| Status | needs-backend |
| File (planned) | `ios/Tradeguruelectrical/Views/PipelineStatusDots.swift` |
| Blocked by | nothing |

**Changes needed:**
- [ ] Create animated three-dot view (sequential pulse, 0.2s offset per dot)
- [ ] Use existing typing indicator pattern from `ChatView.swift:254-274` as reference
- [ ] Extract into reusable component

---

### 10. PipelineStatusView

**Pain Point:** User has no visibility into what the backend is doing during the 3-5s delay.

| Property | Value |
|----------|-------|
| Type | swift-view |
| Status | needs-backend |
| File (planned) | `ios/Tradeguruelectrical/Views/PipelineStatusView.swift` |
| Blocked by | PipelineStage (#8), PipelineStatusDots (#9) |

**Changes needed:**
- [ ] Create pill-shaped status bar with SF Symbol icon + text + dots:
  - `.searching` → magnifyingglass.circle + "Searching knowledge base..." + dots
  - `.synthesizing` → brain.head.profile + "Synthesizing response..." + dots
  - `.streaming` → text.bubble + "Streaming..." + dots
  - `.idle` → hidden (fade out)
- [ ] Animated transitions between stages
- [ ] Colors: use `tradeTextSecondary` for subtle appearance

---

### 11. ChatViewModel — Add Pipeline Stage

**Pain Point:** ViewModel has no pipeline stage tracking.

| Property | Value |
|----------|-------|
| Type | swift-modify |
| Status | needs-backend |
| File | `ios/Tradeguruelectrical/ViewModels/ChatViewModel.swift` |
| Lines | 8 (add property), 124 (handle status events) |
| Blocked by | PipelineStage (#8) |

**Changes needed:**
- [ ] `ChatViewModel.swift:8` — Add: `var pipelineStage: PipelineStage = .idle`
- [ ] `ChatViewModel.swift:124` — In the `for await result in TradeGuruAPI.chat(...)` loop, add `.status` case
- [ ] Reset to `.idle` on `.done` and `.error`

---

### 12. StreamParser — Handle Status Events

**Pain Point:** StreamParser ignores `event: status` SSE events.

| Property | Value |
|----------|-------|
| Type | swift-modify |
| Status | needs-backend |
| File | `ios/Tradeguruelectrical/Services/StreamParser.swift` |
| Lines | 66-86 (processEvent switch) |
| Blocked by | PipelineStage (#8) |

**Changes needed:**
- [ ] `StreamParser.swift:3-6` — Add `StatusPayload` struct:
  ```swift
  nonisolated struct StatusPayload: Codable {
      let stage: String
      let detail: String?
  }
  ```
- [ ] `StreamParser.swift:89-93` — Add `.status` case to `StreamResult`:
  ```swift
  case status(StatusPayload)
  ```
- [ ] `StreamParser.swift:79` — Add `"status"` case in `processEvent()`:
  ```swift
  case "status":
      guard let payload = try? decoder.decode(StatusPayload.self, from: jsonData) else { return nil }
      return .status(payload)
  ```

---

### 13. PipelineStatusBar (HTML)

**Pain Point:** HTML preview has no status indicator — must match Swift 1:1 per Mandatory Rule #5.

| Property | Value |
|----------|-------|
| Type | html-component |
| Status | needs-backend |
| File | `preview/chat.html` |
| Blocked by | nothing |

**Changes needed:**
- [ ] Add CSS for `.pipeline-status` pill bar (animated fade in/out, icon + text + dots)
- [ ] Add `@keyframes dotPulse` for three sequential dots
- [ ] Add HTML structure inside device viewport, below nav bar
- [ ] Stage icons: magnifying glass (searching), brain (synthesizing), chat bubble (streaming)
- [ ] Match SwiftUI sizing, colors, animation timing

---

### 14. HTML SSE Parser — Handle Status Events

**Pain Point:** HTML SSE parser doesn't understand `event: status`.

| Property | Value |
|----------|-------|
| Type | html-modify |
| Status | needs-backend |
| File | `preview/chat.html` |
| Lines | ~1117 (SSE event switch in apiChat()) |
| Blocked by | PipelineStatusBar (#13) |

**Changes needed:**
- [ ] `chat.html:~1117` — Add `else if (currentEvent === 'status')` handler:
  ```javascript
  else if (currentEvent === 'status') {
      updatePipelineStatus(parsed.stage, parsed.detail);
  }
  ```
- [ ] Add `updatePipelineStatus(stage, detail)` function
- [ ] On `done` or `error`, call `updatePipelineStatus(null)` to hide

---

## Wired Items (no changes needed)

| # | Item | File | Status |
|---|------|------|--------|
| 1 | OPENAI_API_KEY | Vercel env var | done — already configured |

---

## Additional Bug Fix Required

| # | Item | File | Line | Change |
|---|------|------|------|--------|
| BF-1 | Wrong API base URL | `ios/.../APIConfig.swift` | 5 | `"https://tradeguru.com/api/v1"` → `"https://tradeguru.com.au/api/v1"` |

---

## Implementation Order

| Phase | Items | Description | Dependency |
|-------|-------|-------------|------------|
| 0 | BF-1 | Fix APIConfig.swift base URL | None |
| 1 | #1, #2, #8 | Types: VectorSearchTypes, PipelineStatus, PipelineStage | None |
| 2 | #3, #4, #5 | Backend services: search, formatter, streamer modification | Phase 1 |
| 3 | #6, #7 | Backend integration: wire into chat + vision routes | Phase 2 |
| 4 | #9, #10, #11, #12 | Swift UI: dots, status view, viewmodel, stream parser | Phase 1 |
| 5 | #13, #14 | HTML: status bar + SSE parser | Phase 3 |

---

## Verification

- [ ] `npx tsc --noEmit` passes (0 errors in v1 files)
- [ ] No Node.js-only APIs in Edge Runtime files (Buffer, require('crypto'), etc.)
- [ ] Chat endpoint returns `event: status` before `event: block`
- [ ] Swift StreamParser handles `event: status` → updates ChatViewModel.pipelineStage
- [ ] PipelineStatusView shows/hides based on pipelineStage
- [ ] HTML pipeline status bar matches Swift PipelineStatusView pixel-for-pixel
- [ ] `event: status` → `event: block` → `event: done` sequence is correct
- [ ] Research mode skips vector search (web search only)
- [ ] Cached responses skip vector search (no `event: status` before cached blocks)
- [ ] Existing endpoints untouched: `git diff --name-only -- api/assistants-*.ts api/stripe-*.ts api/user-*.ts` shows nothing
- [ ] APIConfig.swift uses correct base URL: `tradeguru.com.au`
- [ ] PipelineStatusView added to HTML component picker dropdown (Mandatory Rule #2)
