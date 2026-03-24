package com.tradeguru.electrical.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import com.tradeguru.electrical.data.db.entities.ConversationEntity
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import com.tradeguru.electrical.data.db.entities.PartsItemEntity
import com.tradeguru.electrical.models.AttachmentType
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.ThinkingMode

object DomainMappers {

    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val nestedListType = object : TypeToken<List<List<String>>>() {}.type

    data class Conversation(
        val id: String,
        val title: String,
        val mode: ThinkingMode,
        val messages: List<ChatMessage> = emptyList(),
        val createdAt: Long,
        val updatedAt: Long
    )

    data class ChatMessage(
        val id: String,
        val role: MessageRole,
        val blocks: List<ContentBlock>,
        val timestamp: Long,
        val mode: ThinkingMode,
        val attachments: List<MessageAttachment> = emptyList()
    )

    data class ContentBlock(
        val id: String,
        val type: ContentBlockType,
        val content: String? = null,
        val title: String? = null,
        val steps: List<String>? = null,
        val items: List<PartsItem> = emptyList(),
        val language: String? = null,
        val code: String? = null,
        val clause: String? = null,
        val summary: String? = null,
        val url: String? = null,
        val rows: List<List<String>>? = null,
        val headers: List<String>? = null,
        val level: Int? = null,
        val style: String? = null
    )

    data class PartsItem(
        val id: String,
        val name: String,
        val spec: String,
        val qty: Int
    )

    data class MessageAttachment(
        val id: String,
        val type: AttachmentType,
        val fileName: String,
        val fileSize: Int? = null,
        val thumbnailData: ByteArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MessageAttachment) return false
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()
    }

    fun ContentBlockEntity.toDomain(parts: List<PartsItemEntity> = emptyList()) = ContentBlock(
        id = id,
        type = ContentBlockType.fromValue(type) ?: ContentBlockType.TEXT,
        content = content,
        title = title,
        steps = steps?.let { gson.fromJson<List<String>>(it, stringListType) },
        items = parts.map { it.toDomain() },
        language = language,
        code = code,
        clause = clause,
        summary = summary,
        url = url,
        rows = rows?.let { gson.fromJson<List<List<String>>>(it, nestedListType) },
        headers = headers?.let { gson.fromJson<List<String>>(it, stringListType) },
        level = level,
        style = style
    )

    fun PartsItemEntity.toDomain() = PartsItem(
        id = id,
        name = name,
        spec = spec,
        qty = qty
    )

    fun MessageAttachmentEntity.toDomain() = MessageAttachment(
        id = id,
        type = AttachmentType.fromValue(type) ?: AttachmentType.IMAGE,
        fileName = fileName,
        fileSize = fileSize,
        thumbnailData = thumbnailData
    )

    fun ChatMessageEntity.toDomain(
        blocks: List<ContentBlock>,
        attachments: List<MessageAttachment> = emptyList()
    ) = ChatMessage(
        id = id,
        role = MessageRole.fromValue(role) ?: MessageRole.USER,
        blocks = blocks,
        timestamp = timestamp,
        mode = ThinkingMode.fromValue(mode) ?: ThinkingMode.FAULT_FINDER,
        attachments = attachments
    )

    fun ConversationEntity.toDomain(
        messages: List<ChatMessage> = emptyList()
    ) = Conversation(
        id = id,
        title = title,
        mode = ThinkingMode.fromValue(mode) ?: ThinkingMode.FAULT_FINDER,
        messages = messages,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
