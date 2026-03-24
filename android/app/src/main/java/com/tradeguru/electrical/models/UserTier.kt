package com.tradeguru.electrical.models

enum class UserTier(val value: String, val displayName: String) {
    FREE("free", "Free"),
    PRO("pro", "Pro"),
    UNLIMITED("unlimited", "Unlimited");

    companion object {
        fun fromValue(value: String): UserTier? =
            entries.find { it.value == value }
    }
}
