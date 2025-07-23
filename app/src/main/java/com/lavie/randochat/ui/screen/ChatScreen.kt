package com.lavie.randochat.ui.screen

import android.Manifest
import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.lavie.randochat.R
import com.lavie.randochat.model.Message
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.ui.component.VoiceRecordState
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.ui.theme.MessageBackground
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import com.lavie.randochat.utils.formatMillis
import com.lavie.randochat.utils.getAudioDurationMs
import com.lavie.randochat.utils.resolveAudioFile
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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
    val isLoadingMore by chatViewModel.isLoadingMore.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val chatType by chatViewModel.chatType.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val voiceRecordState by chatViewModel.voiceRecordState.collectAsState()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            scope.launch {
                chatViewModel.sendImage(roomId, myUserId, uri, context)
            }
        }
    }
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, context.getString(R.string.record_permission_required), Toast.LENGTH_SHORT).show()
        }
    }
    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(messages) {
        if (CommonUtils.isAppInForeground(context)) {
            chatViewModel.markMessagesAsSeen(roomId, myUserId, messages)
        }
    }

    LaunchedEffect(roomId) {
        chatViewModel.loadChatType(roomId)
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
        onSendImage = {
            galleryLauncher.launch("image/*")
        },
        navController = navController,
        onLoadMore = { chatViewModel.loadMoreMessages() },
        isLoadingMore = isLoadingMore,
        chatType = chatType,
        voiceRecordState = voiceRecordState,
        onVoiceRecordStart = {
            chatViewModel.startRecording(context) {
                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onVoiceRecordStop = { chatViewModel.stopRecording() },
        onVoiceRecordCancel = { chatViewModel.cancelRecording() },
        onVoiceRecordSend = { chatViewModel.sendVoiceMessageOptimistic(context, roomId, myUserId) },
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    messages: List<Message>,
    myUserId: String,
    isTyping: Boolean,
    onTypingStatusChanged: (Boolean) -> Unit,
    onSendText: (String) -> Unit,
    onSendImage: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    voiceRecordState: VoiceRecordState,
    onVoiceRecordStart: () -> Unit,
    onVoiceRecordStop: () -> Unit,
    onVoiceRecordSend: () -> Unit,
    onVoiceRecordCancel: () -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    chatType: String,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(getTitleFromChatType(getTitleFromChatType(chatType).toString()))) },
                actions = {
                    ImageButton(onClick = { navController.navigate(Constants.SETTINGS_SCREEN) }, icon = Icons.Default.Settings)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(bottom = Dimens.emptySize),
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
                onSendImage = { onSendImage() },
                voiceRecordState = voiceRecordState,
            	onVoiceRecordStart = { onVoiceRecordStart() },
            	onVoiceRecordStop = { onVoiceRecordStop() },
            	onVoiceRecordCancel = { onVoiceRecordCancel() },
            	onVoiceRecordSend = { onVoiceRecordSend() },
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
            )
        }
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
        ) {
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
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val density = LocalDensity.current
                        val sizePx = with(density) { 200.dp.toPx().toInt() }

                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    if (isMe) {
                                        Glide.with(this)
                                            .load(content)
                                            .placeholder(R.drawable.placeholder)
                                            .into(this)
                                    } else {
                                        Glide.with(this)
                                            .load(content)
                                            .placeholder(R.drawable.placeholder)
                                            .transform(BlurTransformation(25, 3))
                                            .into(this)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    navController.navigate("imagePreview/${Uri.encode(content)}")
                                }
                        )

                        if (status == MessageStatus.SENDING) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.sending),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                MessageType.VOICE -> {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val mediaPlayer = remember { MediaPlayer() }
                    val isPlaying = remember { mutableStateOf(false) }
                    var durationText by remember { mutableStateOf("0:00") }
                    var displayTime by remember { mutableStateOf("0:00") }
                    var durationMs by remember { mutableStateOf(0L) }
                    var lastPlaybackPosition by remember { mutableStateOf(0) }

                    LaunchedEffect(content) {
                        scope.launch {
                            val file = resolveAudioFile(context, content)
                            if (file != null) {
                                durationMs = getAudioDurationMs(file)
                                durationText = formatMillis(durationMs)
                                displayTime = durationText
                            }
                        }
                    }

                    fun startPlayback(file: File) {
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(file.absolutePath)
                            mediaPlayer.prepare()
                            mediaPlayer.seekTo(lastPlaybackPosition)
                            mediaPlayer.start()
                            isPlaying.value = true

                            scope.launch {
                                while (isPlaying.value && mediaPlayer.isPlaying) {
                                    delay(1000)
                                    val current = mediaPlayer.currentPosition
                                    displayTime = formatMillis(current.toLong())
                                }
                            }

                            mediaPlayer.setOnCompletionListener {
                                isPlaying.value = false
                                lastPlaybackPosition = 0
                                displayTime = durationText
                            }
                        } catch (e: Exception) {
                            isPlaying.value = false
                            customToast(context,R.string.voice_playback_failed)

                        }
                    }

                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MessageBackground)
                            .clickable {
                                scope.launch {
                                    val file = resolveAudioFile(context, content)
                                    if (file != null) {
                                        if (isPlaying.value) {
                                            lastPlaybackPosition = mediaPlayer.currentPosition
                                            mediaPlayer.pause()
                                            isPlaying.value = false
                                        } else {
                                            displayTime = formatMillis(lastPlaybackPosition.toLong())
                                            startPlayback(file)
                                        }
                                    }
                                }
                            },
                        color = Color(0xFF007AFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying.value) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayTime, color = Color.White)
                        }
                    }
                }
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
                        MessageStatus.FAILED -> stringResource(R.string.play_audio_failed)
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

private fun getTitleFromChatType(chatType: String) : Int {
    return when (chatType) {
        ChatType.AGE.name -> {
            R.string.chat_by_age
        }
        ChatType.LOCATION.name -> {
            R.string.chat_by_location
        }
        ChatType.RANDOM.name -> {
            R.string.random_chat
        }

        else -> {R.string.random_chat}
    }
}

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class TimestampItem(val timestamp: Long) : ChatItem()
}
