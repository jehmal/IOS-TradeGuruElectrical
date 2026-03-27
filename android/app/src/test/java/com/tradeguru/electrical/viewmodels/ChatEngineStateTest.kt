package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.services.AuthManager
import com.tradeguru.electrical.services.DeviceManager
import com.tradeguru.electrical.services.StreamErrorPayload
import com.tradeguru.electrical.services.StreamResult
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
class ChatEngineStateTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var engine: ChatEngine

    private val testConversation = DomainMappers.Conversation(
        id = "conv-eng-1",
        title = "Engine Test",
        mode = ThinkingMode.FAULT_FINDER,
        messages = listOf(
            DomainMappers.ChatMessage(
                id = "msg-1", role = MessageRole.USER,
                blocks = listOf(
                    DomainMappers.ContentBlock(id = "b1", type = ContentBlockType.TEXT, content = "Help")
                ),
                timestamp = 1000L, mode = ThinkingMode.FAULT_FINDER
            )
        ),
        createdAt = 1000L,
        updatedAt = 2000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val deviceManager = mockk<DeviceManager>()
        val authManager = mockk<AuthManager>()
        every { deviceManager.getOrCreateDeviceId() } returns "test-device"
        every { deviceManager.save(any()) } returns Unit
        every { authManager.currentJwt } returns null

        engine = ChatEngine(deviceManager, authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- clearError ---

    @Test
    fun `clearError sets error to null`() {
        engine.clearError()

        assertThat(engine.error.value).isNull()
    }

    @Test
    fun `clearError after handleStreamError clears the error message`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "timeout", message = "Request timed out", partial = false
            )
        )
        engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)
        assertThat(engine.error.value).isEqualTo("Request timed out")

        engine.clearError()

        assertThat(engine.error.value).isNull()
    }

    // --- finalizeResponse edge cases ---

    @Test
    fun `finalizeResponse with no streaming blocks returns empty blocks`() = runTest {
        val result = engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(result.blocks).isEmpty()
        assertThat(result.role).isEqualTo(MessageRole.ASSISTANT)
    }

    @Test
    fun `finalizeResponse resets pipelineStage to IDLE`() = runTest {
        engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.pipelineStage.value).isEqualTo(PipelineStage.IDLE)
    }

    @Test
    fun `finalizeResponse preserves the mode passed in`() = runTest {
        val result = engine.finalizeResponse(ThinkingMode.LEARN, testConversation)

        assertThat(result.mode).isEqualTo(ThinkingMode.LEARN)
    }

    @Test
    fun `finalizeResponse generates unique message IDs`() = runTest {
        val r1 = engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)
        val r2 = engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(r1.id).isNotEqualTo(r2.id)
    }

    // --- handleStreamError edge cases ---

    @Test
    fun `handleStreamError with partial true but empty blocks returns null`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "partial_empty", message = "Partial but empty", partial = true
            )
        )

        val result = engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(result).isNull()
        assertThat(engine.error.value).isEqualTo("Partial but empty")
    }

    @Test
    fun `handleStreamError with null partial clears blocks`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "unknown", message = "Unknown error", partial = null
            )
        )

        engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.streamingBlocks.value).isEmpty()
        assertThat(engine.isStreaming.value).isFalse()
    }

    @Test
    fun `handleStreamError always resets isStreaming to false`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "err", message = "Error", partial = true
            )
        )

        engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.isStreaming.value).isFalse()
    }

    // --- resetForNewConversation ---

    @Test
    fun `resetForNewConversation after error clears error state`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "err", message = "Some error", partial = false
            )
        )
        engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)
        assertThat(engine.error.value).isNotNull()

        engine.resetForNewConversation()

        assertThat(engine.error.value).isNull()
        assertThat(engine.isStreaming.value).isFalse()
        assertThat(engine.streamingBlocks.value).isEmpty()
    }

    // --- initial state ---

    @Test
    fun `engine initial state is correct`() {
        assertThat(engine.isStreaming.value).isFalse()
        assertThat(engine.streamingBlocks.value).isEmpty()
        assertThat(engine.error.value).isNull()
        assertThat(engine.pipelineStage.value).isEqualTo(PipelineStage.IDLE)
        assertThat(engine.lastResponseId).isNull()
    }
}
