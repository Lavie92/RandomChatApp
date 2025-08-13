package com.lavie.randochat.repository

import com.lavie.randochat.model.Report

interface ReportRepository {
    suspend fun getReportedUserId(roomId: String, reporterId: String): String?
    suspend fun submitReport(report: Report): Result<Unit>
}
