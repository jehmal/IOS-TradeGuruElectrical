# Spec: Agentic Fault Finder Pipeline

## Task Description

Replace the single Chat Completions call in the v1 chat endpoint with a two-agent Responses API pipeline for `fault_finder` and `learn` modes. An orchestrator agent analyzes the user's question and decides the search strategy, then a synthesis agent uses the vector results + orchestrator instructions to generate the final blocks response.

## Objective

When this spec is complete:
1. Fault finder and learn modes use a two-call Responses API pipeline (orchestrator → vector search → synthesis)
2. The orchestrator decides what to search and how to instruct the synthesis agent per question
3. Vector search uses the orchestrator's optimized query instead of the raw user message
4. The synthesis agent streams the response via Responses API SSE
5. Research mode continues using the current single-call pattern (no vector store)
6. The client-facing SSE contract is unchanged: `event: status`, `event: block`, `event: done`
7. All existing caching, analytics, and quota logic continues to work
8. Swift and HTML renderers need zero changes

## Architecture

```
User: "Why is my RCD tripping after rain?"
       │
       ▼
┌─── STEP 1: Orchestrator (Responses API, non-streaming) ───────┐
│                                                                │
│  POST https://api.openai.com/v1/responses                      │
│  {                                                             │
│    model: "gpt-4o-mini",                                       │
│    instructions: ORCHESTRATOR_PROMPT,                          │
│    input: "Why is my RCD tripping after rain?",                │
│    text: { format: { type: "json_object" } }                   │
│  }                                                             │
│                                                                │
│  → Returns (fast, cheap, ~200ms):                              │
│  {                                                             │
│    "kb_query": "RCD tripping moisture water ingress earth      │
│      fault neutral leak insulation resistance",                │
│    "kb_max_results": 5,                                        │
│    "synthesis_instructions": "This is likely a moisture-       │
│      induced earth fault. Focus on: 1) water ingress paths    │
│      (cable entries, junction boxes, outdoor GPOs), 2) IR     │
│      testing procedure for wet circuits, 3) RCD vs RCBO       │
│      selection for wet areas. Use the First-Contact Fault     │
│      Flow. Start with Safety Gate. Request IR readings at     │
│      500V on each circuit with neutral lifted.",               │
│    "web_search_needed": false                                  │
│  }                                                             │
└────────────────────────┬───────────────────────────────────────┘
                         │
                         ▼
┌─── STEP 2: Vector Store Search (manual, raw fetch) ───────────┐
│                                                                │
│  POST https://api.openai.com/v1/vector_stores/vs_690.../search │
│  {                                                             │
│    query: orchestrator.kb_query,   ← optimized query           │
│    max_num_results: orchestrator.kb_max_results,               │
│    rewrite_query: true                                         │
│  }                                                             │
│                                                                │
│  → Returns: relevant chunks from 127-page knowledge base       │
│  Format as <sources>XML</sources>                              │
└────────────────────────┬───────────────────────────────────────┘
                         │
                         ▼
┌─── STEP 3: Synthesis (Responses API, STREAMING) ──────────────┐
│                                                                │
│  POST https://api.openai.com/v1/responses                      │
│  {                                                             │
│    model: "gpt-4o",                                            │
│    instructions: SYSTEM_PROMPT + orchestrator.synthesis_instructions,│
│    input: [                                                    │
│      { role: "developer", content: "<sources>...</sources>" }, │
│      ...conversation_messages,                                 │
│      { role: "user", content: "Why is my RCD tripping..." }   │
│    ],                                                          │
│    text: { format: { type: "json_object" } },                  │
│    stream: true                                                │
│  }                                                             │
│                                                                │
│  → Streams SSE events:                                         │
│    response.output_text.delta → accumulate JSON buffer          │
│    response.completed → parse { blocks: [...] } → emit blocks  │
└────────────────────────────────────────────────────────────────┘
```

## SSE Contract (unchanged — clients don't change)

