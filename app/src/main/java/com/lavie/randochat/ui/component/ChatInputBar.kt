package com.lavie.randochat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens

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
                .padding(Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        ImageButton(
            onVoiceRecord, R.drawable.vector_voice, modifier = Modifier
                .padding(Dimens.baseMargin)
                .width(Dimens.baseIconSize)
        )

        CustomSpacer(width = Dimens.smallMargin)

        CustomChatTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Dimens.smallMargin)
        )

        Spacer(modifier = Modifier.width(Dimens.smallMargin))

        ImageButton(
            onSend,
            R.drawable.vector_send,
            modifier = Modifier
                .width(Dimens.sendButtonWidth)
                .padding(end = Dimens.baseMargin)
        )
    }
}
