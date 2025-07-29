package com.lavie.randochat.service

import androidx.compose.runtime.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

object DialogService {
    private var isVisible by mutableStateOf(false)

    private var titleText by mutableStateOf("")
    private var messageText by mutableStateOf("")
    private var confirmText by mutableStateOf("")
    private var dismissText by mutableStateOf("")

    private var onConfirm: (() -> Unit)? = null
    private var onDismiss: (() -> Unit)? = null

    fun show(
        title: String,
        message: String,
        confirmButton: String = "",
        dismissButton: String = "",
        onConfirmAction: () -> Unit,
        onDismissAction: (() -> Unit)? = null,
    ) {
        titleText = title
        messageText = message
        confirmText = confirmButton
        dismissText = dismissButton
        onConfirm = onConfirmAction
        onDismiss = onDismissAction
        isVisible = true
    }

    private fun dismiss() {
        isVisible = false
        onDismiss?.invoke()
        onConfirm = null
        onDismiss = null
    }

    @Composable
    fun Render() {
        if (!isVisible) return

        AlertDialog(
            onDismissRequest = { dismiss() },
            title = { Text(titleText) },
            text = { Text(messageText) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm?.invoke()
                    dismiss()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = { dismiss() }) {
                    Text(dismissText)
                }
            }
        )
    }
}
