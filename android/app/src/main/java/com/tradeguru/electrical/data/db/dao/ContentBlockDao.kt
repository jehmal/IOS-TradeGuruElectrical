package com.tradeguru.electrical.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: ContentBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blocks: List<ContentBlockEntity>)

    @Update
    suspend fun update(block: ContentBlockEntity)

    @Delete
    suspend fun delete(block: ContentBlockEntity)

    @Query("DELETE FROM content_blocks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM content_blocks WHERE messageId = :messageId")
    fun getByMessageId(messageId: String): Flow<List<ContentBlockEntity>>

    @Query("SELECT * FROM content_blocks WHERE messageId = :messageId")
    suspend fun getByMessageIdSync(messageId: String): List<ContentBlockEntity>

    @Query("SELECT * FROM content_blocks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ContentBlockEntity?
}
