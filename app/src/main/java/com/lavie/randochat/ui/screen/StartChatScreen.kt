package com.lavie.randochat.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.ui.theme.Dimens.baseMargin
import com.lavie.randochat.ui.theme.Dimens.buttonRadius
import com.lavie.randochat.ui.theme.Dimens.textFieldHeight
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.singleClickHandler
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

    var chatType by remember { mutableStateOf(ChatType.RANDOM) }

    val activity = context as? Activity

    BackHandler {
        activity?.finish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.baseMarginDouble),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomSpacer(height = Dimens.baseMarginDouble)

        Icon(
            painter = painterResource(id = R.drawable.vector_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = stringResource(R.string.welcome_to_rando_chat),
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(top = baseMargin)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            ChatType.values().forEach { type ->
                val label = when (type) {
                    ChatType.RANDOM -> stringResource(R.string.global_random_chat)
                    ChatType.LOCATION -> stringResource(R.string.chat_nearby)
                    ChatType.AGE -> stringResource(R.string.chat_by_age)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = baseMargin)
                ) {
                    RadioButton(
                        selected = chatType == type,
                        onClick = { chatType = type }
                    )
                    Spacer(modifier = Modifier.width(baseMargin))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Button(
            onClick = singleClickHandler {
                if (myUserId != null) {
                    navController.navigate("${Constants.SPLASH_SCREEN}/${Constants.SPLASH_MODE_MATCHING}/${R.string.matching}")
                    matchViewModel.startMatching(myUserId, chatType)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight),
            shape = RoundedCornerShape(buttonRadius)
        ) {
            Text(text = stringResource(R.string.start_matching))
        }

        CustomSpacer(height = Dimens.baseMarginDouble)
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