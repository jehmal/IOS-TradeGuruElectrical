package com.tradeguru.electrical.viewmodels

import com.google.common.truth.Truth.assertThat
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.data.db.TradeGuruDatabase
import com.tradeguru.electrical.data.db.dao.ChatMessageDao
import com.tradeguru.electrical.data.db.dao.ContentBlockDao
import com.tradeguru.electrical.data.db.dao.ConversationDao
import com.tradeguru.electrical.data.db.dao.MessageAttachmentDao
import com.tradeguru.electrical.data.db.dao.PartsItemDao
import com.tradeguru.electrical.data.db.entities.ConversationEntity
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

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationManagerCrudTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: TradeGuruDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var contentBlockDao: ContentBlockDao
    private lateinit var partsItemDao: PartsItemDao
    private lateinit var messageAttachmentDao: MessageAttachmentDao
    private lateinit var manager: ConversationManager

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

    // --- deleteConversation ---

    @Test
    fun `deleteConversation removes active conversation when it matches`() = runTest {
        val conv = DomainMappers.Conversation(
            id = "conv-1", title = "Test", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(conv)
        assertThat(manager.activeConversation.value).isNotNull()

        manager.deleteConversation("conv-1")

        coVerify { conversationDao.deleteById("conv-1") }
        assertThat(manager.activeConversation.value).isNull()
    }

    @Test
    fun `deleteConversation does not clear active when different conversation deleted`() = runTest {
        val active = DomainMappers.Conversation(
            id = "conv-active", title = "Active", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(active)

        manager.deleteConversation("conv-other")

        coVerify { conversationDao.deleteById("conv-other") }
        assertThat(manager.activeConversation.value).isNotNull()
        assertThat(manager.activeConversation.value!!.id).isEqualTo("conv-active")
    }

    @Test
    fun `deleteConversation with no active conversation does not crash`() = runTest {
        assertThat(manager.activeConversation.value).isNull()

        manager.deleteConversation("conv-1")

        coVerify { conversationDao.deleteById("conv-1") }
        assertThat(manager.activeConversation.value).isNull()
    }

    // --- clearAllConversations ---

    @Test
    fun `clearAllConversations nulls activeConversation`() = runTest {
        val conv = DomainMappers.Conversation(
            id = "conv-1", title = "Test", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(conv)

        manager.clearAllConversations()

        coVerify { conversationDao.deleteAll() }
        assertThat(manager.activeConversation.value).isNull()
    }

    @Test
    fun `clearAllConversations works when no active conversation`() = runTest {
        manager.clearAllConversations()

        coVerify { conversationDao.deleteAll() }
        assertThat(manager.activeConversation.value).isNull()
    }

    // --- updateConversationTitle ---

    @Test
    fun `updateConversationTitle updates active conversation title in StateFlow`() = runTest {
        val entity = ConversationEntity(
            id = "conv-1", title = "Old Title", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 1000L
        )
        coEvery { conversationDao.getById("conv-1") } returns entity

        val conv = DomainMappers.Conversation(
            id = "conv-1", title = "Old Title", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(conv)

        manager.updateConversationTitle("conv-1", "New Title")

        assertThat(manager.activeConversation.value!!.title).isEqualTo("New Title")
        val entitySlot = slot<ConversationEntity>()
        coVerify { conversationDao.update(capture(entitySlot)) }
        assertThat(entitySlot.captured.title).isEqualTo("New Title")
    }

    @Test
    fun `updateConversationTitle for non-active conversation updates DB only`() = runTest {
        val entity = ConversationEntity(
            id = "conv-other", title = "Old", mode = "fault_finder",
            createdAt = 1000L, updatedAt = 1000L
        )
        coEvery { conversationDao.getById("conv-other") } returns entity

        val active = DomainMappers.Conversation(
            id = "conv-active", title = "Active", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(active)

        manager.updateConversationTitle("conv-other", "Renamed")

        assertThat(manager.activeConversation.value!!.title).isEqualTo("Active")
        coVerify { conversationDao.update(any()) }
    }

    @Test
    fun `updateConversationTitle for nonexistent conversation is a no-op`() = runTest {
        coEvery { conversationDao.getById("ghost") } returns null

        manager.updateConversationTitle("ghost", "Title")

        coVerify(exactly = 0) { conversationDao.update(any()) }
    }

    // --- searchConversations ---

    @Test
    fun `searchConversations filters by title case-insensitive`() {
        val conversations = listOf(
            DomainMappers.Conversation(
                id = "1", title = "Fix my Outlet", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            ),
            DomainMappers.Conversation(
                id = "2", title = "Breaker Panel Tripping", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            ),
            DomainMappers.Conversation(
                id = "3", title = "outlet wiring diagram", mode = ThinkingMode.LEARN,
                createdAt = 1000L, updatedAt = 1000L
            )
        )

        val results = manager.searchConversations(conversations, "outlet")

        assertThat(results).hasSize(2)
        assertThat(results.map { it.id }).containsExactly("1", "3")
    }

    @Test
    fun `searchConversations with empty query returns all`() {
        val conversations = listOf(
            DomainMappers.Conversation(
                id = "1", title = "Test", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )

        val results = manager.searchConversations(conversations, "")

        assertThat(results).hasSize(1)
    }

    @Test
    fun `searchConversations with no matches returns empty`() {
        val conversations = listOf(
            DomainMappers.Conversation(
                id = "1", title = "Wiring help", mode = ThinkingMode.FAULT_FINDER,
                createdAt = 1000L, updatedAt = 1000L
            )
        )

        val results = manager.searchConversations(conversations, "plumbing")

        assertThat(results).isEmpty()
    }

    // --- newConversation ---

    @Test
    fun `newConversation creates and sets active conversation`() = runTest {
        manager.newConversation(ThinkingMode.LEARN)

        val active = manager.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.title).isEqualTo("New Conversation")
        coVerify { conversationDao.insert(any()) }
    }

    @Test
    fun `newConversation replaces existing active conversation`() = runTest {
        val old = DomainMappers.Conversation(
            id = "old", title = "Old", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(old)

        manager.newConversation(ThinkingMode.FAULT_FINDER)

        val active = manager.activeConversation.value
        assertThat(active).isNotNull()
        assertThat(active!!.id).isNotEqualTo("old")
    }

    // --- ensureConversation ---

    @Test
    fun `ensureConversation returns existing active conversation`() = runTest {
        val existing = DomainMappers.Conversation(
            id = "existing", title = "Existing", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        manager.selectConversation(existing)

        val result = manager.ensureConversation("New text", ThinkingMode.FAULT_FINDER)

        assertThat(result.id).isEqualTo("existing")
        coVerify(exactly = 0) { conversationDao.insert(any()) }
    }

    @Test
    fun `ensureConversation creates new when no active conversation`() = runTest {
        assertThat(manager.activeConversation.value).isNull()

        val result = manager.ensureConversation("Hello world", ThinkingMode.LEARN)

        assertThat(result).isNotNull()
        assertThat(result.title).isEqualTo("Hello world")
        coVerify { conversationDao.insert(any()) }
        assertThat(manager.activeConversation.value).isNotNull()
    }

    @Test
    fun `ensureConversation truncates long titles to 40 chars`() = runTest {
        val longText = "A".repeat(100)

        val result = manager.ensureConversation(longText, ThinkingMode.FAULT_FINDER)

        assertThat(result.title).hasLength(40)
    }

    // --- selectConversation ---

    @Test
    fun `selectConversation sets activeConversation StateFlow`() {
        val conv = DomainMappers.Conversation(
            id = "conv-1", title = "Test", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )

        manager.selectConversation(conv)

        assertThat(manager.activeConversation.value).isEqualTo(conv)
    }

    @Test
    fun `selectConversation overwrites previous active`() {
        val conv1 = DomainMappers.Conversation(
            id = "conv-1", title = "First", mode = ThinkingMode.FAULT_FINDER,
            createdAt = 1000L, updatedAt = 1000L
        )
        val conv2 = DomainMappers.Conversation(
            id = "conv-2", title = "Second", mode = ThinkingMode.LEARN,
            createdAt = 2000L, updatedAt = 2000L
        )

        manager.selectConversation(conv1)
        manager.selectConversation(conv2)

        assertThat(manager.activeConversation.value!!.id).isEqualTo("conv-2")
    }
}
