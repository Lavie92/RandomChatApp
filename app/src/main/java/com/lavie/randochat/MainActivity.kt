package com.lavie.randochat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lavie.randochat.ui.navigation.AppNavHost
import com.lavie.randochat.ui.theme.RandomChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomChatTheme {
                AppNavHost()
            }
        }
    }
}