package com.tradeguru.electrical.ui.views.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun ChatNavBar(
    onMenuTap: () -> Unit,
    onSettingsTap: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        IconButton(
            onClick = onMenuTap,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "Menu" }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = colors.tradeText,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onSettingsTap,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "Settings" }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = colors.tradeText,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onNewChat,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "New conversation" }
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = colors.tradeText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
