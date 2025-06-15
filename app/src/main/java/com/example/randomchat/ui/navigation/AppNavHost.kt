package com.example.randomchat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.randomchat.ui.screen.*

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "chat") {

        composable("login") { LoginScreen(navController) }

        composable("chat") { ChatScreen(navController) }

        composable("settings") { SettingScreen(navController) }
    }
}
