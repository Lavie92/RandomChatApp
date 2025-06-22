package com.example.randomchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.randomchat.ui.navigation.AppNavHost
import com.example.randomchat.ui.theme.RandomChatTheme

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