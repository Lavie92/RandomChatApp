package com.lavie.randochat.ui.component

import android.media.MediaPlayer
import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.lavie.randochat.R
import com.lavie.randochat.model.Emoji
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.ui.theme.MessageBackground
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.CommonUtils.parseMessageWithEmojis
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import com.lavie.randochat.utils.formatMillis
import com.lavie.randochat.utils.getAudioDurationMs
import com.lavie.randochat.utils.resolveAudioFile
import com.lavie.randochat.utils.startVoicePlayback
import com.lavie.randochat.viewmodel.EmojiViewModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessageBubble(
    content: String,
    isMe: Boolean,
    type: MessageType,
    time: Long,
    status: MessageStatus,
    showStatus: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    navController: NavController
) {
    val emojiViewModel: EmojiViewModel = koinViewModel()
    val emojiList by emojiViewModel.emojis.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.smallMargin, horizontal = Dimens.baseMarginDouble)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MessageBackground else Color.Transparent,
            shape = RoundedCornerShape(
                topStart = Dimens.baseMarginDouble,
                topEnd = Dimens.baseMarginDouble,
                bottomEnd = if (isMe) Dimens.emptySize else Dimens.baseMarginDouble,
                bottomStart = if (isMe) Dimens.baseMarginDouble else Dimens.emptySize
            ),
            border = if (!isMe && type != MessageType.VOICE)
                BorderStroke(Dimens.smallBorderStrokeWidth, Color.LightGray)
            else null
        ) {
            when (type) {
                MessageType.TEXT -> {
                    val parsedContent = remember(content, emojiList) {
                        parseMessageWithEmojis(content, emojiList)
                    }

                    if (parsedContent.any { it is MessageComponent.EmojiComponent }) {
                        RichTextMessage(
                            components = parsedContent,
                            isMe = isMe,
                            modifier = Modifier
                                .padding(horizontal = Dimens.baseMarginDouble, vertical = Dimens.baseMargin)
                                .widthIn(max = Dimens.messageMaxSizeable)
                        )
                    } else {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(horizontal = Dimens.baseMarginDouble, vertical = Dimens.baseMargin)
                                .widthIn(max = Dimens.messageMaxSizeable)
                        )
                    }
                }

                MessageType.IMAGE -> {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val density = LocalDensity.current
                        val sizePx = with(density) { 200.dp.toPx().toInt() }

                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    if (isMe) {
                                        Glide.with(this)
                                            .load(content)
                                            .placeholder(R.drawable.placeholder)
                                            .into(this)
                                    } else {
                                        Glide.with(this)
                                            .load(content)
                                            .placeholder(R.drawable.placeholder)
                                            .transform(BlurTransformation(25, 3))
                                            .into(this)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    navController.navigate("${Constants.ROUTE_IMAGE_PREVIEW}/${Uri.encode(content)}")
                                }
                        )

                        if (status == MessageStatus.SENDING) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.sending),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                MessageType.VOICE -> {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val mediaPlayer = remember { MediaPlayer() }
                    val isPlaying = remember { mutableStateOf(false) }
                    val displayTime = remember { mutableStateOf(Constants.DEFAULT_TIME_DISPLAY) }
                    val lastPlaybackPosition = remember { mutableIntStateOf(Constants.DEFAULT_PLAYBACK_POSITION) }
                    var durationText by remember { mutableStateOf(Constants.DEFAULT_TIME_DISPLAY) }
                    var durationMs by remember { mutableLongStateOf(Constants.DEFAULT_DURATION_MS) }

                    LaunchedEffect(content) {
                        scope.launch {
                            val file = resolveAudioFile(context, content)
                            if (file != null) {
                                durationMs = getAudioDurationMs(file)
                                durationText = formatMillis(durationMs)
                                displayTime.value = durationText
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MessageBackground)
                            .clickable {
                                scope.launch {
                                    val file = resolveAudioFile(context, content)
                                    if (file != null) {
                                        if (isPlaying.value) {
                                            lastPlaybackPosition.intValue = mediaPlayer.currentPosition
                                            mediaPlayer.pause()
                                            isPlaying.value = false
                                        } else {
                                            displayTime.value = formatMillis(lastPlaybackPosition.intValue.toLong())
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
                                    }
                                }
                            },
                        color = Color(0xFF007AFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying.value) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayTime.value, color = Color.White)
                        }
                    }
                }
            }
        }

        if (isSelected) {
            Text(
                text = CommonUtils.formatToTime(time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.smallMargin, end = Dimens.baseMarginDouble)
            )
        } else {
            if (showStatus) {
                Text(
                    text = when (status) {
                        MessageStatus.SENT -> stringResource(R.string.message_sent)
                        MessageStatus.SEEN -> stringResource(R.string.message_seen)
                        MessageStatus.SENDING -> stringResource(R.string.message_sending)
                        MessageStatus.FAILED -> stringResource(R.string.play_audio_failed)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        top = Dimens.smallMargin,
                        end = Dimens.baseMarginDouble
                    )
                )
            }
        }
    }
}

sealed class MessageComponent {
    data class TextComponent(val text: String) : MessageComponent()
    data class EmojiComponent(val emoji: Emoji) : MessageComponent()
}

@Composable
fun RichTextMessage(
    components: List<MessageComponent>,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        items(components) { component ->
            when (component) {
                is MessageComponent.TextComponent -> {
                    Text(
                        text = component.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                is MessageComponent.EmojiComponent -> {
                    AsyncImage(
                        model = component.emoji.imageUrl,
                        contentDescription = component.emoji.displayName,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder)
                    )
                }
            }
        }
    }
}