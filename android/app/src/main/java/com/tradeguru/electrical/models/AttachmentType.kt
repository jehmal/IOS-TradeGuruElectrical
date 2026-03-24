package com.tradeguru.electrical.models

enum class AttachmentType(val value: String) {
    IMAGE("image"),
    VIDEO("video"),
    DOCUMENT("document");

    companion object {
        fun fromValue(value: String): AttachmentType? =
            entries.find { it.value == value }
    }
}
