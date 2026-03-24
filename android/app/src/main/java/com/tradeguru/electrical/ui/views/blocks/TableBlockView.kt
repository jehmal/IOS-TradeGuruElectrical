package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun TableBlockView(headers: List<String>?, rows: List<List<String>>) {
    val colors = LocalTradeGuruColors.current
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.tradeBorder, shape)
            .horizontalScroll(rememberScrollState())
    ) {
        if (headers != null) {
            Row(
                modifier = Modifier.background(colors.tradeSurface)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.tradeText
                        ),
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.tradeBorder)
            )
        }

        rows.forEachIndexed { rowIndex, row ->
            Row {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = colors.tradeText
                        ),
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (rowIndex < rows.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(colors.tradeBorder)
                )
            }
        }
    }
}
