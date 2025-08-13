package com.lavie.randochat.model

import java.util.UUID

/**
 * Domain model representing a user report
 */
data class Report(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String,
    val reportedUserId: String,
    val reason: String,
    val evidenceMessages: List<EvidenceMessage>,
    val screenshotUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
