package com.tradeguru.electrical.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tradeguru.electrical.models.AttachmentType
import com.tradeguru.electrical.models.ContentBlockType
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.ThinkingMode
import java.util.Date
import java.util.UUID

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromNestedStringList(value: List<List<String>>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toNestedStringList(value: String?): List<List<String>>? {
        if (value == null) return null
        val type = object : TypeToken<List<List<String>>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromThinkingMode(mode: ThinkingMode): String = mode.value

    @TypeConverter
    fun toThinkingMode(value: String): ThinkingMode =
        ThinkingMode.fromValue(value) ?: ThinkingMode.FAULT_FINDER

    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.value

    @TypeConverter
    fun toMessageRole(value: String): MessageRole =
        MessageRole.fromValue(value) ?: MessageRole.USER

    @TypeConverter
    fun fromContentBlockType(type: ContentBlockType): String = type.value

    @TypeConverter
    fun toContentBlockType(value: String): ContentBlockType =
        ContentBlockType.fromValue(value) ?: ContentBlockType.TEXT

    @TypeConverter
    fun fromAttachmentType(type: AttachmentType): String = type.value

    @TypeConverter
    fun toAttachmentType(value: String): AttachmentType =
        AttachmentType.fromValue(value) ?: AttachmentType.IMAGE
}
