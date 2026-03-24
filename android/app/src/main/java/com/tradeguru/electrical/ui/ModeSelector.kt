package com.tradeguru.electrical.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ModeSelector(
    selectedMode: ThinkingMode,
    onModeSelected: (ThinkingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        ThinkingMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val modeColor = modeColor(mode)
            val shape = RoundedCornerShape(10.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (isSelected) modeColor.copy(alpha = 0.12f) else colors.tradeInput)
                    .border(1.dp, if (isSelected) modeColor else colors.tradeBorder, shape)
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .semantics { contentDescription = "Select ${mode.displayName} mode" }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = mode.icon),
                        contentDescription = null,
                        tint = if (isSelected) modeColor else colors.tradeText,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = mode.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) modeColor else colors.tradeText
                    )
                }
                Text(
                    text = mode.shortDescription,
                    fontSize = 10.sp,
                    color = if (isSelected) modeColor else colors.tradeText
                )
            }
        }
    }
}

@Composable
fun modeColor(mode: ThinkingMode): androidx.compose.ui.graphics.Color {
    val colors = LocalTradeGuruColors.current
    return when (mode) {
        ThinkingMode.FAULT_FINDER -> colors.modeFaultFinder
        ThinkingMode.LEARN -> colors.modeLearn
        ThinkingMode.RESEARCH -> colors.modeResearch
    }
}
