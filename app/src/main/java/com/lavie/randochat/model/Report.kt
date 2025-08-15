package com.lavie.randochat.model

data class Report(
    val id: String,
    val roomId: String,
    val reporterId: String,
    val reportedId: String,
    val reason: String,
    val note: String,
    val screenshotUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
