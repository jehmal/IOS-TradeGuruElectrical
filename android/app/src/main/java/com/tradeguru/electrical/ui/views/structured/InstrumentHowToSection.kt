package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.InstrumentHowTo
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun InstrumentHowToSection(howto: InstrumentHowTo) {
    val colors = LocalTradeGuruColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeGreen.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Text(
            text = "Instrument How-To",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeGreen
        )
        howto.meterMode?.let {
            Text(text = "Mode: $it", fontSize = 12.sp, color = colors.tradeText)
        }
        howto.leadsJacks?.let {
            Text(text = "Leads/Jacks: $it", fontSize = 12.sp, color = colors.tradeText)
        }
        howto.probePlacement?.let {
            Text(text = "Probe Placement: $it", fontSize = 12.sp, color = colors.tradeText)
        }
        howto.holdTime?.let {
            Text(text = "Hold Time: $it", fontSize = 12.sp, color = colors.tradeText)
        }
        howto.donts?.forEach {
            Text(text = "DON'T: $it", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFFDC2626))
        }
        howto.commonErrors?.forEach {
            Text(text = "Error: $it", fontSize = 12.sp, color = colors.tradeTextSecondary)
        }
        howto.asciiDiagram?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.tradeText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.tradeSurface)
                    .padding(8.dp)
            )
        }
    }
}
