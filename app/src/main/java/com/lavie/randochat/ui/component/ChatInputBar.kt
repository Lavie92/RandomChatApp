package com.lavie.randochat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.delay
import java.io.File

sealed class VoiceRecordState {
    data object Recording : VoiceRecordState()
    data object Idle : VoiceRecordState()
    data class Recorded(val file: File) : VoiceRecordState()
    data object Locked : VoiceRecordState()
}

@Composable
fun ChatInputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSendImage: () -> Unit,
    voiceRecordState: VoiceRecordState,
    onVoiceRecordStart: () -> Unit,
    onVoiceRecordStop: () -> Unit,
    onVoiceRecordCancel: () -> Unit,
    onSend: () -> Unit,
    onLikeClick: () -> Unit,
    onEndChatClick: () -> Unit,
    onVoiceRecordSend: () -> Unit,
    onToggleEmojiPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentTime by remember { mutableLongStateOf(startTime) }
    val elapsedSeconds = ((currentTime - startTime) / Constants.MILLISECONDS_PER_SECOND).coerceAtLeast(0L)

    LaunchedEffect(voiceRecordState) {
        if (voiceRecordState is VoiceRecordState.Recording) {
            startTime = System.currentTimeMillis()
            while (true) {
                delay(Constants.VOICE_RECORD_DELAY)
                currentTime = System.currentTimeMillis()

                val elapsed = (currentTime - startTime) / Constants.MILLISECONDS_PER_SECOND
                if (elapsed >= Constants.VOICE_RECORD_DELAY_MAX_SECOND) {
                    onVoiceRecordStop()
                    break
                }
            }
        }
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        when (voiceRecordState) {
            is VoiceRecordState.Recording -> VoiceRecordingBar(
                elapsedSeconds = elapsedSeconds,
                onStop = onVoiceRecordStop,
                onCancel = onVoiceRecordCancel,
                onSend = onVoiceRecordSend
            )

            is VoiceRecordState.Recorded -> VoiceRecordedBar(
                file = voiceRecordState.file,
                onCancel = onVoiceRecordCancel,
                onSend = {
                    onVoiceRecordStop()
                    onVoiceRecordSend()
                }
            )
            VoiceRecordState.Idle -> IdleInputBar(
                value = value,
                onValueChange = onValueChange,
                onSendImage = onSendImage,
                onVoiceRecordStart = onVoiceRecordStart,
                onSend = onSend,
                onLikeClick = onLikeClick,
                onEndChatClick = onEndChatClick,
                onToggleEmojiPicker = onToggleEmojiPicker
            )
            VoiceRecordState.Locked -> LockedVoiceBar()
        }
    }
}
