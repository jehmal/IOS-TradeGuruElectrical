package com.tradeguru.electrical.ui.views.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tradeguru.electrical.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen

@Composable
fun OnboardingFinalPageView(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(TradeGreen),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tg_logo),
                contentDescription = "Trade Guru Logo",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = "Ready to start?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.tradeText,
            modifier = Modifier.padding(top = 32.dp)
        )

        Text(
            text = "Your AI-powered electrical assistant. Ask anything about wiring, faults, standards, or theory.",
            fontSize = 16.sp,
            color = colors.tradeTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 40.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGetStarted,
            colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        TextButton(
            onClick = onSignIn,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Already have an account? Sign In",
                fontSize = 14.sp,
                color = colors.tradeTextSecondary
            )
        }
    }
}
