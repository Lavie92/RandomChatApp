package com.lavie.randochat.datamodel

class ChatScreenDataModel (
        val messageText: String,
        val onMessageTextChanged: (String) -> Unit,
        val onSendMessage: () -> Unit,
        val onSendImage: () -> Unit,
        val onSendHeart: () -> Unit,
        val onReport: () -> Unit,
        val onExitChat: () -> Unit,
        val onVoiceRecordStart: () -> Unit,
        val onVoiceRecordStop: () -> Unit,
        val onVoiceRecordCancel: () -> Unit,
        val onVoiceRecordSend: () -> Unit
)