package com.tradeguru.electrical.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_blocks",
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
data class ContentBlockEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val content: String?,
    val title: String?,
    val steps: String?,
    val language: String?,
    val code: String?,
    val clause: String?,
    val summary: String?,
    val url: String?,
    val rows: String?,
    val headers: String?,
    val level: Int?,
    val style: String?,
    val messageId: String
)
