package com.tradeguru.electrical.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_attachments",
    foreignKeys = [
        ForeignKey(
            entity = ChatMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["messageId"])]
)
data class MessageAttachmentEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val fileName: String,
    val fileSize: Int?,
    val thumbnailData: ByteArray?,
    val messageId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageAttachmentEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
