package com.lavie.randochat.ui.screen

import android.widget.Toast
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomOutlinedTextField
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.component.ImageButton
import com.lavie.randochat.ui.component.customToast
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel

) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState(false)
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val errorMessageId by viewModel.errorMessageId.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF8FAFC),
        unfocusedContainerColor = Color(0xFFF8FAFC),
        disabledContainerColor = Color(0xFFF8FAFC),
        focusedBorderColor = Color(0xFFB0B0B0),
        unfocusedBorderColor = Color(0xFFB0B0B0),
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        cursorColor = Color.Black,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        CustomSpacer(height = 16.dp)

        Text(
            text = stringResource(R.string.hello_register_to_get_started),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        CustomSpacer(height = 24.dp)

        CustomOutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = stringResource(R.string.username),
            modifier = Modifier.fillMaxWidth()
        )

        CustomSpacer(height = 12.dp)

        CustomOutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            placeholder = stringResource(R.string.email),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Email
        )


        CustomSpacer(height = 12.dp)

        CustomOutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(R.string.password),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            keyboardType = KeyboardType.Password
        )

        CustomSpacer(height = 12.dp)

        CustomOutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.confirm_password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        )

        CustomSpacer(height = 24.dp)

        Button(
            onClick = { when {
                !CommonUtils.isValidEmail(email.trim()) -> {
                    customToast(context, R.string.invalid_email)
                }
                !CommonUtils.isValidPassword(password) -> {
                    customToast(context, R.string.invalid_password)
                }
                password != confirmPassword -> {
                    customToast(context, R.string.password_mismatch)
                }
                else -> {
                    viewModel.registerWithEmail(email, password)
                }
            } },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.register))
        }

        LaunchedEffect(loginState) {
            if (loginState != null) {
                navController.navigate(Constants.START_CHAT_SCREEN) {
                    popUpTo(Constants.REGISTER_SCREEN) { inclusive = true }
                }
            }
        }

        LaunchedEffect(errorMessageId) {
            errorMessageId?.let { msgId ->
                Toast.makeText(context, context.getString(msgId), Toast.LENGTH_SHORT).show()
            }
        }


        CustomSpacer(height = 24.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                stringResource(R.string.or_register_with),
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
                        viewModel.onGoogleLoginClick()
                    }
                },
                vectorId = R.drawable.vector_google,
                enabled = !isLoading
            )
        }

        CustomSpacer(height = 24.dp)

        Text(
            text = stringResource(R.string.already_have_account_login_now),
            modifier = Modifier
                .clickable { navController.navigate(Constants.LOGIN_SCREEN) },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF00BFA6)
        )
    }
}
