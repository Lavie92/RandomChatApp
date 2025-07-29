package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.Constants
import java.util.Locale

@Composable
fun VoiceRecordingBar(
    elapsedSeconds: Long,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val minutes = elapsedSeconds / Constants.SECONDS_PER_MINUTE
    val seconds = elapsedSeconds % Constants.SECONDS_PER_MINUTE
    val formattedTime = String.format(Locale.US, Constants.TIME_FORMAT, minutes, seconds)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.baseMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VoiceCancelButton(onCancel)

        CustomSpacer(width = Dimens.baseMargin)

        VoiceBox(
            icon = Icons.Default.Stop,
            onIconClick = onStop,
            timeText = formattedTime,
            modifier = Modifier.weight(1f)
        )

        CustomSpacer(width = Dimens.baseMargin)

        ImageButton(
            onClick = {
                if (elapsedSeconds >= Constants.VOICE_RECORD_MIN_DURATION_TO_SEND) {
                    onStop()
                    onSend()
                }
            },
            vectorId = R.drawable.vector_send,
            vectorColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(Dimens.sendButtonWidth)
                .padding(end = Dimens.baseMargin)
        )
    }
}
