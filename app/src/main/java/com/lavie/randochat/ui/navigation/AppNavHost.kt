package com.lavie.randochat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lavie.randochat.ui.screen.*
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun AppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()


        composable("login") { LoginScreen(navController, authViewModel) }

        composable("chat") { ChatScreen(navController) }

        composable("settings") { SettingScreen(navController) }
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }

            )
        }

    }
}
