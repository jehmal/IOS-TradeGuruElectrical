package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun TextBlockView(content: String) {
    val colors = LocalTradeGuruColors.current

    Text(
        text = content,
        style = TextStyle(
            fontSize = 15.sp,
            lineHeight = (15 * 1.5).sp,
            color = colors.tradeText
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
