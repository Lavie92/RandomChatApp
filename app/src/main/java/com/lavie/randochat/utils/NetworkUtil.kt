package com.lavie.randochat.utils

import kotlinx.coroutines.TimeoutCancellationException

fun isNetworkError(exception: Exception): Boolean {

    val message = exception.message?.lowercase() ?: ""

    return message.contains("network") ||
            message.contains("timeout") ||
            message.contains("connection") ||
            message.contains("unreachable") ||
            exception is TimeoutCancellationException
}