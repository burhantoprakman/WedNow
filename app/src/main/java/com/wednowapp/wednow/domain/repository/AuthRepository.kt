package com.wednowapp.wednow.domain.repository

import android.app.Activity
import com.wednowapp.wednow.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Cold flow that emits the current user on every Firebase Auth state change. */
    fun authStateFlow(): Flow<AuthUser?>

    /** Synchronous snapshot — null when not signed in. */
    val currentUser: AuthUser?

    val isSignedIn: Boolean get() = currentUser != null

    /** Launches Google One-Tap / Credential Manager picker and signs in. */
    suspend fun signInWithGoogle(activity: Activity): Result<AuthUser>

    /**
     * Launches Firebase OAuth flow for Apple.
     * Opens a SFSafariViewController / Chrome Custom Tab inside [activity].
     */
    suspend fun signInWithApple(activity: Activity): Result<AuthUser>

    suspend fun signOut(): Result<Unit>
}
