package com.lavie.randochat.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.lavie.randochat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ImageFileRepositoryImpl : ImageFileRepository {
    override fun saveImageToGallery(context: Context, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(imageUrl).openStream()
                val filename = "chat_image_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver
                var savedUri: Uri? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RandoChat")
                    }
                    savedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    savedUri?.let { uri ->
                        resolver.openOutputStream(uri).use { outputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/RandoChat")
                    if (!picturesDir.exists()) picturesDir.mkdirs()
                    val imageFile = File(picturesDir, filename)
                    FileOutputStream(imageFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), arrayOf("image/jpeg"), null)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.image_saved), Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.download_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
