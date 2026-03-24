package com.tradeguru.electrical.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parts_items",
    foreignKeys = [
        ForeignKey(
            entity = ContentBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentBlockId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contentBlockId"])]
)
data class PartsItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val spec: String,
    val qty: Int,
    val contentBlockId: String
)
