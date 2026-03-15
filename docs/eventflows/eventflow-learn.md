# Event Flow: Learn Mode

**Mode:** `learn`
**Endpoint:** `POST /api/v1/chat`
**Runtime:** Vercel Edge Runtime
**Vector Store:** YES (127-page electrical knowledge base)
**System Prompt:** Trade Guru identity + Educator Mode Enhancements + Blocks JSON wrapper

---

## Flow Diagram

```
USER SENDS MESSAGE
"Explain insulation resistance testing"
       │
       ▼
┌──────────────────────────────────────────────────────┐
│  1. PRE-FLIGHT                                       │
│  2. AUTHENTICATE (same as fault_finder)              │
│  3. VALIDATE REQUEST (mode = "learn" ✓)              │
│  4. CACHE CHECK (key: cache:{hash}:learn:{identity}) │
│  5. QUOTA CHECK (same limits as fault_finder)        │
└──────────────────────┬───────────────────────────────┘
                       │ (identical to fault_finder
                       │  steps 1-5)
                       ▼
┌──────────────────────────────────────────────────────┐
│  6. BUILD SYSTEM PROMPT                              │
│                                                      │
│  buildSystemPrompt('learn') returns:                 │
│  ┌────────────────────────────────────────────────┐  │
│  │ SHARED_PREAMBLE (144 lines)                    │  │
│  │ - Same Trade Guru identity as fault_finder     │  │
│  │ - Same safety governance                       │  │
│  │ - Same on-site fault protocol                  │  │
│  ├────────────────────────────────────────────────┤  │
│  │ EDUCATOR MODE ENHANCEMENTS                     │  │
│  │ 1. Principle-First Teaching (WHY before HOW)   │  │
│  │ 2. Step-by-Step Depth (sub-steps, sensory)     │  │
│  │ 3. Common Mistakes (what beginners do wrong)   │  │
│  │ 4. Meter Technique Details (jacks, dial, hold) │  │
│  │ 5. Visual Learning Aids (component markings)   │  │
│  │ 6. Real-World Context (job site scenarios)     │  │
│  │ 7. Safety Explanations (consequences)          │  │
│  │ 8. Apprentice-Friendly Language                │  │
│  ├────────────────────────────────────────────────┤  │
│  │ BLOCKS_WRAPPER                                 │  │
│  │ - "Respond with { blocks: [...] } JSON"        │  │
│  │ - 12 block type definitions                    │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  KEY DIFFERENCE from fault_finder:                   │
│  - Adds 8 educator enhancements between preamble    │
│    and blocks wrapper                                │
│  - Responses are more detailed, more educational     │
│  - Explains WHY before HOW at every step             │
│  - Includes meter technique detail (which jacks,     │
│    dial position, hold time, good vs bad readings)   │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
══════════════════════════════════════════════════════════
  SSE STREAM BEGINS
══════════════════════════════════════════════════════════
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  7. VECTOR STORE SEARCH (learn mode uses this)       │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: status                                     │
│    data: {"stage":"searching"}                       │
│                                                      │
│  POST api.openai.com/v1/vector_stores/vs_690.../search│
│  Body: {                                             │
│    query: "Explain insulation resistance testing",   │
│    max_num_results: 5,                               │
│    rewrite_query: true                               │
│  }                                                   │
│                                                      │
│  ← Returns relevant educational content:             │
│    - "Insulation Resistance Testing.md" (score 0.91) │
│    - "Testing Equipment Guide.md" (score 0.78)       │
│    - "Electrical Safety Procedures.md" (score 0.65)  │
│                                                      │
│  Format → <sources>XML</sources>                     │
│  Filter score < 0.3, truncate 8000 chars             │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  8. SYNTHESIZE                                       │
│                                                      │
│  → CLIENT RECEIVES:                                  │
│    event: status                                     │
│    data: {"stage":"synthesizing"}                     │
│                                                      │
│  POST api.openai.com/v1/chat/completions             │
│  Messages:                                           │
│  [                                                   │
│    { role: "developer", content: "<sources>..." },   │
│    { role: "system", content: "Trade Guru + EDU..." },│
│    { role: "user", content: "Explain insulation..." }│
│  ]                                                   │
│                                                      │
│  OpenAI accumulates full JSON, then on finish:       │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  9. PARSE & EMIT BLOCKS                              │
│                                                      │
│  EXPECTED LEARN MODE BLOCK TYPES:                    │
│                                                      │
│  → event: block {"type":"text"}                      │
│    Core concept intro (principle-first)               │
│                                                      │
│  → event: block {"type":"text"}                      │
│    WHY this matters in real electrical work           │
│                                                      │
│  → event: block {"type":"formula"}                   │
│    Key electrical formula with variables              │
│    e.g. R = V/I, insulation resistance formula       │
│                                                      │
│  → event: block {"type":"step_list"}                 │
│    Detailed procedure with sub-steps:                │
│    - Which meter jacks to use (COM, VΩ)              │
│    - Dial position and range                         │
│    - How long to hold probes                         │
│    - What good vs bad readings look like             │
│                                                      │
│  → event: block {"type":"diagram"}                   │
│    Circuit diagram or wiring layout description      │
│                                                      │
│  → event: block {"type":"table"}                     │
│    Comparison of related concepts                    │
│    e.g. cable types vs insulation resistance values  │
│                                                      │
│  → event: block {"type":"warning"}                   │
│    Safety with CONSEQUENCES explained:               │
│    "If you skip this, you could..."                  │
│                                                      │
│  → event: block {"type":"tip"}                       │
│    Common mistakes + memory aids                     │
│    category: "best_practice" or "troubleshooting"    │
│                                                      │
│  → event: block {"type":"reference"}                 │
│    Knowledge base source citation                    │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│  10. DONE + POST-STREAM                              │
│                                                      │
│  → event: done {"response_id":"...","cached":false}  │
│                                                      │
│  Cache, classify, log analytics (same as fault_finder)│
│  Cache key: cache:{hash}:learn:{identity}            │
└──────────────────────────────────────────────────────┘
```

---

## Key Differences from Fault Finder

| Aspect | Fault Finder | Learn |
|--------|-------------|-------|
| System prompt | Preamble + Blocks wrapper | Preamble + **8 Educator Enhancements** + Blocks wrapper |
| Vector search | YES | YES |
| Response style | Concise, iterative, one-action-at-a-time | Detailed, principle-first, sub-steps, visual aids |
| Block emphasis | warning, step_list, tip | text (explanations), formula, diagram, table, step_list |
| Safety approach | "State isolation, verify dead" | "If you skip verification of dead, you could complete a circuit through your body, causing cardiac arrest" |
| Meter detail | "Check continuity" | "COM jack → black lead, VΩ jack → red lead, dial to 200Ω range, hold 3 seconds for stable reading, good = <1Ω, bad = OL" |
| Language | Field language, direct | Apprentice-friendly, defines terms on first use |
| Cache key | cache:{hash}:**fault_finder**:{id} | cache:{hash}:**learn**:{id} |

---

## Typical Response Block Pattern (Learn Mode)

```
event: block  → text (principle explanation — WHY)
event: block  → formula (key formula with worked example)
event: block  → step_list (detailed procedure with meter technique)
event: block  → diagram (circuit layout description)
event: block  → table (comparison or spec reference)
event: block  → warning (safety with consequence explanation)
event: block  → tip (common mistakes + memory aid)
event: block  → tip (pro insight from field)
event: block  → reference (knowledge base citation)
event: done
```
