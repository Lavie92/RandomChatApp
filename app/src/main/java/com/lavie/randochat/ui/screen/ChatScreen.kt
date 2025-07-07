package com.lavie.randochat.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.lavie.randochat.utils.MessageType

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    roomId: String
) {
    val myUser by authViewModel.loginState.collectAsState()
    val myUserId = myUser?.id ?: return
    val messages by chatViewModel.messages.collectAsState()

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
        onSendImage = { imageUrl ->
            chatViewModel.sendImageMessage(roomId, myUserId, imageUrl)
        },
        onSendVoice = { audioUrl ->
            chatViewModel.sendVoiceMessage(roomId, myUserId, audioUrl)
        }
    )
}

@Composable
fun ConversationScreen(
    messages: List<Message>,
    myUserId: String,
    onSendText: (String) -> Unit,
    onSendImage: (String) -> Unit,
    onSendVoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }

    var shouldScrollToBottom by remember { mutableStateOf(true) }

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
                .padding()
                .fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(bottom = Dimens.baseMargin),
        ) {
            items(messages) { message ->
                val isMe = message.senderId == myUserId
                MessageBubble(
                    content = message.content,
                    isMe = isMe,
                    type = message.type
                )
            }
        }

        ChatInputBar(
            value = messageText,
            onValueChange = { messageText = it },
            onSendImage = {onSendImage },
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
    type: MessageType
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = Dimens.smallMargin, horizontal = Dimens.baseMarginDouble),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
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

                MessageType.IMAGE -> {}

                MessageType.VOICE -> {}
            }
        }
    }
}