package com.tradeguru.electrical.ui.views.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val colors = LocalTradeGuruColors.current
    val modes = ThinkingMode.entries
    val totalPages = modes.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPages })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.tradeBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < modes.size) {
                TextButton(onClick = onComplete) {
                    Text(
                        text = "Skip",
                        fontSize = 14.sp,
                        color = colors.tradeTextSecondary
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            if (page < modes.size) {
                val mode = modes[page]
                OnboardingPageView(
                    mode = mode,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                OnboardingFinalPageView(
                    onGetStarted = onComplete,
                    onSignIn = onComplete,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        PageIndicator(
            pageCount = totalPages,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isActive) TradeGreen else LocalTradeGuruColors.current.tradeBorder,
                animationSpec = tween(300),
                label = "dot_color"
            )
            Box(
                modifier = Modifier
                    .then(
                        if (isActive) Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                        else Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                    )
                    .background(color)
            )
        }
    }
}
