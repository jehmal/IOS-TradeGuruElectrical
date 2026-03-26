package com.tradeguru.electrical.services

import com.google.gson.annotations.SerializedName
import com.tradeguru.electrical.data.DomainMappers.ContentBlock
import com.tradeguru.electrical.models.StructuredResponse

sealed class StreamResult {
    data class Block(val block: ContentBlock) : StreamResult()
    data class Response(val structured: StructuredResponse, val rawJson: String) : StreamResult()
    data class Status(val payload: StatusPayload) : StreamResult()
    data class Done(val payload: StreamDonePayload) : StreamResult()
    data class Error(val payload: StreamErrorPayload) : StreamResult()
}

data class StatusPayload(
    val stage: String,
    val detail: String? = null
)

data class StreamDonePayload(
    @SerializedName("response_id") val responseId: String,
    val usage: TokenUsage? = null,
    val cached: Boolean? = null,
    val category: String? = null
)

data class TokenUsage(
    @SerializedName("input_tokens") val inputTokens: Int,
    @SerializedName("output_tokens") val outputTokens: Int
)

data class StreamErrorPayload(
    val code: String,
    val message: String,
    val partial: Boolean? = null
)

data class DeviceRegisterResponse(
    @SerializedName("device_id") val deviceId: String
)

data class TranscribeResponse(
    val text: String
)

data class FileUploadResponse(
    val id: String,
    val filename: String
)

data class ApiMessage(
    val role: String,
    val content: String
)
