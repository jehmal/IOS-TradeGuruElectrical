# Event Flow: Fault Finder Mode

**Mode:** `fault_finder`
**Endpoint:** `POST /api/v1/chat`
**Runtime:** Vercel Edge Runtime
**Vector Store:** YES (127-page electrical knowledge base)
**System Prompt:** Trade Guru identity + On-Site Fault Protocol + Blocks JSON wrapper

---

## Flow Diagram

```
USER SENDS MESSAGE
"Why is my RCD tripping?"
       │
       ▼
┌──────────────────────────────────────────────────────┐
│  1. PRE-FLIGHT                                       │
│                                                      │
│  generate request_id (crypto.randomUUID())           │
│  read X-Idempotency-Key header (optional)            │
│  start timer                                         │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  2. AUTHENTICATE                                     │
│                                                      │
│  Read X-Device-ID header                             │
│  Validate UUID v4 format                             │
│  Redis: GET device:{device_id}                       │
│  Redis: GET device:user:{device_id} (check link)     │
│  Resolve subscription tier (free/pro/unlimited)      │
│                                                      │
│  ← FAIL: 401 {"error":{"code":"UNAUTHORIZED",...}}   │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  3. VALIDATE REQUEST                                 │
│                                                      │
│  Parse JSON body                                     │
│  Check: messages[] non-empty, ≤100 items             │
│  Check: each message has role + content ≤10000 chars │
│  Check: mode = "fault_finder" ✓                      │
│  Check: platform ∈ {ios, android, web}               │
│                                                      │
│  ← FAIL: 400 {"error":{"code":"INVALID_REQUEST"}}   │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  4. CACHE CHECK                                      │
│                                                      │
│  Hash question: SHA-256(normalise(last_user_msg))    │
│  Build identity key: usr:{user_id} or dev:{device_id}│
│  Redis: GET cache:{hash}:{fault_finder}:{identity}   │
│                                                      │
│  → IF HIT: skip to CACHED RESPONSE FLOW (below)     │
│  → IF MISS: continue                                 │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  5. QUOTA CHECK (atomic Redis Lua)                   │
│                                                      │
│  Key: usage:{identity}:chat:{YYYY-MM-DD}             │
│  Lua: INCR → check limit → DECR if exceeded          │
│  Free: 10/day │ Pro: 100/day │ Unlimited: 1000/day   │
│                                                      │
│  ← FAIL: 429 {"error":{"code":"QUOTA_EXCEEDED",...}} │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  6. BUILD SYSTEM PROMPT                              │
│                                                      │
│  buildSystemPrompt('fault_finder') returns:          │
│  ┌────────────────────────────────────────────────┐  │
│  │ SHARED_PREAMBLE (144 lines)                    │  │
│  │ - "Trade Guru: World's Greatest Electrician"   │  │
│  │ - On-Site Fault Protocol (mandatory)           │  │
│  │ - First-Contact Fault Flow                     │  │
│  │ - Safety Gate checklist                        │  │
│  │ - Meter Instructions (probe placement, range)  │  │
│  │ - RCD-specific instructions                    │  │
│  │ - Solar/Battery focus                          │  │
│  │ - Branch Logic workflow                        │  │
│  ├────────────────────────────────────────────────┤  │
│  │ BLOCKS_WRAPPER                                 │  │
│  │ - "Respond with { blocks: [...] } JSON"        │  │
│  │ - 12 block type definitions                    │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
══════════════════════════════════════════════════════════
  SSE STREAM BEGINS (ReadableStream)
══════════════════════════════════════════════════════════
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  7. VECTOR STORE SEARCH (fault_finder uses this)     │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: status                                     │
│    data: {"stage":"searching"}                       │
│                                                      │
│  POST api.openai.com/v1/vector_stores/vs_690.../search│
│  Headers: Authorization: Bearer $OPENAI_API_KEY      │
│  Body: {                                             │
│    query: "Why is my RCD tripping?",                 │
│    max_num_results: 5,                               │
│    rewrite_query: true                               │
│  }                                                   │
│  Timeout: 10 seconds                                 │
│                                                      │
│  ← Response: {                                       │
│    data: [                                           │
│      { filename: "Operation Of RCDs.md",             │
│        score: 0.89,                                  │
│        content: [{ type: "text", text: "..." }] },   │
│      { filename: "Fault Finding Guide.md",           │
│        score: 0.72,                                  │
│        content: [{ type: "text", text: "..." }] },   │
│      ...up to 5 results                              │
│    ]                                                 │
│  }                                                   │
│                                                      │
│  Filter: remove score < 0.3                          │
│  Format as XML: <sources><result>...</result></sources>│
│  Truncate to 8000 chars                              │
│                                                      │
│  On error: vectorContext = undefined (silent fail)    │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  8. SYNTHESIZE (call OpenAI Chat Completions)        │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: status                                     │
│    data: {"stage":"synthesizing"}                     │
│                                                      │
│  POST api.openai.com/v1/chat/completions             │
│  Body: {                                             │
│    model: "gpt-4o",                                  │
│    stream: true,                                     │
│    response_format: { type: "json_object" },         │
│    messages: [                                       │
│      {                                               │
│        role: "developer",                            │
│        content: "Use the following knowledge base    │
│          sources to inform your response...\n\n      │
│          <sources>                                   │
│            <result file_name='Operation Of RCDs.md'> │
│              <content>RCD testing procedures...</content>│
│            </result>                                 │
│            ...                                       │
│          </sources>"                                 │
│      },                                              │
│      {                                               │
│        role: "system",                               │
│        content: "SYSTEM PROMPT — Trade Guru..."      │
│      },                                              │
│      {                                               │
│        role: "user",                                 │
│        content: "Why is my RCD tripping?"            │
│      }                                               │
│    ]                                                 │
│  }                                                   │
│                                                      │
│  OpenAI streams character-by-character:              │
│    data: {"choices":[{"delta":{"content":"{"}}]}     │
│    data: {"choices":[{"delta":{"content":"\"b"}}]}   │
│    ... (accumulates full JSON in memory buffer)      │
│    data: {"choices":[{"finish_reason":"stop"}],      │
│           "usage":{"prompt_tokens":N,                │
│                    "completion_tokens":N}}            │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  9. PARSE & EMIT BLOCKS                              │
│                                                      │
│  On finish_reason="stop":                            │
│  Parse accumulated buffer as JSON:                   │
│  {                                                   │
│    "blocks": [                                       │
│      { "type": "warning", "content": "...",          │
│        "severity": "critical" },                     │
│      { "type": "text", "content": "An RCD trips     │
│        when it detects an imbalance..." },           │
│      { "type": "step_list", "steps": [               │
│        { "step_number": 1, "title": "Safety Gate",  │
│          "description": "Isolate circuit...",        │
│          "safety_note": "Verify dead" },             │
│        { "step_number": 2, "title": "First Test",   │
│          "description": "IR @500V..." }              │
│      ]},                                             │
│      { "type": "tip", "content": "...",              │
│        "category": "troubleshooting" },              │
│      { "type": "reference",                          │
│        "source": "Operation Of RCDs.md",             │
│        "standard": "RCD Testing",                    │
│        "section": "Step-by-Step Procedures" }        │
│    ]                                                 │
│  }                                                   │
│                                                      │
│  → CLIENT RECEIVES (rapid burst):                    │
│    event: block                                      │
│    data: {"type":"warning","content":"..."}           │
│                                                      │
│    event: block                                      │
│    data: {"type":"text","content":"..."}              │
│                                                      │
│    event: block                                      │
│    data: {"type":"step_list","steps":[...]}           │
│                                                      │
│    event: block                                      │
│    data: {"type":"tip","content":"..."}               │
│                                                      │
│    event: block                                      │
│    data: {"type":"reference","source":"..."}          │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  10. DONE EVENT                                      │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: done                                       │
│    data: {                                           │
│      "response_id": "resp_1773498842648_164de978",   │
│      "usage": {                                      │
│        "input_tokens": 4200,                         │
│        "output_tokens": 850                          │
│      },                                              │
│      "cached": false                                 │
│    }                                                 │
│                                                      │
│  Stream closed.                                      │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  11. POST-STREAM (fire-and-forget)                   │
│                                                      │
│  a) Classify category:                               │
│     Keyword match (score ≥ 0.6) → "rcd"              │
│     If < 0.6 → "other" (GPT-4o-mini background job)  │
│                                                      │
│  b) Cache response (if finish_reason == "stop"       │
│     AND buffer < 1MB):                               │
│     Redis SET cache:{hash}:fault_finder:{identity}   │
│     TTL: 24 hours                                    │
│     Value: { blocks, token_usage, created_at,        │
│              category }                              │
│                                                      │
│  c) Log analytics:                                   │
│     Redis SET analytics:{date}:{request_id}          │
│     TTL: 90 days                                     │
│     Value: { question, mode, tokens_in, tokens_out,  │
│       cost_usd, category, region, platform,          │
│       device_id, user_id, latency_ms, cached,        │
│       response_id, timestamp }                       │
│                                                      │
│  d) Increment stats counters:                        │
│     stats:total_queries, stats:tokens:{date},        │
│     stats:platform:{platform}:{date},                │
│     stats:category:{category}:{date}                 │
└──────────────────────────────────────────────────────┘
```

