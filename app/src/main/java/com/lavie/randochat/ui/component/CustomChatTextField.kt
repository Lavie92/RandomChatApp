package com.lavie.randochat.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lavie.randochat.R
import com.lavie.randochat.ui.theme.Dimens

@Composable
fun CustomChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                stringResource(R.string.type_a_message),
                style = MaterialTheme.typography.bodySmall
            )
        },
        shape = RoundedCornerShape(Dimens.textFieldRadius),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF2979FF),
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        modifier = modifier
            .height(Dimens.textFieldHeight)
    )
}

@Preview
@Composable
fun PreviewTextField() {
    CustomChatTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .padding(Dimens.smallMargin)
    )
}


