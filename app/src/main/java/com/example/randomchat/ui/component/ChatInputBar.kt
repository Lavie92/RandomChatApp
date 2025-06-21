package com.example.randomchat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.randomchat.R

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
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageButton(
            onSendImage, R.drawable.vector_image, modifier = Modifier
                .padding(8.dp)
                .width(20.dp)
        )

        ImageButton(
            onVoiceRecord, R.drawable.vector_voice, modifier = Modifier
                .padding(8.dp)
                .width(20.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))

        ImageButton(
            onSend,
            R.drawable.vector_send,
            modifier = Modifier
                .width(28.dp)
                .padding(end = 8.dp)
        )
    }
}
