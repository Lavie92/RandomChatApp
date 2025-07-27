    package com.lavie.randochat.ui.component

    import android.media.MediaPlayer
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.lazy.grid.items
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.PlayArrow
    import androidx.compose.material.icons.filled.Stop
    import androidx.compose.material3.Divider
    import androidx.compose.material3.DropdownMenu
    import androidx.compose.material3.DropdownMenuItem
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
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import coil.compose.AsyncImage
    import com.lavie.randochat.R
    import com.lavie.randochat.ui.theme.Dimens
    import com.lavie.randochat.utils.getAudioDuration
    import com.lavie.randochat.utils.startVoicePlayback
    import kotlinx.coroutines.delay
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
        onLikeClick: () -> Unit,
        onReportClick: () -> Unit,
        onExitClick: () -> Unit,
        onVoiceRecordSend: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
        var currentTime by remember { mutableStateOf(startTime) }
        var menuExpanded by remember { mutableStateOf(false) }
        var emojiExpanded by remember { mutableStateOf(false) }

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
            // Emoji Picker
            if (emojiExpanded && voiceRecordState == VoiceRecordState.Idle) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        onValueChange(value + emoji)
                    },
                    onDismiss = { emojiExpanded = false }
                )
            }

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

                is VoiceRecordState.Recorded -> {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val mediaPlayer = remember { MediaPlayer() }
                    val isPlaying = remember { mutableStateOf(false) }
                    val lastPlaybackPosition = remember { mutableStateOf(0) }
                    val displayTime = remember { mutableStateOf("0:00") }
                    var durationText by remember { mutableStateOf("0:00") }

                    LaunchedEffect(voiceRecordState) {
                        durationText = getAudioDuration(voiceRecordState.file)
                        displayTime.value = durationText
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
                                            lastPlaybackPosition.value = 0
                                            displayTime.value = "0:00"
                                            startVoicePlayback(
                                                context = context,
                                                file = voiceRecordState.file,
                                                mediaPlayer = mediaPlayer,
                                                scope = scope,
                                                isPlaying = isPlaying,
                                                lastPlaybackPosition = lastPlaybackPosition,
                                                displayTime = displayTime,
                                                durationText = durationText,
                                                onError = { customToast(context, R.string.voice_playback_failed) }
                                            )
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
                                        text = displayTime.value,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(Dimens.baseMargin))

                        ImageButton(
                            onClick = {
                                onVoiceRecordStop()
                                onVoiceRecordSend()
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

                        Spacer(modifier = Modifier.width(Dimens.baseMarginDouble))

                        ImageButton(
                            onClick = onSendImage,
                            vectorId = R.drawable.vector_welcome_background,
                            modifier = Modifier
                                .padding(end = Dimens.baseMargin)
                                .width(Dimens.baseIconSize)
                        )

                        // Emoji Button
                        IconButton(
                            onClick = { emojiExpanded = !emojiExpanded },
                            modifier = Modifier
                                .padding(end = Dimens.baseMargin)
                                .width(Dimens.baseIconSize)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.placeholder), // Thay bằng vector emoji của bạn
                                contentDescription = "Emoji",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

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

    data class Emoji(
        val name: String,
        val imageUrl: String,
        val displayName: String
    )

    @Composable
    fun EmojiPicker(
        onEmojiSelected: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        // Danh sách Popo emoji từ GitHub repository
        val popoEmojiList = listOf(
            Emoji("popo_after_boom", "https://i.imgur.com/OSBToxi.png", "after boom"),
            Emoji("popo_ah", "https://i.imgur.com/WwOzTnn.png", "ah"),
            Emoji("popo_amazed", "https://i.imgur.com/5k5PtZs.png", "amazed"),
            Emoji("popo_angry", "https://i.imgur.com/fhlFBMe.png", "angry"),
            Emoji("popo_bad_smelly", "https://i.imgur.com/QZYLsIx.png", "bad smelly"),
            Emoji("popo_baffle", "https://i.imgur.com/WPDnrZE.png", "baffle"),
            Emoji("popo_beat_brick", "https://i.imgur.com/82hTVpU.png", "beat brick"),
            Emoji("popo_beat_plaster", "https://i.imgur.com/eOTZi9h.png", "beat plaster"),
            Emoji("popo_beat_shot", "https://i.imgur.com/1t0wClt.png", "beat shot"),
            Emoji("popo_beated", "https://i.imgur.com/gppyG81.png", "beated"),
            Emoji("popo_beauty", "https://i.imgur.com/MW0iDpD.png", "beauty"),
            Emoji("popo_big_smile", "https://i.imgur.com/ufXMdK9.png", "big smile"),
            Emoji("popo_boss", "https://i.imgur.com/Mar84O5.png", "boss"),
            Emoji("popo_burn_joss_stick", "https://i.imgur.com/z9ot2jb.png", "burn joss stick"),
            Emoji("popo_byebye", "https://i.imgur.com/gT5UVDg.png", "bye bye"),
            Emoji("popo_canny", "https://i.imgur.com/UPr0xKx.png", "canny"),
            Emoji("popo_choler", "https://i.imgur.com/sCH0UDC.png", "choler"),
            Emoji("popo_cold", "https://i.imgur.com/qAA41Yo.png", "cold"),
            Emoji("popo_confident", "https://i.imgur.com/DY04hge.png", "confident"),
            Emoji("popo_confuse", "https://i.imgur.com/YPCFWkw.png", "confuse"),
            Emoji("popo_cool", "https://i.imgur.com/0K1wrzZ.png", "cool"),
            Emoji("popo_cry", "https://i.imgur.com/4QZYYIC.png", "cry"),
            Emoji("popo_doubt", "https://i.imgur.com/lzHbzV1.png", "doubt"),
            Emoji("popo_dribble", "https://i.imgur.com/X5uZ93a.png", "dribble"),
            Emoji("popo_embarrassed", "https://i.imgur.com/YyRX2tG.png", "embarrassed"),
            Emoji("popo_extreme_sexy_girl", "https://i.imgur.com/haYEQW2.png", "extreme sexy girl"),
            Emoji("popo_feel_good", "https://i.imgur.com/xStugfC.png", "feel good"),
            Emoji("popo_go", "https://i.imgur.com/V5csBWd.png", "go"),
            Emoji("popo_haha", "https://i.imgur.com/sJzUVQV.png", "haha"),
            Emoji("popo_hell_boy", "https://i.imgur.com/XNaTnxM.png", "hell boy"),
            Emoji("popo_hungry", "https://i.imgur.com/LoyXegV.png", "hungry"),
            Emoji("popo_look_down", "https://i.imgur.com/pOe1P2B.png", "look down"),
            Emoji("popo_matrix", "https://i.imgur.com/FjAn1I2.png", "matrix"),
            Emoji("popo_misdoubt", "https://i.imgur.com/pH6xpRo.png", "misdoubt"),
            Emoji("popo_nosebleed", "https://i.imgur.com/4Z1RVAZ.png", "nosebleed"),
            Emoji("popo_oh", "https://i.imgur.com/k82XpMK.png", "oh"),
            Emoji("popo_ops", "https://i.imgur.com/OLaODha.png", "ops"),
            Emoji("popo_pudency", "https://i.imgur.com/Y6ZBwnf.png", "pudency"),
            Emoji("popo_rap", "https://i.imgur.com/5oPvtkJ.png", "rap"),
            Emoji("popo_sad", "https://i.imgur.com/sJIVd5I.png", "sad"),
            Emoji("popo_sexy_girl", "https://i.imgur.com/VgcppPF.png", "sexy girl"),
            Emoji("popo_shame", "https://i.imgur.com/VQUcio9.png", "shame"),
            Emoji("popo_smile", "https://i.imgur.com/Gyfx5fY.png", "smile"),
            Emoji("popo_spiderman", "https://i.imgur.com/x16WUY6.png", "spiderman"),
            Emoji("popo_still_dreaming", "https://i.imgur.com/qTAN0JP.png", "still dreaming"),
            Emoji("popo_sure", "https://i.imgur.com/aTBArr0.png", "sure"),
            Emoji("popo_surrender", "https://i.imgur.com/3FZkrnP.png", "surrender"),
            Emoji("popo_sweat", "https://i.imgur.com/c6fiw3M.png", "sweat"),
            Emoji("popo_sweet_kiss", "https://i.imgur.com/KzcQ36Q.png", "sweet kiss"),
            Emoji("popo_tire", "https://i.imgur.com/5NeIQas.png", "tire"),
            Emoji("popo_too_sad", "https://i.imgur.com/JJv8JGC.png", "too sad"),
            Emoji("popo_waaaht", "https://i.imgur.com/fNGW21s.png", "waaaht"),
            Emoji("popo_what", "https://i.imgur.com/1ryGK1k.png", "what")
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = Dimens.baseMargin),
            shape = RoundedCornerShape(Dimens.textFieldRadius),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column {
                // Header với nút đóng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popo Emoji",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Grid Popo emoji
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(popoEmojiList) { popoEmoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    // Trả về format [emoji_name] để dễ parse sau này
                                    onEmojiSelected("[:${popoEmoji.name}:]")
                                    onDismiss()
                                }
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = popoEmoji.imageUrl,
                                contentDescription = popoEmoji.displayName,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(R.drawable.placeholder), // Thêm placeholder nếu cần
                                error = painterResource(R.drawable.placeholder) // Thêm error icon nếu cần
                            )
                        }
                    }
                }
            }
        }
    }