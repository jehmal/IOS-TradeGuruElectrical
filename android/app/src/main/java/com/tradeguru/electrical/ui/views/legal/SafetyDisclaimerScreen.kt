package com.tradeguru.electrical.ui.views.legal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen
import kotlinx.coroutines.delay

private val CriticalRed = Color(0xFFD32F2F)

@Composable
fun SafetyDisclaimerScreen(onAccept: () -> Unit) {
    val colors = LocalTradeGuruColors.current
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value to scrollState.maxValue }.collect { (value, max) ->
            if (max > 0 && value >= max - 50) { delay(500); hasScrolledToBottom = true }
        }
    }

    val buttonColor by animateColorAsState(
        if (hasScrolledToBottom) TradeGreen else colors.tradeTextSecondary, tween(300), label = "btn"
    )

    Column(modifier = Modifier.fillMaxSize().background(colors.tradeBg)) {
        DisclaimerHeader(colors)
        DisclaimerBody(Modifier.weight(1f), scrollState, colors)
        DisclaimerFooter(hasScrolledToBottom, buttonColor, colors, onAccept)
    }
}

@Composable
private fun DisclaimerHeader(colors: com.tradeguru.electrical.ui.theme.TradeGuruColors) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Warning, null,
            tint = colors.modeFaultFinder,
            modifier = Modifier.padding(top = 32.dp).size(60.dp)
        )
        Text(
            "Important Safety Notice",
            fontSize = 34.sp, fontWeight = FontWeight.Bold,
            color = colors.tradeText, letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            "Please read carefully before using TradeGuru",
            fontSize = 14.sp, color = colors.tradeTextSecondary
        )
    }
}

@Composable
private fun DisclaimerBody(
    modifier: Modifier,
    scrollState: androidx.compose.foundation.ScrollState,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(
        modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        eulaSections.forEachIndexed { index, section ->
            if (index == 3 || index == 6 || index == 11 || index == 16) {
                Spacer(
                    Modifier.fillMaxWidth().height(1.dp).background(colors.tradeBorder)
                )
            }
            DisclaimerSectionItem(section, colors)
        }

        AcknowledgementSection(colors)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DisclaimerSectionItem(
    section: EulaSection,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    val badgeColor = if (section.isCritical) CriticalRed else colors.modeFaultFinder
    val wrapMod = if (section.isCritical) {
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CriticalRed.copy(alpha = 0.08f))
            .padding(16.dp)
    } else {
        Modifier
    }

    Column(modifier = wrapMod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                section.number,
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(badgeColor)
            )
            Text(
                section.title,
                fontSize = if (section.isCritical) 19.sp else 17.sp,
                fontWeight = if (section.isCritical) FontWeight.Bold else FontWeight.SemiBold,
                color = if (section.isCritical) CriticalRed else colors.tradeText
            )
        }
        Text(
            section.body,
            fontSize = 15.sp, color = colors.tradeTextSecondary, lineHeight = 22.sp,
            modifier = Modifier.padding(start = 34.dp)
        )
    }
}

@Composable
private fun AcknowledgementSection(
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TradeGreen.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "By tapping \"I Understand and Agree\" below, you acknowledge:",
            fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        acknowledgementItems.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Check, null,
                    tint = TradeGreen,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Text(item, fontSize = 14.sp, color = colors.tradeTextSecondary, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun DisclaimerFooter(
    hasScrolledToBottom: Boolean,
    buttonColor: Color,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors,
    onAccept: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.tradeBorder))
        if (!hasScrolledToBottom) {
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowDown, null,
                    tint = colors.tradeTextSecondary, modifier = Modifier.size(12.dp)
                )
                Text("Scroll to read all terms", fontSize = 13.sp, color = colors.tradeTextSecondary)
            }
        }
        Button(
            onClick = onAccept,
            enabled = hasScrolledToBottom,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = colors.tradeTextSecondary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .height(56.dp)
        ) {
            Text(
                "I Understand and Agree",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }
        Text(
            "By continuing, you confirm you are a licensed electrical professional",
            fontSize = 12.sp, color = colors.tradeTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
        )
    }
}
