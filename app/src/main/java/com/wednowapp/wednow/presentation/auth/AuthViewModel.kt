package com.wednowapp.wednow.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.domain.model.AuthUser
import com.wednowapp.wednow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel scoped to the Activity (provided via [LocalAuthViewModel]).
 *
 * Every screen that needs auth-gated actions queries [isSignedIn] and calls
 * [signInWithGoogle] / [signInWithApple] from the [SignInBottomSheet].
 *
 * On successful sign-in this ViewModel notifies [IdentityManager] which then:
 *  • upgrades the in-memory identity from GUEST → USER
 *  • schedules background migration of guest-created content
 *  • saves the USER identity to local SharedPreferences
 *
 * On sign-out [IdentityManager] reverts to a fresh GUEST identity automatically.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val identityManager: IdentityManager,
) : ViewModel() {

    /** Emits the current [AuthUser] or null.  Eagerly started so callers never block. */
    val authState: StateFlow<AuthUser?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser)

    val isSignedIn: Boolean get() = authState.value != null

    // ── Sign-in loading / error state (consumed by SignInBottomSheet) ──────────

    private val _signInLoading = MutableStateFlow(false)
    val signInLoading: StateFlow<Boolean> = _signInLoading.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    // ── Sign-in ───────────────────────────────────────────────────────────────

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _signInLoading.value = true
            _signInError.value = null
            authRepository.signInWithGoogle(activity)
                .onSuccess { user -> handleSignInSuccess(user) }
                .onFailure { e ->
                    _signInError.value = e.message ?: "Sign-in failed. Please try again."
                }
            _signInLoading.value = false
        }
    }

    fun signInWithApple(activity: Activity) {
        viewModelScope.launch {
            _signInLoading.value = true
            _signInError.value = null
            authRepository.signInWithApple(activity)
                .onSuccess { user -> handleSignInSuccess(user) }
                .onFailure { e ->
                    _signInError.value = e.message ?: "Sign-in failed. Please try again."
                }
            _signInLoading.value = false
        }
    }

    // ── Sign-out ──────────────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            identityManager.onSignOut()
        }
    }

    fun clearError() {
        _signInError.value = null
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun handleSignInSuccess(user: AuthUser) {
        // Upgrade identity and trigger background content migration
        identityManager.onSignIn(user)
    }
}
