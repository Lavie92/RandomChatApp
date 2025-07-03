package com.lavie.randochat.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 12.dp,
    containerColor: Color = Color(0xFFF8FAFC),
    borderColor: Color = Color(0xFFB0B0B0),
    textColor: Color = Color.Black,
    placeholderColor: Color = Color.Gray,
    cursorColor: Color = Color.Black,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = modifier,
        singleLine = true,
        maxLines = 1,
        shape = RoundedCornerShape(shapeRadius),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            disabledBorderColor = borderColor,
            errorBorderColor = borderColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = cursorColor,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            focusedLabelColor = Color.Transparent,
            unfocusedLabelColor = Color.Transparent,
            disabledLabelColor = Color.Transparent,
            errorLabelColor = Color.Transparent,
        )


    )
}
