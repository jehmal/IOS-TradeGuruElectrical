package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.StepTable
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun StepTableView(table: StepTable) {
    val colors = LocalTradeGuruColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .horizontalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            table.headers.forEach { header ->
                Text(
                    text = header,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.tradeText,
                    modifier = Modifier.widthIn(min = 80.dp).padding(end = 8.dp)
                )
            }
        }
        HorizontalDivider(color = colors.tradeTextSecondary.copy(alpha = 0.3f))
        table.rows.forEach { row ->
            Row(modifier = Modifier.padding(8.dp)) {
                table.headers.forEach { header ->
                    Text(
                        text = row[header] ?: "",
                        fontSize = 11.sp,
                        color = colors.tradeTextSecondary,
                        modifier = Modifier.widthIn(min = 80.dp).padding(end = 8.dp)
                    )
                }
            }
        }
    }
}
