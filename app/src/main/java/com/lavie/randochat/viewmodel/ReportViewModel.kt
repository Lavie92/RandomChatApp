package com.lavie.randochat.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.model.EvidenceMessage
import com.lavie.randochat.model.Report
import com.lavie.randochat.model.Message
import com.lavie.randochat.repository.ImageFileRepository
import com.lavie.randochat.repository.ReportRepository
import com.lavie.randochat.utils.ReportReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val imageFileRepository: ImageFileRepository
) : ViewModel() {

    private val _selectedReason = MutableStateFlow<ReportReason?>(null)
    val selectedReason: StateFlow<ReportReason?> = _selectedReason

    private val _selectedMessages = MutableStateFlow<List<Message>>(emptyList())
    val selectedMessages: StateFlow<List<Message>> = _selectedMessages

    private val _screenshotUris = MutableStateFlow<List<Uri>>(emptyList())
    val screenshotUris: StateFlow<List<Uri>> = _screenshotUris

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    fun selectReason(reason: ReportReason) {
        _selectedReason.value = reason
    }

    fun toggleMessageSelection(message: Message) {
        _selectedMessages.update { current ->
            if (current.any { it.id == message.id }) {
                current.filterNot { it.id == message.id }
            } else {
                current + message
            }
        }
    }

    fun addScreenshot(uri: Uri) {
        _screenshotUris.update { it + uri }
    }

    fun submitReport(
        context: Context,
        roomId: String,
        reporterId: String,
        onResult: (Boolean) -> Unit
    ) {
        val reason = _selectedReason.value ?: return
        val messages = _selectedMessages.value
        if (messages.isEmpty()) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val reportedUserId = reportRepository.getReportedUserId(roomId, reporterId)
            if (reportedUserId == null) {
                _isSubmitting.value = false
                onResult(false)
                return@launch
            }

            val screenshotUrls = mutableListOf<String>()
            for (uri in _screenshotUris.value) {
                val result = imageFileRepository.uploadImageToCloudinary(context, uri)
                result.getOrNull()?.let { screenshotUrls.add(it) }
            }

            val evidence = messages.map { msg ->
                EvidenceMessage(id = msg.id, senderId = msg.senderId, content = msg.content)
            }

            val report = Report(
                reporterId = reporterId,
                reportedUserId = reportedUserId,
                reason = reason.value,
                evidenceMessages = evidence,
                screenshotUrls = screenshotUrls
            )

            val submitResult = reportRepository.submitReport(report)
            _isSubmitting.value = false
            val success = submitResult.isSuccess
            if (success) {
                _selectedReason.value = null
                _selectedMessages.value = emptyList()
                _screenshotUris.value = emptyList()
            }
            onResult(success)
        }
    }
}
