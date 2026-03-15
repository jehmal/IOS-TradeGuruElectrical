# Event Flow: Research Mode

**Mode:** `research`
**Endpoint:** `POST /api/v1/chat`
**Runtime:** Vercel Edge Runtime
**Vector Store:** NO (research uses web search knowledge, not the local vector store)
**System Prompt:** Trade Guru Research Assistant identity + Blocks JSON wrapper

---

## Flow Diagram

```
USER SENDS MESSAGE
"What are the specs for a Schneider iC60N 20A MCB?"
       │
       ▼
┌──────────────────────────────────────────────────────┐
│  1. PRE-FLIGHT                                       │
│  2. AUTHENTICATE (same as other modes)               │
│  3. VALIDATE REQUEST (mode = "research" ✓)           │
│  4. CACHE CHECK (key: cache:{hash}:research:{identity})│
│  5. QUOTA CHECK (same limits)                        │
└──────────────────────┬───────────────────────────────┘
                       │ (identical to fault_finder
                       │  steps 1-5)
                       ▼
┌──────────────────────────────────────────────────────┐
│  6. BUILD SYSTEM PROMPT                              │
│                                                      │
│  buildSystemPrompt('research') returns:              │
│  ┌────────────────────────────────────────────────┐  │
│  │ RESEARCH_PROMPT (standalone, NOT shared preamble)│ │
│  │                                                │  │
│  │ Identity:                                      │  │
│  │ "Trade Guru's Research Assistant — specialist   │  │
│  │  in finding technical information for licensed  │  │
│  │  electricians in Australia/New Zealand"         │  │
│  │                                                │  │
│  │ Capabilities:                                  │  │
│  │ - Search web for datasheets, manuals, diagrams │  │
│  │ - Cross-reference multiple sources             │  │
│  │ - Extract installation/servicing instructions  │  │
│  │ - Include specific values (V, A, Ω, IP, °C)   │  │
│  │                                                │  │
│  │ STRICT RULES:                                  │  │
│  │ - NEVER answer AS/NZS 3000 standards directly  │  │
│  │ - Earth fault loop → "review AS/NZ tables 8.1  │  │
│  │   and 8.2"                                     │  │
│  │ - Cable sizes → route to jcalc.net             │  │
│  │ - Maximum demand → route to jcalc.net          │  │
│  │ - NEVER cite internal knowledge base           │  │
│  │ - NEVER fabricate URLs                         │  │
│  │ - Source with no URL > fabricated URL           │  │
│  │                                                │  │
│  ├────────────────────────────────────────────────┤  │
│  │ BLOCKS_WRAPPER                                 │  │
│  │ - "Respond with { blocks: [...] } JSON"        │  │
│  │ - 12 block type definitions                    │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  KEY DIFFERENCE: Research has its OWN prompt          │
│  (not shared preamble + enhancements). It's a        │
│  research tool, not a fault-finder or trainer.        │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
══════════════════════════════════════════════════════════
  SSE STREAM BEGINS
══════════════════════════════════════════════════════════
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  7. NO VECTOR STORE SEARCH                           │
│                                                      │
│  ╔══════════════════════════════════════════════════╗ │
│  ║  mode === 'research' → SKIP vector search       ║ │
│  ║  NO event: status {"stage":"searching"}         ║ │
│  ║  NO event: status {"stage":"synthesizing"}      ║ │
│  ║  vectorContext = undefined                       ║ │
│  ╚══════════════════════════════════════════════════╝ │
│                                                      │
│  Research mode relies on the model's training data   │
│  and the system prompt's routing rules (jcalc.net,   │
│  AS/NZ tables) — NOT the local vector store.         │
│                                                      │
│  NOTE: The original web app's research mode used     │
│  OpenAI's built-in web_search tool via Responses API.│
│  The v1 endpoint does NOT have web search — it uses  │
│  chat completions only. Research responses are based  │
│  on model training data + system prompt guidance.     │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  8. CALL OPENAI (no vector context)                  │
│                                                      │
│  POST api.openai.com/v1/chat/completions             │
│  Messages:                                           │
│  [                                                   │
│    *** NO developer message (no vector context) ***  │
│    { role: "system", content: "Trade Guru Research   │
│      Assistant..." },                                │
│    { role: "user", content: "What are the specs      │
│      for a Schneider iC60N 20A MCB?" }               │
│  ]                                                   │
│                                                      │
│  Only 2 messages (system + user), not 3.             │
│  No developer role message since no vector context.  │
│                                                      │
│  OpenAI accumulates full JSON in buffer...           │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  9. PARSE & EMIT BLOCKS                              │
│                                                      │
│  EXPECTED RESEARCH MODE BLOCK TYPES:                 │
│                                                      │
│  → event: block {"type":"text"}                      │
│    Direct answer with key specs/findings              │
│    "The Schneider iC60N is a Type C MCB rated        │
│     at 20A, 230/400V AC, 6kA breaking capacity"     │
│                                                      │
│  → event: block {"type":"table"}                     │
│    Specifications table:                              │
│    | Label | Value |                                 │
│    | Rated Current | 20A |                           │
│    | Breaking Capacity | 6kA |                       │
│    | Curve Type | C |                                │
│                                                      │
│  → event: block {"type":"step_list"}                 │
│    Installation/servicing instructions               │
│    (if user asked how to install/wire/service)       │
│                                                      │
│  → event: block {"type":"warning"}                   │
│    Safety warnings (if applicable)                   │
│                                                      │
│  → event: block {"type":"tip"}                       │
│    Practical tips from research                      │
│                                                      │
│  → event: block {"type":"link"}                      │
│    Source URLs (manufacturer pages, datasheets)       │
│    NOTE: May be from training data, not live search  │
│                                                      │
│  → event: block {"type":"reference"}                 │
│    Standards citations (AS/NZS references)           │
│                                                      │
│  ROUTING RESPONSES (when triggered):                 │
│  → event: block {"type":"text"}                      │
│    "For cable sizing calculations, please use:       │
│     https://www.jcalc.net/cable-sizing-calculator..."│
│  → event: block {"type":"link"}                      │
│    { url: "https://www.jcalc.net/...", title: "..." }│
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  10. DONE + POST-STREAM                              │
│                                                      │
│  → event: done {"response_id":"...","cached":false}  │
│                                                      │
│  Cache key: cache:{hash}:research:{identity}         │
│  Classify, log analytics (same as other modes)       │
└──────────────────────────────────────────────────────┘
```

