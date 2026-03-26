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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.Equipment
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun EquipmentCard(equipment: Equipment) {
    val colors = LocalTradeGuruColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tradeSurface)
            .padding(12.dp)
    ) {
        Text(
            text = equipment.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.tradeText
        )
        equipment.manufacturer?.let {
            Text(text = "Manufacturer: $it", fontSize = 12.sp, color = colors.tradeTextSecondary)
        }
        equipment.modelNumber?.let {
            Text(text = "Model: $it", fontSize = 12.sp, color = colors.tradeTextSecondary)
        }
        equipment.category?.let {
            Text(text = "Category: $it", fontSize = 12.sp, color = colors.tradeTextSecondary)
        }
    }
}
