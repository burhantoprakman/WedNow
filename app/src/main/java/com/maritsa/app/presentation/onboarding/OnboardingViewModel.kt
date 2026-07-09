package com.maritsa.app.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import com.maritsa.app.core.session.OnboardingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun complete() = OnboardingManager.markCompleted(context)
}
