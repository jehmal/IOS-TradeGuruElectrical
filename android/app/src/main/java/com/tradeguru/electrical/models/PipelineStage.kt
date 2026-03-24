package com.tradeguru.electrical.models

enum class PipelineStage(val value: String) {
    IDLE("idle"),
    SEARCHING("searching"),
    SYNTHESIZING("synthesizing"),
    STREAMING("streaming"),
    ERROR("error");

    companion object {
        fun fromValue(value: String): PipelineStage? =
            entries.find { it.value == value }
    }
}
