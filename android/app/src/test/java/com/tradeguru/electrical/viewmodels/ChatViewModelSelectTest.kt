package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class ChatViewModelSelectTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var conversationManager: ConversationManager
    private lateinit var engine: ChatEngine
    private lateinit var viewModel: ChatViewModel

    private val activeConversationFlow = MutableStateFlow<DomainMappers.Conversation?>(null)
    private val conversationId = "conv-select-1"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

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

        viewModel = ChatViewModel(engine, conversationManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeFullConversation(
        id: String = conversationId,
        messages: List<Pair<MessageRole, String>>
    ) = DomainMappers.Conversation(
        id = id,
        title = "Test",
        mode = ThinkingMode.FAULT_FINDER,
        messages = messages.mapIndexed { i, (role, content) ->
            DomainMappers.ChatMessage(
                id = "msg-$i",
                role = role,
                blocks = listOf(
                    DomainMappers.ContentBlock(
                        id = "blk-$i", type = ContentBlockType.TEXT, content = content
                    )
                ),
                timestamp = 1000L + i * 100,
                mode = ThinkingMode.FAULT_FINDER
            )
        },
        createdAt = 1000L,
        updatedAt = 2000L
    )

    @Test
    fun `selectConversation deep-loads messages from database`() = runTest {
        val fullConv = makeFullConversation(
            messages = listOf(MessageRole.USER to "Hello", MessageRole.ASSISTANT to "Hi")
        )
        coEvery { conversationManager.loadConversation(conversationId) } returns fullConv

        val shallowConv = DomainMappers.Conversation(
            id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).isNotEmpty()
    }

    @Test
    fun `selectConversation sets activeConversation with loaded messages`() = runTest {
        val fullConv = makeFullConversation(
            messages = listOf(MessageRole.USER to "Fix my outlet", MessageRole.ASSISTANT to "Check breaker")
        )
        coEvery { conversationManager.loadConversation(conversationId) } returns fullConv

        val shallowConv = DomainMappers.Conversation(
            id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active!!.messages).hasSize(2)
        assertThat(active.messages[0].blocks[0].content).isEqualTo("Fix my outlet")
        assertThat(active.messages[1].blocks[0].content).isEqualTo("Check breaker")
    }

    @Test
    fun `selectConversation resets streaming state`() = runTest {
        val fullConv = makeFullConversation(messages = listOf(MessageRole.USER to "Hello"))
        coEvery { conversationManager.loadConversation(conversationId) } returns fullConv

        val shallowConv = DomainMappers.Conversation(
            id = conversationId, title = "Test", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        verify { engine.resetForNewConversation() }
    }

    @Test
    fun `selectConversation handles conversation with zero messages`() = runTest {
        val emptyConv = DomainMappers.Conversation(
            id = conversationId, title = "Empty", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )
        coEvery { conversationManager.loadConversation(conversationId) } returns emptyConv

        viewModel.selectConversation(emptyConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.messages).isEmpty()
    }

    @Test
    fun `selectConversation handles conversation with many messages and blocks`() = runTest {
        val messages = (1..20).map { i ->
            val role = if (i % 2 == 1) MessageRole.USER else MessageRole.ASSISTANT
            role to "Message $i"
        }
        val fullConv = makeFullConversation(messages = messages)
        coEvery { conversationManager.loadConversation(conversationId) } returns fullConv

        val shallowConv = DomainMappers.Conversation(
            id = conversationId, title = "Long Chat", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active!!.messages).hasSize(20)
    }

    @Test
    fun `selectConversation handles deleted conversation gracefully`() = runTest {
        coEvery { conversationManager.loadConversation("deleted-conv") } returns null

        val shallowConv = DomainMappers.Conversation(
            id = "deleted-conv", title = "Gone", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )

        viewModel.selectConversation(shallowConv)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.id).isEqualTo("deleted-conv")
    }

    @Test
    fun `rapid selectConversation calls resolve to last selection`() = runTest {
        val convA = makeFullConversation(
            id = "conv-a", messages = listOf(MessageRole.USER to "First")
        )
        val convB = makeFullConversation(
            id = "conv-b", messages = listOf(MessageRole.USER to "Second")
        )
        coEvery { conversationManager.loadConversation("conv-a") } returns convA
        coEvery { conversationManager.loadConversation("conv-b") } returns convB

        val shallowA = DomainMappers.Conversation(
            id = "conv-a", title = "A", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 2000L
        )
        val shallowB = DomainMappers.Conversation(
            id = "conv-b", title = "B", mode = ThinkingMode.FAULT_FINDER,
            messages = emptyList(), createdAt = 1000L, updatedAt = 3000L
        )

        viewModel.selectConversation(shallowA)
        viewModel.selectConversation(shallowB)
        advanceUntilIdle()

        val active = viewModel.activeConversation.value
        assertThat(active!!.id).isEqualTo("conv-b")
    }
}
