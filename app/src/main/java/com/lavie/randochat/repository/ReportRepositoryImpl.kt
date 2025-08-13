package com.lavie.randochat.repository

import com.google.firebase.database.DatabaseReference
import com.lavie.randochat.model.Report
import com.lavie.randochat.model.mapper.toDto
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ReportRepositoryImpl(
    private val database: DatabaseReference
) : ReportRepository {

    override suspend fun getReportedUserId(roomId: String, reporterId: String): String? {
        return try {
            val snapshot = database
                .child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.PARTICIPANTS_ID)
                .get()
                .await()
            snapshot.children.mapNotNull { it.getValue(String::class.java) }
                .firstOrNull { it != reporterId }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override suspend fun submitReport(report: Report): Result<Unit> {
        return try {
            val reportId = database.child(Constants.REPORTS).push().key
                ?: return Result.failure(Exception("No report id"))
            val dto = report.copy(id = reportId).toDto()
            database.child(Constants.REPORTS).child(reportId).setValue(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }
}