---

## Key Differences from Fault Finder & Learn

| Aspect | Fault Finder | Learn | Research |
|--------|-------------|-------|----------|
| System prompt | Shared preamble + Blocks | Shared preamble + Educator + Blocks | **Standalone research prompt** + Blocks |
| Vector search | YES | YES | **NO** |
| Status events | searching → synthesizing → streaming | searching → synthesizing → streaming | **NONE** (direct to streaming) |
| Messages to OpenAI | 3 (developer + system + user) | 3 (developer + system + user) | **2 (system + user only)** |
| Identity | "World's Greatest Electrician" | "World's Greatest Electrician" | **"Research Assistant"** |
| Response style | Iterative fault diagnosis | Educational, principle-first | **Evidence-based, source-cited** |
| Routing rules | None | None | **jcalc.net for cables, AS/NZ tables for impedance** |
| URL rules | N/A | N/A | **Never fabricate, never cite internal sources** |
| Block emphasis | warning, step_list | formula, diagram, table | **text, table, link, reference** |
| Cache key mode | fault_finder | learn | research |

---

## What Client Sees (Timeline)

```
0ms    ─── Request sent ───
50ms   ─── Auth + validation complete ───
100ms  ─── Cache miss, quota OK ───
       *** NO "Searching knowledge base..." status ***
       *** NO "Synthesizing response..." status ***
100ms  ─── OpenAI call starts (streaming) ───
       *** User sees typing indicator only ***
3-8s   ─── finish_reason: stop ───
       ─── Blocks arrive (rapid burst) ───
       ─── Done event ───
```

Research mode feels faster to start because there's no vector search delay (~500-2000ms saved). But the user doesn't see status events, so they only see the typing indicator until blocks arrive.

---

## Limitation: No Web Search

The original web app's research mode used the **OpenAI Responses API** with the `web_search` tool, which gave it access to live web data (datasheets, manufacturer pages, current pricing).

The v1 endpoint uses **Chat Completions** which does NOT have web search. Research responses are based on:
- GPT-4o's training data (knowledge cutoff)
- The system prompt's routing rules (jcalc.net links, AS/NZ table references)

This means research mode cannot:
- Find current product URLs (may hallucinate them)
- Access recently published datasheets
- Verify live web sources

**Future improvement:** Switch research mode to the Responses API with `web_search` tool enabled.

---

## Typical Response Block Pattern (Research Mode)

```
event: block  → text (direct answer with key specs)
event: block  → table (specifications)
event: block  → step_list (installation instructions, if asked)
event: block  → warning (safety warnings, if applicable)
event: block  → tip (practical tips)
event: block  → link (source URLs — may be from training data)
event: block  → reference (standards citations)
event: done
```
