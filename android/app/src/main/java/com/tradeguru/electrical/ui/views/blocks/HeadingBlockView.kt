package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun HeadingBlockView(content: String, level: Int) {
    val colors = LocalTradeGuruColors.current

    val fontSize = when (level) {
        1 -> 20.sp
        2 -> 18.sp
        else -> 16.sp
    }

    Text(
        text = content,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = colors.tradeText
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
