package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.ResearchInstruction
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ResearchInstructionCard(instruction: ResearchInstruction) {
    val colors = LocalTradeGuruColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .padding(12.dp)
    ) {
        Text(
            text = "Step ${instruction.stepNumber}: ${instruction.title}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        instruction.details.forEach { detail ->
            Text(text = "• $detail", fontSize = 12.sp, color = colors.tradeText)
        }
        instruction.warning?.let {
            Text(text = "Warning: $it", fontSize = 12.sp, color = Color(0xFFF97316))
        }
    }
}
