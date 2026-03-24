package com.tradeguru.electrical.models

enum class ContentBlockType(val value: String) {
    TEXT("text"),
    HEADING("heading"),
    STEP_LIST("step_list"),
    WARNING("warning"),
    CODE("code"),
    PARTS_LIST("parts_list"),
    REGULATION("regulation"),
    DIAGRAM_REF("diagram_ref"),
    TOOL_CALL("tool_call"),
    TABLE("table"),
    CALLOUT("callout"),
    LINK("link");

    companion object {
        fun fromValue(value: String): ContentBlockType? =
            entries.find { it.value == value }
    }
}
