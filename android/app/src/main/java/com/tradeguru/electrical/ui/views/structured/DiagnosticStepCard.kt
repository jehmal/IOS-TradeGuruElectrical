package com.tradeguru.electrical.ui.views.structured

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.DiagnosticStep
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun DiagnosticStepCard(step: DiagnosticStep) {
    val colors = LocalTradeGuruColors.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Step ${step.stepNumber}: ${step.title}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.tradeText,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.tradeTextSecondary
            )
        }
        if (expanded) {
            DiagnosticStepDetails(step = step, colors = colors)
        }
    }
}

@Composable
private fun DiagnosticStepDetails(
    step: DiagnosticStep,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        step.questions?.forEach {
            Text(text = "? $it", fontSize = 12.sp, color = colors.tradeText)
        }
        step.instructions?.forEach {
            Text(text = "• $it", fontSize = 12.sp, color = colors.tradeText)
        }
        step.visualChecks?.forEach {
            Text(text = "👁 $it", fontSize = 12.sp, color = colors.tradeText)
        }
        step.instrumentHowto?.let { InstrumentHowToSection(howto = it) }
        step.measurements?.forEach { m ->
            MeasurementRow(measurement = m, colors = colors)
        }
        step.table?.let { StepTableView(table = it) }
        step.notes?.forEach {
            Text(text = "Note: $it", fontSize = 12.sp, color = colors.tradeTextSecondary)
        }
    }
}

@Composable
private fun MeasurementRow(
    measurement: com.tradeguru.electrical.models.Measurement,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = measurement.test,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.tradeText
        )
        Text(text = measurement.procedure, fontSize = 11.sp, color = colors.tradeTextSecondary)
        Text(
            text = "Expected: ${measurement.expectedResult}",
            fontSize = 11.sp,
            color = colors.tradeGreen
        )
        Text(
            text = "If fail: ${measurement.ifFail}",
            fontSize = 11.sp,
            color = Color(0xFFDC2626)
        )
    }
}
