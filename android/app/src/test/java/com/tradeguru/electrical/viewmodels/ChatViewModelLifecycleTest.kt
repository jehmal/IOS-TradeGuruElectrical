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
class ChatViewModelLifecycleTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var conversationManager: ConversationManager
    private lateinit var engine: ChatEngine
    private lateinit var viewModel: ChatViewModel

    private val activeConversationFlow = MutableStateFlow<DomainMappers.Conversation?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)

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
        every { engine.error } returns errorFlow
        every { engine.lastResponseId } returns null
        every { engine.pipelineStage } returns MutableStateFlow(PipelineStage.IDLE)

        viewModel = ChatViewModel(engine, conversationManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- newConversation ---

    @Test
    fun `newConversation delegates to conversationManager and resets engine`() = runTest {
        viewModel.newConversation(ThinkingMode.FAULT_FINDER)
        advanceUntilIdle()

        coVerify { conversationManager.newConversation(ThinkingMode.FAULT_FINDER) }
        verify { engine.resetForNewConversation() }
    }

    @Test
    fun `newConversation with LEARN mode passes correct mode`() = runTest {
        viewModel.newConversation(ThinkingMode.LEARN)
        advanceUntilIdle()

        coVerify { conversationManager.newConversation(ThinkingMode.LEARN) }
    }

    // --- deleteConversation ---

    @Test
    fun `deleteConversation delegates to conversationManager`() = runTest {
        val conv = DomainMappers.Conversation(
            id = "conv-del-1", title = "Delete Me", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )

        viewModel.deleteConversation(conv)
        advanceUntilIdle()

        coVerify { conversationManager.deleteConversation("conv-del-1") }
    }

    // --- dismissError ---

    @Test
    fun `dismissError clears engine error`() {
        viewModel.dismissError()

        verify { engine.clearError() }
    }

    // --- searchConversations ---

    @Test
    fun `searchConversations delegates to conversationManager`() {
        val convList = listOf(
            DomainMappers.Conversation(
                id = "1", title = "Wiring help", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )
        every { conversationManager.searchConversations(any(), "wiring") } returns convList

        val results = viewModel.searchConversations("wiring")

        assertThat(results).hasSize(1)
    }

    // --- retryLastRequest ---

    @Test
    fun `retryLastRequest does nothing when no active conversation`() = runTest {
        activeConversationFlow.value = null

        viewModel.retryLastRequest()
        advanceUntilIdle()

        coVerify(exactly = 0) { conversationManager.loadConversation(any()) }
    }

    @Test
    fun `retryLastRequest loads conversation and retries via engine`() = runTest {
        val conv = DomainMappers.Conversation(
            id = "conv-retry", title = "Retry", mode = ThinkingMode.FAULT_FINDER,
            messages = listOf(
                DomainMappers.ChatMessage(
                    id = "msg-1", role = MessageRole.USER,
                    blocks = listOf(DomainMappers.ContentBlock(id = "b1", type = ContentBlockType.TEXT, content = "Help")),
                    timestamp = 1000L, mode = ThinkingMode.FAULT_FINDER
                )
            ),
            createdAt = 1000L, updatedAt = 1000L
        )
        activeConversationFlow.value = conv
        coEvery { conversationManager.loadConversation("conv-retry") } returns conv

        viewModel.retryLastRequest()
        advanceUntilIdle()

        coVerify { engine.retryLastRequest(conv) }
    }

    @Test
    fun `retryLastRequest skips when loadConversation returns null`() = runTest {
        val conv = DomainMappers.Conversation(
            id = "conv-gone", title = "Gone", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        activeConversationFlow.value = conv
        coEvery { conversationManager.loadConversation("conv-gone") } returns null

        viewModel.retryLastRequest()
        advanceUntilIdle()

        coVerify(exactly = 0) { engine.retryLastRequest(any()) }
    }

    // --- selectedMode ---

    @Test
    fun `selectedMode defaults to FAULT_FINDER`() {
        assertThat(viewModel.selectedMode.value).isEqualTo(ThinkingMode.FAULT_FINDER)
    }

    @Test
    fun `selectedMode can be changed`() {
        viewModel.selectedMode.value = ThinkingMode.LEARN

        assertThat(viewModel.selectedMode.value).isEqualTo(ThinkingMode.LEARN)
    }

    // --- sendWithVision ---

    @Test
    fun `sendWithVision with empty text and no attachments is ignored`() = runTest {
        viewModel.sendWithVision("", ThinkingMode.FAULT_FINDER, null)
        advanceUntilIdle()

        coVerify(exactly = 0) { conversationManager.saveMessage(any(), any()) }
    }

    @Test
    fun `sendWithVision with empty text but attachments uses Photo placeholder`() = runTest {
        val testConv = DomainMappers.Conversation(
            id = "conv-v", title = "Vision", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        coEvery { conversationManager.ensureConversation(any(), any()) } returns testConv
        coEvery { conversationManager.loadConversation(any()) } returns testConv.copy(
            messages = listOf(
                DomainMappers.ChatMessage(
                    id = "m1", role = MessageRole.USER,
                    blocks = listOf(DomainMappers.ContentBlock(id = "b1", type = ContentBlockType.TEXT, content = "[Photo]")),
                    timestamp = 1000L, mode = ThinkingMode.FAULT_FINDER
                )
            )
        )
        coEvery { engine.finalizeResponse(any(), any()) } returns DomainMappers.ChatMessage(
            id = "a1", role = MessageRole.ASSISTANT,
            blocks = listOf(DomainMappers.ContentBlock(id = "ba1", type = ContentBlockType.TEXT, content = "I see")),
            timestamp = 2000L, mode = ThinkingMode.FAULT_FINDER
        )

        val att = DomainMappers.MessageAttachment(
            id = "att-1",
            type = com.tradeguru.electrical.models.AttachmentType.IMAGE,
            fileName = "photo.jpg"
        )

        viewModel.sendWithVision("", ThinkingMode.FAULT_FINDER, listOf(att))
        advanceUntilIdle()

        coVerify { conversationManager.saveMessage(any(), match { it.blocks[0].content == "[Photo]" }) }
    }

    // --- sendWithDocument ---

    @Test
    fun `sendWithDocument with empty text and no attachments is ignored`() = runTest {
        viewModel.sendWithDocument("", ThinkingMode.FAULT_FINDER, null)
        advanceUntilIdle()

        coVerify(exactly = 0) { conversationManager.saveMessage(any(), any()) }
    }
}
