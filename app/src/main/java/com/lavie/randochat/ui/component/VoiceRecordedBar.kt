package com.lavie.randochat.ui.component

import android.media.MediaPlayer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.getAudioDuration
import com.lavie.randochat.utils.startVoicePlayback
import java.io.File

@Composable
fun VoiceRecordedBar(
    file: File,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaPlayer = remember { MediaPlayer() }
    val isPlaying = remember { mutableStateOf(false) }
    val displayTime = remember { mutableStateOf("0:00") }
    var durationText by remember { mutableStateOf("0:00") }
    val lastPlaybackPosition = remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    LaunchedEffect(file) {
        durationText = getAudioDuration(file)
        displayTime.value = durationText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.baseMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VoiceCancelButton(
            onCancel
        )

        CustomSpacer(width = Dimens.baseMargin)

        VoiceBox(
            icon = if (isPlaying.value) Icons.Default.Stop else Icons.Default.PlayArrow,
            onIconClick = {
                if (isPlaying.value) {
                    mediaPlayer.pause()
                    isPlaying.value = false
                } else {
                    lastPlaybackPosition.intValue = 0
                    displayTime.value = durationText
                    startVoicePlayback(
                        file = file,
                        mediaPlayer = mediaPlayer,
                        scope = scope,
                        isPlaying = isPlaying,
                        lastPlaybackPosition = lastPlaybackPosition,
                        displayTime = displayTime,
                        durationText = durationText,
                        onError = { customToast(context, R.string.voice_playback_failed) }
                    )
                }
            },
            timeText = displayTime.value,
            modifier = Modifier.weight(1f)
        )

        CustomSpacer(width = Dimens.baseMargin)

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

@Composable
fun VoiceCancelButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(Dimens.baseIconSize),
        shape = CircleShape,
        color = Color(0xFF757575)
    ) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun VoiceBox(
    icon: ImageVector,
    onIconClick: () -> Unit,
    timeText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(Dimens.textFieldHeight),
        shape = RoundedCornerShape(Dimens.textFieldRadius),
        color = Color(0xFF007AFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.baseMargin),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(Dimens.baseIconSize),
                shape = CircleShape,
                color = Color.White
            ) {
                ImageButton(onClick = onIconClick, icon = icon)
            }

            CustomSpacer(modifier = Modifier.weight(1f))

            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .border(1.dp, Color.White, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = timeText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
