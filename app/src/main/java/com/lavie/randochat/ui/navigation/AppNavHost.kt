package com.lavie.randochat.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.screen.*
import com.lavie.randochat.ui.theme.Dimens
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import com.lavie.randochat.viewmodel.MatchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val matchViewModel: MatchViewModel = koinViewModel()
    val chatViewmodel: ChatViewModel = koinViewModel()

       NavHost(navController, startDestination = Constants.WELCOME_SCREEN) {


        composable(Constants.LOGIN_SCREEN) { LoginScreen(navController, authViewModel) }

        composable(Constants.REGISTER_SCREEN) { RegisterScreen(navController, authViewModel) }

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
            route = "${Constants.CHAT_SCREEN}/{${Constants.ROOM_ID}}",
            arguments = listOf(
                navArgument(Constants.ROOM_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(Constants.ROOM_ID)
            if (roomId != null) {
                ChatScreen(chatViewmodel, authViewModel, roomId)
            }
        }

        composable(Constants.SETTINGS_SCREEN) { SettingScreen(navController) }

        composable(Constants.START_CHAT_SCREEN) {
            StartChatScreen(
                navController,
                matchViewModel,
                authViewModel
            )
        }

           composable(Constants.MATCHING_LOADING_SCREEN) {
               SplashScreen(
                   navController = navController,
                   matchViewModel = matchViewModel,
                   contentAlignment = Alignment.BottomCenter,
                   content = {
                       Column(horizontalAlignment = Alignment.CenterHorizontally) {
                           Text(
                               text = stringResource(R.string.matching),
                               style = MaterialTheme.typography.titleMedium,
                               color = Color(0xFF2979FF)
                           )

                           CustomSpacer(height = Dimens.baseSpacerHeight)

                           TextButton(onClick = {
                               matchViewModel.cancelWaiting()
                               navController.popBackStack()
                           }) {
                               Text(
                                   text = stringResource(R.string.stop_matching),
                                   style = MaterialTheme.typography.titleMedium,
                                   color = Color(0xFF2979FF)
                               )
                           }
                       }
                   }
               )
           }



       }
}
