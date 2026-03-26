package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun SourcesSection(sources: List<Pair<String, String>>) {
    val colors = LocalTradeGuruColors.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Sources",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        sources.forEach { (title, url) ->
            if (url.isNotBlank()) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = colors.tradeGreen,
                    textDecoration = TextDecoration.Underline
                )
            } else {
                Text(text = title, fontSize = 12.sp, color = colors.tradeTextSecondary)
            }
        }
    }
}
