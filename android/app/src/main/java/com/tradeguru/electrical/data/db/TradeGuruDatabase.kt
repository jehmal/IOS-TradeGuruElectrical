package com.tradeguru.electrical.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.tradeguru.electrical.data.db.converters.Converters
import com.tradeguru.electrical.data.db.dao.ChatMessageDao
import com.tradeguru.electrical.data.db.dao.ContentBlockDao
import com.tradeguru.electrical.data.db.dao.ConversationDao
import com.tradeguru.electrical.data.db.dao.MessageAttachmentDao
import com.tradeguru.electrical.data.db.dao.PartsItemDao
import com.tradeguru.electrical.data.db.entities.ChatMessageEntity
import com.tradeguru.electrical.data.db.entities.ContentBlockEntity
import com.tradeguru.electrical.data.db.entities.ConversationEntity
import com.tradeguru.electrical.data.db.entities.MessageAttachmentEntity
import com.tradeguru.electrical.data.db.entities.PartsItemEntity

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        ContentBlockEntity::class,
        PartsItemEntity::class,
        MessageAttachmentEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TradeGuruDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun contentBlockDao(): ContentBlockDao
    abstract fun partsItemDao(): PartsItemDao
    abstract fun messageAttachmentDao(): MessageAttachmentDao

    companion object {
        private const val DATABASE_NAME = "tradeguru.db"

        @Volatile
        private var instance: TradeGuruDatabase? = null

        fun getInstance(context: Context): TradeGuruDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TradeGuruDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
