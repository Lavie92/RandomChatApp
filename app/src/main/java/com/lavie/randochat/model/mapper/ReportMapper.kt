package com.lavie.randochat.model.mapper

import com.lavie.randochat.model.EvidenceMessage
import com.lavie.randochat.model.EvidenceMessageDto
import com.lavie.randochat.model.Report
import com.lavie.randochat.model.ReportDto

fun Report.toDto(): ReportDto = ReportDto(
    id = id,
    reporterId = reporterId,
    reportedUserId = reportedUserId,
    reason = reason,
    evidenceMessages = evidenceMessages.map { it.toDto() },
    screenshotUrls = screenshotUrls,
    timestamp = timestamp
)

fun ReportDto.toDomain(): Report = Report(
    id = id ?: "",
    reporterId = reporterId ?: "",
    reportedUserId = reportedUserId ?: "",
    reason = reason ?: "",
    evidenceMessages = evidenceMessages?.map { it.toDomain() } ?: emptyList(),
    screenshotUrls = screenshotUrls ?: emptyList(),
    timestamp = timestamp ?: 0L
)

fun EvidenceMessage.toDto(): EvidenceMessageDto = EvidenceMessageDto(
    id = id,
    senderId = senderId,
    content = content
)

fun EvidenceMessageDto.toDomain(): EvidenceMessage = EvidenceMessage(
    id = id ?: "",
    senderId = senderId ?: "",
    content = content ?: ""
)
