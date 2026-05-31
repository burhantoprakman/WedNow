package com.wednowapp.wednow.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigate: (destination: String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    // Custom splash UI commented out — system splash screen handles the launch moment.
    // To restore, bring back the animated logo + gradient background from git history.
    LaunchedEffect(Unit) {
        onNavigate(viewModel.startDestination)
    }
}
