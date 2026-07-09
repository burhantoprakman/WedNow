package com.maritsa.app.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.maritsa.app.core.navigation.Screen
import com.maritsa.app.core.session.OnboardingManager
import com.maritsa.app.core.session.WeddingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    val startDestination: String = when {
        !OnboardingManager.isCompleted(context) -> Screen.Onboarding.route
        WeddingSessionManager.getWeddingId(context) != null ->
            Screen.WeddingHome.createRoute(WeddingSessionManager.getWeddingId(context)!!)

        else -> Screen.CreateWedding.route
    }
}
