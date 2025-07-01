package com.lavie.randochat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lavie.randochat.ui.screen.*
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {

        composable("login") { LoginScreen(navController, authViewModel) }

// Khi chưa có partner (chat chờ match)
        composable("chat") {
            val chatViewModel: ChatViewModel = koinViewModel()
            ChatScreen(navController, chatViewModel, authViewModel, partnerUserId = null.toString())
        }

        composable(
            "chat?partnerUserId={partnerUserId}",
            arguments = listOf(navArgument("partnerUserId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val partnerUserId = backStackEntry.arguments?.getString("partnerUserId")
            if (partnerUserId != null) {
                val chatViewModel: ChatViewModel = koinViewModel()

                ChatScreen(navController, chatViewModel, authViewModel, partnerUserId)
            }
        }

        composable("settings") { SettingScreen(navController) }
    }
}
