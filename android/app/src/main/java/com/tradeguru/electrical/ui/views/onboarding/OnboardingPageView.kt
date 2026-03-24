package com.tradeguru.electrical.ui.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun OnboardingPageView(
    mode: ThinkingMode,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current
    val modeColor = modeColor(mode)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = mode.icon),
            contentDescription = null,
            tint = modeColor,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(modeColor.copy(alpha = 0.12f))
                .padding(20.dp)
        )

        Text(
            text = mode.displayName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.tradeText,
            modifier = Modifier.padding(top = 32.dp)
        )

        Text(
            text = mode.fullDescription,
            fontSize = 16.sp,
            color = colors.tradeTextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 4,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 40.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun modeColor(mode: ThinkingMode): androidx.compose.ui.graphics.Color {
    val colors = LocalTradeGuruColors.current
    return when (mode) {
        ThinkingMode.FAULT_FINDER -> colors.modeFaultFinder
        ThinkingMode.LEARN -> colors.modeLearn
        ThinkingMode.RESEARCH -> colors.modeResearch
    }
}
