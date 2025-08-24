package com.lavie.randochat.model

data class User(
    val id: String = "",
    val nickname: String = "",
    val isOnline: Boolean = false,
    val email: String = "",
    val isDisabled: Boolean = false,
    val citizenScore: Int = 0,
    val imageCredit: Int = 0,
    val fcmTokens: List<String> = emptyList()
)
