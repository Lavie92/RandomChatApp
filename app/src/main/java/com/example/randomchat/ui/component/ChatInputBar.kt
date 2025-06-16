package com.example.randomchat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendImage: () -> Unit,
    onVoiceRecord: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SendImageButton(onClick = onSendImage)
        Spacer(modifier = Modifier.width(4.dp))
        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        VoiceRecorderButton(onClick = onVoiceRecord)
        Spacer(modifier = Modifier.width(2.dp))
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send Message",
                tint = if (value.isNotBlank()) Color(0xFF2979FF) else Color.Gray
            )
        }
    }
}