```
event: status
data: {"stage":"searching"}

event: status
data: {"stage":"synthesizing"}

event: block
data: {"type":"warning","content":"...","severity":"critical"}

event: block
data: {"type":"step_list","steps":[...]}

event: block
data: {"type":"tip","content":"...","category":"troubleshooting"}

event: block
data: {"type":"reference","source":"Operation Of RCDs.md",...}

event: done
data: {"response_id":"resp_...","usage":{...},"cached":false}
```

## Orchestrator Prompt

```
You are the Trade Guru Orchestrator. Your job is to analyze an electrician's question and produce a search strategy + synthesis instructions.

You receive the user's question and must output JSON:

{
  "kb_query": "optimized search query for the electrical knowledge base — expand abbreviations, add synonyms, include relevant technical terms that would appear in fault-finding guides, manuals, and standards references",
  "kb_max_results": 5,
  "synthesis_instructions": "specific instructions for the synthesis agent based on your analysis of the question — what fault type this likely is, which diagnostic protocol to follow, what measurements to request first, what safety considerations apply, what standards sections are relevant",
  "web_search_needed": false
}

Rules:
- kb_query should be broader than the user's question — add technical synonyms and related terms
- kb_max_results: 3 for simple questions, 5 for standard faults, 8 for complex multi-system issues
- synthesis_instructions should be specific to THIS question — not generic. Tell the synthesis agent exactly what fault pattern to investigate, what the likely root causes are, and what the diagnostic priority should be
- web_search_needed: true only if the question requires current product specs, firmware versions, recall notices, or recent standards amendments — false for general fault diagnosis
- Always include in synthesis_instructions: which protocol to use (First-Contact Fault Flow, On-Site Fault Protocol, Trainer Pack), what safety gates apply, and what meter steps to start with
- For RCD questions: always instruct to check for neutral-earth faults and instruct user to UNPLUG (not just switch off) all appliances
- For solar/battery questions: instruct to check Voc, Isc, inverter fault codes
- Be concise — the synthesis agent has the full Trade Guru system prompt already
```

## Files to Create/Modify

### New Files (Expo project)

| File | Purpose |
|------|---------|
| `utils/v1/orchestrator.ts` | Orchestrator agent — calls Responses API, returns search strategy + synthesis instructions |
| `utils/v1/responses-stream.ts` | Responses API streaming proxy — replaces openai-responses.ts for fault_finder/learn modes |
| `types/v1/orchestrator.ts` | Types: OrchestratorRequest, OrchestratorResponse, SynthesisConfig |

### Modified Files (Expo project)

| File | Change |
|------|--------|
| `api/v1/chat.ts` | Route ALL modes through orchestrator pipelines (fault_finder/learn + research) |
| `api/v1/chat/vision.ts` | Same routing for vision endpoint |
| `utils/v1/system-prompt-builder.ts` | Export orchestrator prompts (fault_finder + research) |
| `utils/v1/search-result-formatter.ts` | Add `formatSearchResultsSanitized()` for research mode (strips filenames) |

### No Changes Required

| File | Why |
|------|-----|
| All Swift files | SSE contract unchanged |
| `preview/chat.html` | SSE contract unchanged |
| `utils/v1/vector-search.ts` | Reused as-is (orchestrator provides query) |
| `utils/v1/auth-middleware.ts` | Unchanged |
| `utils/v1/response-cache.ts` | Unchanged |
| `utils/v1/analytics-logger.ts` | Unchanged |

## Detailed Implementation

### 1. types/v1/orchestrator.ts

```typescript
export interface OrchestratorResponse {
  kb_query: string;
  kb_max_results: number;
  synthesis_instructions: string;
  web_search_needed: boolean;
}

export interface SynthesisConfig {
  systemPrompt: string;
  orchestratorInstructions: string;
  vectorContext?: string;
  messages: Array<{ role: string; content: string }>;
}
```

### 2. utils/v1/orchestrator.ts

