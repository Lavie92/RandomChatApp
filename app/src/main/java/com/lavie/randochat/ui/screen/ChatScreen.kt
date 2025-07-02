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
import androidx.compose.ui.tooling.preview.Preview
import com.lavie.randochat.model.Message
import com.lavie.randochat.ui.component.ChatInputBar
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.theme.*
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    partnerUserId: String?,
    authViewModel: AuthViewModel
)  {
    var chatStarted by remember { mutableStateOf(false) }

        ConversationScreen(sampleMessages, myUserId)

}

//TODO: just hard code to test UI
val myUserId = "user_1"
val sampleMessages = listOf(
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!"),
    Message(id = "1", senderId = "user_2", receiverId = "user_1", content = "Hello!"),
    Message(id = "2", senderId = "user_1", receiverId = "user_2", content = "Hi!"),
    Message(id = "3", senderId = "user_2", receiverId = "user_1", content = "How are you?"),
    Message(id = "4", senderId = "user_1", receiverId = "user_2", content = "I'm good, thanks!")
)

@Composable
fun ConversationScreen(
    messages: List<Message>,
    myUserId: String,
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
                .fillMaxSize()
                .weight(1f)
                .padding(top = Dimens.baseMargin, start = Dimens.baseMarginDouble, end = Dimens.baseMarginDouble),
            verticalArrangement = Arrangement.spacedBy(Dimens.baseMargin),
            reverseLayout = true
        ) {
            items(messages.asReversed()) { message ->
                val isMe = message.senderId == myUserId
                MessageBubble(
                    content = message.content,
                    isMe = isMe
                )
            }
        }

        ChatInputBar(
            value = messageText,
            onValueChange = { messageText = it },
            onVoiceRecord = { /* TODO: Handle voice */ },
            onSendImage = {/* TODO: Handle send image */ },
            onSend = { messageText /*TODO: Handle send */ },
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
    isMe: Boolean
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
            border = if (!isMe) BorderStroke(Dimens.smallBorderStrokeWidth, Color.LightGray) else null
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = Dimens.baseMarginDouble, vertical = Dimens.baseMargin)
                    .widthIn(max = Dimens.messageMaxSizeable)
            )
        }
    }
}
