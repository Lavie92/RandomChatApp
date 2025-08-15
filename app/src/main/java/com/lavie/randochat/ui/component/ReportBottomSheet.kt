package com.lavie.randochat.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lavie.randochat.R
import com.lavie.randochat.utils.ReportReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    onPickImages: () -> Unit,
    selectedImageUris: List<Uri>,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    selectedReasonValue: String?,
    onReasonSelected: (String) -> Unit
    ) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.report_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.report_select_reason), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            ReportReason.entries.forEach { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedReasonValue == reason.value,
                        onClick = { onReasonSelected(reason.value) }
                    )
                    Text(text = stringResource(reason.labelRes))
                }
            }
            Spacer(Modifier.height(8.dp))

            LazyRow {
                items(selectedImageUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            TextButton(onClick = onPickImages) {
                Text(stringResource(R.string.report_pick_screenshot))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSubmit, enabled = selectedImageUris.isNotEmpty()) {
                Text(stringResource(R.string.report_submit))
            }
        }
    }
}