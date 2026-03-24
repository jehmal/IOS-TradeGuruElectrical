package com.tradeguru.electrical.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class ChatViewModel(
    val engine: ChatEngine,
    val conversationManager: ConversationManager
) : ViewModel() {

    val selectedMode = MutableStateFlow(ThinkingMode.FAULT_FINDER)

    val conversations: StateFlow<List<DomainMappers.Conversation>> =
        conversationManager.getAllConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeConversation: StateFlow<DomainMappers.Conversation?> =
        conversationManager.activeConversation

    val isStreaming: StateFlow<Boolean> = engine.isStreaming
    val streamingBlocks: StateFlow<List<DomainMappers.ContentBlock>> = engine.streamingBlocks
    val error: StateFlow<String?> = engine.error
    val lastResponseId: String? get() = engine.lastResponseId
    val pipelineStage: StateFlow<PipelineStage> = engine.pipelineStage

    fun send(
        text: String,
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>? = null
    ) {
        val trimmed = text.trim()
        val hasAttachments = !attachments.isNullOrEmpty()
        if (trimmed.isEmpty() && !hasAttachments) return
        val displayText = if (trimmed.isEmpty()) "[Photo]" else text

        viewModelScope.launch {
            val conversation = conversationManager.ensureConversation(displayText, mode)
            val userMessage = createUserMessage(displayText, mode, attachments)
            conversationManager.saveMessage(conversation.id, userMessage)

            val fullConversation = conversationManager.loadConversation(conversation.id)
                ?: conversation.copy(messages = listOf(userMessage))

            engine.fetchResponse(mode, fullConversation)

            persistAssistantResponse(conversation.id, mode, fullConversation)
        }
    }

    fun sendWithVision(
        text: String,
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>?
    ) {
        val trimmed = text.trim()
        val hasAttachments = !attachments.isNullOrEmpty()
        if (trimmed.isEmpty() && !hasAttachments) return
        val displayText = if (trimmed.isEmpty()) "[Photo]" else text

        viewModelScope.launch {
            val conversation = conversationManager.ensureConversation(displayText, mode)
            val userMessage = createUserMessage(displayText, mode, attachments)
            conversationManager.saveMessage(conversation.id, userMessage)

            val fullConversation = conversationManager.loadConversation(conversation.id)
                ?: conversation.copy(messages = listOf(userMessage))

            engine.fetchVisionResponse(mode, attachments, fullConversation)

            persistAssistantResponse(conversation.id, mode, fullConversation)
        }
    }

    fun sendWithDocument(
        text: String,
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>?
    ) {
        val trimmed = text.trim()
        val hasAttachments = !attachments.isNullOrEmpty()
        if (trimmed.isEmpty() && !hasAttachments) return

        val docAttachment = attachments?.firstOrNull {
            it.type == com.tradeguru.electrical.models.AttachmentType.DOCUMENT
        }
        val displayText = if (trimmed.isEmpty()) {
            "[File: ${docAttachment?.fileName ?: "document"}]"
        } else {
            text
        }

        viewModelScope.launch {
            val conversation = conversationManager.ensureConversation(displayText, mode)
            val userMessage = createUserMessage(displayText, mode, attachments)
            conversationManager.saveMessage(conversation.id, userMessage)

            val fullConversation = conversationManager.loadConversation(conversation.id)
                ?: conversation.copy(messages = listOf(userMessage))

            engine.uploadDocumentAndChat(mode, attachments, fullConversation)

            persistAssistantResponse(conversation.id, mode, fullConversation)
        }
    }

    fun retryLastRequest() {
        val conversation = activeConversation.value ?: return
        viewModelScope.launch {
            val fullConversation = conversationManager.loadConversation(conversation.id)
                ?: return@launch
            engine.retryLastRequest(fullConversation)
        }
    }

    fun newConversation(mode: ThinkingMode) {
        viewModelScope.launch {
            conversationManager.newConversation(mode)
            engine.resetForNewConversation()
        }
    }

    fun selectConversation(conversation: DomainMappers.Conversation) {
        conversationManager.selectConversation(conversation)
        engine.resetForNewConversation()
    }

    fun deleteConversation(conversation: DomainMappers.Conversation) {
        viewModelScope.launch {
            conversationManager.deleteConversation(conversation.id)
        }
    }

    fun searchConversations(query: String): List<DomainMappers.Conversation> =
        conversationManager.searchConversations(conversations.value, query)

    fun dismissError() {
        engine.clearError()
    }

    fun rateLastResponse(stars: Int, comment: String? = null) {
        viewModelScope.launch {
            engine.rateLastResponse(stars, comment, selectedMode.value)
        }
    }

    fun transcribeAudio(audioFile: File) {
        viewModelScope.launch {
            engine.transcribeAudio(audioFile)
        }
    }

    fun speakText(text: String) {
        viewModelScope.launch {
            engine.speakText(text)
        }
    }

    private fun createUserMessage(
        displayText: String,
        mode: ThinkingMode,
        attachments: List<DomainMappers.MessageAttachment>?
    ): DomainMappers.ChatMessage = DomainMappers.ChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.USER,
        blocks = listOf(
            DomainMappers.ContentBlock(
                id = UUID.randomUUID().toString(),
                type = ContentBlockType.TEXT,
                content = displayText
            )
        ),
        timestamp = System.currentTimeMillis(),
        mode = mode,
        attachments = attachments ?: emptyList()
    )

    private suspend fun persistAssistantResponse(
        conversationId: String,
        mode: ThinkingMode,
        fullConversation: DomainMappers.Conversation
    ) {
        if (engine.error.value != null) return

        val assistantMessage = engine.finalizeResponse(mode, fullConversation)
        if (assistantMessage.blocks.isNotEmpty()) {
            conversationManager.saveMessage(conversationId, assistantMessage)
        }

        if (fullConversation.messages.count { it.role == MessageRole.USER } == 1) {
            val firstUserText = fullConversation.messages
                .firstOrNull { it.role == MessageRole.USER }
                ?.blocks?.firstOrNull()?.content ?: "Chat"
            conversationManager.updateConversationTitle(
                conversationId,
                firstUserText.take(40)
            )
        }
    }
}

