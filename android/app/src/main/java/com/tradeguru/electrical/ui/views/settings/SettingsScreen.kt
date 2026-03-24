package com.tradeguru.electrical.ui.views.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.models.AuthUser
import com.tradeguru.electrical.models.UserTier
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import com.tradeguru.electrical.ui.theme.TradeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isAuthenticated: Boolean = false, currentUser: AuthUser? = null, tier: UserTier = UserTier.FREE,
    appVersion: String = "1.0 (1)", onSignIn: () -> Unit = {}, onSignOut: () -> Unit = {},
    onClearConversations: () -> Unit = {}, onDismiss: () -> Unit = {}
) {
    val colors = LocalTradeGuruColors.current
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) AlertDialog(
        onDismissRequest = { showClearDialog = false },
        title = { Text("Clear All Conversations") },
        text = { Text("This will permanently delete all your conversations. This action cannot be undone.") },
        confirmButton = { TextButton(onClick = { showClearDialog = false; onClearConversations() }) { Text("Clear All", color = Color.Red) } },
        dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
    )

    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") }, actions = {
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = colors.tradeTextSecondary, modifier = Modifier.size(14.dp)) }
        })
    }) { padding ->
        LazyColumn(contentPadding = padding) {
            item { SectionLabel("Account") }
            item {
                if (isAuthenticated && currentUser != null) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = colors.tradeTextSecondary, modifier = Modifier.size(60.dp).clip(CircleShape))
                        Column {
                            currentUser.name?.let { Text(it, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.tradeText) }
                            Text(currentUser.email, fontSize = 14.sp, color = colors.tradeTextSecondary)
                            TierBadgeView(tier = tier)
                        }
                    }
                    TextButton(onClick = onSignOut, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Sign Out", color = Color.Red) }
                } else {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("Sign In to TradeGuru", fontSize = 15.sp, color = colors.tradeText)
                        Text("Access Pro features and sync across devices", fontSize = 13.sp, color = colors.tradeTextSecondary)
                    }
                    TextButton(onClick = onSignIn, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Sign In", color = TradeGreen) }
                }
            }
            item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }
            item { SectionLabel("Data") }
            item { TextButton(onClick = { showClearDialog = true }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Clear All Conversations", color = Color.Red) } }
            item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }
            item { SectionLabel("About") }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TradeGuru Electrical", color = colors.tradeText); Text(appVersion, color = colors.tradeTextSecondary)
                }
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tradeguru.com.au/terms"))) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Terms of Service", color = TradeGreen) }
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tradeguru.com.au/privacy"))) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Privacy Policy", color = TradeGreen) }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LocalTradeGuruColors.current.tradeTextSecondary, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp))
}
