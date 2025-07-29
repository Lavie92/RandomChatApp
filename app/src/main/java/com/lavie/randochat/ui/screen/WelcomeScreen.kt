package com.lavie.randochat.ui.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val loginState by authViewModel.loginState.collectAsState()
    val activeRoom by authViewModel.activeRoom.collectAsState()

    LaunchedEffect(loginState, activeRoom) {
        if (loginState != null) {
            if (activeRoom != null) {
                val roomId = activeRoom!!.id

                navController.navigate("${Constants.CHAT_SCREEN}/${roomId}") {
                    popUpTo(Constants.WELCOME_SCREEN) { inclusive = true }
                }
            } else {
                navController.navigate(Constants.START_CHAT_SCREEN) {
                    popUpTo(Constants.WELCOME_SCREEN) { inclusive = true }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomSpacer(height = 16.dp)

        Image(
            painter = painterResource(id = R.drawable.vector_welcome_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        CustomSpacer(height = 16.dp)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.vector_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(64.dp)
            )
            CustomSpacer(height = Dimens.baseMargin)

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Rando")
                    }
                },
                style = MaterialTheme.typography.headlineSmall
            )
        }

        CustomSpacer(height = 32.dp)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.login))
            }

            CustomSpacer(height = 12.dp)

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.dp, Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1A1A1A)
                )
            ) {
                Text(stringResource(R.string.register))
            }
        }
    }
}
