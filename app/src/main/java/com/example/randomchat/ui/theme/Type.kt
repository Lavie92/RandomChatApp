package com.example.randomchat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = Dimens.baseTextSize,
        lineHeight = Dimens.welcomeTextSize
    ),
    // Message timestamp
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = Dimens.smallTestSize,
        lineHeight = Dimens.baseTextSize
    ),
    // Username in chat
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.baseTextSize,
        lineHeight = Dimens.largeTextSize
    ),
    // Chat room title or main screen title
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = Dimens.largeTextSize,
        lineHeight = Dimens.titleLargeTextSize
    ),
    // Placeholder in message input box
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = Dimens.smallTestSize,
        lineHeight = Dimens.baseTextSize
    )
)