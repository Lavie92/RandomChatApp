package com.lavie.randochat.ui.screen

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.model.Message
import com.lavie.randochat.service.DialogService
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.component.EmojiPicker
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.ui.component.MessageBubble
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    roomId: String
) {
    val chatViewModel: ChatViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    val context = LocalContext.current
    val myUser by authViewModel.loginState.collectAsState()
    val myUserId = myUser?.id ?: return

    val messages by chatViewModel.messages.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val chatType by chatViewModel.chatType.collectAsState()
    val isChatRoomEnded by chatViewModel.isChatRoomEnded.collectAsState()
    val isLoadingMore by chatViewModel.isLoadingMore.collectAsState()
    val voiceRecordState by chatViewModel.voiceRecordState.collectAsState()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var shouldScrollToBottom by remember { mutableStateOf(true) }
    val endChatTitle = stringResource(R.string.end_chat_title)
    val endChatMessage = stringResource(R.string.end_chat_message)
    val confirmOption = stringResource(R.string.confirm)
    val cancelOption = stringResource(R.string.cancel)
    val chatItems = remember(messages) {
        createChatItemsWithTimestamps(messages)
    }
    var isKeyboardVisible by remember { mutableStateOf(false) }
    var emojiExpanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            chatViewModel.sendImage(roomId, myUserId, uri, context)
        }
    }

    val isKeyboardOpen by keyboardAsState()

    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && emojiExpanded) {
            emojiExpanded = false
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) customToast(context, R.string.record_permission_required)
    }

    BackHandler { (context as? Activity)?.finish() }

    LaunchedEffect(roomId) {
        chatViewModel.loadChatType(roomId)
        chatViewModel.loadInitialMessages(roomId)
        chatViewModel.listenToRoomStatus(roomId)
        chatViewModel.startRealtimeMessageListener(roomId)
    }

    DisposableEffect(roomId) {
        val typingListener = chatViewModel.startTypingListener(roomId, myUserId)

        onDispose {
            chatViewModel.clearListeners()
            chatViewModel.updateTypingStatus(roomId, myUserId, false)
            chatViewModel.removeTypingListener(roomId, typingListener)
        }
    }

    LaunchedEffect(messages.lastOrNull()) {
        if (CommonUtils.isAppInForeground(context)) {
            chatViewModel.markMessagesAsSeen(roomId, myUserId, messages)
        }
    }

    LaunchedEffect(messages.size, shouldScrollToBottom) {
        if (shouldScrollToBottom && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            shouldScrollToBottom = false
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                if (index == 0 && offset == 0) chatViewModel.loadMoreMessages()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = getTitleFromChatType(chatType)))
                },
                actions = {
                    ImageButton(
                        onClick = { navController.navigate(Constants.SETTINGS_SCREEN) },
                        icon = Icons.Default.Settings
                    )
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
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(bottom = Dimens.emptySize)
            ) {
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(Dimens.baseMargin),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                items(chatItems, key = {
                    when (it) {
                        is ChatItem.MessageItem -> it.message.id
                        is ChatItem.TimestampItem -> "timestamp_${it.timestamp}"
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
                        is ChatItem.TimestampItem -> TimestampHeader(timestamp = item.timestamp)
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

            if (isChatRoomEnded) {
                Button(
                    onClick = {
                        chatViewModel.clearCachedMessages(roomId)
                        authViewModel.clearActiveRoom()
                        navController.navigate(Constants.START_CHAT_SCREEN) {
                            popUpTo(Constants.CHAT_SCREEN) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(Dimens.baseMargin)
                ) {
                    Text(stringResource(R.string.ok))
                }
            } else {
                ChatInputBar(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        chatViewModel.updateTypingStatus(roomId, myUserId, it.text.isNotBlank())
                    },
                    onSendImage = { galleryLauncher.launch(Constants.MIME_TYPE_IMAGE) },
                    voiceRecordState = voiceRecordState,
                    onVoiceRecordStart = {
                        chatViewModel.startRecording(context) {
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onVoiceRecordStop = chatViewModel::stopRecording,
                    onVoiceRecordCancel = chatViewModel::cancelRecording,
                    onVoiceRecordSend = {
                        chatViewModel.sendVoiceMessageOptimistic(context, roomId, myUserId)
                    },
                    onSend = {
                        val trimmed = messageText.text.trim()
                        if (trimmed.isNotBlank()) {
                            chatViewModel.sendTextMessage(roomId, myUserId, trimmed)
                            chatViewModel.updateTypingStatus(roomId, myUserId, false)
                            messageText = TextFieldValue("")
                            shouldScrollToBottom = true
                            emojiExpanded = false
                        }
                    },
                    onReportClick = {},
                    onLikeClick = {},
                    onEndChatClick = {
                        DialogService.show(
                            title = endChatTitle,
                            message = endChatMessage,
                            confirmButton = confirmOption,
                            dismissButton = cancelOption,
                            onConfirmAction = {
                                chatViewModel.endChat(roomId, myUserId)
                            }
                        )
                    },
                    onToggleEmojiPicker = {
                        if (!emojiExpanded) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        emojiExpanded = !emojiExpanded
                        if (!emojiExpanded) {
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (emojiExpanded) {
                    EmojiPicker(
                        onEmojiSelected = { emoji ->
                            val newText = messageText.text + emoji
                            messageText = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        },
                        onDismiss = { emojiExpanded = false }
                    )
                }
            }
        }
    }

    OnAppResumed {
        chatViewModel.startRealtimeMessageListener(roomId)
    }

    DialogService.Render()
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
    val timeGapMillis = Constants.TIME_GAP_MINUTES * Constants.SECONDS_PER_MINUTE * Constants.MILLISECONDS_PER_SECOND


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

private fun getTitleFromChatType(chatType: String): Int {
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

        else -> {
            R.string.random_chat
        }
    }
}

@Composable
fun OnAppResumed(action: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                action()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = keypadHeight > screenHeight * 0.15 // Ngưỡng 15% để phát hiện bàn phím
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return keyboardState
}

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class TimestampItem(val timestamp: Long) : ChatItem()
}
