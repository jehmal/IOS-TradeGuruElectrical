package com.tradeguru.electrical.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradeguru.electrical.data.db.entities.PartsItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartsItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PartsItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PartsItemEntity>)

    @Update
    suspend fun update(item: PartsItemEntity)

    @Delete
    suspend fun delete(item: PartsItemEntity)

    @Query("DELETE FROM parts_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM parts_items WHERE contentBlockId = :contentBlockId")
    fun getByContentBlockId(contentBlockId: String): Flow<List<PartsItemEntity>>

    @Query("SELECT * FROM parts_items WHERE contentBlockId = :contentBlockId")
    suspend fun getByContentBlockIdSync(contentBlockId: String): List<PartsItemEntity>

    @Query("SELECT * FROM parts_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PartsItemEntity?
}
