package com.tradeguru.electrical.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen
import com.tradeguru.electrical.ui.views.onboarding.modeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarView(
    conversations: List<DomainMappers.Conversation>,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNewChat: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalTradeGuruColors.current
    var searchText by remember { mutableStateOf("") }
    val filtered = remember(conversations, searchText) {
        if (searchText.isEmpty()) conversations
        else conversations.filter { it.title.lowercase().contains(searchText.lowercase()) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(onClick = onClose))
        Column(Modifier.width(300.dp).fillMaxHeight().background(colors.tradeBg).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Conversations", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = colors.tradeText)
                IconButton(onClick = onClose, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close menu", tint = colors.tradeTextSecondary)
                }
            }
            HorizontalDivider(color = colors.tradeBorder)
            TextField(
                value = searchText, onValueChange = { searchText = it }, singleLine = true,
                placeholder = { Text("Search conversations", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(14.dp)) },
                trailingIcon = { if (searchText.isNotEmpty()) IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Close, "Clear", Modifier.size(14.dp)) } },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = colors.tradeInput, focusedContainerColor = colors.tradeInput, unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered, key = { it.id }) { conv ->
                    val state = rememberSwipeToDismissBoxState()
                    LaunchedEffect(state.currentValue) { if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete(conv.id) }
                    SwipeToDismissBox(state = state, enableDismissFromStartToEnd = false, backgroundContent = {
                        Box(Modifier.fillMaxSize().background(Color.Red).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterEnd) { Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold) }
                    }) {
                        Row(Modifier.fillMaxWidth().background(colors.tradeBg).clickable { onSelect(conv.id) }.padding(horizontal = 16.dp, vertical = 10.dp).height(44.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(conv.mode.icon), null, tint = modeColor(conv.mode), modifier = Modifier.size(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(conv.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.tradeText, maxLines = 1)
                                Text(formatDate(conv.updatedAt), fontSize = 12.sp, color = colors.tradeTextSecondary)
                                Text("${conv.messages.size} messages", fontSize = 12.sp, color = colors.tradeTextSecondary)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = colors.tradeBorder)
            Button(onClick = onNewChat, colors = ButtonDefaults.buttonColors(containerColor = TradeGreen), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("New Conversation", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale("en", "US")).format(Date(timestamp))
