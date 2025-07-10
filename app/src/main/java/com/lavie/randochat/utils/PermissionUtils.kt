package com.lavie.randochat.utils

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermissionUtils(
    private val activity: FragmentActivity
) {
    private var onResult: ((Boolean) -> Unit)? = null

    val permissionLauncher: ActivityResultLauncher<String> by lazy {
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onResult?.invoke(isGranted)
        }
    }

    fun requestPermission(permission: String, onResult: (Boolean) -> Unit) {
        this.onResult = onResult
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            onResult(true)
        } else {
            permissionLauncher.launch(permission)
        }
    }
}
