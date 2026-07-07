package com.wednowapp.wednow.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.OnboardingManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    val startDestination: String = run {
        val savedWeddingId = WeddingSessionManager.getWeddingId(context)
        when {
            !OnboardingManager.isCompleted(context) -> Screen.Onboarding.route
            savedWeddingId != null -> Screen.WeddingHome.createRoute(savedWeddingId)
            else -> Screen.CreateWedding.route
        }
    }
}
