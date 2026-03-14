# OpenAI Endpoint Factcheck Report

Date: 2026-03-14  
Source: [todo/inventory/1-ai-chat-endpoints.md](/mnt/c/users/jehma/desktop/Tradeguru-swft/todo/inventory/1-ai-chat-endpoints.md)  
Verified against: OpenAI API official documentation

## 1. `POST /v1/chat/completions` (streaming)
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: ISSUES. `response_format: { type: "json_schema", json_schema: { name, schema } }` is valid for GPT-4o-family Chat Completions, but `json_schema.strict: true` is the current best-practice if you want strict schema adherence. If `tools` means native OpenAI tools such as `file_search`, that is not valid on Chat Completions.
- Response shape verdict: ISSUES. Stream chunks are full `chat.completion.chunk` SSE objects, not just `{"choices":[{"delta":{"content":"..."}}]}`. Chunks may include `role`, `tool_calls`, `finish_reason`, and optional usage data.
- Deprecated? No. Newer preferred alternative for native tools/multimodal is `POST /v1/responses`.
- Missing params? None

## 2. `POST /v1/chat/completions` (non-streaming)
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: ISSUES. Structured-output syntax is valid, with the same `strict: true` recommendation. If you plan to use native OpenAI tools, Chat Completions is the wrong surface.
- Response shape verdict: ISSUES. The raw response is a full chat completion object. `choices[0].message.content` is the relevant field, but your example is oversimplified and omits other fields such as `role`, `refusal`, `annotations`, and possible `tool_calls`.
- Deprecated? No. Responses API is the newer alternative.
- Missing params? None

## 3. `POST /v1/vector_stores`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: CORRECT with omissions. `{ name, file_ids }` is valid. Current docs also support fields such as `description`, `metadata`, `expires_after`, and `chunking_strategy`.
- Response shape verdict: ISSUES. The actual vector store object includes more than `{ id, name, status }`, including object metadata and file counts.
- Deprecated? No
- Missing params? None

## 4. `POST /v1/vector_stores/{vector_store_id}/files`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: CORRECT with omissions. `{ file_id }` is valid; current docs also allow `attributes` and `chunking_strategy`.
- Response shape verdict: ISSUES. The returned `vector_store.file` object is larger than `{ id, status }` and includes fields like `vector_store_id`, usage bytes, timestamps, and error/status details.
- Deprecated? No
- Missing params? None

## 5. `POST /v1/files`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: CORRECT. Multipart upload with `file` and `purpose` is current, and `purpose: "assistants"` is still valid.
- Response shape verdict: ISSUES. The file object is more complete than `{ id, filename, bytes }`; it also includes fields like `object`, `purpose`, and timestamps.
- Deprecated? No
- Missing params? None

## 6. `POST /v1/chat/completions` with `file_search`
- URL verdict: INCORRECT. Native `file_search` belongs on `POST /v1/responses`, not `POST /v1/chat/completions`.
- Method verdict: CORRECT
- Request shape verdict: ISSUES. Current native tool format is `tools: [{ "type": "file_search", "vector_store_ids": ["vs_..."] }]`. Your nested shape `file_search: { vector_store_ids: [...] }` is not the current format, and the request should use `input` on Responses API, not Chat Completions `messages`.
- Response shape verdict: ISSUES. Current Responses output uses `file_search_call` items in `output`; it is not a Chat Completions `tool_calls` flow. Search hit details are not returned unless you request `include: ["file_search_call.results"]`.
- Deprecated? No
- Missing params? On the corrected Responses request, `model` and `input` are required. `include` is needed if you want raw search results back.

## 7. `POST /v1/chat/completions` (vision)
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: ISSUES. The multimodal `messages[].content` shape with `{ type: "text" }` plus `{ type: "image_url" }` is correct, including a base64 data URL, but the example omits required `model`. If you want structured JSON back, it also needs `response_format`.
- Response shape verdict: ISSUES. The raw response is still a chat completion object; “structured diagnosis blocks” are an application-level parse of `choices[0].message.content`, not the native envelope.
- Deprecated? No. Responses API is the newer multimodal surface.
- Missing params? `model` required. `response_format` required for the structured-output behavior you describe.

