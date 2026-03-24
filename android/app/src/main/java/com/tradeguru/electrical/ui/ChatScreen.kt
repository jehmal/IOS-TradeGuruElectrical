package com.tradeguru.electrical.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tradeguru.electrical.R
import com.tradeguru.electrical.models.PipelineStage
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.views.SidebarView
import com.tradeguru.electrical.ui.views.chat.ChatErrorBanner
import com.tradeguru.electrical.ui.views.chat.ChatMessageList
import com.tradeguru.electrical.ui.views.chat.ChatNavBar
import com.tradeguru.electrical.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val colors = LocalTradeGuruColors.current
    var selectedMode by remember { mutableStateOf(ThinkingMode.FAULT_FINDER) }
    var inputText by remember { mutableStateOf("") }
    var showModeCard by remember { mutableStateOf(true) }
    var userDismissedCard by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val activeConversation by viewModel.activeConversation.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamingBlocks by viewModel.streamingBlocks.collectAsState()
    val error by viewModel.error.collectAsState()
    val pipelineStage by viewModel.pipelineStage.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarView(
                conversations = conversations,
                onSelect = { conversationId ->
                    val conv = conversations.find { it.id == conversationId }
                    if (conv != null) {
                        viewModel.selectConversation(conv)
                    }
                    scope.launch { drawerState.close() }
                },
                onDelete = { conversationId ->
                    val conv = conversations.find { it.id == conversationId }
                    if (conv != null) {
                        viewModel.deleteConversation(conv)
                    }
                },
                onNewChat = {
                    viewModel.newConversation(selectedMode)
                    scope.launch { drawerState.close() }
                },
                onClose = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(colors.tradeBg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ChatNavBar(
                    onMenuTap = { scope.launch { drawerState.open() } },
                    onSettingsTap = onNavigateToSettings,
                    onNewChat = { viewModel.newConversation(selectedMode) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(colors.tradeBg)
                )

                ConversationArea(
                    showModeCard = showModeCard && !userDismissedCard,
                    selectedMode = selectedMode,
                    activeConversation = activeConversation,
                    isStreaming = isStreaming,
                    streamingBlocks = streamingBlocks,
                    pipelineStage = pipelineStage,
                    viewModel = viewModel,
                    onDismissCard = { showModeCard = false; userDismissedCard = true },
                    modifier = Modifier.weight(1f)
                )

                error?.let {
                    ChatErrorBanner(
                        errorMessage = it,
                        onRetry = { viewModel.retryLastRequest() },
                        onDismiss = { viewModel.dismissError() }
                    )
                }

                ChatInputBar(
                    text = inputText,
                    onTextChange = { newText ->
                        inputText = newText
                        if (newText.isNotEmpty()) {
                            showModeCard = false
                            userDismissedCard = true
                        }
                    },
                    selectedMode = selectedMode,
                    onModeChange = { mode ->
                        selectedMode = mode
                        userDismissedCard = false
                        viewModel.selectedMode.value = mode
                        showModeCard = true
                    },
                    onSend = { text, attachment ->
                        val attachments = attachment?.let { listOf(it) }
                        when (attachment?.type) {
                            com.tradeguru.electrical.models.AttachmentType.IMAGE ->
                                viewModel.sendWithVision(text, selectedMode, attachments)
                            com.tradeguru.electrical.models.AttachmentType.DOCUMENT ->
                                viewModel.sendWithDocument(text, selectedMode, attachments)
                            else -> viewModel.send(text, selectedMode)
                        }
                        inputText = ""
                    },
                    isStreaming = isStreaming,
                    onAudioRecorded = { audioFile ->
                        viewModel.transcribeAudio(audioFile)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationArea(
    showModeCard: Boolean,
    selectedMode: ThinkingMode,
    activeConversation: com.tradeguru.electrical.data.DomainMappers.Conversation?,
    isStreaming: Boolean,
    streamingBlocks: List<com.tradeguru.electrical.data.DomainMappers.ContentBlock>,
    pipelineStage: PipelineStage,
    viewModel: ChatViewModel,
    onDismissCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current
    Box(modifier = modifier.fillMaxSize().background(colors.tradeBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = showModeCard) {
                ModeInfoCard(
                    mode = selectedMode,
                    onDismiss = onDismissCard,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }

            val hasMessages = activeConversation != null &&
                (activeConversation.messages.isNotEmpty() || streamingBlocks.isNotEmpty())

            if (hasMessages) {
                ChatMessageList(
                    messages = activeConversation?.messages ?: emptyList(),
                    streamingBlocks = streamingBlocks,
                    isStreaming = isStreaming,
                    pipelineStage = pipelineStage,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            } else {
                EmptyStateLogo(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EmptyStateLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val colors = LocalTradeGuruColors.current
        Image(
            painter = painterResource(id = R.drawable.ic_bolt),
            contentDescription = null,
            modifier = Modifier.size(180.dp).alpha(0.08f)
        )
    }
}
