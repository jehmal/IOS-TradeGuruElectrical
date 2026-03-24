package com.tradeguru.electrical.viewmodels

import android.media.MediaPlayer
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.services.ApiMessage
import com.tradeguru.electrical.services.AuthManager
import com.tradeguru.electrical.services.DeviceManager
import com.tradeguru.electrical.services.StreamResult
import com.tradeguru.electrical.services.TradeGuruAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ChatEngine(
    private val deviceManager: DeviceManager,
    private val authManager: AuthManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var deviceId: String = deviceManager.getOrCreateDeviceId()
    private val currentJwt: String? get() = authManager.currentJwt

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _streamingBlocks = MutableStateFlow<List<DomainMappers.ContentBlock>>(emptyList())
    val streamingBlocks: StateFlow<List<DomainMappers.ContentBlock>> = _streamingBlocks

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _pipelineStage = MutableStateFlow(PipelineStage.IDLE)
    val pipelineStage: StateFlow<PipelineStage> = _pipelineStage

    var lastResponseId: String? = null
        private set

    private enum class SendType { TEXT, VISION, DOCUMENT }

    private var lastSendType = SendType.TEXT
    private var lastSendMode = ThinkingMode.FAULT_FINDER
    private var lastAttachments: List<DomainMappers.MessageAttachment>? = null

    private var mediaPlayer: MediaPlayer? = null

    init {
        registerDeviceIfNeeded()
    }

    suspend fun fetchResponse(
        mode: ThinkingMode,
        conversation: DomainMappers.Conversation
    ) {
        val apiMessages = buildApiMessages(conversation)

        _isStreaming.value = true
        _streamingBlocks.value = emptyList()
        _error.value = null
        lastSendMode = mode
        lastAttachments = null
        lastSendType = SendType.TEXT

        TradeGuruAPI.chat(
            messages = apiMessages,
            mode = mode,
            deviceId = deviceId,
            jwt = currentJwt
        ).collect { result ->
            processStreamEvent(result)
        }

        if (_isStreaming.value) _isStreaming.value = false
    }

    suspend fun fetchVisionResponse(
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>?,
        conversation: DomainMappers.Conversation
    ) {
        val imageAttachment = attachments?.firstOrNull {
            it.type == com.tradeguru.electrical.models.AttachmentType.IMAGE && it.thumbnailData != null
        }

        if (imageAttachment == null || imageAttachment.thumbnailData == null) {
            fetchResponse(mode, conversation)
            return
        }

        val lastUserMsg = conversation.messages.lastOrNull { it.role == MessageRole.USER }
        val messageText = lastUserMsg?.blocks
            ?.mapNotNull { it.content }
            ?.joinToString("\n") ?: ""

        val imageDataUri = try {
            "data:image/jpeg;base64,${
                android.util.Base64.encodeToString(imageAttachment.thumbnailData, android.util.Base64.NO_WRAP)
            }"
        } catch (e: Exception) {
            _error.value = "Failed to encode image"
            return
        }

        if (imageDataUri.length < 30) {
            fetchResponse(mode, conversation)
            return
        }

        _isStreaming.value = true
        _streamingBlocks.value = emptyList()
        _error.value = null
        lastSendMode = mode
        lastAttachments = attachments
        lastSendType = SendType.VISION

        TradeGuruAPI.chatVision(
            message = messageText,
            image = imageDataUri,
            mode = mode,
            deviceId = deviceId,
            jwt = currentJwt
        ).collect { result ->
            processStreamEvent(result)
        }

        if (_isStreaming.value) _isStreaming.value = false
    }

    suspend fun uploadDocumentAndChat(
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>?,
        conversation: DomainMappers.Conversation
    ) {
        lastSendMode = mode
        lastAttachments = attachments
        lastSendType = SendType.DOCUMENT

        val doc = attachments?.firstOrNull {
            it.type == com.tradeguru.electrical.models.AttachmentType.DOCUMENT
        }

        if (doc == null || doc.thumbnailData == null) {
            fetchResponse(mode, conversation)
            return
        }

        val mimeType = when {
            doc.fileName.endsWith(".pdf") -> "application/pdf"
            doc.fileName.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }

        try {
            val uploaded = TradeGuruAPI.uploadFile(
                fileData = doc.thumbnailData,
                fileName = doc.fileName,
                mimeType = mimeType,
                deviceId = deviceId,
                jwt = currentJwt
            )

            val lastUserText = conversation.messages
                .lastOrNull { it.role == MessageRole.USER }
                ?.blocks?.mapNotNull { it.content }
                ?.joinToString("\n") ?: ""

            val messageWithFile = if (lastUserText.isEmpty()) {
                "[Uploaded: ${uploaded.filename}]"
            } else {
                "$lastUserText\n\n[Attached file: ${uploaded.filename} (id: ${uploaded.id})]"
            }

            val apiMessages = conversation.messages.map { msg ->
                var content = msg.blocks.mapNotNull { it.content }.joinToString("\n")
                if (msg.id == conversation.messages.lastOrNull()?.id && msg.role == MessageRole.USER) {
                    content = messageWithFile
                }
                ApiMessage(role = msg.role.value, content = content)
            }

            _isStreaming.value = true
            _streamingBlocks.value = emptyList()
            _error.value = null

            TradeGuruAPI.chat(
                messages = apiMessages,
                mode = mode,
                deviceId = deviceId,
                jwt = currentJwt
            ).collect { result ->
                processStreamEvent(result)
            }

            if (_isStreaming.value) _isStreaming.value = false
        } catch (e: Exception) {
            _error.value = "File upload failed: ${e.message}"
        }
    }

    fun retryLastRequest(conversation: DomainMappers.Conversation) {
        _error.value = null
        when (lastSendType) {
            SendType.TEXT -> scope.launch {
                fetchResponse(lastSendMode, conversation)
            }
            SendType.VISION -> scope.launch {
                fetchVisionResponse(lastSendMode, lastAttachments, conversation)
            }
            SendType.DOCUMENT -> scope.launch {
                uploadDocumentAndChat(lastSendMode, lastAttachments, conversation)
            }
        }
    }

    suspend fun rateLastResponse(stars: Int, comment: String?, mode: ThinkingMode) {
        val responseId = lastResponseId ?: return
        withContext(Dispatchers.IO) {
            try {
                TradeGuruAPI.rate(
                    responseId = responseId,
                    stars = stars,
                    mode = mode,
                    deviceId = deviceId,
                    comment = comment,
                    jwt = currentJwt
                )
            } catch (_: Exception) { }
        }
    }

    suspend fun transcribeAudio(audioFile: File): String? {
        return try {
            TradeGuruAPI.transcribe(
                audioData = audioFile.readBytes(),
                mimeType = "audio/m4a",
                deviceId = deviceId,
                jwt = currentJwt
            )
        } catch (_: Exception) {
            _error.value = "Transcription failed"
            null
        }
    }

    suspend fun speakText(text: String) {
        try {
            val audioData = TradeGuruAPI.speak(
                text = text,
                deviceId = deviceId,
                jwt = currentJwt
            )
            mediaPlayer?.release()
            val tempFile = File.createTempFile("tts_", ".mp3")
            tempFile.writeBytes(audioData)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener { mp ->
                    mp.release()
                    tempFile.delete()
                }
            }
        } catch (_: Exception) {
            _error.value = "Speech playback failed"
        }
    }

    fun registerDeviceIfNeeded() {
        if (deviceId.isEmpty() || deviceId == "pending") {
            scope.launch {
                try {
                    val newDeviceId = withContext(Dispatchers.IO) {
                        TradeGuruAPI.registerDevice()
                    }
                    deviceManager.getOrCreateDeviceId()
                    deviceId = newDeviceId
                } catch (_: Exception) {
                    _error.value = "Device registration failed. Retrying on next launch."
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun finalizeResponse(
        mode: ThinkingMode,
        conversation: DomainMappers.Conversation
    ): DomainMappers.ChatMessage {
        val blocks = _streamingBlocks.value.toList()
        val assistantMessage = DomainMappers.ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.ASSISTANT,
            blocks = blocks,
            timestamp = System.currentTimeMillis(),
            mode = mode
        )

        _streamingBlocks.value = emptyList()
        _isStreaming.value = false
        _pipelineStage.value = PipelineStage.IDLE

        return assistantMessage
    }

    fun handleStreamError(
        error: StreamResult.Error,
        mode: ThinkingMode,
        conversation: DomainMappers.Conversation
    ): DomainMappers.ChatMessage? {
        _error.value = error.payload.message
        var partialMessage: DomainMappers.ChatMessage? = null

        if (error.payload.partial == true && _streamingBlocks.value.isNotEmpty()) {
            partialMessage = DomainMappers.ChatMessage(
                id = UUID.randomUUID().toString(),
                role = MessageRole.ASSISTANT,
                blocks = _streamingBlocks.value.toList(),
                timestamp = System.currentTimeMillis(),
                mode = mode
            )
        }

        _streamingBlocks.value = emptyList()
        _isStreaming.value = false
        _pipelineStage.value = PipelineStage.IDLE

        return partialMessage
    }

    fun resetForNewConversation() {
        _streamingBlocks.value = emptyList()
        _isStreaming.value = false
        _error.value = null
    }

    private fun buildApiMessages(
        conversation: DomainMappers.Conversation
    ): List<ApiMessage> =
        conversation.messages.map { msg ->
            val content = msg.blocks.mapNotNull { it.content }.joinToString("\n")
            ApiMessage(role = msg.role.value, content = content)
        }

    private fun processStreamEvent(result: StreamResult) {
        when (result) {
            is StreamResult.Block -> {
                _streamingBlocks.value = _streamingBlocks.value + result.block
            }
            is StreamResult.Status -> {
                val stage = PipelineStage.fromValue(result.payload.stage)
                if (stage != null) _pipelineStage.value = stage
            }
            is StreamResult.Done -> {
                lastResponseId = result.payload.responseId
            }
            is StreamResult.Error -> {
                _error.value = result.payload.message
                if (result.payload.partial != true) {
                    _streamingBlocks.value = emptyList()
                }
                _isStreaming.value = false
                _pipelineStage.value = PipelineStage.IDLE
            }
        }
    }
}
