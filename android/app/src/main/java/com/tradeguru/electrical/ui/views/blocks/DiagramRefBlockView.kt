package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun DiagramRefBlockView(content: String?) {
    val colors = LocalTradeGuruColors.current

    Text(
        text = "Diagram: ${content ?: "reference"}",
        style = TextStyle(
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            color = colors.tradeTextSecondary
        )
    )
}
