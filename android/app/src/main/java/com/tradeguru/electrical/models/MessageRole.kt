package com.tradeguru.electrical.models

enum class MessageRole(val value: String) {
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun fromValue(value: String): MessageRole? =
            entries.find { it.value == value }
    }
}
