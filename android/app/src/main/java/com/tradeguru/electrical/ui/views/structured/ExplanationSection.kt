package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.Explanation
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ExplanationSection(explanation: Explanation) {
    val colors = LocalTradeGuruColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = explanation.principle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        explanation.details.forEach { detail ->
            Text(text = "• $detail", fontSize = 13.sp, color = colors.tradeText)
        }
    }
}
