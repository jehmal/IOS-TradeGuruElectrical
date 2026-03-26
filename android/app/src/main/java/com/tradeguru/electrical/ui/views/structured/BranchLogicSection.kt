package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.tradeguru.electrical.models.BranchLogic
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun BranchLogicSection(branches: List<BranchLogic>) {
    val colors = LocalTradeGuruColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Decision Points",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        branches.forEach { branch ->
            BranchCard(branch = branch, colors = colors)
        }
    }
}

@Composable
private fun BranchCard(
    branch: BranchLogic,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .padding(12.dp)
    ) {
        Text(
            text = "IF: ${branch.condition}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.tradeText
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "YES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                Text(text = branch.ifTrue, fontSize = 12.sp, color = colors.tradeText)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "NO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Text(text = branch.ifFalse, fontSize = 12.sp, color = colors.tradeText)
            }
        }
    }
}
