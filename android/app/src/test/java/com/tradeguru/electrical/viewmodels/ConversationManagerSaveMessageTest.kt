package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.data.db.TradeGuruDatabase
import com.tradeguru.electrical.data.db.dao.ChatMessageDao
import com.tradeguru.electrical.data.db.dao.ContentBlockDao
import com.tradeguru.electrical.data.db.dao.ConversationDao
import com.tradeguru.electrical.data.db.dao.MessageAttachmentDao
import com.tradeguru.electrical.data.db.dao.PartsItemDao
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import com.tradeguru.electrical.data.db.entities.ConversationEntity
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import com.tradeguru.electrical.data.db.entities.PartsItemEntity
import com.tradeguru.electrical.models.AttachmentType
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.ThinkingMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationManagerSaveMessageTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: TradeGuruDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var contentBlockDao: ContentBlockDao
    private lateinit var partsItemDao: PartsItemDao
    private lateinit var messageAttachmentDao: MessageAttachmentDao
    private lateinit var manager: ConversationManager

    private val conversationId = "conv-1"
    private val conversationEntity = ConversationEntity(
        id = conversationId,
        title = "Test Chat",
        mode = "fault_finder",
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        database = mockk()
        conversationDao = mockk(relaxed = true)
        chatMessageDao = mockk(relaxed = true)
        contentBlockDao = mockk(relaxed = true)
        partsItemDao = mockk(relaxed = true)
        messageAttachmentDao = mockk(relaxed = true)

        every { database.conversationDao() } returns conversationDao
        every { database.chatMessageDao() } returns chatMessageDao
        every { database.contentBlockDao() } returns contentBlockDao
        every { database.partsItemDao() } returns partsItemDao
        every { database.messageAttachmentDao() } returns messageAttachmentDao

        manager = ConversationManager(database)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeUserMessage(
        blocks: List<DomainMappers.ContentBlock> = listOf(
            DomainMappers.ContentBlock(
                id = UUID.randomUUID().toString(),
                type = ContentBlockType.TEXT,
                content = "Hello world"
            )
        ),
        attachments: List<DomainMappers.MessageAttachment> = emptyList()
    ) = DomainMappers.ChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.USER,
        blocks = blocks,
        timestamp = System.currentTimeMillis(),
        mode = ThinkingMode.FAULT_FINDER,
        attachments = attachments
    )

    private fun stubLoadConversation(
        messages: List<DomainMappers.ChatMessage> = emptyList()
    ) {
        coEvery { conversationDao.getById(conversationId) } returns conversationEntity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns
            messages.map { msg ->
                ChatMessageEntity(
                    id = msg.id,
                    role = msg.role.value,
                    timestamp = msg.timestamp,
                    mode = msg.mode.value,
                    conversationId = conversationId
                )
            }
        messages.forEach { msg ->
            val blockEntities = msg.blocks.map { block ->
                ContentBlockEntity(
                    id = block.id,
                    type = block.type.value,
                    content = block.content,
                    title = block.title,
                    steps = null,
                    language = block.language,
                    code = block.code,
                    clause = block.clause,
                    summary = block.summary,
                    url = block.url,
                    rows = null,
                    headers = null,
                    level = block.level,
                    style = block.style,
                    messageId = msg.id
                )
            }
            coEvery { contentBlockDao.getByMessageIdSync(msg.id) } returns blockEntities
            blockEntities.forEach { block ->
                val partEntities = msg.blocks
                    .find { it.id == block.id }?.items?.map { part ->
                        PartsItemEntity(
                            id = part.id,
                            name = part.name,
                            spec = part.spec,
                            qty = part.qty,
                            contentBlockId = block.id
                        )
                    } ?: emptyList()
                coEvery { partsItemDao.getByContentBlockIdSync(block.id) } returns partEntities
            }
            coEvery { messageAttachmentDao.getByMessageIdSync(msg.id) } returns
                msg.attachments.map { att ->
                    MessageAttachmentEntity(
                        id = att.id,
                        type = att.type.value,
                        fileName = att.fileName,
                        fileSize = att.fileSize,
                        thumbnailData = att.thumbnailData,
                        messageId = msg.id
                    )
                }
        }
    }

    @Test
    fun `saveMessage updates activeConversation with the saved message`() = runTest {
        val userMessage = makeUserMessage()
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).hasSize(1)
        assertThat(active.messages[0].role).isEqualTo(MessageRole.USER)
    }

    @Test
    fun `saveMessage includes all content blocks in the refreshed conversation`() = runTest {
        val blocks = listOf(
            DomainMappers.ContentBlock(id = "b1", type = ContentBlockType.TEXT, content = "Hello"),
            DomainMappers.ContentBlock(id = "b2", type = ContentBlockType.WARNING, content = "Danger")
        )
        val userMessage = makeUserMessage(blocks = blocks)
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active!!.messages[0].blocks).hasSize(2)
        assertThat(active.messages[0].blocks[0].type).isEqualTo(ContentBlockType.TEXT)
        assertThat(active.messages[0].blocks[1].type).isEqualTo(ContentBlockType.WARNING)
    }

    @Test
    fun `saveMessage includes parts items in content blocks`() = runTest {
        val parts = listOf(
            DomainMappers.PartsItem(id = "p1", name = "Wire", spec = "14 AWG", qty = 3)
        )
        val block = DomainMappers.ContentBlock(
            id = "b1", type = ContentBlockType.PARTS_LIST, items = parts
        )
        val userMessage = makeUserMessage(blocks = listOf(block))
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active!!.messages[0].blocks[0].items).hasSize(1)
        assertThat(active.messages[0].blocks[0].items[0].name).isEqualTo("Wire")
    }

    @Test
    fun `saveMessage includes attachments in the refreshed conversation`() = runTest {
        val att = DomainMappers.MessageAttachment(
            id = "att-1", type = AttachmentType.IMAGE, fileName = "photo.jpg"
        )
        val userMessage = makeUserMessage(attachments = listOf(att))
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active!!.messages[0].attachments).hasSize(1)
        assertThat(active.messages[0].attachments[0].fileName).isEqualTo("photo.jpg")
    }

    @Test
    fun `saveMessage updates conversation updatedAt timestamp`() = runTest {
        val userMessage = makeUserMessage()
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val entitySlot = slot<ConversationEntity>()
        coVerify { conversationDao.update(capture(entitySlot)) }
        assertThat(entitySlot.captured.updatedAt).isGreaterThan(1000L)
    }

    @Test
    fun `saveMessage with empty blocks still refreshes conversation`() = runTest {
        val userMessage = makeUserMessage(blocks = emptyList())
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        stubLoadConversation(listOf(userMessage))

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).hasSize(1)
        assertThat(active.messages[0].blocks).isEmpty()
    }

    @Test
    fun `consecutive saveMessage calls accumulate messages in activeConversation`() = runTest {
        val msg1 = makeUserMessage()
        val msg2 = makeUserMessage(
            blocks = listOf(
                DomainMappers.ContentBlock(
                    id = "b-reply", type = ContentBlockType.TEXT, content = "Reply"
                )
            )
        )
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )

        stubLoadConversation(listOf(msg1))
        manager.saveMessage(conversationId, msg1)

        stubLoadConversation(listOf(msg1, msg2))
        manager.saveMessage(conversationId, msg2)

        val active = manager.activeConversation.value
        assertThat(active!!.messages).hasSize(2)
    }

    @Test
    fun `activeConversation messages count matches DB after multiple saves`() = runTest {
        val messages = (1..5).map { i ->
            makeUserMessage(
                blocks = listOf(
                    DomainMappers.ContentBlock(
                        id = "b-$i", type = ContentBlockType.TEXT, content = "Message $i"
                    )
                )
            )
        }
        manager.selectConversation(
            DomainMappers.Conversation(
                id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )

        messages.forEachIndexed { index, msg ->
            stubLoadConversation(messages.subList(0, index + 1))
            manager.saveMessage(conversationId, msg)
        }

        val active = manager.activeConversation.value
        assertThat(active!!.messages).hasSize(5)
    }
}
