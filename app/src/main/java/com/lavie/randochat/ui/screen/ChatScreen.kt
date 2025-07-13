package com.lavie.randochat.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.lavie.randochat.R
import com.lavie.randochat.model.Message
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.ui.theme.MessageBackground
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    roomId: String
) {
    val myUser by authViewModel.loginState.collectAsState()
    val myUserId = myUser?.id ?: return
    val messages by chatViewModel.messages.collectAsState()
    val isLoadingMore by chatViewModel.isLoadingMore.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(messages) {
        if (CommonUtils.isAppInForeground(context)) {
            chatViewModel.markMessagesAsSeen(roomId, myUserId, messages)
        }
    }

    LaunchedEffect(roomId) {
        chatViewModel.loadInitialMessages(roomId)
    }

    DisposableEffect(roomId) {
        val typingListener = chatViewModel.startTypingListener(roomId, myUserId)

        onDispose {
            chatViewModel.removeMessageListener()
            chatViewModel.updateTypingStatus(roomId, myUserId, false)
            chatViewModel.removeTypingListener(roomId, typingListener)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                chatViewModel.updateTypingStatus(roomId, myUserId, false)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ConversationScreen(
        messages = messages,
        myUserId = myUserId,
        isTyping = isTyping,
        onTypingStatusChanged = { typing ->
            chatViewModel.updateTypingStatus(roomId, myUserId, typing)
        },
        onSendText = { text ->
            chatViewModel.sendTextMessage(roomId, myUserId, text)
            chatViewModel.updateTypingStatus(roomId, myUserId, false)
        },
        onSendImage = { imageUrl ->
            chatViewModel.sendImageMessage(roomId, myUserId, imageUrl)
        },
        onSendVoice = { audioUrl ->
            chatViewModel.sendVoiceMessage(roomId, myUserId, audioUrl)
        },
        onLoadMore = { chatViewModel.loadMoreMessages() },
        isLoadingMore = isLoadingMore
    )
}

@Composable
fun ConversationScreen(
    messages: List<Message>,
    myUserId: String,
    isTyping: Boolean,
    onTypingStatusChanged: (Boolean) -> Unit,
    onSendText: (String) -> Unit,
    onSendImage: (String) -> Unit,
    onSendVoice: (String) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    modifier: Modifier = Modifier
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

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index == 0 && offset == 0) {
                    onLoadMore()
                }
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
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.baseMargin),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

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
                        val lastMessageId = messages.lastOrNull()?.id

                        MessageBubble(
                            content = if (message.isSystemMessage() && message.contentResId != null) {
                                stringResource(message.contentResId)
                            } else message.content,
                            isMe = isMe,
                            type = message.type,
                            time = message.timestamp,
                            status = message.status,
                            showStatus = isMe && message.id == lastMessageId,
                            isSelected = selectedMessageId == message.id,
                            onClick = {
                                selectedMessageId =
                                    if (selectedMessageId == message.id) null else message.id
                            }
                        )
                    }

                    is ChatItem.TimestampItem -> {
                        TimestampHeader(timestamp = item.timestamp)
                    }
                }
            }
        }

        if (isTyping) {
            Text(
                text = stringResource(R.string.typing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.baseMarginDouble)
            )
        }

        ChatInputBar(
            value = messageText,
            onValueChange = {
                messageText = it
                onTypingStatusChanged(it.isNotBlank())
            },
            onSendImage = { onSendImage },
            onVoiceRecord = { onSendVoice },
            onSend = {
                val messageTrimmed = messageText.trim()
                if (messageTrimmed.isNotBlank()) {
                    onSendText(messageTrimmed)
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
    status: MessageStatus,
    showStatus: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
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

                MessageType.IMAGE -> {}

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
        } else {
            if (showStatus) {
                Text(
                    text = when (status) {
                        MessageStatus.SENT -> stringResource(R.string.message_sent)
                        MessageStatus.SEEN -> stringResource(R.string.message_seen)
                        MessageStatus.SENDING -> stringResource(R.string.message_sending)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        top = Dimens.smallMargin,
                        end = Dimens.baseMarginDouble
                    )
                )
            }
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
