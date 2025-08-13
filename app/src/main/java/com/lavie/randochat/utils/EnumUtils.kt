package com.lavie.randochat.utils

import androidx.annotation.StringRes
import com.lavie.randochat.R
enum class ChatType {
    RANDOM,
    AGE,
    LOCATION
}

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE
}

enum class SplashType {
    LOGIN_CHECK,
    MATCHING
}

enum class MessageStatus {
    SENDING,
    SENT,
    SEEN,
    FAILED
}

enum class ReportReason(val value: String, @StringRes val labelRes: Int) {
    SPAM("spam", R.string.report_reason_spam),
    HARASSMENT("harassment", R.string.report_reason_harassment),
    INAPPROPRIATE("inappropriate", R.string.report_reason_inappropriate),
    OTHER("other", R.string.report_reason_other)
}