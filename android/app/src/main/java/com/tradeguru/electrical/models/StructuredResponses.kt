package com.tradeguru.electrical.models

import com.google.gson.annotations.SerializedName

sealed class StructuredResponse {
    abstract val summary: String
}

data class FaultFindingResponse(
    override val summary: String,
    val principle: String? = null,
    val safety: SafetyInfo,
    @SerializedName("diagnostic_steps") val diagnosticSteps: List<DiagnosticStep>,
    @SerializedName("branch_logic") val branchLogic: List<BranchLogic>? = null,
    @SerializedName("common_mistakes") val commonMistakes: List<String>? = null,
    @SerializedName("pro_insights") val proInsights: List<String>? = null,
    val examples: List<Example>? = null,
    @SerializedName("next_actions") val nextActions: List<NextAction>,
    val references: List<String>? = null,
    @SerializedName("additional_info") val additionalInfo: String? = null
) : StructuredResponse()

data class QuestionResponse(
    val intent: String,
    override val summary: String,
    val explanation: Explanation,
    @SerializedName("safety_note") val safetyNote: String? = null,
    @SerializedName("pro_insights") val proInsights: List<String>? = null,
    @SerializedName("common_mistakes") val commonMistakes: List<String>? = null,
    val sources: List<Source>? = null,
    @SerializedName("related_topics") val relatedTopics: List<String>? = null,
    @SerializedName("next_actions") val nextActions: List<NextAction>? = null
) : StructuredResponse()

data class ResearchStructuredResponse(
    val intent: String,
    override val summary: String,
    val equipment: Equipment? = null,
    val specifications: List<Specification>? = null,
    val instructions: List<ResearchInstruction>? = null,
    @SerializedName("safety_warnings") val safetyWarnings: List<String>? = null,
    @SerializedName("key_findings") val keyFindings: List<KeyFinding>? = null,
    val sources: List<Source>? = null,
    val tips: List<String>? = null,
    @SerializedName("next_steps") val nextSteps: List<String>? = null,
    @SerializedName("confidence_note") val confidenceNote: String? = null
) : StructuredResponse()

data class SafetyInfo(
    val priority: String,
    val steps: List<SafetyStep>,
    val warnings: List<String>? = null
)

data class SafetyStep(val action: String, val details: String? = null)

data class DiagnosticStep(
    @SerializedName("step_number") val stepNumber: Int,
    val title: String,
    val type: String,
    val questions: List<String>? = null,
    val instructions: List<String>? = null,
    @SerializedName("visual_checks") val visualChecks: List<String>? = null,
    @SerializedName("instrument_howto") val instrumentHowto: InstrumentHowTo? = null,
    val measurements: List<Measurement>? = null,
    val table: StepTable? = null,
    val notes: List<String>? = null
)

data class InstrumentHowTo(
    @SerializedName("meter_mode") val meterMode: String? = null,
    @SerializedName("leads_jacks") val leadsJacks: String? = null,
    @SerializedName("probe_placement") val probePlacement: String? = null,
    @SerializedName("hold_time") val holdTime: String? = null,
    val donts: List<String>? = null,
    @SerializedName("common_errors") val commonErrors: List<String>? = null,
    @SerializedName("ascii_diagram") val asciiDiagram: String? = null
)

data class Measurement(
    val test: String,
    val procedure: String,
    @SerializedName("expected_result") val expectedResult: String,
    @SerializedName("if_fail") val ifFail: String
)

data class StepTable(
    val headers: List<String>,
    val rows: List<Map<String, String>>
)

data class BranchLogic(
    val condition: String,
    @SerializedName("if_true") val ifTrue: String,
    @SerializedName("if_false") val ifFalse: String
)

data class NextAction(
    val priority: Int,
    val action: String,
    val required: Boolean
)

data class Example(val scenario: String, val solution: String)

data class Explanation(val principle: String, val details: List<String>)

data class Equipment(
    val name: String,
    val manufacturer: String? = null,
    @SerializedName("model_number") val modelNumber: String? = null,
    val category: String? = null
)

data class Specification(val label: String, val value: String, val notes: String? = null)

data class ResearchInstruction(
    @SerializedName("step_number") val stepNumber: Int,
    val title: String,
    val details: List<String>,
    val warning: String? = null
)

data class KeyFinding(val title: String, val details: String)

data class Source(val title: String, val url: String)