---

## Cached Response Flow (Cache Hit)

```
  CACHE HIT at step 4
       │
       ▼
┌──────────────────────────────────────────────────────┐
│  QUOTA CHECK (same Lua script)                       │
│  ← 429 if exceeded                                   │
│                                                      │
│  NO vector search (skip steps 7-8)                   │
│  NO status events (no searching/synthesizing)        │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: block (for each cached block)              │
│    event: done { ..., "cached": true }               │
│                                                      │
│  Log analytics with cached: true                     │
│  No cache write (already cached)                     │
└──────────────────────────────────────────────────────┘
```

---

## Error Flows

| Error | When | Client Receives |
|-------|------|-----------------|
| 401 Unauthorized | Bad device ID, unregistered device | `{"error":{"code":"UNAUTHORIZED",...}}` |
| 400 Bad Request | Invalid mode/platform/messages | `{"error":{"code":"INVALID_REQUEST",...}}` |
| 429 Quota Exceeded | Daily limit hit | `{"error":{"code":"QUOTA_EXCEEDED","current":10,"limit":10}}` |
| 500 Internal Error | Unhandled exception | `{"error":{"code":"INTERNAL_ERROR",...}}` |
| Mid-stream error | OpenAI failure/timeout | `event: error\ndata: {"code":"STREAM_INTERRUPTED",...}` |

---

## Typical Latency Breakdown

| Step | Duration |
|------|----------|
| Auth + validation | ~50ms |
| Cache check | ~20ms |
| Quota check | ~20ms |
| Vector store search | ~500-2000ms |
| OpenAI streaming (time to finish) | ~3000-8000ms |
| Block emission | ~5ms |
| Post-stream tasks | ~100ms (async) |
| **Total (cache miss)** | **~4-10 seconds** |
| **Total (cache hit)** | **~100ms** |