## 8. `POST /v1/audio/transcriptions`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: CORRECT. Multipart upload with `model`, `file`, and optional `language` is current, and `whisper-1` is still supported.
- Response shape verdict: CORRECT for the `whisper-1` JSON case. Newer transcription models can return additional fields such as `usage`.
- Deprecated? No. Newer alternatives exist: `gpt-4o-transcribe`, `gpt-4o-mini-transcribe`, and diarization-capable variants.
- Missing params? None

## 9. `POST /v1/audio/speech`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: CORRECT. `tts-1`, `voice: "nova"`, and `response_format: "aac"` are valid. The docs also document newer `gpt-4o-mini-tts` usage.
- Response shape verdict: CORRECT. This endpoint returns binary audio / audio stream output.
- Deprecated? No. `tts-1` is supported, but not the newest TTS model.
- Missing params? None

## 10. `POST /v1/images/generations`
- URL verdict: CORRECT
- Method verdict: CORRECT
- Request shape verdict: ISSUES. `model: "gpt-image-1"` is valid, but the parameter set is not current for GPT Image. Official GPT Image docs use fields like `output_format`, `quality`, `background`, and related options; `response_format: "b64_json"` is the older DALL-E-style pattern.
- Response shape verdict: ISSUES. `data[].b64_json` is still the important output field for base64 image data, but the actual response is more complete than your example.
- Deprecated? Yes. The `response_format` style is not the current GPT Image parameterization.
- Missing params? None

## Cross-Cutting Checks
- Structured outputs: The Chat Completions `response_format.json_schema` syntax is valid for GPT-4o-family models. Add `strict: true` if you need strong schema enforcement. If you migrate to Responses API, the structured-output field changes shape.
- Structured-output contract mismatch: Your endpoint tables describe a bare `[ContentBlock]` array / `ContentBlockArraySchema`, but the document’s JSON contract is `{ "blocks": [...] }`. That mismatch will cause parser/schema confusion if implemented literally.
- File search tool config: The current native format is `{"type":"file_search","vector_store_ids":[...]}` on `POST /v1/responses`. Your Chat Completions + nested `file_search` config is not current.
- Vector stores: The listed vector store endpoints are current. Bulk ingestion also has a `/v1/vector_stores/{vector_store_id}/file_batches` path, which may be a better fit for multi-file setup.
- Whisper/STT: `whisper-1` and multipart form data are valid, but OpenAI now exposes newer `gpt-4o-*` transcription models.
- TTS: `nova` and `aac` are valid. Your config’s voice list is incomplete versus current docs; newer built-in voices are documented beyond the original six.
- Images: `gpt-image-1` is valid, but the request body in the file is using older image-API conventions.

## Summary
- Total endpoints checked: 10
- Correct count: 2
- Issues found count: 8
- Critical errors:
  - Endpoint 6 uses the wrong API surface and wrong tool schema for native `file_search`.
  - Endpoint 7 omits required `model`.
  - The structured-output contract is internally inconsistent: bare array in endpoint specs vs `{ "blocks": [...] }` in the JSON contract.
  - Endpoint 10 uses a non-current GPT Image parameter set.
- Warnings:
  - Chat Completions still works, but Responses API is the better current fit for native tools and multimodal.
  - Endpoints 3, 4, and 5 are mostly right but their response shapes are incomplete.
  - `whisper-1`, `tts-1`, and `gpt-image-1` are supported but no longer the newest choices.
  - File-search hit details are not returned unless you explicitly request them with `include`.

Sources used:
- [Chat Completions API reference](https://platform.openai.com/docs/api-reference/chat/create)
- [Structured outputs guide](https://platform.openai.com/docs/guides/structured-outputs)
- [Responses API reference](https://platform.openai.com/docs/api-reference/responses)
- [File search guide](https://platform.openai.com/docs/guides/tools-file-search)
- [Vector stores API reference](https://platform.openai.com/docs/api-reference/vector-stores)
- [Files API reference](https://platform.openai.com/docs/api-reference/files/create)
- [Audio API reference](https://platform.openai.com/docs/api-reference/audio)
- [Speech-to-text guide](https://platform.openai.com/docs/guides/speech-to-text)
- [Text-to-speech guide](https://platform.openai.com/docs/guides/text-to-speech)
- [Images API reference](https://platform.openai.com/docs/api-reference/images)
- [Image generation guide](https://platform.openai.com/docs/guides/image-generation)