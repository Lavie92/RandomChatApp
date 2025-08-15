package com.lavie.randochat.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

@Composable
fun ImageButton(
    onClick: () -> Unit,
    vectorId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    vectorColor: Color = Color.Unspecified,
) {
    if (vectorId == 0) {
        return
    }

    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(vectorId),
            contentDescription = null,
            tint = vectorColor
        )
    }
}

@Composable
fun ImageButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}