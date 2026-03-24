package com.tradeguru.electrical.ui.views.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.MessageRole
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.views.MessageBubble
import com.tradeguru.electrical.ui.views.PipelineStatusView
import com.tradeguru.electrical.ui.views.blocks.RenderBlock
import com.tradeguru.electrical.viewmodels.ChatViewModel

@Composable
fun ChatMessageList(
    messages: List<DomainMappers.ChatMessage>,
    streamingBlocks: List<DomainMappers.ContentBlock>,
    isStreaming: Boolean,
    pipelineStage: PipelineStage,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val colors = LocalTradeGuruColors.current
    val lastAssistantId = messages.lastOrNull { it.role == MessageRole.ASSISTANT }?.id

    LaunchedEffect(messages.size, streamingBlocks.size) {
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(
                message = message,
                isLastAssistantMessage = message.id == lastAssistantId,
                onRate = { stars -> viewModel.rateLastResponse(stars) },
                onFlag = { },
                onSpeak = { text -> viewModel.speakText(text) }
            )
        }

        if (isStreaming && streamingBlocks.isNotEmpty()) {
            item(key = "streaming") {
                StreamingBubble(blocks = streamingBlocks)
            }
        } else if (isStreaming && streamingBlocks.isEmpty()) {
            item(key = "typing") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PipelineStatusView(
                        stage = pipelineStage,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
private fun StreamingBubble(blocks: List<DomainMappers.ContentBlock>) {
    val colors = LocalTradeGuruColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = 330.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.tradeSurface)
            .padding(14.dp)
    ) {
        blocks.forEach { block ->
            RenderBlock(block = block)
        }
    }
}

@Composable
private fun TypingIndicator() {
    val colors = LocalTradeGuruColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .widthIn(max = 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.tradeSurface)
            .padding(14.dp)
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "typingDot_$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .alpha(alpha)
                    .background(colors.tradeTextSecondary)
            )
        }
    }
}
