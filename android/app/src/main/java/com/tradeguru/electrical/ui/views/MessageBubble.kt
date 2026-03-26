package com.tradeguru.electrical.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.ui.modeColor
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.views.blocks.RenderBlock
import com.tradeguru.electrical.ui.views.structured.StructuredMessageRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: DomainMappers.ChatMessage,
    isLastAssistantMessage: Boolean = false,
    onRate: ((Int) -> Unit)? = null,
    onFlag: ((String) -> Unit)? = null,
    onSpeak: ((String) -> Unit)? = null
) {
    val colors = LocalTradeGuruColors.current
    val alignment = if (message.role == MessageRole.USER) Alignment.End else Alignment.Start

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (message.role == MessageRole.USER) {
            UserBubble(message, colors)
        } else {
            AssistantBubble(message, colors)
        }

        TimestampRow(message, colors)

        if (message.role == MessageRole.ASSISTANT && isLastAssistantMessage) {
            ActionRow(message, onRate, onFlag, onSpeak, colors)
        }
    }
}

@Composable
private fun UserBubble(
    message: DomainMappers.ChatMessage,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    val textContent = message.blocks.mapNotNull { it.content }.joinToString("\n")
    Text(
        text = textContent,
        style = TextStyle(fontSize = 15.sp, color = Color.White),
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.tradeGreen)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable
private fun AssistantBubble(
    message: DomainMappers.ChatMessage,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = 330.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.tradeSurface)
            .padding(14.dp)
    ) {
        if (message.structuredData != null) {
            StructuredMessageRenderer(response = message.structuredData)
        } else {
            message.blocks.forEach { block ->
                RenderBlock(block = block)
            }
        }
    }
}

@Composable
private fun TimestampRow(
    message: DomainMappers.ChatMessage,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    val formatter = remember {
        SimpleDateFormat("h:mm a", Locale("en", "US", "POSIX"))
    }
    val timeText = remember(message.timestamp) {
        formatter.format(Date(message.timestamp))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (message.role == MessageRole.ASSISTANT) {
            val mc = modeColor(message.mode)
            Icon(
                painter = painterResource(id = message.mode.icon),
                contentDescription = null,
                tint = mc,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = timeText,
            fontSize = 11.sp,
            color = colors.tradeTextSecondary
        )
    }
}

@Composable
private fun ActionRow(
    message: DomainMappers.ChatMessage,
    onRate: ((Int) -> Unit)?,
    onFlag: ((String) -> Unit)?,
    onSpeak: ((String) -> Unit)?,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors
) {
    var userRating by remember { mutableIntStateOf(0) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(44.dp)
    ) {
        (1..5).forEach { star ->
            IconButton(
                onClick = { userRating = star; onRate?.invoke(star) },
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Rate $star stars" }
            ) {
                Icon(
                    imageVector = if (star <= userRating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (star <= userRating) colors.tradeGreen else colors.tradeTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        IconButton(
            onClick = { onFlag?.invoke("inappropriate") },
            modifier = Modifier
                .size(24.dp)
                .semantics { contentDescription = "Report response" }
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = colors.tradeTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        IconButton(
            onClick = {
                val text = message.blocks.mapNotNull { it.content }.joinToString("\n")
                onSpeak?.invoke(text)
            },
            modifier = Modifier
                .size(24.dp)
                .semantics { contentDescription = "Read aloud" }
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = colors.tradeTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
