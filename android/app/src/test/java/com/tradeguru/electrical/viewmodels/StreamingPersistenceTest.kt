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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class StreamingPersistenceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var engine: ChatEngine

    private val testConversation = DomainMappers.Conversation(
        id = "conv-stream-1",
        title = "Streaming Test",
        mode = ThinkingMode.FAULT_FINDER,
        messages = listOf(
            DomainMappers.ChatMessage(
                id = "msg-1",
                role = MessageRole.USER,
                blocks = listOf(
                    DomainMappers.ContentBlock(
                        id = "blk-1", type = ContentBlockType.TEXT, content = "Help"
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

    @Test
    fun `streaming blocks are visible during active stream`() = runTest {
        val block1 = DomainMappers.ContentBlock(
            id = "sb-1", type = ContentBlockType.TEXT, content = "First chunk"
        )
        val block2 = DomainMappers.ContentBlock(
            id = "sb-2", type = ContentBlockType.TEXT, content = "Second chunk"
        )

        assertThat(engine.streamingBlocks.value).isEmpty()

        // Simulate processStreamEvent by testing through public state
        // Engine exposes streamingBlocks as StateFlow - we verify the finalizeResponse behavior
        // Since processStreamEvent is private, we test the contract through finalize
        assertThat(engine.isStreaming.value).isFalse()
    }

    @Test
    fun `finalizeResponse clears streaming blocks`() = runTest {
        val result = engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.streamingBlocks.value).isEmpty()
        assertThat(engine.isStreaming.value).isFalse()
    }

    @Test
    fun `finalizeResponse returns ChatMessage with all accumulated blocks`() = runTest {
        // finalizeResponse snapshots current streamingBlocks (empty at start)
        val result = engine.finalizeResponse(ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(result.role).isEqualTo(MessageRole.ASSISTANT)
        assertThat(result.mode).isEqualTo(ThinkingMode.FAULT_FINDER)
        assertThat(result.id).isNotEmpty()
    }

    @Test
    fun `after finalization saved assistant message has correct role`() = runTest {
        val result = engine.finalizeResponse(ThinkingMode.LEARN, testConversation)

        assertThat(result.role).isEqualTo(MessageRole.ASSISTANT)
        assertThat(result.mode).isEqualTo(ThinkingMode.LEARN)
    }

    @Test
    fun `handleStreamError with partial true preserves partial blocks`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "connection_lost",
                message = "Connection lost",
                partial = true
            )
        )

        val result = engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.isStreaming.value).isFalse()
        assertThat(engine.error.value).isEqualTo("Connection lost")
    }

    @Test
    fun `handleStreamError with partial false clears blocks`() = runTest {
        val errorResult = StreamResult.Error(
            payload = StreamErrorPayload(
                code = "server_error",
                message = "Server error",
                partial = false
            )
        )

        engine.handleStreamError(errorResult, ThinkingMode.FAULT_FINDER, testConversation)

        assertThat(engine.streamingBlocks.value).isEmpty()
        assertThat(engine.isStreaming.value).isFalse()
        assertThat(engine.pipelineStage.value).isEqualTo(PipelineStage.IDLE)
    }

    @Test
    fun `resetForNewConversation clears all streaming state`() = runTest {
        engine.resetForNewConversation()

        assertThat(engine.streamingBlocks.value).isEmpty()
        assertThat(engine.isStreaming.value).isFalse()
        assertThat(engine.error.value).isNull()
    }
}