- `orchestrate(question: string, mode: ThinkingMode): Promise<OrchestratorResponse>`
- Calls `POST /v1/responses` with `gpt-4o-mini` (fast, cheap — ~200ms, ~$0.001 per call)
- Uses `text.format: { type: "json_object" }` for reliable JSON output
- 10-second timeout
- On error: returns sensible defaults (raw question as kb_query, generic instructions)
- MUST use raw fetch(), NOT OpenAI SDK (Edge Runtime)

### 3. utils/v1/responses-stream.ts

- `streamResponsesAPI(config: SynthesisConfig, onBlock, onDone, onError): Promise<void>`
- Calls `POST /v1/responses` with `gpt-4o`, `stream: true`
- Messages array:
  ```
  instructions: config.systemPrompt + "\n\n" + config.orchestratorInstructions
  input: [
    ...(config.vectorContext ? [{ role: "developer", content: config.vectorContext }] : []),
    ...config.messages
  ]
  ```
- `text.format: { type: "json_object" }` for blocks contract
- Parses Responses API SSE events:
  - `response.output_text.delta` → accumulate in buffer
  - `response.completed` → parse JSON, emit blocks, call onDone
  - Error events → call onError
- 30-second timeout for synthesis, 120-second overall
- Retry once on 500/503 (same pattern as current openai-responses.ts)

### 4. Modify api/v1/chat.ts

Current flow (lines 186-270):
```
buildSystemPrompt → vector search → streamOpenAIResponses
```

New flow for fault_finder/learn:
```
emit status: searching
→ orchestrate(question, mode)
→ searchVectorStore(orchestrator.kb_query, { max_num_results: orchestrator.kb_max_results })
→ formatSearchResults(results)
emit status: synthesizing
→ streamResponsesAPI({
    systemPrompt: buildSystemPrompt(mode),
    orchestratorInstructions: orchestrator.synthesis_instructions,
    vectorContext: formattedResults,
    messages: chatRequest.messages
  })
→ blocks arrive → emit event: block for each
→ emit event: done
```

Research mode: unchanged (keeps current Chat Completions call)

### 5. Modify api/v1/chat/vision.ts

Same pattern as chat.ts for fault_finder/learn modes.

## Mode Routing

| Mode | Pipeline | Orchestrator | Vector Search | Web Search | Synthesis API |
|------|----------|-------------|---------------|------------|---------------|
| fault_finder | Agentic | gpt-4o-mini | YES | NO | Responses API streaming |
| learn | Agentic | gpt-4o-mini | YES | NO | Responses API streaming |
| research | Agentic (new) | gpt-4o-mini | YES (filtered) | YES | Responses API streaming |

---

## Research Mode — Agentic Pipeline

### Background

Research mode previously used the Responses API with both `web_search` and `file_search` tools in a single call. The `file_search` tool was removed in commit `dca04f3` because the vector store contains confidential company data — file citations (filenames, internal doc references) were leaking into responses visible to users.

The current research mode is **web-only**, which means it cannot reference the 127-page internal knowledge base (testing procedures, fault-finding guides, meter instructions, standards summaries).

### Solution: Orchestrator-Mediated Safe Retrieval

Use the same orchestrator pattern as fault_finder/learn, but with an additional **web search step** and a **source sanitization layer** that strips internal filenames before the synthesis agent sees them.

