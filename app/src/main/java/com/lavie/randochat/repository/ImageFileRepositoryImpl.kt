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
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ImageFileRepositoryImpl(
    private val httpClient: OkHttpClient
) : ImageFileRepository {
    override fun saveImageToGallery(context: Context, imageUrl: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(imageUrl).openStream()
                val filename = "${Constants.IMAGE_FILE_PREFIX}${System.currentTimeMillis()}${Constants.IMAGE_FILE_EXTENSION}"
                val resolver = context.contentResolver
                var savedUri: Uri? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, Constants.IMAGE_MIME_TYPE)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + Constants.IMAGE_FOLDER_NAME)
                    }
                    savedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    savedUri?.let { uri ->
                        resolver.openOutputStream(uri).use { outputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + Constants.IMAGE_FOLDER_NAME
                    )
                    if (!picturesDir.exists()) picturesDir.mkdirs()
                    val imageFile = File(picturesDir, filename)
                    FileOutputStream(imageFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    MediaScannerConnection.scanFile(
                        context, arrayOf(imageFile.absolutePath), arrayOf(Constants.IMAGE_MIME_TYPE), null
                    )
                }
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    override suspend fun compressImage(context: Context, uri: Uri): File {
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            val filename = "${Constants.IMAGE_FILE_PREFIX}${System.currentTimeMillis()}${Constants.IMAGE_FILE_EXTENSION}"
            val compressedFile = File(context.cacheDir, filename)
            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESS_QUALITY, out)
            }
            compressedFile
        }
    }

    override suspend fun uploadImageToCloudinary(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            val uploadPreset = Constants.CLOUDINARY_IMAGE_UPLOAD_PRESET
            val url = Constants.CLOUDINARY_IMAGE_UPLOAD_URL

            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(
                    Exception(Constants.ERROR_CANNOT_OPEN_INPUT_STREAM.format(uri))
                )

            tempFile = File.createTempFile(Constants.TEMP_IMAGE_FILE_PREFIX, Constants.IMAGE_FILE_EXTENSION, context.cacheDir)
            inputStream.use { input ->
                tempFile.outputStream().use { fileOut ->
                    input.copyTo(fileOut)
                }
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.CLOUDINARY_FORM_KEY_FILE, tempFile.name, tempFile.asRequestBody(Constants.IMAGE_MIME_TYPE.toMediaTypeOrNull()))
                .addFormDataPart(Constants.CLOUDINARY_FORM_KEY_UPLOAD_PRESET, uploadPreset)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                val jsonObj = org.json.JSONObject(json)
                Result.success(jsonObj.getString(Constants.CLOUDINARY_RESPONSE_KEY_SECURE_URL))
            } else {
                Result.failure(Exception("${Constants.CLOUDINARY_ERROR_PREFIX}${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempFile?.delete()
        }
    }
}
