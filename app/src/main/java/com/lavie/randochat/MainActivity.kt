package com.lavie.randochat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.lavie.randochat.ui.navigation.AppNavHost
import com.lavie.randochat.ui.theme.RandomChatTheme
import com.lavie.randochat.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = koinViewModel()
            val context = LocalContext.current

            val signInLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        Log.d("MainActivity", "Google Sign-In successful, idToken: ${idToken.take(20)}...")
                        authViewModel.loginWithGoogle(idToken)
                    } else {
                        Log.e("MainActivity", "ID Token is null")
                    }
                } catch (e: ApiException) {
                    Log.e("MainActivity", "Google sign in failed", e)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Unexpected error during Google sign in", e)
                }
            }

            LaunchedEffect(authViewModel) {
                authViewModel.signInRequest.collect {
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        signInLauncher.launch(googleSignInClient.signInIntent)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error launching Google Sign-In", e)
                    }
                }
            }

            RandomChatTheme {
                AppNavHost(authViewModel)
            }
        }
    }
}
