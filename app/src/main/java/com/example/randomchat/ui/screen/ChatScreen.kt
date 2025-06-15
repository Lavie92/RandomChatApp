package com.example.randomchat.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ChatScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onStartChatClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MailOutline,
            contentDescription = "test",
            modifier = Modifier.size(160.dp),
            tint = Color(0xFFD8D8D8)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Let’s start chatting!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(
            onClick = onStartChatClick
        ) {
            Text(
                text = "Start a chat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2979FF)
            )
        }
    }
}