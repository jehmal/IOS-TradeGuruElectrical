package com.tradeguru.electrical.viewmodels

import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.data.DomainMappers.toDomain
import com.tradeguru.electrical.data.db.TradeGuruDatabase
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import com.tradeguru.electrical.data.db.entities.ConversationEntity
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import com.tradeguru.electrical.data.db.entities.PartsItemEntity
import com.tradeguru.electrical.models.ThinkingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ConversationManager(private val database: TradeGuruDatabase) {

    private val conversationDao = database.conversationDao()
    private val chatMessageDao = database.chatMessageDao()
    private val contentBlockDao = database.contentBlockDao()
    private val partsItemDao = database.partsItemDao()
    private val messageAttachmentDao = database.messageAttachmentDao()

    private val _activeConversation = MutableStateFlow<DomainMappers.Conversation?>(null)
    val activeConversation: StateFlow<DomainMappers.Conversation?> = _activeConversation

    fun getAllConversations(): Flow<List<DomainMappers.Conversation>> =
        conversationDao.getAllOrderedByUpdatedAt()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    suspend fun loadConversation(conversationId: String): DomainMappers.Conversation? =
        withContext(Dispatchers.IO) {
            val entity = conversationDao.getById(conversationId) ?: return@withContext null
            val messageEntities = conversationDao.getMessagesForConversation(conversationId)
            val messages = messageEntities.map { msg ->
                val blockEntities = contentBlockDao.getByMessageIdSync(msg.id)
                val blocks = blockEntities.map { block ->
                    val parts = partsItemDao.getByContentBlockIdSync(block.id)
                    block.toDomain(parts)
                }
                val attachments = messageAttachmentDao.getByMessageIdSync(msg.id)
                msg.toDomain(blocks, attachments.map { it.toDomain() })
            }
            entity.toDomain(messages)
        }

    suspend fun ensureConversation(
        text: String,
        mode: ThinkingMode
    ): DomainMappers.Conversation = withContext(Dispatchers.IO) {
        val active = _activeConversation.value
        if (active != null) return@withContext active

        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val title = text.take(40)
        val entity = ConversationEntity(
            id = id,
            title = title,
            mode = mode.value,
            createdAt = now,
            updatedAt = now
        )
        conversationDao.insert(entity)
        val conversation = entity.toDomain()
        _activeConversation.value = conversation
        conversation
    }

    suspend fun saveMessage(
        conversationId: String,
        message: DomainMappers.ChatMessage
    ) = withContext(Dispatchers.IO) {
        val messageEntity = ChatMessageEntity(
            id = message.id,
            role = message.role.value,
            timestamp = message.timestamp,
            mode = message.mode.value,
            conversationId = conversationId
        )
        chatMessageDao.insert(messageEntity)

        val gson = com.google.gson.Gson()

        val blockEntities = message.blocks.map { block ->
            ContentBlockEntity(
                id = block.id,
                type = block.type.value,
                content = block.content,
                title = block.title,
                steps = block.steps?.let { gson.toJson(it) },
                language = block.language,
                code = block.code,
                clause = block.clause,
                summary = block.summary,
                url = block.url,
                rows = block.rows?.let { gson.toJson(it) },
                headers = block.headers?.let { gson.toJson(it) },
                level = block.level,
                style = block.style,
                messageId = message.id
            )
        }
        if (blockEntities.isNotEmpty()) {
            contentBlockDao.insertAll(blockEntities)
        }

        val partEntities = message.blocks.flatMap { block ->
            block.items.map { part ->
                PartsItemEntity(
                    id = part.id,
                    name = part.name,
                    spec = part.spec,
                    qty = part.qty,
                    contentBlockId = block.id
                )
            }
        }
        if (partEntities.isNotEmpty()) {
            partsItemDao.insertAll(partEntities)
        }

        val attachmentEntities = message.attachments.map { att ->
            MessageAttachmentEntity(
                id = att.id,
                type = att.type.value,
                fileName = att.fileName,
                fileSize = att.fileSize,
                thumbnailData = att.thumbnailData,
                messageId = message.id
            )
        }
        if (attachmentEntities.isNotEmpty()) {
            messageAttachmentDao.insertAll(attachmentEntities)
        }

        conversationDao.update(
            ConversationEntity(
                id = conversationId,
                title = _activeConversation.value?.title ?: "Chat",
                mode = message.mode.value,
                createdAt = _activeConversation.value?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun newConversation(mode: ThinkingMode) {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val entity = ConversationEntity(
            id = id,
            title = "New Conversation",
            mode = mode.value,
            createdAt = now,
            updatedAt = now
        )
        withContext(Dispatchers.IO) { conversationDao.insert(entity) }
        _activeConversation.value = entity.toDomain()
    }

    fun selectConversation(conversation: DomainMappers.Conversation) {
        _activeConversation.value = conversation
    }

    suspend fun deleteConversation(id: String) {
        withContext(Dispatchers.IO) { conversationDao.deleteById(id) }
        if (_activeConversation.value?.id == id) {
            _activeConversation.value = null
        }
    }

    fun searchConversations(
        conversations: List<DomainMappers.Conversation>,
        query: String
    ): List<DomainMappers.Conversation> {
        val lowered = query.lowercase()
        return conversations.filter { it.title.lowercase().contains(lowered) }
    }

    suspend fun updateConversationTitle(conversationId: String, title: String) =
        withContext(Dispatchers.IO) {
            val entity = conversationDao.getById(conversationId) ?: return@withContext
            conversationDao.update(entity.copy(title = title))
            val active = _activeConversation.value
            if (active != null && active.id == conversationId) {
                _activeConversation.value = active.copy(title = title)
            }
        }
}
