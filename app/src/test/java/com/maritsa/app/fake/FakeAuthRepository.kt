package com.maritsa.app.fake

import android.app.Activity
import com.maritsa.app.domain.model.AuthUser
import com.maritsa.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {

    private val _authState = MutableStateFlow<AuthUser?>(null)

    var signInResult: Result<AuthUser> = Result.success(TestData.authUser())
    var signInDelay: Long = 0L

    override fun authStateFlow(): Flow<AuthUser?> = _authState.asStateFlow()

    override val currentUser: AuthUser? get() = _authState.value

    override val isSignedIn: Boolean get() = _authState.value != null

    override suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> {
        if (signInDelay > 0) kotlinx.coroutines.delay(signInDelay)
        return signInResult.also { result ->
            result.onSuccess { user -> _authState.value = user }
        }
    }

    override suspend fun signInWithApple(activity: Activity): Result<AuthUser> =
        signInWithGoogle(activity)

    override suspend fun signOut(): Result<Unit> {
        _authState.value = null
        return Result.success(Unit)
    }

    /** Test helper — directly emit an auth state. */
    fun emitUser(user: AuthUser?) {
        _authState.value = user
    }

    fun reset() {
        _authState.value = null
        signInResult = Result.success(TestData.authUser())
        signInDelay = 0L
    }
}
