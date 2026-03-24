package com.tradeguru.electrical.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradeguru.electrical.data.DomainMappers
import com.tradeguru.electrical.models.AttachmentType
import com.tradeguru.electrical.models.ThinkingMode
import com.tradeguru.electrical.ui.theme.LocalTradeGuruColors
import java.io.File
import java.util.UUID

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedMode: ThinkingMode,
    onModeChange: (ThinkingMode) -> Unit,
    onSend: (String, DomainMappers.MessageAttachment?) -> Unit,
    isStreaming: Boolean,
    onAudioRecorded: ((File) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalTradeGuruColors.current

    var attachmentActive by remember { mutableStateOf(false) }
    var attachmentType by remember { mutableStateOf(AttachmentType.IMAGE) }
    var attachmentData by remember { mutableStateOf<ByteArray?>(null) }
    var attachmentFileName by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.tradeBorder)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.tradeBg.copy(alpha = 0.85f))
                .padding(top = 10.dp)
        ) {
            ModeSelector(
                selectedMode = selectedMode,
                onModeSelected = onModeChange,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (attachmentActive && attachmentData != null) {
                AttachmentPreviewStrip(
                    attachmentType = attachmentType,
                    attachmentData = attachmentData,
                    attachmentFileName = attachmentFileName,
                    colors = colors,
                    onClear = {
                        attachmentActive = false
                        attachmentData = null
                        attachmentFileName = null
                        attachmentType = AttachmentType.IMAGE
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            InputRow(
                text = text,
                onTextChange = onTextChange,
                onSend = { sendText, attachment ->
                    onSend(sendText, attachment)
                    attachmentActive = false
                    attachmentData = null
                    attachmentFileName = null
                    attachmentType = AttachmentType.IMAGE
                },
                isStreaming = isStreaming,
                colors = colors,
                attachmentActive = attachmentActive,
                attachmentType = attachmentType,
                attachmentData = attachmentData,
                attachmentFileName = attachmentFileName,
                isRecording = isRecording,
                onAttachmentSelected = { type, data, fileName ->
                    attachmentType = type
                    attachmentData = data
                    attachmentFileName = fileName
                    attachmentActive = true
                },
                onClearAttachment = {
                    attachmentActive = false
                    attachmentData = null
                    attachmentFileName = null
                    attachmentType = AttachmentType.IMAGE
                },
                onStartRecording = { isRecording = true },
                onStopRecording = {
                    isRecording = false
                },
                onAudioRecorded = onAudioRecorded
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AttachmentPreviewStrip(
    attachmentType: AttachmentType,
    attachmentData: ByteArray?,
    attachmentFileName: String?,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.tradeInput)
            .padding(6.dp)
    ) {
        if (attachmentType == AttachmentType.IMAGE && attachmentData != null) {
            val bitmap = remember(attachmentData) {
                BitmapFactory.decodeByteArray(attachmentData, 0, attachmentData.size)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        } else if (attachmentType == AttachmentType.DOCUMENT) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = colors.tradeTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = attachmentFileName ?: "File",
                    style = TextStyle(fontSize = 8.sp, color = colors.tradeTextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = attachmentFileName ?: "Attachment",
            style = TextStyle(fontSize = 12.sp, color = colors.tradeTextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onClear,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                tint = colors.tradeTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun InputRow(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String, DomainMappers.MessageAttachment?) -> Unit,
    isStreaming: Boolean,
    colors: com.tradeguru.electrical.ui.theme.TradeGuruColors,
    attachmentActive: Boolean,
    attachmentType: AttachmentType,
    attachmentData: ByteArray?,
    attachmentFileName: String?,
    isRecording: Boolean,
    onAttachmentSelected: (AttachmentType, ByteArray, String) -> Unit,
    onClearAttachment: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onAudioRecorded: ((File) -> Unit)?
) {
    val context = LocalContext.current
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                onAttachmentSelected(AttachmentType.IMAGE, bytes, "photo.jpg")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
            onAttachmentSelected(AttachmentType.IMAGE, stream.toByteArray(), "photo.jpg")
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val fileName = uri.lastPathSegment ?: "document"
            if (bytes != null) {
                onAttachmentSelected(AttachmentType.DOCUMENT, bytes, fileName)
            }
        }
    }

    val plusRotation by animateFloatAsState(
        targetValue = if (attachmentActive) 45f else 0f,
        animationSpec = spring(),
        label = "plusRotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(start = 12.dp)) {
            IconButton(
                onClick = {
                    if (attachmentActive) {
                        onClearAttachment()
                    } else {
                        showAttachmentMenu = true
                    }
                },
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(colors.tradeInput)
                    .semantics { contentDescription = "Attach file" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.tradeText,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(plusRotation)
                )
            }

            DropdownMenu(
                expanded = showAttachmentMenu,
                onDismissRequest = { showAttachmentMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Take Photo") },
                    onClick = {
                        showAttachmentMenu = false
                        cameraLauncher.launch(null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Photo Library") },
                    onClick = {
                        showAttachmentMenu = false
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Browse Files") },
                    onClick = {
                        showAttachmentMenu = false
                        documentLauncher.launch("application/*")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            maxLines = 5,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = colors.tradeText
            ),
            cursorBrush = SolidColor(colors.tradeGreen),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.tradeLight)
                        .border(0.5.dp, colors.tradeBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask TradeGuru",
                            style = TextStyle(fontSize = 16.sp, color = colors.tradeTextSecondary)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        if (text.isNotEmpty() || attachmentActive) {
            IconButton(
                onClick = {
                    val attachment = if (attachmentActive && attachmentData != null) {
                        DomainMappers.MessageAttachment(
                            id = UUID.randomUUID().toString(),
                            type = attachmentType,
                            fileName = attachmentFileName
                                ?: if (attachmentType == AttachmentType.IMAGE) "photo.jpg" else "file",
                            fileSize = attachmentData.size,
                            thumbnailData = attachmentData
                        )
                    } else {
                        null
                    }
                    onSend(text, attachment)
                },
                enabled = !isStreaming,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(28.dp)
                    .semantics { contentDescription = "Send message" }
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.tradeGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else if (!isRecording) {
            IconButton(
                onClick = {
                    onStartRecording()
                },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(44.dp)
                    .semantics { contentDescription = "Record voice message" }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = colors.tradeGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            IconButton(
                onClick = {
                    onStopRecording()
                },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(44.dp)
                    .semantics { contentDescription = "Stop recording" }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
