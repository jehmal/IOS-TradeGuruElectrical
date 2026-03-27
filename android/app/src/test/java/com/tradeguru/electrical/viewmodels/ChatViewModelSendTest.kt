package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelSendTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var conversationManager: ConversationManager
    private lateinit var engine: ChatEngine
    private lateinit var viewModel: ChatViewModel

    private val activeConversationFlow = MutableStateFlow<DomainMappers.Conversation?>(null)

    private val testConversation = DomainMappers.Conversation(
        id = "conv-send-1",
        title = "Test",
        mode = ThinkingMode.FAULT_FINDER,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val testConversationWithMessages = DomainMappers.Conversation(
        id = "conv-send-1",
        title = "Test",
        mode = ThinkingMode.FAULT_FINDER,
        messages = listOf(
            DomainMappers.ChatMessage(
                id = "msg-user-1",
                role = MessageRole.USER,
                blocks = listOf(
                    DomainMappers.ContentBlock(
                        id = "blk-1", type = ContentBlockType.TEXT, content = "Hello"
                    )
                ),
                timestamp = 1000L,
                mode = ThinkingMode.FAULT_FINDER
            )
        ),
        createdAt = 1000L,
        updatedAt = 2000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        conversationManager = mockk(relaxed = true)
        every { conversationManager.activeConversation } returns activeConversationFlow
        every { conversationManager.getAllConversations() } returns flowOf(emptyList())
        every { conversationManager.selectConversation(any()) } answers {
            activeConversationFlow.value = firstArg()
        }
        coEvery { conversationManager.ensureConversation(any(), any()) } returns testConversation
        coEvery { conversationManager.loadConversation(any()) } returns testConversationWithMessages

        engine = mockk(relaxed = true)
        every { engine.isStreaming } returns MutableStateFlow(false)
        every { engine.streamingBlocks } returns MutableStateFlow(emptyList())
        every { engine.error } returns MutableStateFlow(null)
        every { engine.lastResponseId } returns null
        every { engine.pipelineStage } returns MutableStateFlow(PipelineStage.IDLE)

        coEvery { engine.finalizeResponse(any(), any()) } returns DomainMappers.ChatMessage(
            id = "assistant-msg-1",
            role = MessageRole.ASSISTANT,
            blocks = listOf(
                DomainMappers.ContentBlock(
                    id = "blk-asst-1", type = ContentBlockType.TEXT,
                    content = "Check your breaker panel"
                )
            ),
            timestamp = System.currentTimeMillis(),
            mode = ThinkingMode.FAULT_FINDER
        )

        viewModel = ChatViewModel(engine, conversationManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send creates user message and saves it`() = runTest {
        viewModel.send("What's wrong with my outlet?", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify { conversationManager.saveMessage(any(), match { it.role == MessageRole.USER }) }
    }

    @Test
    fun `send triggers fetchResponse and finalizes assistant message`() = runTest {
        viewModel.send("Fix my wiring", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify { engine.fetchResponse(ThinkingMode.FAULT_FINDER, any()) }
        coVerify { engine.finalizeResponse(any(), any()) }
    }

    @Test
    fun `send with empty text is ignored`() = runTest {
        viewModel.send("", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify(exactly = 0) { conversationManager.saveMessage(any(), any()) }
    }

    @Test
    fun `send with whitespace-only text is ignored`() = runTest {
        viewModel.send("   \n\t  ", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify(exactly = 0) { conversationManager.saveMessage(any(), any()) }
    }

    @Test
    fun `send creates conversation if none active`() = runTest {
        viewModel.send("Hello", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify { conversationManager.ensureConversation(any(), any()) }
    }

    @Test
    fun `send reuses existing active conversation`() = runTest {
        activeConversationFlow.value = testConversation
        coEvery { conversationManager.ensureConversation(any(), any()) } returns testConversation

        viewModel.send("Another question", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify { conversationManager.saveMessage("conv-send-1", any()) }
    }

    @Test
    fun `send passes full conversation history to engine`() = runTest {
        viewModel.send("Query", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        val convSlot = slot<DomainMappers.Conversation>()
        coVerify { engine.fetchResponse(any(), capture(convSlot)) }
        assertThat(convSlot.captured.messages).isNotEmpty()
    }

    @Test
    fun `send saves assistant response after streaming`() = runTest {
        viewModel.send("My outlet sparks when I plug in", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify(atLeast = 2) { conversationManager.saveMessage(any(), any()) }
    }

    @Test
    fun `send does not persist assistant response on error`() = runTest {
        every { engine.error } returns MutableStateFlow("Network error")
        viewModel = ChatViewModel(engine, conversationManager)

        viewModel.send("Help", ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify(exactly = 1) { conversationManager.saveMessage(any(), any()) }
    }
}
