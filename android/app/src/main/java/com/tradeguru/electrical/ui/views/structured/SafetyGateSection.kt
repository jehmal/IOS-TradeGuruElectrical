package com.tradeguru.electrical.ui.views.structured

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.SafetyInfo

@Composable
fun SafetyGateSection(safety: SafetyInfo) {
    val priorityColor = when (safety.priority.lowercase()) {
        "critical" -> Color(0xFFDC2626)
        "high" -> Color(0xFFF97316)
        "medium" -> Color(0xFFEAB308)
        else -> Color(0xFFEAB308)
    }
    val bgColor = priorityColor.copy(alpha = 0.1f)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = priorityColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Safety — ${safety.priority.uppercase()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = priorityColor
            )
        }
        safety.steps.forEachIndexed { index, step ->
            Text(
                text = "${index + 1}. ${step.action}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = priorityColor
            )
            step.details?.let {
                Text(text = it, fontSize = 12.sp, color = priorityColor.copy(alpha = 0.8f))
            }
        }
        safety.warnings?.forEach { warning ->
            Text(
                text = "⚠ $warning",
                fontSize = 12.sp,
                color = priorityColor
            )
        }
    }
}
