package com.example.randomchat.ui.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.Dp
import com.example.randomchat.ui.theme.Dimens

@Composable
fun CustomSpacer(modifier: Modifier = Modifier, width: Dp = Dimens.emptySize, height: Dp = Dimens.baseSpacerHeight) {
    Spacer(modifier
        .width(width)
        .height(height))
}