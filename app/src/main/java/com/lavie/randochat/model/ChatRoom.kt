package com.lavie.randochat.model

data class ChatRoom(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = 0L,
    val status: ChatStatus
)
