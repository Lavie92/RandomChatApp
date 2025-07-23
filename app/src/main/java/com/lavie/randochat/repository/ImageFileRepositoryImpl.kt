package com.lavie.randochat.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.customToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
                    customToast(context, R.string.image_saved)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    customToast(context, R.string.download_failed)
                }
            }
        }
    }

    override suspend fun compressImage(context: Context, uri: Uri): File {
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            val fileSizeKb = compressedFile.length() / 1024
            Timber.d("⏱️ [Repo] Compressed file size: $fileSizeKb KB")
            compressedFile
        }
    }

    override suspend fun uploadImageToCloudinary(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cloudName = "durhoy6iq"
            val uploadPreset = "Chat_Rando"
            val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { fileOut ->
                    input.copyTo(fileOut)
                }
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                val jsonObj = org.json.JSONObject(json)
                Result.success(jsonObj.getString("secure_url"))
            } else {
                Result.failure(Exception("Cloudinary upload failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
