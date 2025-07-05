package com.lavie.randochat.ui.component

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun customToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun customToast(context: Context, @StringRes messageId: Int) {
    Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT).show()
}
