package com.lavie.randochat.ui.component

import android.content.Context
import android.widget.Toast

fun customToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
