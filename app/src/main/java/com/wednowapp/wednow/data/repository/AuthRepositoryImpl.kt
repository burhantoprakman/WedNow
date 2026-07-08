package com.wednowapp.wednow.data.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.wednowapp.wednow.BuildConfig
import com.wednowapp.wednow.domain.model.AuthUser
import com.wednowapp.wednow.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    private val credentialManager = CredentialManager.create(context)

    override fun authStateFlow(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    // ── Google Sign-In (Credential Manager) ──────────────────────────────────

    override suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> = runCatching {
        val googleOption =
            GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val result = try {
            credentialManager.getCredential(request = request, context = activity)
        } catch (e: GetCredentialCancellationException) {
            throw Exception("Sign-in cancelled.")
        } catch (e: NoCredentialException) {
            throw Exception("No Google account found on this device. Please add a Google account in Settings.")
        }

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            authResult.user?.toAuthUser()
                ?: throw Exception("Google sign-in completed but no user was returned.")
        } else {
            throw Exception("Unexpected credential type: ${credential.type}")
        }
    }

    // ── Apple Sign-In (Firebase OAuth / Safari web view) ─────────────────────

    override suspend fun signInWithApple(activity: Activity): Result<AuthUser> = runCatching {
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()

        val authResult = firebaseAuth
            .startActivityForSignInWithProvider(activity, provider)
            .await()

        authResult.user?.toAuthUser()
            ?: throw Exception("Apple sign-in completed but no user was returned.")
    }

    // ── Sign-out ──────────────────────────────────────────────────────────────

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
    }

    override suspend fun clearGoogleCredential(): Result<Unit> = runCatching {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun FirebaseUser.toAuthUser(): AuthUser {
        val provider = providerData
            .firstOrNull { it.providerId != "firebase" }
            ?.providerId ?: ""
        return AuthUser(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString(),
            provider = provider,
        )
    }
}
