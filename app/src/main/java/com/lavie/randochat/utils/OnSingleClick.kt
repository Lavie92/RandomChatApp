package com.lavie.randochat.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun onSingleClick(
    debounceTime: Long = Constants.SINGLE_CLICK_TIMEOUT,
    onClick: () -> Unit
): () -> Unit {
    var isClicked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    return {
        if (!isClicked) {
            isClicked = true
            onClick()
            scope.launch {
                delay(debounceTime)
                isClicked = false
            }
        }
    }
}
