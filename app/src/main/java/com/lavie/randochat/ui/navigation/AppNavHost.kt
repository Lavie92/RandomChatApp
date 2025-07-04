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

    NavHost(navController, startDestination = Constants.LOGIN_SCREEN) {

        composable(Constants.LOGIN_SCREEN) { LoginScreen(navController, authViewModel) }

        composable(
            route = "chat?partnerUserId={partnerUserId}",
            arguments = listOf(
                navArgument("partnerUserId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val partnerUserId = backStackEntry.arguments?.getString("partnerUserId")
            ChatScreen(navController, partnerUserId, authViewModel)
        }

        composable("settings") { SettingScreen(navController) }

        composable("welcome") { StartChatScreen(navController, matchViewModel, authViewModel) }

        composable(Constants.START_CHAT_SCREEN) { StartChatScreen(navController, matchViewModel, authViewModel) }
    }
}
