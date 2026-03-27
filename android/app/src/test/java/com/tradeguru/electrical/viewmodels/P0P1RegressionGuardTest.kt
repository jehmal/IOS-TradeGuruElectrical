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
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * REGRESSION GUARDS for P0 and P1 bugs.
 * These tests verify the exact fix behaviors described in spec.md.
 * If any test here fails, the P0/P1 bugs have regressed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class P0P1RegressionGuardTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // ConversationManager dependencies (for P0 tests)
    private lateinit var database: TradeGuruDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var contentBlockDao: ContentBlockDao
    private lateinit var partsItemDao: PartsItemDao
    private lateinit var messageAttachmentDao: MessageAttachmentDao

    // ViewModel dependencies (for P1 tests)
    private lateinit var conversationManager: ConversationManager
    private lateinit var engine: ChatEngine

    private val conversationId = "conv-regression"

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =============================================
    // P0 REGRESSION GUARD: saveMessage must refresh activeConversation
    // Root cause: _activeConversation was never updated after DB writes
    // Fix: _activeConversation.value = loadConversation(conversationId)
    // =============================================

    @Test
    fun `P0 GUARD - saveMessage MUST re-emit activeConversation with fresh messages`() = runTest {
        val manager = ConversationManager(database)

        val emptyConv = DomainMappers.Conversation(
            id = conversationId, title = "P0 Test", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(emptyConv)

        assertThat(manager.activeConversation.value!!.messages).isEmpty()

        val userMessage = DomainMappers.ChatMessage(
            id = "user-msg-p0", role = MessageRole.USER,
            blocks = listOf(
                DomainMappers.ContentBlock(id = "b-p0", type = ContentBlockType.TEXT, content = "Why is my outlet sparking?")
            ),
            timestamp = System.currentTimeMillis(), mode = ThinkingMode.FAULT_FINDER
        )

        val entity = ConversationEntity(
            id = conversationId, title = "P0 Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )
        val msgEntity = ChatMessageEntity(
            id = "user-msg-p0", role = "user", timestamp = 1500L,
            mode = "fault_finder", conversationId = conversationId
        )
        val blockEntity = ContentBlockEntity(
            id = "b-p0", type = "text", content = "Why is my outlet sparking?",
            title = null, steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "user-msg-p0"
        )
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(msgEntity)
        coEvery { contentBlockDao.getByMessageIdSync("user-msg-p0") } returns listOf(blockEntity)
        coEvery { partsItemDao.getByContentBlockIdSync("b-p0") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("user-msg-p0") } returns emptyList()

        manager.saveMessage(conversationId, userMessage)

        val active = manager.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).isNotEmpty()
        assertThat(active.messages[0].role).isEqualTo(MessageRole.USER)
        assertThat(active.messages[0].blocks[0].content).isEqualTo("Why is my outlet sparking?")
    }

    @Test
    fun `P0 GUARD - second saveMessage MUST show both messages in activeConversation`() = runTest {
        val manager = ConversationManager(database)

        val conv = DomainMappers.Conversation(
            id = conversationId, title = "P0 Test", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(conv)

        val msg1 = DomainMappers.ChatMessage(
            id = "msg-1", role = MessageRole.USER,
            blocks = listOf(DomainMappers.ContentBlock(id = "b1", type = ContentBlockType.TEXT, content = "Q1")),
            timestamp = 1000L, mode = ThinkingMode.FAULT_FINDER
        )
        val msg2 = DomainMappers.ChatMessage(
            id = "msg-2", role = MessageRole.ASSISTANT,
            blocks = listOf(DomainMappers.ContentBlock(id = "b2", type = ContentBlockType.TEXT, content = "A1")),
            timestamp = 2000L, mode = ThinkingMode.FAULT_FINDER
        )

        val entity = ConversationEntity(
            id = conversationId, title = "P0 Test", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )

        // After first save: 1 message
        coEvery { conversationDao.getById(conversationId) } returns entity
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(
            ChatMessageEntity(id = "msg-1", role = "user", timestamp = 1000L, mode = "fault_finder", conversationId = conversationId)
        )
        coEvery { contentBlockDao.getByMessageIdSync("msg-1") } returns listOf(
            ContentBlockEntity(id = "b1", type = "text", content = "Q1", title = null, steps = null, language = null, code = null, clause = null, summary = null, url = null, rows = null, headers = null, level = null, style = null, messageId = "msg-1")
        )
        coEvery { partsItemDao.getByContentBlockIdSync("b1") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-1") } returns emptyList()

        manager.saveMessage(conversationId, msg1)
        assertThat(manager.activeConversation.value!!.messages).hasSize(1)

        // After second save: 2 messages
        coEvery { conversationDao.getMessagesForConversation(conversationId) } returns listOf(
            ChatMessageEntity(id = "msg-1", role = "user", timestamp = 1000L, mode = "fault_finder", conversationId = conversationId),
            ChatMessageEntity(id = "msg-2", role = "assistant", timestamp = 2000L, mode = "fault_finder", conversationId = conversationId)
        )
        coEvery { contentBlockDao.getByMessageIdSync("msg-2") } returns listOf(
            ContentBlockEntity(id = "b2", type = "text", content = "A1", title = null, steps = null, language = null, code = null, clause = null, summary = null, url = null, rows = null, headers = null, level = null, style = null, messageId = "msg-2")
        )
        coEvery { partsItemDao.getByContentBlockIdSync("b2") } returns emptyList()
        coEvery { messageAttachmentDao.getByMessageIdSync("msg-2") } returns emptyList()

        manager.saveMessage(conversationId, msg2)
        assertThat(manager.activeConversation.value!!.messages).hasSize(2)
    }

    // =============================================
    // P1 REGRESSION GUARD: selectConversation must deep-load
    // Root cause: sidebar conversations had messages = emptyList()
    // Fix: viewModelScope.launch { loadConversation(id) -> selectConversation(full) }
    // =============================================

    @Test
    fun `P1 GUARD - selectConversation MUST deep-load messages from DB`() = runTest {
        val activeConversationFlow = MutableStateFlow<DomainMappers.Conversation?>(null)
        conversationManager = mockk(relaxed = true)
        every { conversationManager.activeConversation } returns activeConversationFlow
        every { conversationManager.getAllConversations() } returns flowOf(emptyList())
        every { conversationManager.selectConversation(any()) } answers {
            activeConversationFlow.value = firstArg()
        }

        engine = mockk(relaxed = true)
        every { engine.isStreaming } returns MutableStateFlow(false)
        every { engine.streamingBlocks } returns MutableStateFlow(emptyList())
        every { engine.error } returns MutableStateFlow(null)
        every { engine.lastResponseId } returns null
        every { engine.pipelineStage } returns MutableStateFlow(PipelineStage.IDLE)

        val fullConversation = DomainMappers.Conversation(
            id = conversationId, title = "P1 Test", mode = ThinkingMode.FAULT_FINDER,
            messages = listOf(
                DomainMappers.ChatMessage(
                    id = "msg-p1", role = MessageRole.USER,
                    blocks = listOf(DomainMappers.ContentBlock(id = "bp1", type = ContentBlockType.TEXT, content = "Old question")),
                    timestamp = 1000L, mode = ThinkingMode.FAULT_FINDER
                ),
                DomainMappers.ChatMessage(
                    id = "msg-p1-asst", role = MessageRole.ASSISTANT,
                    blocks = listOf(DomainMappers.ContentBlock(id = "bp1a", type = ContentBlockType.TEXT, content = "Old answer")),
                    timestamp = 2000L, mode = ThinkingMode.FAULT_FINDER
                )
            ),
            createdAt = 1000L, updatedAt = 2000L
        )
        coEvery { conversationManager.loadConversation(conversationId) } returns fullConversation

        val viewModel = ChatViewModel(engine, conversationManager)

        val shallowConv = DomainMappers.Conversation(
            id = conversationId, title = "P1 Test", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(),
            createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).hasSize(2)
        assertThat(active.messages[0].blocks[0].content).isEqualTo("Old question")
        assertThat(active.messages[1].blocks[0].content).isEqualTo("Old answer")
    }

    @Test
    fun `P1 GUARD - selectConversation with shallow conversation must NOT show empty`() = runTest {
        val activeConversationFlow = MutableStateFlow<DomainMappers.Conversation?>(null)
        conversationManager = mockk(relaxed = true)
        every { conversationManager.activeConversation } returns activeConversationFlow
        every { conversationManager.getAllConversations() } returns flowOf(emptyList())
        every { conversationManager.selectConversation(any()) } answers {
            activeConversationFlow.value = firstArg()
        }

        engine = mockk(relaxed = true)
        every { engine.isStreaming } returns MutableStateFlow(false)
        every { engine.streamingBlocks } returns MutableStateFlow(emptyList())
        every { engine.error } returns MutableStateFlow(null)
        every { engine.lastResponseId } returns null
        every { engine.pipelineStage } returns MutableStateFlow(PipelineStage.IDLE)

        val fullConversation = DomainMappers.Conversation(
            id = "conv-with-history", title = "History", mode = ThinkingMode.FAULT_FINDER,
            messages = listOf(
                DomainMappers.ChatMessage(
                    id = "h-msg", role = MessageRole.USER,
                    blocks = listOf(DomainMappers.ContentBlock(id = "hb", type = ContentBlockType.TEXT, content = "Previous chat")),
                    timestamp = 500L, mode = ThinkingMode.FAULT_FINDER
                )
            ),
            createdAt = 500L, updatedAt = 500L
        )
        coEvery { conversationManager.loadConversation("conv-with-history") } returns fullConversation

        val viewModel = ChatViewModel(engine, conversationManager)

        val sidebarEntry = DomainMappers.Conversation(
            id = "conv-with-history", title = "History", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(),
            createdAt = 500L, updatedAt = 500L
        )

        viewModel.selectConversation(sidebarEntry)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active!!.messages).isNotEmpty()
    }
}
