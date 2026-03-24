package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

private enum class CalloutStyle(
    val icon: ImageVector,
    val colorAccessor: (com.tradeguru.electrical.ui.theme.TradeGuruColors) -> Color
) {
    TIP(Icons.Default.Lightbulb, { it.tradeGreen }),
    INFO(Icons.Default.Info, { it.modeLearn }),
    IMPORTANT(Icons.Default.Warning, { it.modeFaultFinder });

    companion object {
        fun fromString(style: String?): CalloutStyle = when (style) {
            "tip" -> TIP
            "important" -> IMPORTANT
            else -> INFO
        }
    }
}

@Composable
fun CalloutView(content: String, style: String?) {
    val colors = LocalTradeGuruColors.current
    val resolved = CalloutStyle.fromString(style)
    val tintColor = resolved.colorAccessor(colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(tintColor)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = resolved.icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = content,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = colors.tradeText
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
