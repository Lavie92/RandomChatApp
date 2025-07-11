package com.lavie.randochat.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lavie.randochat.model.Message
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.theme.*
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageType
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    roomId: String
) {
    val myUser by authViewModel.loginState.collectAsState()
    val myUserId = myUser?.id ?: return
    val messages by chatViewModel.messages.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            chatViewModel.sendImage(roomId, myUserId, uri)
        }
    }

    DisposableEffect(roomId) {
        val listener = chatViewModel.startListening(roomId)
        onDispose { chatViewModel.removeMessageListener(roomId, listener) }
    }

    ConversationScreen(
        messages = messages,
        myUserId = myUserId,
        onSendText = { text ->
            chatViewModel.sendTextMessage(roomId, myUserId, text)
        },
        onSendImage = {
            galleryLauncher.launch("image/*")
        },
        onSendVoice = { audioUrl ->
            chatViewModel.sendVoiceMessage(roomId, myUserId, audioUrl)
        },
        navController = navController
    )
}

@Composable
fun ConversationScreen(
    messages: List<Message>,
    myUserId: String,
    onSendText: (String) -> Unit,
    onSendImage: () -> Unit,
    onSendVoice: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }

    var shouldScrollToBottom by remember { mutableStateOf(true) }

    val chatItems = remember(messages) {
        createChatItemsWithTimestamps(messages)
    }

    LaunchedEffect(messages.size, shouldScrollToBottom) {
        if (shouldScrollToBottom && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            shouldScrollToBottom = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(bottom = Dimens.baseMargin),
        ) {
            items(chatItems, key = { item ->
                when (item) {
                    is ChatItem.MessageItem -> item.message.id
                    is ChatItem.TimestampItem -> "${Constants.TIMESTAMP}${item.timestamp}"
                }
            }) { item ->
                when (item) {
                    is ChatItem.MessageItem -> {
                        val message = item.message
                        val isMe = message.senderId == myUserId

                        MessageBubble(
                            content = message.content,
                            isMe = isMe,
                            type = message.type,
                            time = message.timestamp,
                            isSelected = selectedMessageId == message.id,
                            onClick = {
                                selectedMessageId =
                                    if (selectedMessageId == message.id) null else message.id
                            },
                            navController = navController
                        )
                    }

                    is ChatItem.TimestampItem -> {
                        TimestampHeader(timestamp = item.timestamp)
                    }
                }
            }
        }

        ChatInputBar(
            value = messageText,
            onValueChange = { messageText = it },
            onSendImage = {onSendImage() },
            onVoiceRecord = { onSendVoice },
            onSend = {
                if (messageText.trim().isNotBlank()) {
                    onSendText(messageText)
                    messageText = ""
                    shouldScrollToBottom = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(top = Dimens.smallMargin)
        )
    }
}

@Composable
fun MessageBubble(
    content: String,
    isMe: Boolean,
    type: MessageType,
    time: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.smallMargin, horizontal = Dimens.baseMarginDouble)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MessageBackground else Color.Transparent,
            shape = RoundedCornerShape(
                topStart = Dimens.baseMarginDouble,
                topEnd = Dimens.baseMarginDouble,
                bottomEnd = if (isMe) Dimens.emptySize else Dimens.baseMarginDouble,
                bottomStart = if (isMe) Dimens.baseMarginDouble else Dimens.emptySize
            ),
            border = if (!isMe) BorderStroke(
                Dimens.smallBorderStrokeWidth,
                Color.LightGray
            ) else null
        )
        {
            when (type) {
                MessageType.TEXT -> Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = Dimens.baseMarginDouble, vertical = Dimens.baseMargin)
                        .widthIn(max = Dimens.messageMaxSizeable)
                )

                MessageType.IMAGE -> {
                    val context = LocalContext.current
                    AsyncImage(
                        model = content,
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                navController.navigate("imagePreview/${Uri.encode(content)}")
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                MessageType.VOICE -> {}
            }
        }

        if (isSelected) {
            Text(
                text = CommonUtils.formatToTime(time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.smallMargin, end = Dimens.baseMarginDouble)
            )
        }
    }
}

@Composable
fun TimestampHeader(timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.baseMargin),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(Dimens.buttonRadius),
            modifier = Modifier.padding(horizontal = Dimens.baseMarginDouble)
        ) {
            Text(
                text = CommonUtils.formatToDateTimeDetailed(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = Dimens.baseMargin,
                    vertical = Dimens.smallMargin
                )
            )
        }
    }
}

private fun createChatItemsWithTimestamps(
    messages: List<Message>,
): List<ChatItem> {
    if (messages.isEmpty()) return emptyList()

    val chatItems = mutableListOf<ChatItem>()
    val timeGapMillis = Constants.TIME_GAP_MINUTES * 60 * 1000

    messages.forEachIndexed { index, message ->
        val shouldShowTimestamp = if (index == 0) {
            true
        } else {
            val previousMessage = messages[index - 1]
            val timeDifference = message.timestamp - previousMessage.timestamp
            timeDifference >= timeGapMillis
        }

        if (shouldShowTimestamp) {
            chatItems.add(ChatItem.TimestampItem(message.timestamp))
        }

        chatItems.add(ChatItem.MessageItem(message))
    }

    return chatItems
}

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class TimestampItem(val timestamp: Long) : ChatItem()
}

fun downloadImageToGallery(context: Context, imageUrl: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = URL(imageUrl).openStream()

            val filename = "chat_image_${System.currentTimeMillis()}.jpg"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RandoChat")
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { uri ->
                resolver.openOutputStream(uri).use { outputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Image saved to Gallery!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}