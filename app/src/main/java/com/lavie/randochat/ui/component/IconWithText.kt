package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lavie.randochat.R

@Composable
fun IconWithText(
    onFacebookClick: () -> Unit,
    onGoogleClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ImageButton(
            onClick = onFacebookClick,
            vectorId = R.drawable.vector_facebook,
            enabled = enabled
        )
        ImageButton(
            onClick = onGoogleClick,
            vectorId = R.drawable.vector_google,
            enabled = enabled
        )
    }
}
