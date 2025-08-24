package com.lavie.randochat.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lavie.randochat.R
import com.lavie.randochat.service.DialogService
import com.lavie.randochat.service.PreferencesService
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.ui.component.CustomSpacer
import org.koin.androidx.compose.koinViewModel
import com.lavie.randochat.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
) {
    val prefs: PreferencesService = koinInject()
    val authViewModel: AuthViewModel = koinViewModel()

    val user by authViewModel.loginState.collectAsState()
    var notificationEnabled by remember {
        mutableStateOf(prefs.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true))
    }
    val title = stringResource(R.string.logout_confirm_title)
    val message = stringResource(R.string.logout_confirm_message)
    val confirmButton = stringResource(R.string.confirm)
    val dismissButton = stringResource(R.string.cancel)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.settings_account_info),
                style = MaterialTheme.typography.titleMedium
            )
            CustomSpacer(height = 8.dp)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_email, user?.email ?: ""))
                    CustomSpacer(width = 8.dp)
                    Text(text = stringResource(R.string.settings_nickname, user?.nickname ?: ""))
                    CustomSpacer(width = 8.dp)
                    Text(text = stringResource(R.string.citizen_score, user?.citizenScore ?: ""))
                    CustomSpacer(width = 8.dp)
                    Text(text = stringResource(R.string.image_credit, user?.imageCredit ?: ""))
                }
            }

            CustomSpacer(height = 24.dp)

            Text(
                text = stringResource(R.string.settings_notifications),
                style = MaterialTheme.typography.titleMedium
            )
            CustomSpacer(width = 8.dp)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.settings_enable_notifications))
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = {
                            notificationEnabled = it
                            prefs.putBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    DialogService.show(
                        title = title,
                        message = message,
                        confirmButton = confirmButton,
                        dismissButton = dismissButton,
                        onConfirmAction = {
                            authViewModel.logout()
                            navController.navigate(Constants.LOGIN_SCREEN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.logout))
            }
        }
    }

    DialogService.Render()
}