```
User: "What are the specs for a Schneider iC60N 20A MCB?"
       │
       ▼
┌─── STEP 1: Research Orchestrator (gpt-4o-mini) ──────────────┐
│                                                                │
│  Decides:                                                      │
│  {                                                             │
│    "kb_query": "Schneider iC60N MCB specifications ratings",   │
│    "kb_max_results": 3,                                        │
│    "web_search_query": "Schneider Acti9 iC60N 20A datasheet", │
│    "web_search_needed": true,                                  │
│    "synthesis_instructions": "Focus on: rated current, breaking│
│      capacity, curve type, pole options, standards compliance. │
│      Cross-reference internal knowledge with web sources.      │
│      Prefer manufacturer datasheets. Include product URLs."    │
│  }                                                             │
└────────────────────────┬───────────────────────────────────────┘
                         │
                    ┌────┴────┐
                    │         │
                    ▼         ▼
┌─── STEP 2a: Vector Search ──┐  ┌─── STEP 2b: (parallel) ──────┐
│                              │  │                               │
│  query: orchestrator.kb_query│  │  Web search happens INSIDE    │
│  max_results: 3              │  │  the synthesis call via the   │
│  → Internal knowledge chunks │  │  web_search tool              │
│                              │  │                               │
│  SANITIZE: strip filenames,  │  │  (Responses API handles this  │
│  replace with generic labels:│  │   natively — no manual call)  │
│  "Internal Reference 1"     │  │                               │
│  "Internal Reference 2"     │  │                               │
└────────────────┬─────────────┘  └───────────────────────────────┘
                 │
                 ▼
┌─── STEP 3: Research Synthesis (gpt-4o, streaming) ────────────┐
│                                                                │
│  POST /v1/responses                                            │
│  {                                                             │
│    model: "gpt-4o",                                            │
│    instructions: RESEARCH_PROMPT + orchestrator.synthesis_inst, │
│    input: [                                                    │
│      { role: "developer", content: "<sanitized vector results>"}│
│      { role: "user", content: "What are the specs for..." }   │
│    ],                                                          │
│    tools: [                                                    │
│      { type: "web_search", search_context_size: "medium" }     │
│    ],                                                          │
│    stream: true                                                │
│  }                                                             │
│                                                                │
│  The synthesis agent:                                          │
│  1. Has internal knowledge via developer message (sanitized)   │
│  2. Can call web_search for live data (datasheets, URLs)       │
│  3. Cross-references both sources                              │
│  4. Returns blocks JSON with real URLs from web search         │
│  5. NEVER exposes internal filenames (already stripped)         │
│                                                                │
│  → Streams blocks to client                                    │
└────────────────────────────────────────────────────────────────┘
```

### Source Sanitization

The security issue was that `file_search` returned file citations with internal filenames (`Operation Of RCDs.md`, `Fault Finding Guide.md`). Users should never see these.

With manual `vector_stores.search()`, we control the results. The `search-result-formatter.ts` is modified for research mode to:

1. Strip `filename` and `file_id` from results
2. Replace with generic labels: `"Internal Reference 1"`, `"Internal Reference 2"`
3. Keep the actual text content (the knowledge is fine to use — just not the source metadata)

```typescript
// New function in search-result-formatter.ts
export function formatSearchResultsSanitized(results: VectorStoreSearchResult[]): string {
  const filtered = results.filter(r => r.score >= 0.3);
  let output = '<internal_knowledge>\n';
  filtered.forEach((r, i) => {
    output += `<reference id="${i + 1}">\n`;
    for (const part of r.content) {
      output += `${part.text}\n`;
    }
    output += `</reference>\n`;
  });
  output += '</internal_knowledge>';
  return output.substring(0, 8000);
}
```

The synthesis agent's instructions say: "Internal knowledge references are provided for context only. Do NOT cite them as sources. Only cite publicly accessible web URLs in your sources array."

### Research Orchestrator Prompt

```
You are the Trade Guru Research Orchestrator. Analyze the electrician's research query and produce a search strategy.

Output JSON:
{
  "kb_query": "search query for internal electrical knowledge base — expand with technical synonyms",
  "kb_max_results": 3,
  "web_search_query": "optimized web search query for finding datasheets, manuals, product pages",
  "web_search_needed": true,
  "synthesis_instructions": "specific instructions for the research agent — what type of information to prioritize, how to structure the response, what to cross-reference"
}

Rules:
- web_search_needed is almost always true for research mode (it's the primary source)
- kb_query searches the internal knowledge base for background context (testing procedures, standards summaries)
- web_search_query should target manufacturer websites, distributor pages, official datasheets
- For product lookups: include model number, brand, and "datasheet" or "specifications" in web query
- For installation questions: include "installation guide" or "wiring diagram" in web query
- For cable sizing: instruct synthesis to route user to https://www.jcalc.net/cable-sizing-calculator-as3008
- For earth fault loop impedance: instruct synthesis to reference AS/NZ tables 8.1 and 8.2
- For maximum demand: instruct synthesis to route to jcalc.net
- NEVER instruct synthesis to cite internal knowledge base filenames
```

