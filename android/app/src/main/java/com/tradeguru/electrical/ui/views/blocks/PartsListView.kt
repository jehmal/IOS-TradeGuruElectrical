package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.data.PartsItem
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun PartsListView(items: List<PartsItem>) {
    val colors = LocalTradeGuruColors.current
    val shape = RoundedCornerShape(12.dp)

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.tradeBorder, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.tradeSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Item",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.tradeText
                ),
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Specification",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.tradeText
                ),
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Qty",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.tradeText,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.width(44.dp)
            )
        }

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) colors.tradeBg else colors.tradeSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = TextStyle(fontSize = 13.sp, color = colors.tradeText),
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = item.spec,
                    style = TextStyle(fontSize = 13.sp, color = colors.tradeText),
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "${item.qty}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = colors.tradeText,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}
