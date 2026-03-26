package com.tradeguru.electrical.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tradeguru.electrical.data.DomainMappers.ContentBlock
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.FaultFindingResponse
import com.tradeguru.electrical.models.QuestionResponse
import com.tradeguru.electrical.models.ResearchStructuredResponse

object StreamParser {
    private val gson = Gson()

    fun parse(event: String, data: String): StreamResult {
        return when (event) {
            "block" -> parseBlock(data)
            "response" -> parseResponse(data)
            "status" -> parseStatus(data)
            "done" -> parseDone(data)
            "error" -> parseError(data)
            else -> StreamResult.Error(
                StreamErrorPayload(
                    code = "UNKNOWN_EVENT",
                    message = "Unknown event type: $event"
                )
            )
        }
    }

    private fun parseBlock(data: String): StreamResult {
        return try {
            val json = gson.fromJson(data, JsonObject::class.java)
            val typeStr = json.get("type")?.asString ?: "text"
            val type = ContentBlockType.fromValue(typeStr) ?: ContentBlockType.TEXT

            val block = ContentBlock(
                id = json.get("id")?.asString ?: "",
                type = type,
                content = json.get("content")?.asString,
                title = json.get("title")?.asString,
                steps = json.getAsJsonArray("steps")?.map { it.asString },
                language = json.get("language")?.asString,
                code = json.get("code")?.asString,
                clause = json.get("clause")?.asString,
                summary = json.get("summary")?.asString,
                url = json.get("url")?.asString,
                rows = json.getAsJsonArray("rows")?.map { row ->
                    row.asJsonArray.map { it.asString }
                },
                headers = json.getAsJsonArray("headers")?.map { it.asString },
                level = json.get("level")?.asInt,
                style = json.get("style")?.asString
            )
            StreamResult.Block(block)
        } catch (e: Exception) {
            StreamResult.Error(
                StreamErrorPayload(
                    code = "PARSE_ERROR",
                    message = "Failed to parse block: ${e.message}"
                )
            )
        }
    }

    private fun parseStatus(data: String): StreamResult {
        return try {
            val payload = gson.fromJson(data, StatusPayload::class.java)
            StreamResult.Status(payload)
        } catch (e: Exception) {
            StreamResult.Error(
                StreamErrorPayload(
                    code = "PARSE_ERROR",
                    message = "Failed to parse status: ${e.message}"
                )
            )
        }
    }

    private fun parseDone(data: String): StreamResult {
        return try {
            val payload = gson.fromJson(data, StreamDonePayload::class.java)
            StreamResult.Done(payload)
        } catch (e: Exception) {
            StreamResult.Error(
                StreamErrorPayload(
                    code = "PARSE_ERROR",
                    message = "Failed to parse done: ${e.message}"
                )
            )
        }
    }

    private fun parseError(data: String): StreamResult {
        return try {
            val payload = gson.fromJson(data, StreamErrorPayload::class.java)
            StreamResult.Error(payload)
        } catch (e: Exception) {
            StreamResult.Error(
                StreamErrorPayload(
                    code = "PARSE_ERROR",
                    message = "Failed to parse error: ${e.message}"
                )
            )
        }
    }

    private fun parseResponse(data: String): StreamResult {
        return try {
            val json = JsonParser.parseString(data).asJsonObject
            val structured = when {
                json.has("intent") && json.get("intent").asString == "question" ->
                    gson.fromJson(data, QuestionResponse::class.java)
                json.has("intent") && json.get("intent").asString == "research" ->
                    gson.fromJson(data, ResearchStructuredResponse::class.java)
                json.has("safety") && json.has("diagnostic_steps") ->
                    gson.fromJson(data, FaultFindingResponse::class.java)
                else -> null
            }
            if (structured != null) {
                StreamResult.Response(structured, data)
            } else {
                StreamResult.Block(ContentBlock(id = "", type = ContentBlockType.TEXT, content = data))
            }
        } catch (e: Exception) {
            StreamResult.Error(
                StreamErrorPayload(
                    code = "PARSE_ERROR",
                    message = "Failed to parse response: ${e.message}"
                )
            )
        }
    }
}