### Research SSE Flow

```
event: status  {"stage":"searching"}     ← vector search + web search prep
event: status  {"stage":"synthesizing"}  ← synthesis agent (has web_search tool)
event: status  {"stage":"streaming"}     ← first block arrives
event: block   {"type":"text","content":"The Schneider iC60N is..."}
event: block   {"type":"table","headers":["Spec","Value"],"rows":[...]}
event: block   {"type":"step_list","steps":[...]}   ← if installation asked
event: block   {"type":"warning","content":"..."}   ← if safety relevant
event: block   {"type":"link","url":"https://se.com/...","title":"Datasheet"}
event: block   {"type":"reference","source":"AS/NZS 60898","section":"4.3"}
event: done    {"response_id":"...","usage":{...},"cached":false}
```

### What This Restores

| Capability | Before (web-only) | After (agentic) |
|------------|-------------------|-----------------|
| Web search for live data | YES | YES |
| Internal knowledge base context | NO | YES (sanitized) |
| Product spec lookups | Partial (web only) | Full (web + internal) |
| Installation procedures | Web only | Web + internal procedures |
| Standards references | Blocked by prompt | Internal summaries + web |
| Source URLs | Web URLs only | Web URLs only (internal stripped) |
| Response quality | Good for products | Excellent for everything |

### Security Maintained

- Internal filenames NEVER appear in responses (stripped in formatter)
- The synthesis agent sees content labeled as "Internal Reference 1/2/3" — no filenames
- Instructions explicitly prohibit citing internal references as sources
- Only web_search URLs appear in the `sources` array
- The `file_search` tool is NOT used — we do manual retrieval + sanitization

## Cost Analysis

| Component | Per Request | Model |
|-----------|------------|-------|
| Orchestrator call | ~$0.001 (200 input + 100 output tokens) | gpt-4o-mini |
| Vector search | $0 (storage-only pricing) | N/A |
| Synthesis call | ~$0.02-0.05 (2000 input + 500-1500 output tokens) | gpt-4o |
| **Total** | **~$0.02-0.05** | |

Current cost (single Chat Completions): ~$0.02-0.04. The orchestrator adds ~$0.001 — negligible.

## Edge Runtime Rules

All new files MUST:
- Use raw `fetch()` for OpenAI API calls
- NOT import the `openai` npm package
- NOT use `Buffer`, `require()`, `fs`, `path`, `crypto` (Node module)
- Use `TextEncoder` for byte operations
- Use `crypto.getRandomValues()` / `crypto.randomUUID()` for random values

## Caching Behavior

- Cache check happens BEFORE the orchestrator call (step 4 in chat.ts)
- If cache hit → skip entire pipeline, serve cached blocks
- If cache miss → run orchestrator → search → synthesis → cache the result
- Cache key unchanged: `cache:{hash}:fault_finder:{identity}`
- The hash is of the USER's question (not orchestrator's kb_query)
- This means different phrasings of the same question still cache separately (acceptable)

## Error Handling

| Error | Recovery |
|-------|----------|
| Orchestrator fails | Use defaults: raw question as kb_query, generic synthesis instructions |
| Vector search fails | Continue with no vector context (same as current fallback) |
| Synthesis stream fails | Emit `event: error`, no cache write |
| Orchestrator timeout (10s) | Use defaults and continue |
| Synthesis timeout (30s) | Abort, emit `event: error` |

## Acceptance Criteria

