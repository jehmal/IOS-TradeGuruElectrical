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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.NextAction
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun NextActionsSection(actions: List<NextAction>) {
    val colors = LocalTradeGuruColors.current
    val sorted = actions.sortedBy { it.priority }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Next Actions",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        sorted.forEach { action ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.tradeSurface)
                    .padding(10.dp)
            ) {
                Text(
                    text = action.action,
                    fontSize = 12.sp,
                    color = colors.tradeText,
                    modifier = Modifier.weight(1f)
                )
                if (action.required) {
                    Text(
                        text = "REQUIRED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDC2626))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
