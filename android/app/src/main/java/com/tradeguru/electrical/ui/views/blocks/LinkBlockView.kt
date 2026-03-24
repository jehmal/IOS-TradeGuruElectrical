package com.tradeguru.electrical.ui.views.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors

@Composable
fun LinkBlockView(content: String?, url: String?) {
    val colors = LocalTradeGuruColors.current
    val uriHandler = LocalUriHandler.current
    val displayText = content ?: url ?: ""
    val targetUrl = url ?: ""

    Text(
        text = displayText,
        style = TextStyle(
            fontSize = 14.sp,
            color = colors.modeLearn,
            textDecoration = TextDecoration.Underline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = targetUrl.isNotEmpty()) {
                uriHandler.openUri(targetUrl)
            }
    )
}
