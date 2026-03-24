package com.tradeguru.electrical.models

import androidx.annotation.DrawableRes
import com.tradeguru.electrical.R

enum class ThinkingMode(
    val value: String,
    val displayName: String,
    val shortDescription: String,
    @DrawableRes val icon: Int,
    val fullDescription: String
) {
    FAULT_FINDER(
        value = "fault_finder",
        displayName = "Fault Finder",
        shortDescription = "Get it fixed",
        icon = R.drawable.ic_bolt,
        fullDescription = "Diagnose electrical faults, trace circuits, and identify issues with AI-assisted troubleshooting."
    ),
    LEARN(
        value = "learn",
        displayName = "Learn",
        shortDescription = "Show me how",
        icon = R.drawable.ic_book,
        fullDescription = "Study electrical theory, code compliance, and best practices with interactive AI tutoring."
    ),
    RESEARCH(
        value = "research",
        displayName = "Research",
        shortDescription = "Look it up",
        icon = R.drawable.ic_search,
        fullDescription = "Research products, specifications, regulations, and technical documentation."
    );

    companion object {
        fun fromValue(value: String): ThinkingMode? =
            entries.find { it.value == value }
    }
}
