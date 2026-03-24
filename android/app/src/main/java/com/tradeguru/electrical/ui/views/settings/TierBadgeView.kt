package com.tradeguru.electrical.ui.views.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.UserTier
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen

@Composable
fun TierBadgeView(tier: UserTier) {
    val colors = LocalTradeGuruColors.current
    val tierColor = tierColor(tier, colors)

    Text(
        text = tier.displayName,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = tierColor,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tierColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun tierColor(
    tier: UserTier,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
): Color {
    return when (tier) {
        UserTier.FREE -> colors.tradeTextSecondary
        UserTier.PRO -> colors.modeLearn
        UserTier.UNLIMITED -> TradeGreen
    }
}
