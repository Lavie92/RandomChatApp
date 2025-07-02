package com.lavie.randochat.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lavie.randochat.R
import com.lavie.randochat.ui.component.CustomSpacer
import com.lavie.randochat.ui.theme.RandomChatTheme

@Composable
fun PasswordChangedScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = RoundedCornerShape(100),
            color = Color(0xFF00BFA6).copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Success",
                modifier = Modifier.padding(28.dp)
            )
        }

        CustomSpacer(height = 32.dp)

        Text(
            text = stringResource(R.string.password_changed),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        CustomSpacer(height = 8.dp)

        Text(
            text = stringResource(R.string.password_changed_successfully),
            fontSize = 14.sp,
            color = Color.Gray
        )

        CustomSpacer(height = 32.dp)

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.back_to_login))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PasswordChangedScreen() {
    val navController = rememberNavController()

    RandomChatTheme {
        PasswordChangedScreen(
            navController = navController
        )
    }
}
