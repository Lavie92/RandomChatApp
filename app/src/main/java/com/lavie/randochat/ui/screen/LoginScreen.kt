package com.lavie.randochat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val errorMessageId by viewModel.errorMessageId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)
    val progressMessageId by viewModel.progressMessageId.collectAsState()
    val errorMsg: String? = errorMessageId?.let { stringResource(it) }
    val progressMessage: String? = progressMessageId?.let { stringResource(it) }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        ImageButton(
            onClick = {
                if (!isLoading) {
                    viewModel.onGoogleLoginClick()
                }
            },
            vectorId = R.drawable.vector_google,
            modifier = Modifier.align(alignment = Alignment.Center),
            enabled = !isLoading
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                progressMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AuthViewModel.NavigationEvent.NavigateToStartChat -> {
                    navController.navigate(Constants.START_CHAT_SCREEN) {
                        popUpTo(Constants.LOGIN_SCREEN) { inclusive = true }
                    }
                }
                is AuthViewModel.NavigationEvent.NavigateToChat -> {
                    navController.navigate("${Constants.CHAT_SCREEN}?${Constants.PARTNER_USER_ID}=${event.partnerId}") {
                        popUpTo(Constants.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            }
        }
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            customToast(context, errorMsg)
        }
    }
}