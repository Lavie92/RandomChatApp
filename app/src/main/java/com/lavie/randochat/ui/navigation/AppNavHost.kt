package com.lavie.randochat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lavie.randochat.R
import com.lavie.randochat.ui.screen.ChatScreen
import com.lavie.randochat.ui.screen.ImagePreviewScreen
import com.lavie.randochat.ui.screen.LoginScreen
import com.lavie.randochat.ui.screen.PasswordChangedScreen
import com.lavie.randochat.ui.screen.RegisterScreen
import com.lavie.randochat.ui.screen.SettingScreen
import com.lavie.randochat.ui.screen.SplashScreen
import com.lavie.randochat.ui.screen.StartChatScreen
import com.lavie.randochat.ui.screen.WelcomeScreen
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.SplashType
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import com.lavie.randochat.viewmodel.MatchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val matchViewModel: MatchViewModel = koinViewModel()
    val chatViewModel: ChatViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = Constants.SPLASH_SCREEN_LOGIN) {

        composable(Constants.LOGIN_SCREEN) {
            LoginScreen(navController, authViewModel)
        }

        composable(Constants.REGISTER_SCREEN) {
            RegisterScreen(navController, authViewModel)
        }

        composable("password_changed") {
            PasswordChangedScreen(navController)
        }

        composable(Constants.WELCOME_SCREEN) {
            WelcomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                onLoginClick = { navController.navigate(Constants.LOGIN_SCREEN) },
                onRegisterClick = { navController.navigate(Constants.REGISTER_SCREEN) }
            )
        }

        composable(
            route = "${Constants.CHAT_SCREEN}/{${Constants.ROOM_ID}}",
            arguments = listOf(
                navArgument(Constants.ROOM_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(Constants.ROOM_ID)
            if (roomId != null) {
                ChatScreen(navController, chatViewModel, authViewModel, roomId)
            }
        }

        composable(Constants.SETTINGS_SCREEN) {
            SettingScreen(navController)
        }

        composable(Constants.START_CHAT_SCREEN) {
            StartChatScreen(
                navController = navController,
                matchViewModel = matchViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Constants.SPLASH_SCREEN_LOGIN) {
            SplashScreen(
                navController = navController,
                splashType = SplashType.LOGIN_CHECK,
                messageId = R.string.splash_connecting,
                authViewModel = authViewModel
            )
        }

        composable(
            route = "${Constants.SPLASH_SCREEN}/{${Constants.MODE}}/{${Constants.MESSAGE_ID}}",
            arguments = listOf(
                navArgument(Constants.MODE) { type = NavType.StringType },
                navArgument(Constants.MESSAGE_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(Constants.MODE) ?: Constants.SPLASH_MODE_LOGIN
            val messageId = backStackEntry.arguments?.getInt(Constants.MESSAGE_ID) ?: R.string.signing_in
            val splashType = when (mode) {
                Constants.SPLASH_MODE_MATCHING -> SplashType.MATCHING
                else -> SplashType.LOGIN_CHECK
            }

            SplashScreen(
                navController = navController,
                splashType = splashType,
                messageId = messageId,
                authViewModel = authViewModel,
                matchViewModel = matchViewModel
            )
        }

        composable("imagePreview/{imageUrl}") { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl")
            imageUrl?.let {
                ImagePreviewScreen(
                    imageUrl = it,
                    navController = navController,
                    chatViewModel = chatViewModel
                )
            }

        }


    }
}
