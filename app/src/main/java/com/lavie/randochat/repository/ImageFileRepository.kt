package com.lavie.randochat.repository

import android.content.Context
import android.net.Uri
import java.io.File

interface ImageFileRepository {
    fun saveImageToGallery(context: Context,imageUrl: String,onResult: (Boolean) -> Unit)

    suspend fun compressImage(context: Context, uri: Uri): File

    suspend fun uploadImageToCloudinary(context: Context, uri: Uri): Result<String>
}
