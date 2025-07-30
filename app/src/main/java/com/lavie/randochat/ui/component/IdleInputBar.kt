package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.viewmodel.EmojiViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IdleInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendImage: () -> Unit,
    onVoiceRecordStart: () -> Unit,
    onSend: () -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit,
    onEndChatClick: () -> Unit,
    onToggleEmojiPicker: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.baseMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Button
        Box(modifier = Modifier.padding(end = Dimens.baseMargin)) {
            ImageButton(
                onClick = { menuExpanded = true },
                icon = Icons.Default.Add,
                modifier = Modifier.width(Dimens.baseIconSize)
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_heart)) },
                    onClick = {
                        menuExpanded = false
                        onLikeClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.report)) },
                    onClick = {
                        menuExpanded = false
                        onReportClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.end_chat)) },
                    onClick = {
                        menuExpanded = false
                        onEndChatClick()
                    }
                )
            }
        }

        // Voice record
        ImageButton(
            onClick = onVoiceRecordStart,
            vectorId = R.drawable.vector_voice,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        CustomSpacer(width = Dimens.baseMargin)

        // Send image
        ImageButton(
            onClick = onSendImage,
            vectorId = R.drawable.vector_image,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        // Emoji picker
        IconButton(
            onClick = onToggleEmojiPicker,
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        ) {
            Icon(
                painter = painterResource(R.drawable.vector_facebook), // Replace with your emoji icon
                contentDescription = "Emoji",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Text input
        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )

        CustomSpacer(width = Dimens.smallMargin)

        // Send button
        ImageButton(
            onClick = onSend,
            vectorId = R.drawable.vector_send,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(Dimens.sendButtonWidth)
                .padding(end = Dimens.baseMargin)
        )
    }
}
