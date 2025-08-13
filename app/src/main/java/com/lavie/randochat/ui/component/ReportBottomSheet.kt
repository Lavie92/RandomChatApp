package com.lavie.randochat.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lavie.randochat.R
import com.lavie.randochat.model.Message
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.viewmodel.ReportViewModel
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.ReportReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    viewModel: ReportViewModel,
    roomId: String,
    reporterId: String,
    messages: List<Message>,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val selectedReason by viewModel.selectedReason.collectAsState()
    val selectedMessages by viewModel.selectedMessages.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addScreenshot(it) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(Dimens.baseMargin)) {
            Text(text = stringResource(R.string.report))
            Spacer(modifier = Modifier.height(Dimens.baseMargin))
            Text(text = stringResource(R.string.select_reason))
            ReportReason.values().forEach { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.smallMargin)
                ) {
                    RadioButton(
                        selected = selectedReason == reason,
                        onClick = { viewModel.selectReason(reason) }
                    )
                    Text(text = stringResource(id = reason.labelRes))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.baseMargin))
            Text(text = stringResource(R.string.select_messages))
            LazyColumn(modifier = Modifier.height(Dimens.reportListHeight)) {
                items(messages) { msg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.smallMargin)
                    ) {
                        Checkbox(
                            checked = selectedMessages.any { it.id == msg.id },
                            onCheckedChange = { viewModel.toggleMessageSelection(msg) }
                        )
                        Text(text = msg.content)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.baseMargin))
            Button(onClick = { imagePicker.launch(Constants.MIME_TYPE_IMAGE) }) {
                Text(text = stringResource(R.string.add_screenshot))
            }
            Spacer(modifier = Modifier.height(Dimens.baseMargin))
            Button(
                enabled = !isSubmitting,
                onClick = {
                    viewModel.submitReport(context, roomId, reporterId) { success ->
                        if (success) onSubmitted()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.submit))
            }
        }
    }
}
