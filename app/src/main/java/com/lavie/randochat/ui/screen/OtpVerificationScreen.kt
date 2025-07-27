package com.lavie.randochat.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lavie.randochat.ui.component.CustomOutlinedTextField
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.theme.RandomChatTheme
import com.lavie.randochat.R

@Composable
fun OtpVerificationScreen(
    navController: NavController
) {
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CustomSpacer(height = 12.dp)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.Start)
                .clickable { navController.popBackStack() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.Black,
                modifier = Modifier.padding(10.dp)
            )
        }

        CustomSpacer(height = 24.dp)

        Text(
            text = stringResource(R.string.otp_verification),
            style = MaterialTheme.typography.headlineSmall
        )

        CustomSpacer(height = 8.dp)

        Text(
            text = stringResource(R.string.enter_otp_description),
            fontSize = 14.sp,
            color = Color.Gray
        )

        CustomSpacer(height = 32.dp)

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomOutlinedTextField(
                value = code1,
                onValueChange = { if (it.length <= 1) code1 = it },
                placeholder = "",
                shapeRadius = 8.dp,
                modifier = Modifier.width(60.dp)
            )
            CustomOutlinedTextField(
                value = code2,
                onValueChange = { if (it.length <= 1) code2 = it },
                placeholder = "",
                shapeRadius = 8.dp,
                modifier = Modifier.width(60.dp)
            )
            CustomOutlinedTextField(
                value = code3,
                onValueChange = { if (it.length <= 1) code3 = it },
                placeholder = "",
                shapeRadius = 8.dp,
                modifier = Modifier.width(60.dp)
            )
            CustomOutlinedTextField(
                value = code4,
                onValueChange = { if (it.length <= 1) code4 = it },
                placeholder = "",
                shapeRadius = 8.dp,
                modifier = Modifier.width(60.dp)
            )
        }

        CustomSpacer(height = 32.dp)

        Button(
            onClick = { /* TODO verify code */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.verify))
        }

        CustomSpacer(height = 24.dp)

        Text(
            text = stringResource(R.string.didnt_receive_code_resend),
            color = Color(0xFF00BFA6),
            modifier = Modifier.clickable { /* TODO resend */ },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpVerificationScreen() {
    val navController = rememberNavController()

    RandomChatTheme {
        OtpVerificationScreen(
            navController = navController
        )
    }
}

