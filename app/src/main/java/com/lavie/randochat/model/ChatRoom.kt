package com.lavie.randochat.model

import com.lavie.randochat.utils.ChatType

data class ChatRoom(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val lastMessage: String = "",
    val lastUpdated: Long = 0L,
    val isActive: Boolean = true,
    val chatType: ChatType = ChatType.RANDOM
)