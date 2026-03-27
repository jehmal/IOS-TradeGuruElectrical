package com.tradeguru.electrical.data

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers.toDomain
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import com.tradeguru.electrical.data.db.entities.ConversationEntity
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import com.tradeguru.electrical.data.db.entities.PartsItemEntity
import com.tradeguru.electrical.models.AttachmentType
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.ThinkingMode
import org.junit.Test

class DomainMappersTest {

    // --- ConversationEntity.toDomain ---

    @Test
    fun `ConversationEntity toDomain maps all fields correctly`() {
        val entity = ConversationEntity(
            id = "conv-1", title = "Test Chat", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 2000L
        )

        val result = entity.toDomain()

        assertThat(result.id).isEqualTo("conv-1")
        assertThat(result.title).isEqualTo("Test Chat")
        assertThat(result.mode).isEqualTo(ThinkingMode.FAULT_FINDER)
        assertThat(result.messages).isEmpty()
        assertThat(result.createdAt).isEqualTo(1000L)
        assertThat(result.updatedAt).isEqualTo(2000L)
    }

    @Test
    fun `ConversationEntity toDomain with unknown mode falls back to FAULT_FINDER`() {
        val entity = ConversationEntity(
            id = "conv-2", title = "Unknown Mode", mode = "nonexistent_mode",
            createdAt = 1000L, updatedAt = 1000L
        )

        val result = entity.toDomain()

        assertThat(result.mode).isEqualTo(ThinkingMode.FAULT_FINDER)
    }

    @Test
    fun `ConversationEntity toDomain with messages passes them through`() {
        val entity = ConversationEntity(
            id = "conv-3", title = "With Messages", mode = "learn",
            createdAt = 1000L, updatedAt = 2000L
        )
        val messages = listOf(
            DomainMappers.ChatMessage(
                id = "msg-1", role = MessageRole.USER,
                blocks = emptyList(), timestamp = 1500L,
                mode = ThinkingMode.LEARN
            )
        )

        val result = entity.toDomain(messages)

        assertThat(result.messages).hasSize(1)
        assertThat(result.mode).isEqualTo(ThinkingMode.LEARN)
    }

    // --- ChatMessageEntity.toDomain ---

    @Test
    fun `ChatMessageEntity toDomain maps role and mode correctly`() {
        val entity = ChatMessageEntity(
            id = "msg-1", role = "assistant", timestamp = 1000L,
            mode = "learn", conversationId = "conv-1"
        )

        val result = entity.toDomain(emptyList())

        assertThat(result.role).isEqualTo(MessageRole.ASSISTANT)
        assertThat(result.mode).isEqualTo(ThinkingMode.LEARN)
    }

    @Test
    fun `ChatMessageEntity toDomain with unknown role falls back to USER`() {
        val entity = ChatMessageEntity(
            id = "msg-2", role = "unknown_role", timestamp = 1000L,
            mode = "fault_finder", conversationId = "conv-1"
        )

        val result = entity.toDomain(emptyList())

        assertThat(result.role).isEqualTo(MessageRole.USER)
    }

    @Test
    fun `ChatMessageEntity toDomain includes attachments`() {
        val entity = ChatMessageEntity(
            id = "msg-3", role = "user", timestamp = 1000L,
            mode = "fault_finder", conversationId = "conv-1"
        )
        val attachments = listOf(
            DomainMappers.MessageAttachment(
                id = "att-1", type = AttachmentType.IMAGE, fileName = "photo.jpg"
            )
        )

        val result = entity.toDomain(emptyList(), attachments)

        assertThat(result.attachments).hasSize(1)
    }

    // --- ContentBlockEntity.toDomain ---

    @Test
    fun `ContentBlockEntity toDomain maps text block`() {
        val entity = ContentBlockEntity(
            id = "blk-1", type = "text", content = "Hello world",
            title = null, steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.type).isEqualTo(ContentBlockType.TEXT)
        assertThat(result.content).isEqualTo("Hello world")
    }

    @Test
    fun `ContentBlockEntity toDomain with unknown type falls back to TEXT`() {
        val entity = ContentBlockEntity(
            id = "blk-2", type = "nonexistent_type", content = "test",
            title = null, steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.type).isEqualTo(ContentBlockType.TEXT)
    }

    @Test
    fun `ContentBlockEntity toDomain deserializes steps JSON`() {
        val entity = ContentBlockEntity(
            id = "blk-3", type = "safety_steps", content = null,
            title = "Safety", steps = """["Turn off breaker","Test with multimeter","Reconnect"]""",
            language = null, code = null, clause = null, summary = null,
            url = null, rows = null, headers = null, level = null,
            style = null, messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.steps).hasSize(3)
        assertThat(result.steps!![0]).isEqualTo("Turn off breaker")
    }

    @Test
    fun `ContentBlockEntity toDomain deserializes rows and headers JSON`() {
        val entity = ContentBlockEntity(
            id = "blk-4", type = "table", content = null,
            title = "Wire Gauge Table", steps = null,
            language = null, code = null, clause = null, summary = null,
            url = null,
            rows = """[["14 AWG","15A","Lighting"],["12 AWG","20A","Outlets"]]""",
            headers = """["Gauge","Amperage","Use"]""",
            level = null, style = null, messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.headers).hasSize(3)
        assertThat(result.headers!![0]).isEqualTo("Gauge")
        assertThat(result.rows).hasSize(2)
        assertThat(result.rows!![0][0]).isEqualTo("14 AWG")
    }

    @Test
    fun `ContentBlockEntity toDomain maps parts items`() {
        val entity = ContentBlockEntity(
            id = "blk-5", type = "parts_list", content = null,
            title = null, steps = null, language = null, code = null,
            clause = null, summary = null, url = null, rows = null,
            headers = null, level = null, style = null, messageId = "msg-1"
        )
        val parts = listOf(
            PartsItemEntity(id = "p1", name = "Wire nut", spec = "Yellow", qty = 5, contentBlockId = "blk-5")
        )

        val result = entity.toDomain(parts)

        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].name).isEqualTo("Wire nut")
        assertThat(result.items[0].qty).isEqualTo(5)
    }

    // --- PartsItemEntity.toDomain ---

    @Test
    fun `PartsItemEntity toDomain maps all fields`() {
        val entity = PartsItemEntity(
            id = "p1", name = "Circuit breaker", spec = "20A single-pole",
            qty = 2, contentBlockId = "blk-1"
        )

        val result = entity.toDomain()

        assertThat(result.id).isEqualTo("p1")
        assertThat(result.name).isEqualTo("Circuit breaker")
        assertThat(result.spec).isEqualTo("20A single-pole")
        assertThat(result.qty).isEqualTo(2)
    }

    // --- MessageAttachmentEntity.toDomain ---

    @Test
    fun `MessageAttachmentEntity toDomain maps image type`() {
        val entity = MessageAttachmentEntity(
            id = "att-1", type = "image", fileName = "panel.jpg",
            fileSize = 2048, thumbnailData = byteArrayOf(1, 2, 3),
            messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.type).isEqualTo(AttachmentType.IMAGE)
        assertThat(result.fileName).isEqualTo("panel.jpg")
        assertThat(result.fileSize).isEqualTo(2048)
    }

    @Test
    fun `MessageAttachmentEntity toDomain with unknown type falls back to IMAGE`() {
        val entity = MessageAttachmentEntity(
            id = "att-2", type = "unknown_attachment", fileName = "file.bin",
            fileSize = null, thumbnailData = null, messageId = "msg-1"
        )

        val result = entity.toDomain()

        assertThat(result.type).isEqualTo(AttachmentType.IMAGE)
    }
}
