package com.lavie.randochat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lavie.randochat.R
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.viewmodel.MatchViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    matchViewModel: MatchViewModel,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (() -> Unit)? = null
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.processing_cat))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    val matchState by matchViewModel.matchState.collectAsState()

    LaunchedEffect(matchState) {
        if (matchState is MatchViewModel.MatchState.Matched) {
            val matched = matchState as MatchViewModel.MatchState.Matched
            navController.navigate("chat/${matched.roomId}") {
                popUpTo(Constants.MATCHING_LOADING_SCREEN) { inclusive = true }
            }
            matchViewModel.resetState()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress }
        )
    }


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = contentAlignment
    ) {
        content?.invoke()
    }
}


