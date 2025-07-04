package com.lavie.randochat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lavie.randochat.ui.screen.*
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.MatchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val matchViewModel: MatchViewModel = koinViewModel()
    NavHost(navController, startDestination = Constants.WELCOME_SCREEN) {

        composable(Constants.LOGIN_SCREEN) { LoginScreen(navController, authViewModel) }

        composable("register") { RegisterScreen(navController, authViewModel) }

        composable("password_changed") {
            PasswordChangedScreen(navController)
        }

        composable(Constants.WELCOME_SCREEN) {
            WelcomeScreen(
                navController,
                authViewModel,
                onLoginClick = { navController.navigate(Constants.LOGIN_SCREEN) },
                onRegisterClick = { navController.navigate(Constants.REGISTER_SCREEN) }
            )
        }

        composable(
            route = "${Constants.CHAT_SCREEN}/{${Constants.PARTNER_USER_ID}}",
            arguments = listOf(
                navArgument(Constants.PARTNER_USER_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val partnerUserId = backStackEntry.arguments?.getString(Constants.PARTNER_USER_ID)
            ChatScreen(navController, partnerUserId)
        }

        composable(Constants.SETTINGS_SCREEN) { SettingScreen(navController) }

        composable(Constants.START_CHAT_SCREEN) {
            StartChatScreen(
                navController,
                matchViewModel,
                authViewModel
            )
        }
    }
}
