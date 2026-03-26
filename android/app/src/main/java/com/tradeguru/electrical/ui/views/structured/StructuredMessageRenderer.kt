package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.FaultFindingResponse
import com.tradeguru.electrical.models.QuestionResponse
import com.tradeguru.electrical.models.ResearchStructuredResponse
import com.tradeguru.electrical.models.StructuredResponse
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun StructuredMessageRenderer(response: StructuredResponse) {
    val colors = LocalTradeGuruColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = response.summary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        when (response) {
            is FaultFindingResponse -> FaultFindingContent(response)
            is QuestionResponse -> QuestionContent(response)
            is ResearchStructuredResponse -> ResearchContent(response)
            else -> {}
        }
    }
}

@Composable
private fun FaultFindingContent(response: FaultFindingResponse) {
    val colors = LocalTradeGuruColors.current
    response.principle?.let {
        Text(text = it, fontSize = 13.sp, color = colors.tradeTextSecondary)
    }
    SafetyGateSection(safety = response.safety)
    response.diagnosticSteps.forEach { step ->
        DiagnosticStepCard(step = step)
    }
    response.branchLogic?.let { BranchLogicSection(branches = it) }
    response.examples?.let { ExamplesSection(examples = it) }
    response.commonMistakes?.let { InfoListSection(items = it, variant = InfoListVariant.MISTAKES) }
    response.proInsights?.let { InfoListSection(items = it, variant = InfoListVariant.INSIGHTS) }
    NextActionsSection(actions = response.nextActions)
    response.references?.let { SourcesSection(sources = it.map { ref -> ref to "" }) }
    response.additionalInfo?.let {
        Text(text = it, fontSize = 12.sp, color = colors.tradeTextSecondary)
    }
}

@Composable
private fun QuestionContent(response: QuestionResponse) {
    ExplanationSection(explanation = response.explanation)
    response.safetyNote?.let { SafetyNoteCard(note = it) }
    response.commonMistakes?.let { InfoListSection(items = it, variant = InfoListVariant.MISTAKES) }
    response.proInsights?.let { InfoListSection(items = it, variant = InfoListVariant.INSIGHTS) }
    response.sources?.let { SourcesSection(sources = it.map { s -> s.title to s.url }) }
    response.relatedTopics?.let { RelatedTopicsChips(topics = it) }
    response.nextActions?.let { NextActionsSection(actions = it) }
}

@Composable
private fun ResearchContent(response: ResearchStructuredResponse) {
    val colors = LocalTradeGuruColors.current
    response.equipment?.let { EquipmentCard(equipment = it) }
    response.specifications?.let { SpecificationsTable(specs = it) }
    response.keyFindings?.let { KeyFindingsSection(findings = it) }
    response.safetyWarnings?.let { InfoListSection(items = it, variant = InfoListVariant.WARNINGS) }
    response.instructions?.let { it.forEach { inst -> ResearchInstructionCard(instruction = inst) } }
    response.tips?.let { InfoListSection(items = it, variant = InfoListVariant.INSIGHTS) }
    response.sources?.let { SourcesSection(sources = it.map { s -> s.title to s.url }) }
    response.nextSteps?.let { steps ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Next Steps",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.tradeText
            )
            steps.forEach { step ->
                Text(text = "• $step", fontSize = 12.sp, color = colors.tradeText)
            }
        }
    }
    response.confidenceNote?.let { SafetyNoteCard(note = it) }
}
