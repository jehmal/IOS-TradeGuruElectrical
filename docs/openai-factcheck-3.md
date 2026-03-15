Current docs support your plan, but one top-level correction is important: the Chat Completions reference now says new projects should start with [Responses](https://platform.openai.com/docs/api-reference/responses) rather than Chat Completions. That said, the [Retrieval guide](https://platform.openai.com/docs/guides/retrieval) explicitly documents the manual pattern of `vectorStores.search(...)` followed by `chat.completions.create(...)`.

**Docs-aligned TypeScript shapes**
```ts
import OpenAI from "openai";

const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

type ComparisonFilter = {
  key: string;
  type: "eq" | "ne" | "gt" | "gte" | "lt" | "lte" | "in" | "nin";
  value: string | number | boolean | Array<string | number>;
};

type CompoundFilter = {
  type: "and" | "or";
  filters: Array<ComparisonFilter | CompoundFilter>;
};

type VectorStoreSearchParams = {
  vector_store_id: string;
  query: string | string[];
  filters?: ComparisonFilter | CompoundFilter;
  max_num_results?: number;
  ranking_options?: {
    ranker?: "none" | "auto" | "default-2024-11-15";
    score_threshold?: number;
  };
  rewrite_query?: boolean;
};

type VectorStoreSearchResult = {
  file_id: string;
  filename?: string;   // API reference
  file_name?: string;  // retrieval guide formatter example is inconsistent here
  score: number;
  attributes: Record<string, string | number | boolean>;
  content: Array<{ type: "text"; text: string }>;
};

type VectorStoreSearchResponse = {
  object: "vector_store.search_results.page";
  search_query: string | string[]; // docs inconsistent here too
  data: VectorStoreSearchResult[];
  has_more: boolean;
  next_page: string | null;
};
```

**Claim 1: Vector Store Search endpoint**
- **Status:** PARTIALLY CORRECT
- **Official docs say:** The endpoint is `POST /v1/vector_stores/{vector_store_id}/search`. `query` is `string or array of string`, not just `string`. Optional params are `filters`, `max_num_results`, `ranking_options`, and `rewrite_query`. The response is not a bare array; it is a page object with `object`, `search_query`, `data`, `has_more`, and `next_page`. Each result has `file_id`, `filename`, `score`, `attributes`, and `content: [{ type: "text", text: string }]`. Sources: [API reference](https://platform.openai.com/docs/api-reference/vector-stores/search), [Retrieval guide](https://platform.openai.com/docs/guides/retrieval)
- **Fix needed:** Add `ranking_options`; change `query` to `string | string[]`; change response shape from array to paginated object; change `file_name` to `filename` for the raw API response.
- **Code example:**
```ts
const res = await fetch(
  `https://api.openai.com/v1/vector_stores/vs_69017d88924081919fc6599a18ae2231/search`,
  {
    method: "POST",
    headers: {
      Authorization: `Bearer ${process.env.OPENAI_API_KEY!}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      query: "What is the return policy?",
      max_num_results: 5,
      rewrite_query: false,
    }),
  },
);

const results = (await res.json()) as VectorStoreSearchResponse;
```

**Claim 2: TypeScript/Node SDK method**
- **Status:** INCORRECT
- **Official docs say:** The Retrieval guide shows `await client.vectorStores.search({ vector_store_id: vector_store.id, query: userQuery })`. It is a top-level `vectorStores` namespace, not `client.beta.vectorStores.search(...)`, and not a positional signature like `search(vector_store_id, {...})`. Source: [Retrieval guide](https://platform.openai.com/docs/guides/retrieval)
- **Fix needed:** Use `client.vectorStores.search({ vector_store_id, query, ... })`.
- **Code example:**
```ts
import OpenAI from "openai";

const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

const results = await client.vectorStores.search({
  vector_store_id: "vs_69017d88924081919fc6599a18ae2231",
  query: "What does AS3000 say about neutral connections?",
  max_num_results: 5,
});
```

**Claim 3: Synthesizing responses pattern**
- **Status:** CORRECT
- **Official docs say:** The Retrieval guide explicitly shows: 1. call `client.vectorStores.search(...)`; 2. format `results.data`; 3. call `client.chat.completions.create(...)` with a `developer` message and a `user` message containing `Sources: ... Query: ...`. The Chat Completions reference still documents both `developer` and `system` roles; current examples use `developer`. Sources: [Retrieval guide](https://platform.openai.com/docs/guides/retrieval), [Chat Completions reference](https://platform.openai.com/docs/api-reference/chat/create-chat-completion), [Text generation guide](https://platform.openai.com/docs/guides/text-generation)
- **Fix needed:** None for the pattern. For current docs style, prefer `developer` + `user`.
- **Code example:**
```ts
const completion = await client.chat.completions.create({
  model: "gpt-4o",
  messages: [
    {
      role: "developer",
      content: "Produce a concise answer to the query based on the provided sources.",
    },
    {
      role: "user",
      content: `Sources: ${formattedResults}\n\nQuery: '${userQuery}'`,
    },
  ],
});
```

**Claim 4: Streaming compatibility**
- **Status:** PARTIALLY CORRECT
- **Official docs say:** Chat Completions returns “a streamed sequence of chat completion chunk objects if the request is streamed.” The same reference also documents `response_format`, including `json_object`, and notes that `json_object` is an older method and the model will not generate JSON without a `system` or `user` message telling it to do so. The [gpt-4o model page](https://platform.openai.com/docs/models/gpt-4o) lists streaming and structured outputs support. Sources: [Chat Completions reference](https://platform.openai.com/docs/api-reference/chat/create-chat-completion), [gpt-4o model page](https://platform.openai.com/docs/models/gpt-4o)
- **Fix needed:** `stream: true` is fine with manually injected retrieval context. For JSON output, prefer `json_schema` over `json_object`; if you keep `json_object`, add an explicit `system` or `user` instruction like “Return valid JSON only.”
- **Code example:**
```ts
const stream = await client.chat.completions.create({
  model: "gpt-4o",
  stream: true,
  response_format: { type: "json_object" },
  messages: [
    { role: "developer", content: "Use only the supplied sources." },
    { role: "user", content: `Return valid JSON only.\n\nSources: ${formattedResults}\n\nQuery: ${userQuery}` },
  ],
});
```

**Claim 5: Edge Runtime compatibility**
- **Status:** UNVERIFIABLE
- **Official docs say:** The JS SDK is documented for “server-side JavaScript environments like Node.js, Deno, or Bun.” Separately, the API overview says the REST APIs are usable “via HTTP in any environment that supports HTTP requests.” The docs do not mention Vercel Edge specifically and do not document Edge-specific SDK issues. Sources: [Libraries guide](https://platform.openai.com/docs/libraries), [API overview](https://platform.openai.com/docs/api-reference)
- **Fix needed:** If you want the most docs-backed path in Vercel Edge, use raw `fetch()` for both `/vector_stores/{id}/search` and `/chat/completions`. The SDK may work, but that is not explicitly guaranteed in the docs.
- **Code example:**
```ts
export const runtime = "edge";

const chatRes = await fetch("https://api.openai.com/v1/chat/completions", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${process.env.OPENAI_API_KEY!}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    model: "gpt-4o",
    stream: true,
    messages,
  }),
});
```

**Claim 6: Vector Store Search pricing**
- **Status:** UNVERIFIABLE
- **Official docs say:** The Retrieval guide prices vector stores by storage only: up to `1 GB` free, beyond that `$0.10/GB/day`. The pricing page separately lists `File search tool call` pricing as `$2.50 / 1k calls`, but that is for the Responses API `file_search` tool, not explicitly for direct `vector_stores.search`. No per-result fee is documented. Sources: [Retrieval guide](https://platform.openai.com/docs/guides/retrieval), [Pricing](https://platform.openai.com/docs/pricing)
- **Fix needed:** Do not assume a per-query or per-result fee for direct `vector_stores.search` from the current docs. Budget for vector-store storage plus normal model token costs. If you switch to Responses `file_search`, add the tool-call fee.

**Claim 7: Rate limits**
- **Status:** UNVERIFIABLE
- **Official docs say:** Rate limits are org/project level, vary by model, and are visible in the dashboard and in `x-ratelimit-*` headers. The File Search guide says the hosted `file_search` tool has a default rate limit of `100 calls per minute`. The docs do not publish a separate numeric rate limit for `vector_stores.search`, and they do not say it shares Chat Completions limits. Sources: [Rate limits guide](https://platform.openai.com/docs/guides/rate-limits), [File search guide](https://platform.openai.com/docs/guides/tools-file-search)
- **Fix needed:** Do not hard-code a published RPM for `vector_stores.search`; inspect your project limits and response headers instead.

**Claim 8: Required SDK version**
- **Status:** UNVERIFIABLE
- **Official docs say:** The current docs show `npm install openai` and show `client.vectorStores.search(...)`, but they do not state the minimum `openai` npm version that introduced this method. They also do not explicitly label `vectorStores.search` as GA vs beta in the SDK docs. Sources: [Libraries guide](https://platform.openai.com/docs/libraries), [Retrieval guide](https://platform.openai.com/docs/guides/retrieval)
- **Fix needed:** Pin a recent `openai` v4 release. If you need the exact introduction version, the main docs are insufficient; you would need the official SDK changelog/release history.

Two doc inconsistencies to watch:
- The search API schema says top-level `search_query` is `array of string`, but the example response shows a single string.
- The API reference uses `filename`, but the Retrieval guide’s sample formatter uses `result.file_name`.

What I found but could not fully verify from official docs: OpenAI’s main docs do not publish the minimum `openai` npm version for `vectorStores.search()`, and they do not explicitly document Vercel Edge Runtime support for the JS SDK.