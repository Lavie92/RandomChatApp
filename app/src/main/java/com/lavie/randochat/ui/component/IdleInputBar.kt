package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
fun IdleInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendImage: () -> Unit,
    onVoiceRecordStart: () -> Unit,
    onSend: () -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit,
    onEndChatClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.baseMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        ImageButton(
            onClick = onVoiceRecordStart,
            vectorId = R.drawable.vector_voice,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        CustomSpacer(width = Dimens.baseMargin)

        ImageButton(
            onClick = onSendImage,
            vectorId = R.drawable.vector_image,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )

        CustomSpacer(width = Dimens.smallMargin)

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
