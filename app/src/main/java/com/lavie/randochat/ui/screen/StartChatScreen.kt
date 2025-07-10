package com.lavie.randochat.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.onSingleClick
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.MatchViewModel

@Composable
fun StartChatScreen(
    navController: NavController,
    matchViewModel: MatchViewModel,
    authViewModel: AuthViewModel,
) {
    val myUser by authViewModel.loginState.collectAsState()
    val matchState by matchViewModel.matchState.collectAsState()

    val myUserId = myUser?.id
    val context = LocalContext.current
    val isMatching = matchState is MatchViewModel.MatchState.Matching || matchState is MatchViewModel.MatchState.Waiting

    //TODO Need to get value from user choice
    val chatType: ChatType = ChatType.RANDOM

    val activity = context as? Activity

    BackHandler {
        activity?.finish()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MailOutline,
            contentDescription = null,
            modifier = Modifier.size(Dimens.mailIcon),
            tint = Color(0xFFD8D8D8)
        )

        CustomSpacer(height = Dimens.baseSpacerHeight)

        Text(
            fontSize = Dimens.welcomeTextSize,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            text = stringResource(R.string.lets_start_chatting)
        )

        CustomSpacer(height = Dimens.baseSpacerHeight)

        TextButton(
            onClick = onSingleClick {
                if (myUserId != null) {
                    navController.navigate("${Constants.SPLASH_SCREEN}/${Constants.SPLASH_MODE_MATCHING}/${R.string.matching}")
                    matchViewModel.startMatching(myUserId, chatType)
                }
            }
        )
        {
            Text(
                text = stringResource(R.string.start_a_chat),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2979FF)
            )
        }
    }

    LaunchedEffect(matchState) {
        if (matchState is MatchViewModel.MatchState.Error) {
            val errorMsg = context.getString((matchState as MatchViewModel.MatchState.Error).messageResId)
            customToast(context, errorMsg)
        }
        if (matchState is MatchViewModel.MatchState.Matched) {
            val matched = matchState as MatchViewModel.MatchState.Matched
            navController.navigate("${Constants.CHAT_SCREEN}/${matched.roomId}") {
                popUpTo(Constants.WELCOME_SCREEN) { inclusive = true }
            }
        }
    }
}