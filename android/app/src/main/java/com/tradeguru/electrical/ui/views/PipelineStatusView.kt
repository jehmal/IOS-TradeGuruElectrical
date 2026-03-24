package com.tradeguru.electrical.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun PipelineStatusView(
    stage: PipelineStage,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current
    val isVisible = stage != PipelineStage.IDLE && stage != PipelineStage.ERROR

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f),
        modifier = modifier
    ) {
        val icon = stageIcon(stage)
        val text = stageText(stage)

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colors.tradeSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics { contentDescription = text }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.tradeTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = colors.tradeTextSecondary
            )
            PipelineStatusDots()
        }
    }
}

private fun stageIcon(stage: PipelineStage): ImageVector = when (stage) {
    PipelineStage.SEARCHING -> Icons.Default.Search
    PipelineStage.SYNTHESIZING -> Icons.Default.Psychology
    PipelineStage.STREAMING -> Icons.Default.ChatBubbleOutline
    else -> Icons.Default.Search
}

private fun stageText(stage: PipelineStage): String = when (stage) {
    PipelineStage.SEARCHING -> "Searching knowledge base..."
    PipelineStage.SYNTHESIZING -> "Synthesizing response..."
    PipelineStage.STREAMING -> "Streaming..."
    else -> ""
}
