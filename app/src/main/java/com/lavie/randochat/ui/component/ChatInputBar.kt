package com.lavie.randochat.ui.component

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.formatMillis
import com.lavie.randochat.utils.getAudioDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

sealed class VoiceRecordState {
    object Recording : VoiceRecordState()
    object Idle : VoiceRecordState()
    data class Recorded(val file: File) : VoiceRecordState()
    object Locked : VoiceRecordState()
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendImage: () -> Unit,
    voiceRecordState: VoiceRecordState,
    onVoiceRecordStart: () -> Unit,
    onVoiceRecordStop: () -> Unit,
    onVoiceRecordCancel: () -> Unit,
    onSend: () -> Unit,
    onVoiceRecordSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentTime by remember { mutableStateOf(startTime) }
    remember { mutableStateOf(false) }

    LaunchedEffect(voiceRecordState) {
        if (voiceRecordState is VoiceRecordState.Recording) {
            startTime = System.currentTimeMillis()
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
                val elapsed = ((currentTime - startTime) / 1000L)
                if (elapsed >= 180L) {
                    onVoiceRecordStop()
                    break
                }
            }
        }
    }


    val elapsedSeconds = ((currentTime - startTime) / 1000L).coerceAtLeast(0L)
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        when (voiceRecordState) {
            VoiceRecordState.Recording -> {
                if (elapsedSeconds >= 180L) {
                    onVoiceRecordStop()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.baseMargin),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens.baseIconSize),
                        shape = CircleShape,
                        color = Color(0xFF757575)
                    ) {
                        IconButton(onClick = onVoiceRecordCancel) {
                            Icon(Icons.Default.Close, tint = Color.White, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimens.baseMargin))

                    Surface(
                        modifier = Modifier
                            .height(Dimens.textFieldHeight)
                            .weight(1f),
                        shape = RoundedCornerShape(Dimens.textFieldRadius),
                        color = Color(0xFF007AFF)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.baseMargin),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                modifier = Modifier.size(Dimens.baseIconSize),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                IconButton(onClick = onVoiceRecordStop) {
                                    Icon(Icons.Default.Stop, tint = Color(0xFF007AFF), contentDescription = null)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .border(1.dp, Color.White, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                shape = CircleShape,
                                color = Color.Transparent
                            ) {
                                val minutes = elapsedSeconds / 60
                                val seconds = elapsedSeconds % 60
                                Text(
                                    text = String.format("%d:%02d", minutes, seconds),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.width(Dimens.baseMargin))

                    ImageButton(
                        onClick = {
                            if (voiceRecordState is VoiceRecordState.Recording) {
                                if (elapsedSeconds >= 2L) {
                                    onVoiceRecordStop()
                                    onVoiceRecordSend()
                                }
                            }
                            if (voiceRecordState is VoiceRecordState.Recorded) {
                                onVoiceRecordSend()
                        }else {
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

            is VoiceRecordState.Recorded -> {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val mediaPlayer = remember { MediaPlayer() }
                val isPlaying = remember { mutableStateOf(false) }

                var displayTime by remember { mutableStateOf("0:00") }
                var durationText by remember { mutableStateOf("0:00") }

                LaunchedEffect(voiceRecordState) {
                    durationText = getAudioDuration(voiceRecordState.file)
                    displayTime = durationText
                }

                fun startPlayback(file: File) {
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(file.absolutePath)
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        isPlaying.value = true
                        scope.launch {
                            while (isPlaying.value && mediaPlayer.isPlaying) {
                                delay(1000)
                                displayTime = formatMillis(mediaPlayer.currentPosition.toLong())
                            }
                        }
                        mediaPlayer.setOnCompletionListener {
                            isPlaying.value = false
                            displayTime = durationText
                        }
                    } catch (e: Exception) {
                        isPlaying.value = false
                        customToast(context, R.string.voice_playback_failed)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.baseMargin),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens.baseIconSize),
                        shape = CircleShape,
                        color = Color(0xFF757575)
                    ) {
                        IconButton(onClick = onVoiceRecordCancel) {
                            Icon(Icons.Default.Close, tint = Color.White, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimens.baseMargin))

                    Surface(
                        modifier = Modifier
                            .height(Dimens.textFieldHeight)
                            .weight(1f),
                        shape = RoundedCornerShape(Dimens.textFieldRadius),
                        color = Color(0xFF007AFF)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.baseMargin),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                modifier = Modifier.size(Dimens.baseIconSize),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                IconButton(onClick = {
                                    if (isPlaying.value) {
                                        mediaPlayer.pause()
                                        isPlaying.value = false
                                    } else {
                                        startPlayback(voiceRecordState.file)
                                    }
                                }) {
                                    Icon(
                                        if (isPlaying.value) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        tint = Color(0xFF007AFF),
                                        contentDescription = null
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .border(1.dp, Color.White, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                shape = CircleShape,
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = displayTime,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
}
                    Spacer(modifier = Modifier.width(Dimens.baseMargin))

                    ImageButton(
                        onClick = {
                            if (true) {
                                onVoiceRecordStop()
                                onVoiceRecordSend()
                            } else {
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

            VoiceRecordState.Idle -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.baseMargin),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onVoiceRecordStart,
                        modifier = Modifier
                            .padding(end = Dimens.baseMargin)
                            .width(Dimens.baseIconSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.vector_voice),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    ImageButton(
                        onClick = onSendImage,
                        vectorId = R.drawable.vector_welcome_background,
                        modifier = Modifier
                            .padding(end = Dimens.baseMargin)
                            .width(Dimens.baseIconSize)
                    )

                    CustomChatTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(Dimens.smallMargin))

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

            VoiceRecordState.Locked -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.baseMargin),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.record_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

        }
    }

}



