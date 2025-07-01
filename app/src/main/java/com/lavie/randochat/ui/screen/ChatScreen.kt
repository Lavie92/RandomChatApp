package com.lavie.randochat.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.lavie.randochat.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lavie.randochat.model.Message
import com.lavie.randochat.model.MessageType
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.theme.*
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    partnerUserId: String
) {
    var chatStarted by remember { mutableStateOf(false) }

    val myUser by authViewModel.loginState.collectAsState()

    val myUserId = myUser?.id ?: return

    val conversationId = remember(myUserId, partnerUserId) {
        listOf(myUserId, partnerUserId).sorted().joinToString("_")
    }

    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(conversationId) {
        if (chatStarted) {
            viewModel.startListening(conversationId)
        }
    }

//    if (!chatStarted) {
//        WelcomeScreen(
//            onStartChatClick = { chatStarted = true }
//        )
//    } else {
    ConversationScreen(
        messages = messages,
        myUserId = myUserId,
        onSendText = { text ->
            viewModel.sendTextMessage(conversationId, myUserId, partnerUserId, text)
        },
        onSendImage = { imageUrl ->
            viewModel.sendImageMessage(conversationId, myUserId, partnerUserId, imageUrl)
        },
        onSendVoice = { audioUrl ->
            viewModel.sendVoiceMessage(conversationId, myUserId, partnerUserId, audioUrl)
//            }
//        )
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
    var messageText by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(/*...*/),
            verticalArrangement = Arrangement.spacedBy(Dimens.baseMargin),
            reverseLayout = true
        ) {
            items(messages.asReversed()) { message ->
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
            onSendImage = { /* mở picker, upload, xong gọi onSendImage(imageUrl) */ },
            onVoiceRecord = { /* mở ghi âm, upload, xong gọi onSendVoice(audioUrl) */ },
            onSend = {
                if (messageText.trim().isNotBlank()) {
                    onSendText(messageText)
                    messageText = ""
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMe) messageBackground else Color.Transparent,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = Dimens.baseMarginDouble, vertical = Dimens.baseMargin)
                        .widthIn(max = Dimens.messageMaxSizeable)
                )

                MessageType.IMAGE -> {/* Show Image from content = url */
                }

                MessageType.VOICE -> {/* Show audio player */
                }
            }
        }
    }
}
