package com.example.randomchat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.randomchat.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.randomchat.model.Message
import com.example.randomchat.ui.component.ChatInputBar

@Composable
fun ChatScreen(
    navController: NavController
) {
    var chatStarted by remember { mutableStateOf(false) }

    if (!chatStarted) {
        WelcomeScreen(
            onStartChatClick = { chatStarted = true }
        )
    } else {
        ConversationScreen(sampleMessages, myUserId)
    }
}

@Composable
fun WelcomeScreen(
    onStartChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MailOutline,
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = Color(0xFFD8D8D8)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            text = stringResource(R.string.lets_start_chatting)
        )
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(
            onClick = onStartChatClick
        ) {
            Text(
                text = stringResource(R.string.start_a_chat),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2979FF)
            )
        }
    }
}

val myUserId = "user_1"
val sampleMessages = listOf(
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE5DDD5))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp, top = 8.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
            onSendImage = { /* TODO: Handle image */ },
            onVoiceRecord = { /* TODO: Handle voice */ },
            onSend = {messageText /*TODO: Handle send */},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
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
            color = if (isMe) Color(0xFFDCF8C6) else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp
            ),
            shadowElevation = 2.dp
        ) {
            Text(
                text = content,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .widthIn(max = 260.dp)
            )
        }
    }
}
