package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

enum class InfoListVariant { MISTAKES, INSIGHTS, WARNINGS }

@Composable
fun InfoListSection(items: List<String>, variant: InfoListVariant) {
    val colors = LocalTradeGuruColors.current
    val (title, accent) = when (variant) {
        InfoListVariant.MISTAKES -> "Common Mistakes" to Color(0xFFDC2626)
        InfoListVariant.INSIGHTS -> "Pro Insights" to colors.tradeGreen
        InfoListVariant.WARNINGS -> "Safety Warnings" to Color(0xFFF97316)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = accent)
        items.forEach { item ->
            Text(text = "• $item", fontSize = 12.sp, color = colors.tradeText)
        }
    }
}
