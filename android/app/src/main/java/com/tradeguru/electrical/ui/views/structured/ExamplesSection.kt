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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.Example
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ExamplesSection(examples: List<Example>) {
    val colors = LocalTradeGuruColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Examples",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        examples.forEach { example ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.tradeSurface)
                    .padding(12.dp)
            ) {
                Text(
                    text = example.scenario,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.tradeText
                )
                Text(
                    text = example.solution,
                    fontSize = 12.sp,
                    color = colors.tradeTextSecondary
                )
            }
        }
    }
}
