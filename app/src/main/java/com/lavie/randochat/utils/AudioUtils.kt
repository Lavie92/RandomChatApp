package com.lavie.randochat.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadUrlToTempFile(context: Context, url: String): File? = withContext(Dispatchers.IO) {
    var tempFile: File? = null
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val inputStream = connection.inputStream
        tempFile = File.createTempFile(Constants.TEMP_AUDIO_FILE_PREFIX, Constants.AUDIO_FILE_EXTENSION, context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        tempFile?.delete()
        null
    }
}

fun uriToTempFile(context: Context, uriString: String): File? {
    var tempFile: File? = null
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        tempFile = File.createTempFile(Constants.TEMP_AUDIO_FILE_PREFIX, Constants.AUDIO_FILE_EXTENSION, context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        tempFile?.delete()
        null
    }
}

suspend fun resolveAudioFile(context: Context, uriOrUrl: String): File? {
    return if (uriOrUrl.startsWith(Constants.HTTP_PREFIX)){
        downloadUrlToTempFile(context, uriOrUrl)
    } else {
        uriToTempFile(context, uriOrUrl)
    }
}

fun getAudioDuration(file: File): String {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        retriever.release()

        val seconds = (duration / Constants.MILLISECONDS_PER_SECOND) % Constants.SECONDS_PER_MINUTE
        val minutes = (duration / (Constants.MILLISECONDS_PER_SECOND * Constants.SECONDS_PER_MINUTE)) % Constants.SECONDS_PER_MINUTE
        String.format(Constants.TIME_FORMAT, minutes, seconds)
    } catch (e: Exception) {
        Constants.DEFAULT_TIME_DISPLAY
    }
}

fun getAudioDurationMs(file: File): Long {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        retriever.release()
        duration
    } catch (e: Exception) {
        Constants.ZERO_LONG
    }
}

fun formatMillis(ms: Long): String {
    val totalSec = ms / Constants.MILLISECONDS_PER_SECOND
    val minutes = totalSec / Constants.SECONDS_PER_MINUTE
    val seconds = totalSec % Constants.SECONDS_PER_MINUTE
    return String.format(Constants.TIME_FORMAT, minutes, seconds)
}

fun startVoicePlayback(
    context: Context,
    file: File,
    mediaPlayer: MediaPlayer,
    scope: CoroutineScope,
    isPlaying: MutableState<Boolean>,
    lastPlaybackPosition: MutableState<Int>,
    displayTime: MutableState<String>,
    durationText: String,
    onError: () -> Unit = {}
) {
    try {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()
        mediaPlayer.seekTo(lastPlaybackPosition.value)
        mediaPlayer.start()
        isPlaying.value = true

        scope.launch {
            while (isPlaying.value && mediaPlayer.isPlaying) {
                delay(Constants.VOICE_RECORD_DELAY)
                val current = mediaPlayer.currentPosition
                displayTime.value = formatMillis(current.toLong())
            }
        }

        mediaPlayer.setOnCompletionListener {
            isPlaying.value = false
            lastPlaybackPosition.value = Constants.DEFAULT_PLAYBACK_POSITION
            displayTime.value = durationText
        }
    } catch (_: Exception) {
        isPlaying.value = false
        onError()
    }
}
