package com.lavie.randochat.model

/**
 * DTO for report to store in Firebase
 */
data class ReportDto(
    val id: String? = null,
    val reporterId: String? = null,
    val reportedUserId: String? = null,
    val reason: String? = null,
    val evidenceMessages: List<EvidenceMessageDto>? = null,
    val screenshotUrls: List<String>? = null,
    val timestamp: Long? = null
)

data class EvidenceMessageDto(
    val id: String? = null,
    val senderId: String? = null,
    val content: String? = null
)
