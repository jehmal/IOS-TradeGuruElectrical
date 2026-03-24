package com.tradeguru.electrical.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageAttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: MessageAttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<MessageAttachmentEntity>)

    @Update
    suspend fun update(attachment: MessageAttachmentEntity)

    @Delete
    suspend fun delete(attachment: MessageAttachmentEntity)

    @Query("DELETE FROM message_attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM message_attachments WHERE messageId = :messageId")
    fun getByMessageId(messageId: String): Flow<List<MessageAttachmentEntity>>

    @Query("SELECT * FROM message_attachments WHERE messageId = :messageId")
    suspend fun getByMessageIdSync(messageId: String): List<MessageAttachmentEntity>

    @Query("SELECT * FROM message_attachments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageAttachmentEntity?
}
