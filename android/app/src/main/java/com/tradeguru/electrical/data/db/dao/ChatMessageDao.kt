package com.tradeguru.electrical.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Delete
    suspend fun delete(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getByConversationId(conversationId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getByConversationIdSync(conversationId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ChatMessageEntity?

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun getCountForConversation(conversationId: String): Int
}