1. `npx tsc --noEmit` passes with 0 v1 errors
2. No Node.js-only APIs in Edge Runtime files
3. Fault finder mode uses orchestrator → vector search → synthesis pipeline
4. Learn mode uses same pipeline with learn system prompt
5. Research mode uses orchestrator → sanitized vector search → synthesis with web_search tool
6. Research mode NEVER exposes internal filenames in responses (sanitized formatter)
7. Research mode can call web_search for live data (datasheets, product pages, URLs)
8. SSE contract identical across all modes: event: status/block/done/error
9. Cached responses bypass entire pipeline for all modes
10. Orchestrator uses gpt-4o-mini (fast, cheap) for all modes
11. Synthesis uses gpt-4o with Responses API streaming for all modes
12. Research synthesis has `tools: [{ type: "web_search" }]` enabled
13. Fault finder/learn synthesis has NO tools (just system prompt + vector context)
14. Existing endpoints untouched
15. Swift and HTML renderers work without changes
16. Research responses include real web URLs in link/reference blocks (from web_search, not hallucinated)

## Validation Commands

```bash
cd /mnt/c/Users/jehma/Desktop/TradeGuru/expo-chatgpt-clone && npx tsc --noEmit
git diff --name-only -- api/assistants-*.ts api/stripe-*.ts api/user-*.ts  # should be empty
ls types/v1/orchestrator.ts utils/v1/orchestrator.ts utils/v1/responses-stream.ts  # new files exist
```

## Team Orchestration

### Team Members

- Builder
  - Name: pipeline-builder
  - Role: Creates all new files (types, orchestrator, responses-stream) and modifies chat.ts + vision.ts + system-prompt-builder.ts
  - Agent Type: general-purpose

- Validator
  - Name: pipeline-validator
  - Role: Verifies TypeScript compiles, tests endpoint, checks SSE output
  - Agent Type: general-purpose

## Step by Step Tasks

### 1. Create Orchestrator Types
- **Task ID**: create-orchestrator-types
- **Depends On**: none
- **Assigned To**: pipeline-builder
- Create `types/v1/orchestrator.ts`

### 2. Create Orchestrator Agent
- **Task ID**: create-orchestrator
- **Depends On**: create-orchestrator-types
- **Assigned To**: pipeline-builder
- Create `utils/v1/orchestrator.ts`
- Add ORCHESTRATOR_PROMPT to system-prompt-builder.ts

### 3. Create Responses API Streamer
- **Task ID**: create-responses-stream
- **Depends On**: create-orchestrator-types
- **Assigned To**: pipeline-builder
- Create `utils/v1/responses-stream.ts`

### 4. Wire Pipeline into Chat Route
- **Task ID**: wire-chat-route
- **Depends On**: create-orchestrator, create-responses-stream
- **Assigned To**: pipeline-builder
- Modify `api/v1/chat.ts` to route fault_finder/learn through new pipeline

### 5. Wire Pipeline into Vision Route
- **Task ID**: wire-vision-route
- **Depends On**: wire-chat-route
- **Assigned To**: pipeline-builder
- Modify `api/v1/chat/vision.ts` same pattern

### 6. Add Research Orchestrator Prompt
- **Task ID**: research-orchestrator-prompt
- **Depends On**: create-orchestrator
- **Assigned To**: pipeline-builder
- Add RESEARCH_ORCHESTRATOR_PROMPT to system-prompt-builder.ts
- Different from fault_finder orchestrator — includes web_search_query field and research-specific routing rules

### 7. Add Sanitized Formatter
- **Task ID**: sanitized-formatter
- **Depends On**: none
- **Assigned To**: pipeline-builder
- Add `formatSearchResultsSanitized()` to `utils/v1/search-result-formatter.ts`
- Strips filenames, replaces with "Internal Reference N", keeps content

### 8. Wire Research Pipeline into Chat Route
- **Task ID**: wire-research-route
- **Depends On**: research-orchestrator-prompt, sanitized-formatter, create-responses-stream
- **Assigned To**: pipeline-builder
- In chat.ts: research mode now uses orchestrator → sanitized vector search → synthesis with web_search tool
- Synthesis call includes `tools: [{ type: "web_search", search_context_size: "medium" }]`

### 9. Validate and Test
- **Task ID**: validate
- **Depends On**: wire-research-route, wire-vision-route
- **Assigned To**: pipeline-validator
- TypeScript compilation, endpoint testing, SSE verification for all 3 modes
