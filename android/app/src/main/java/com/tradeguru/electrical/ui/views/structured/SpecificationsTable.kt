package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.Specification
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun SpecificationsTable(specs: List<Specification>) {
    val colors = LocalTradeGuruColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .padding(12.dp)
    ) {
        Text(
            text = "Specifications",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        specs.forEachIndexed { index, spec ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(text = spec.label, fontSize = 12.sp, color = colors.tradeTextSecondary)
                Text(text = spec.value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.tradeText)
            }
            spec.notes?.let {
                Text(text = it, fontSize = 11.sp, color = colors.tradeTextSecondary)
            }
            if (index < specs.lastIndex) {
                HorizontalDivider(color = colors.tradeTextSecondary.copy(alpha = 0.2f))
            }
        }
    }
}
