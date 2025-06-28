package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.lavie.randochat.ui.theme.Dimens

@Composable
fun ImageButtonWithText(
    onClick: () -> Unit,
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4267B2),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(Dimens.buttonRadius),
        enabled = enabled,
        modifier = modifier
            .padding(vertical = Dimens.baseMargin)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.width(Dimens.baseIconSize)
        )
        Spacer(modifier = Modifier.width(Dimens.baseMargin))
        Text(text)
    }
}
