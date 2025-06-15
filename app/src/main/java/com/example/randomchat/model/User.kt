package com.example.randomchat.model

data class User(
    val id: String = "",
    val nickname: String = "",
    val isOnline: Boolean = false,
    val lastActive: Long = 0L
)
