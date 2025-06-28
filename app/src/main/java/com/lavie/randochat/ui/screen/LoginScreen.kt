package com.lavie.randochat.ui.screen

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val loginState by viewModel.loginState.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ImageButton(
            onClick = { viewModel.onGoogleLoginClick() },
            vectorId = R.drawable.vector_google,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }

    LaunchedEffect(loginState) {
        if (loginState != null) {
            navController.navigate("chat") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
        }
    }
}