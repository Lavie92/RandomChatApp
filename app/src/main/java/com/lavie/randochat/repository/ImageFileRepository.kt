package com.lavie.randochat.repository

import android.content.Context

interface ImageFileRepository {
    fun saveImageToGallery(context: Context, imageUrl: String)
}
