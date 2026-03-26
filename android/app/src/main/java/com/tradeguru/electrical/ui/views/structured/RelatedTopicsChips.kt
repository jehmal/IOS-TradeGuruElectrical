package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelatedTopicsChips(topics: List<String>) {
    val colors = LocalTradeGuruColors.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topics.forEach { topic ->
            Text(
                text = topic,
                fontSize = 12.sp,
                color = colors.tradeGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.tradeGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
