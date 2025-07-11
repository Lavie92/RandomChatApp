package com.lavie.randochat.ui.screen

import android.Manifest
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomOutlinedTextField
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val errorMessageId by viewModel.errorMessageId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)
    val progressMessageId by viewModel.progressMessageId.collectAsState()
    val errorMsg: String? = errorMessageId?.let { stringResource(it) }
    val progressMessage: String? = progressMessageId?.let { stringResource(it) }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomSpacer(height = 36.dp)

            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            CustomSpacer(height = 24.dp)

            CustomOutlinedTextField(
                value = email,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { email = it.trim() },
                placeholder = stringResource(R.string.enter_your_email),
                keyboardType = KeyboardType.Email
            )

            CustomSpacer(height = 12.dp)

            CustomOutlinedTextField(
                value = password,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { password = it },
                placeholder = stringResource(R.string.enter_your_password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            )

            CustomSpacer(height = 8.dp)

            Text(
                text = stringResource(R.string.forgot_password),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO */ },
                color = Color.Gray
            )

            CustomSpacer(height = 24.dp)

            Button(
                onClick = {
                    viewModel.loginWithEmail(email, password)
                    navController.navigate("${Constants.SPLASH_SCREEN}/${Constants.SPLASH_MODE_LOGIN}/${R.string.signing_in}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.login))
            }

            CustomSpacer(height = 24.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    stringResource(R.string.or_login_with),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            CustomSpacer(height = 16.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                ImageButton(
                    onClick = { /* TODO Facebook */ },
                    vectorId = R.drawable.vector_facebook,
                    enabled = !isLoading
                )

                ImageButton(
                    onClick = {
                        if (!isLoading) {
                            navController.navigate("${Constants.SPLASH_SCREEN}/${Constants.SPLASH_MODE_LOGIN}/${R.string.signing_in}")
                            viewModel.onGoogleLoginClick()
                        }
                    },
                    vectorId = R.drawable.vector_google,
                    enabled = !isLoading
                )
            }

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    progressMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.dont_have_account_register_now),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { navController.navigate(Constants.REGISTER_SCREEN) },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF00BFA6)
        )
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            customToast(context, errorMsg)
        }
    }
}


