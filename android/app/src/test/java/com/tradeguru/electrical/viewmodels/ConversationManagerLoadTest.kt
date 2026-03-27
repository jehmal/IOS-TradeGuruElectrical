package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
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
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationManagerLoadTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: TradeGuruDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var contentBlockDao: ContentBlockDao
    private lateinit var partsItemDao: PartsItemDao
    private lateinit var messageAttachmentDao: MessageAttachmentDao
    private lateinit var manager: ConversationManager

    private val conversationId = "conv-load-1"

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

    @Test
    fun `loadConversation returns conversation with all messages`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        val msgEntity = ChatMessageEntity(
            id = "msg-1", role = "user", timestamp = 1500L,
            mode = "fault_finder", conversationId = conversationId
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msgEntity)
        coEvery { contentBlockDao.getByMessageIdSync("msg-1") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-1") } returns emptyList()

        val result = manager.loadConversation(conversationId)

        assertThat(result).isNotNull()
        assertThat(result!!.messages).hasSize(1)
        assertThat(result.messages[0].role).isEqualTo(MessageRole.USER)
    }

    @Test
    fun `loadConversation includes content blocks for each message`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        val msgEntity = ChatMessageEntity(
            id = "msg-1", role = "user", timestamp = 1500L,
            mode = "fault_finder", conversationId = conversationId
        )
        val blockEntity = ContentBlockEntity(
            id = "blk-1", type = "text", content = "Hello",
            title = null, steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "msg-1"
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msgEntity)
        coEvery { contentBlockDao.getByMessageIdSync("msg-1") } returns listOf(blockEntity)
        coEvery { partsItemDao.getByContentBlockIdSync("blk-1") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-1") } returns emptyList()

        val result = manager.loadConversation(conversationId)

        assertThat(result!!.messages[0].blocks).hasSize(1)
        assertThat(result.messages[0].blocks[0].type).isEqualTo(ContentBlockType.TEXT)
        assertThat(result.messages[0].blocks[0].content).isEqualTo("Hello")
    }

    @Test
    fun `loadConversation includes parts items nested in blocks`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        val msgEntity = ChatMessageEntity(
            id = "msg-1", role = "assistant", timestamp = 1500L,
            mode = "fault_finder", conversationId = conversationId
        )
        val blockEntity = ContentBlockEntity(
            id = "blk-1", type = "parts_list", content = null,
            title = "Parts needed", steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "msg-1"
        )
        val partEntity = PartsItemEntity(
            id = "p-1", name = "Circuit breaker", spec = "20A", qty = 2,
            contentBlockId = "blk-1"
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msgEntity)
        coEvery { contentBlockDao.getByMessageIdSync("msg-1") } returns listOf(blockEntity)
        coEvery { partsItemDao.getByContentBlockIdSync("blk-1") } returns listOf(partEntity)
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-1") } returns emptyList()

        val result = manager.loadConversation(conversationId)

        assertThat(result!!.messages[0].blocks[0].items).hasSize(1)
        assertThat(result.messages[0].blocks[0].items[0].name).isEqualTo("Circuit breaker")
        assertThat(result.messages[0].blocks[0].items[0].qty).isEqualTo(2)
    }

    @Test
    fun `loadConversation includes attachments for each message`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        val msgEntity = ChatMessageEntity(
            id = "msg-1", role = "user", timestamp = 1500L,
            mode = "fault_finder", conversationId = conversationId
        )
        val attEntity = MessageAttachmentEntity(
            id = "att-1", type = "image", fileName = "panel.jpg",
            fileSize = 1024, thumbnailData = byteArrayOf(1, 2, 3),
            messageId = "msg-1"
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msgEntity)
        coEvery { contentBlockDao.getByMessageIdSync("msg-1") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-1") } returns listOf(attEntity)

        val result = manager.loadConversation(conversationId)

        assertThat(result!!.messages[0].attachments).hasSize(1)
        assertThat(result.messages[0].attachments[0].fileName).isEqualTo("panel.jpg")
    }

    @Test
    fun `loadConversation returns null for non-existent conversation`() = runTest {
        coEvery { conversationDao.getById("nonexistent") } returns null

        val result = manager.loadConversation("nonexistent")

        assertThat(result).isNull()
    }

    @Test
    fun `loadConversation returns empty messages list for conversation with no messages`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Empty", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns emptyList()

        val result = manager.loadConversation(conversationId)

        assertThat(result).isNotNull()
        assertThat(result!!.messages).isEmpty()
    }

    @Test
    fun `loadConversation preserves message ordering by timestamp`() = runTest {
        val entity = ConversationEntity(
            id = conversationId, title = "Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 3000L
        )
        val msg1 = ChatMessageEntity(
            id = "msg-1", role = "user", timestamp = 1000L,
            mode = "fault_finder", conversationId = conversationId
        )
        val msg2 = ChatMessageEntity(
            id = "msg-2", role = "assistant", timestamp = 2000L,
            mode = "fault_finder", conversationId = conversationId
        )
        val msg3 = ChatMessageEntity(
            id = "msg-3", role = "user", timestamp = 3000L,
            mode = "fault_finder", conversationId = conversationId
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msg1, msg2, msg3)
        listOf("msg-1", "msg-2", "msg-3").forEach { id ->
            coEvery { contentBlockDao.getByMessageIdSync(id) } returns emptyList()
            coEvery { messageAttachmentDao.getByMessageIdSync(id) } returns emptyList()
        }

        val result = manager.loadConversation(conversationId)

        assertThat(result!!.messages).hasSize(3)
        assertThat(result.messages[0].role).isEqualTo(MessageRole.USER)
        assertThat(result.messages[1].role).isEqualTo(MessageRole.ASSISTANT)
        assertThat(result.messages[2].role).isEqualTo(MessageRole.USER)
    }
}
