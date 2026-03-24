package com.tradeguru.electrical.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ModeInfoCard(
    mode: ThinkingMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current
    val mColor = modeColor(mode)
    val shape = RoundedCornerShape(12.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.tradeSurface)
            .border(1.dp, colors.tradeBorder, shape)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(mColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = mode.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = mColor
            )
            Text(
                text = mode.fullDescription,
                fontSize = 14.sp,
                color = colors.tradeTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(24.dp)
                .semantics { contentDescription = "Dismiss mode info" }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = colors.tradeTextSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
