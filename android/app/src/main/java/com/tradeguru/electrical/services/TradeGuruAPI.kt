package com.tradeguru.electrical.services

import com.google.gson.Gson
import com.tradeguru.electrical.models.ThinkingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TradeGuruAPI {
    private val client = OkHttpClient.Builder()
        .readTimeout(5, TimeUnit.MINUTES)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun registerDevice(): String = withContext(Dispatchers.IO) {
        val localeId = Locale.getDefault().toLanguageTag().ifEmpty { "en" }
        val tz = TimeZone.getDefault()
        val hours = tz.rawOffset / 3600000
        val sign = if (hours >= 0) "+" else ""
        val timezoneId = "GMT$sign$hours"

        val body = mapOf(
            "platform" to APIConfig.PLATFORM,
            "app_version" to APIConfig.APP_VERSION,
            "locale" to localeId,
            "timezone" to timezoneId
        )
        val jsonBody = gson.toJson(body)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(APIConfig.url("device/register"))
            .post(jsonBody)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw IOException("Empty response body")
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: $responseBody")
        }
        gson.fromJson(responseBody, DeviceRegisterResponse::class.java).deviceId
    }

    fun chat(
        messages: List<ApiMessage>,
        mode: ThinkingMode,
        deviceId: String,
        jwt: String? = null
    ): Flow<StreamResult> = flow {
        val body = mapOf(
            "messages" to messages,
            "mode" to mode.value,
            "device_id" to deviceId,
            "platform" to APIConfig.PLATFORM
        )
        val jsonBody = gson.toJson(body)
            .toRequestBody("application/json".toMediaType())

        val request = APIConfig.requestBuilder("chat", deviceId = deviceId, jwt = jwt)
            .post(jsonBody)
            .build()

        val call = client.newCall(request)
        var response: okhttp3.Response? = null
        try {
            response = withContext(Dispatchers.IO) { call.execute() }
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                emit(StreamResult.Error(StreamErrorPayload(
                    code = "HTTP_${response.code}",
                    message = errorBody
                )))
                return@flow
            }

            val source = response.body?.source()
                ?: throw IOException("Empty body")
            var currentEvent = ""
            var currentData = ""
            while (!source.exhausted()) {
                ensureActive()
                val line = withContext(Dispatchers.IO) { source.readUtf8Line() } ?: break
                if (line.startsWith("event: ")) {
                    currentEvent = line.removePrefix("event: ").trim()
                } else if (line.startsWith("data: ")) {
                    currentData = line.removePrefix("data: ").trim()
                } else if (line.isEmpty() && currentEvent.isNotEmpty()) {
                    emit(StreamParser.parse(currentEvent, currentData))
                    currentEvent = ""
                    currentData = ""
                }
            }
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    fun chatVision(
        message: String,
        image: String,
        mode: ThinkingMode,
        deviceId: String,
        jwt: String? = null
    ): Flow<StreamResult> = flow {
        val body = mapOf(
            "message" to message,
            "image" to image,
            "mode" to mode.value,
            "device_id" to deviceId,
            "platform" to APIConfig.PLATFORM
        )
        val jsonBody = gson.toJson(body)
            .toRequestBody("application/json".toMediaType())

        val request = APIConfig.requestBuilder("chat/vision", deviceId = deviceId, jwt = jwt)
            .post(jsonBody)
            .build()

        val call = client.newCall(request)
        var response: okhttp3.Response? = null
        try {
            response = withContext(Dispatchers.IO) { call.execute() }
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                emit(StreamResult.Error(StreamErrorPayload(
                    code = "HTTP_${response.code}",
                    message = errorBody
                )))
                return@flow
            }

            val source = response.body?.source()
                ?: throw IOException("Empty body")
            var currentEvent = ""
            var currentData = ""
            while (!source.exhausted()) {
                ensureActive()
                val line = withContext(Dispatchers.IO) { source.readUtf8Line() } ?: break
                if (line.startsWith("event: ")) {
                    currentEvent = line.removePrefix("event: ").trim()
                } else if (line.startsWith("data: ")) {
                    currentData = line.removePrefix("data: ").trim()
                } else if (line.isEmpty() && currentEvent.isNotEmpty()) {
                    emit(StreamParser.parse(currentEvent, currentData))
                    currentEvent = ""
                    currentData = ""
                }
            }
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun uploadFile(
        fileData: ByteArray,
        fileName: String,
        mimeType: String,
        deviceId: String,
        jwt: String? = null
    ): FileUploadResponse = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", fileName,
                fileData.toRequestBody(mimeType.toMediaType())
            )
            .build()

        val request = APIConfig.requestBuilder("files/upload", deviceId = deviceId, jwt = jwt)
            .post(requestBody)
            .removeHeader("Content-Type")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: $body")
        }
        gson.fromJson(body, FileUploadResponse::class.java)
    }

    suspend fun transcribe(
        audioData: ByteArray,
        mimeType: String,
        deviceId: String,
        jwt: String? = null
    ): String = withContext(Dispatchers.IO) {
        val ext = if (mimeType.contains("wav")) "wav" else "m4a"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio", "recording.$ext",
                audioData.toRequestBody(mimeType.toMediaType())
            )
            .build()

        val request = APIConfig.requestBuilder("audio/transcribe", deviceId = deviceId, jwt = jwt)
            .post(requestBody)
            .removeHeader("Content-Type")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: $body")
        }
        gson.fromJson(body, TranscribeResponse::class.java).text
    }

    suspend fun speak(
        text: String,
        voice: String = "nova",
        deviceId: String,
        jwt: String? = null
    ): ByteArray = withContext(Dispatchers.IO) {
        val body = mapOf("text" to text, "voice" to voice)
        val jsonBody = gson.toJson(body)
            .toRequestBody("application/json".toMediaType())

        val request = APIConfig.requestBuilder("audio/speech", deviceId = deviceId, jwt = jwt)
            .post(jsonBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("HTTP ${response.code}: $errorBody")
        }
        response.body?.bytes() ?: throw IOException("Empty response body")
    }

    suspend fun rate(
        responseId: String,
        stars: Int,
        mode: ThinkingMode,
        deviceId: String,
        comment: String? = null,
        jwt: String? = null
    ): Unit = withContext(Dispatchers.IO) {
        val bodyMap = mutableMapOf<String, Any>(
            "response_id" to responseId,
            "stars" to stars,
            "mode" to mode.value
        )
        if (comment != null) bodyMap["comment"] = comment
        val jsonBody = gson.toJson(bodyMap)
            .toRequestBody("application/json".toMediaType())

        val request = APIConfig.requestBuilder("rating", deviceId = deviceId, jwt = jwt)
            .post(jsonBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("HTTP ${response.code}: $errorBody")
        }
    }

    suspend fun feedback(
        responseId: String,
        reason: String,
        mode: ThinkingMode,
        deviceId: String,
        detail: String? = null,
        jwt: String? = null
    ): Unit = withContext(Dispatchers.IO) {
        val bodyMap = mutableMapOf<String, Any>(
            "response_id" to responseId,
            "reason" to reason,
            "mode" to mode.value
        )
        if (detail != null) bodyMap["detail"] = detail
        val jsonBody = gson.toJson(bodyMap)
            .toRequestBody("application/json".toMediaType())

        val request = APIConfig.requestBuilder("feedback", deviceId = deviceId, jwt = jwt)
            .post(jsonBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("HTTP ${response.code}: $errorBody")
        }
    }
}
