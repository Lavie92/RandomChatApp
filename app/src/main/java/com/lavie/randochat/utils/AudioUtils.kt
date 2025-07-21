package com.lavie.randochat.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


suspend fun downloadUrlToTempFile(context: Context, url: String): File? = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = connection.inputStream
        val tempFile = File.createTempFile("temp_audio", ".m4a", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun uriToTempFile(context: Context, uriString: String): File? {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("temp_audio", ".m4a", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun resolveAudioFile(context: Context, uriOrUrl: String): File? {
    return if (uriOrUrl.startsWith("http")) {
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

        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        String.format("%d:%02d", minutes, seconds)
    } catch (e: Exception) {
        "0:00"
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
        0L
    }
}

fun formatMillis(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%d:%02d", minutes, seconds)
}
