# OpenAI Factcheck Report #2

**Date:** 2026-03-14  
**Scope:** Full endpoint inventory verification + methodology review for relay architecture  
**Source files:** `todo/inventory/1-ai-chat-endpoints.md`, `docs/openai-endpoint-factcheck.md`, `todo/inventory/2-analytics-relay.md`  
**Previous report:** `docs/openai-endpoint-factcheck.md` (factcheck #1)

## Methodology Recommendation

Use a mix, but standardize new conversational AI work on `POST /v1/responses`.

OpenAI's current Chat Completions reference explicitly says new projects should start with Responses. For this relay, that means:

- Use `POST /v1/responses` as the primary surface for text chat, structured outputs, multimodal/vision, and `file_search`.
- Keep dedicated resource endpoints for uploads and asset management: `/v1/files`, `/v1/vector_stores`, `/v1/vector_stores/{id}/files`, `/v1/audio/transcriptions`, `/v1/audio/speech`, and `/v1/images/generations`.
- Keep `POST /v1/chat/completions` only if you want compatibility with an existing client/parser or a temporary migration bridge.

For TradeGuru specifically, the relay should hide OpenAI surface differences from the clients. That removes most of the reason to build new client-facing flows on Chat Completions. The cleanest architecture is:

1. Relay routes stay stable at `tradeguru.com/api/v1/*`.
2. Relay normalizes outputs to your app's `blocks` contract.
3. Relay uses Responses internally for new chat/vision/search flows.
4. Relay keeps dedicated OpenAI endpoints for files, vector stores, STT, TTS, and image generation.

## Endpoint Verdicts

### Endpoint 1: `POST /v1/chat/completions` (streaming)
- **URL:** `https://api.openai.com/v1/chat/completions`
- **Status:** NEEDS UPDATE
- **Current best practice:** Still valid for streaming chat, and `stream: true` is still the flag that enables streaming. The current docs also still support Chat Completions structured outputs via `response_format: { type: "json_schema", json_schema: { name, schema, strict } }`. However, OpenAI now recommends Responses for new projects.
- **Changes needed in inventory:** Keep the endpoint as a compatibility option, but update the note to say Responses is the preferred new surface. Fix the SSE description to the current `chat.completion.chunk` shape: each `data:` frame carries a full chunk object with `id`, `object`, `created`, `model`, and `choices[].delta`; the stream ends with `data: [DONE]`. Also note that if `stream_options.include_usage` is enabled, an extra usage chunk can appear before `[DONE]`.

### Endpoint 2: `POST /v1/chat/completions` (non-streaming)
- **URL:** `https://api.openai.com/v1/chat/completions`
- **Status:** NEEDS UPDATE
- **Current best practice:** Still valid and `choices[0].message.content` is still the main text field for normal assistant output. For your structured-output pattern, that content is still a JSON string that you parse into `{ "blocks": [...] }`. OpenAI's recommendation for new projects is still Responses.
- **Changes needed in inventory:** Keep the note that `choices[0].message.content` is the field you parse, but add that clients should also handle `refusal`, `annotations`, and tool-related fields if enabled later. Update the methodology note so this is a fallback/compatibility surface, not the preferred primary API.

### Endpoint 3: `POST /v1/vector_stores` (create)
- **URL:** `https://api.openai.com/v1/vector_stores`
- **Status:** NEEDS UPDATE
- **Current best practice:** This is still the correct endpoint. Current docs still show `expires_after` support. The current create body is centered on `name`, `file_ids`, `expires_after`, `chunking_strategy`, and `metadata`.
- **Changes needed in inventory:** Keep `expires_after`. Add `metadata` and `chunking_strategy` to the canonical parameter list. Remove `description` unless you re-confirm it from the live reference before implementation; it is not surfaced in the current create reference I checked.

### Endpoint 4: `POST /v1/vector_stores/{vs_id}/files`
- **URL:** `https://api.openai.com/v1/vector_stores/{vector_store_id}/files`
- **Status:** CORRECT
- **Current best practice:** This is still the correct endpoint for attaching an uploaded file to a vector store. `file_id` is required; `attributes` and `chunking_strategy` are supported optional fields.
- **Changes needed in inventory:** No structural change required. Tighten the wording so `file_id` is marked required and `attributes` / `chunking_strategy` are marked optional.

### Endpoint 5: `POST /v1/files` (upload)
- **URL:** `https://api.openai.com/v1/files`
- **Status:** NEEDS UPDATE
- **Current best practice:** This is still the correct upload endpoint, and `purpose: "assistants"` is still valid. The current files reference also documents platform limits around uploaded files.
- **Changes needed in inventory:** Keep `purpose: "assistants"`, but add the current limits: up to 512 MB per file and up to 100 GB total uploaded per organization. Also add a note that accepted file types depend on the downstream feature; for vector-store/file-search use, validate against the current file-search supported document/code formats from the OpenAI docs instead of a project-local guess.

### Endpoint 6: `POST /v1/responses` (`file_search`)
- **URL:** `https://api.openai.com/v1/responses`
- **Status:** NEEDS UPDATE
- **Current best practice:** The Responses request shape is still correct for file search: `model`, `input`, `tools: [{ type: "file_search", vector_store_ids: [...] }]`, with optional search controls such as `max_num_results`, `filters`, and ranking options. `include: ["file_search_call.results"]` is still the documented way to get raw search excerpts/results back in the response.
- **Changes needed in inventory:** Keep the Responses endpoint and request shape, but update the note that says native file search requires Responses only. Current docs show `file_search` availability on Responses and Chat Completions. Responses is still the better default for this relay because OpenAI recommends it for new projects and because its output model makes tool calls/results explicit.

### Endpoint 7: `POST /v1/chat/completions` (vision/multimodal)
- **URL:** `https://api.openai.com/v1/chat/completions`
- **Status:** NEEDS UPDATE
- **Current best practice:** The `messages[].content` pattern with `{ type: "text" }` plus `{ type: "image_url", image_url: { url, detail } }` is still current for Chat Completions, including base64 `data:` URLs. Inference from the current request schema: combining image input and `response_format.json_schema` in the same Chat Completions request is valid, because both features are documented on the same create request surface. For new builds, Responses is the newer multimodal surface.
- **Changes needed in inventory:** The request example itself is fine, but move the architectural recommendation toward Responses for new multimodal work. If you keep this Chat Completions path, explicitly note that it is a compatibility route, not the preferred new default.

### Endpoint 8: `POST /v1/audio/transcriptions` (STT)
- **URL:** `https://api.openai.com/v1/audio/transcriptions`
- **Status:** NEEDS UPDATE
- **Current best practice:** This is still the correct STT endpoint. `whisper-1` is still available, but the current model docs position `gpt-4o-transcribe` and `gpt-4o-mini-transcribe` as the more accurate newer models; there is also a `gpt-4o-transcribe-diarize` variant for speaker labeling. Supported upload formats remain the standard Audio API set: `flac`, `mp3`, `mp4`, `mpeg`, `mpga`, `m4a`, `ogg`, `wav`, and `webm`. Max file size remains 25 MB.
- **Changes needed in inventory:** Change the recommendation so new implementation starts with `gpt-4o-transcribe` or `gpt-4o-mini-transcribe`, not `whisper-1`, unless Whisper compatibility is a specific requirement. Add the supported audio formats and the 25 MB limit.

### Endpoint 9: `POST /v1/audio/speech` (TTS)
- **URL:** `https://api.openai.com/v1/audio/speech`
- **Status:** NEEDS UPDATE
- **Current best practice:** This is still the correct TTS endpoint. `tts-1` is still available, and `response_format: "aac"` is still valid. Current docs also expose newer TTS options, especially `gpt-4o-mini-tts`, which is the better default for new work if you want the newer speech model rather than the older `tts-*` family.
- **Changes needed in inventory:** Keep `tts-1` as supported, but update the recommendation to prefer `gpt-4o-mini-tts` for new implementation. Refresh the voice list from the live speech reference before hardcoding validation in the relay; the current docs expose an expanded voice set beyond the original legacy subset, and `nova` remains valid.

### Endpoint 10: `POST /v1/images/generations`
- **URL:** `https://api.openai.com/v1/images/generations`
- **Status:** NEEDS UPDATE
- **Current best practice:** The endpoint is still correct, but `gpt-image-1` is no longer the latest image model. The current model catalog shows `gpt-image-1.5` as the latest image generation model, with `gpt-image-1` described as the previous model and `gpt-image-1-mini` as a cheaper variant. The current parameter set is built around `prompt` plus optional image-generation fields such as `model`, `size`, `quality`, `background`, `output_format`, `output_compression`, `moderation`, `n`, and `user`; `response_format` is not the current GPT Image parameter for this endpoint.
- **Changes needed in inventory:** Change the default image model from `gpt-image-1` to `gpt-image-1.5` unless you have a cost/compatibility reason to stay on the older model. Keep `output_format`, not `response_format`. Expand the parameter list to include the current GPT Image options used by the official docs.

## Model & Pricing Update

- `gpt-4o` is still a live model alias. Current docs still list `gpt-4o` plus dated snapshots such as `gpt-4o-2024-11-20`.
- Current `gpt-4o` pricing is still:
  - Input: `$2.50 / 1M tokens`
  - Cached input: `$1.25 / 1M tokens`
  - Output: `$10.00 / 1M tokens`
- OpenAI's current model catalog is no longer centered on GPT-4o alone:
  - `gpt-5.4` is the current flagship recommendation for complex reasoning/coding.
  - `gpt-5.1` is the current GPT-5-series flagship for coding/agentic work with configurable reasoning.
  - `gpt-4.1` is the current "smartest non-reasoning model" and supports tool calling, streaming, and structured outputs.
  - `gpt-4o` remains a valid versatile multimodal model and still supports streaming, function calling, and structured outputs.
- For audio:
  - `gpt-4o-transcribe`, `gpt-4o-mini-transcribe`, and `gpt-4o-transcribe-diarize` are current STT models to consider before `whisper-1`.
  - `gpt-4o-mini-tts` is the current newer TTS option; `tts-1` and `tts-1-hd` remain available.
- For images:
  - `gpt-image-1.5` is now the latest image generation model.
  - `gpt-image-1` is still available, but the docs label it as the previous image model.
  - `gpt-image-1-mini` is the cheaper image variant.

## Summary
- Endpoints checked: 10
- Correct as-is: 1
- Need updates: 9
- Critical (would fail at runtime): 0
- Warnings (suboptimal but functional): 9

## Recommended Changes to Inventory

1. Rewrite the top-level methodology note in `todo/inventory/1-ai-chat-endpoints.md` so Responses is the preferred new chat/tool/multimodal surface and Chat Completions is a compatibility path.
2. Update endpoints 1 and 2 to say Chat Completions still works, but OpenAI recommends Responses for new projects.
3. Replace the simplified streaming description in endpoint 1 with the current `chat.completion.chunk` SSE shape and note the terminal `data: [DONE]` frame.
4. Keep `response_format.json_schema` on Chat Completions as valid, but document `strict: true` as part of the canonical structured-output pattern.
5. Keep `choices[0].message.content` as the non-streaming parse target, but note the presence of `refusal`, `annotations`, and tool-related fields.
6. Update endpoint 3's create-vector-store parameters to the current set: `name`, `file_ids`, `expires_after`, `chunking_strategy`, and `metadata`.
7. Remove `description` from endpoint 3 unless you re-verify it in the live vector-store create reference at implementation time.
8. Mark endpoint 4's `file_id` as required and `attributes` / `chunking_strategy` as optional.
9. Keep `purpose: "assistants"` for endpoint 5, but add the 512 MB per-file limit and 100 GB org-level storage limit.
10. Add a file-type validation note for endpoint 5 that points to the current OpenAI file-search supported format list instead of freezing a partial custom whitelist in this document.
11. Keep endpoint 6 on Responses, but delete the claim that file search requires Responses only; current docs also show Chat Completions support.
12. Keep `include: ["file_search_call.results"]` on endpoint 6 when the relay needs raw search excerpts in addition to the model's synthesized answer.
13. Extend endpoint 6's documented tool parameters to include current optional search controls such as `max_num_results`, filters, and ranking options.
14. Keep endpoint 7's `image_url` content-part structure, but move the architecture recommendation toward Responses for new multimodal flows.
15. Update endpoint 8 to recommend `gpt-4o-transcribe` or `gpt-4o-mini-transcribe` as the default STT model, with `whisper-1` as a compatibility fallback.
16. Add endpoint 8's supported audio formats and 25 MB upload limit.
17. Update endpoint 9 to recommend `gpt-4o-mini-tts` for new work while keeping `tts-1` listed as supported.
18. Refresh endpoint 9's documented voice list from the live speech reference before implementing enum validation in the relay.
19. Keep `response_format: "aac"` as valid on endpoint 9.
20. Update endpoint 10's default model from `gpt-image-1` to `gpt-image-1.5`, unless you deliberately choose the older model for cost or compatibility reasons.
21. Keep `output_format`, not `response_format`, for endpoint 10.
22. Expand endpoint 10's parameter list to the current GPT Image fields used by the official docs.
23. Replace hardcoded "`gpt-4o` everywhere" assumptions with route-level model configuration so the relay can swap between `gpt-4o`, `gpt-4.1`, GPT-5-family models, and specialized audio/image models without inventory churn.

## Sources

- Chat Completions API reference: https://platform.openai.com/docs/api-reference/chat/create
- Chat Completions reference on the current developer docs: https://developers.openai.com/api/reference/chat/create
- Responses API reference: https://platform.openai.com/docs/api-reference/responses/create
- File search guide: https://developers.openai.com/api/docs/guides/tools-file-search
- Vector stores API reference: https://platform.openai.com/docs/api-reference/vector-stores
- Files API reference: https://platform.openai.com/docs/api-reference/files/create
- Audio API reference: https://platform.openai.com/docs/api-reference/audio
- Speech-to-text guide: https://developers.openai.com/topics/audio
- Text-to-speech guide: https://platform.openai.com/docs/guides/text-to-speech
- Image generation guide: https://developers.openai.com/topics/imagegen
- Models catalog: https://developers.openai.com/api/docs/models
- GPT-4o model page: https://developers.openai.com/api/docs/models/gpt-4o
- GPT-4.1 model page: https://developers.openai.com/api/docs/models/gpt-4.1
- GPT-5.1 model page: https://developers.openai.com/api/docs/models/gpt-5.1
- GPT-5.4 model page: https://developers.openai.com/api/docs/models/gpt-5.4
- GPT Image 1.5 model page: https://developers.openai.com/api/docs/models/gpt-image-1.5
- GPT Image 1 model page: https://developers.openai.com/api/docs/models/gpt-image-1
- GPT-4o Transcribe model page: https://developers.openai.com/api/docs/models/gpt-4o-transcribe
- GPT-4o mini Transcribe model page: https://developers.openai.com/api/docs/models/gpt-4o-mini-transcribe
- GPT-4o Transcribe Diarize model page: https://developers.openai.com/api/docs/models/gpt-4o-transcribe-diarize
- Whisper model page: https://developers.openai.com/api/docs/models/whisper-1
- GPT-4o mini TTS model page: https://developers.openai.com/api/docs/models/gpt-4o-mini-tts
- TTS-1 model page: https://developers.openai.com/api/docs/models/tts-1
