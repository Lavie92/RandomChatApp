package com.lavie.randochat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendImage: () -> Unit,
    onVoiceRecord: () -> Unit,
    onSend: () -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = Dimens.baseMargin, vertical = Dimens.smallMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .align(Alignment.CenterVertically)
        ) {
            ImageButton(
                onClick = { menuExpanded = true },
                icon = Icons.Default.Add,
                modifier = Modifier.width(Dimens.baseIconSize),
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                    text = { Text(stringResource(R.string.leave_chat)) },
                    onClick = {
                        menuExpanded = false
                        onExitClick()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.baseMarginDouble))

        ImageButton(
            onClick = onSendImage,
            vectorId = R.drawable.vector_welcome_background,
            modifier = Modifier.width(Dimens.baseIconSize)
        )

        Spacer(modifier = Modifier.width(Dimens.baseMarginDouble))

        ImageButton(
            onClick = onVoiceRecord,
            vectorId = R.drawable.vector_voice,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(Dimens.baseIconSize)
        )

        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )

        ImageButton(
            onClick = onSend,
            vectorId = R.drawable.vector_send,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(Dimens.sendButtonWidth)
        )
    }
}
