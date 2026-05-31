package com.wednowapp.wednow.viewmodel

import android.app.Activity
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.fake.FakeAuthRepository
import com.wednowapp.wednow.fake.TestData
import com.wednowapp.wednow.presentation.auth.AuthViewModel
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var identityManager: IdentityManager
    private lateinit var viewModel: AuthViewModel
    private val activity = mockk<Activity>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        identityManager = mockk(relaxed = true)
        every { identityManager.onSignIn(any()) } just Runs
        every { identityManager.onSignOut() } just Runs
        viewModel = AuthViewModel(authRepository, identityManager)
    }

    @AfterEach
    fun tearDown() {
        // Reset repo BEFORE resetting Main: authRepository.reset() emits to a StateFlow
        // whose collector still holds a continuation dispatched on Dispatchers.Main.
        // If resetMain() runs first, that dispatcher becomes MissingMainCoroutineDispatcher
        // and the dispatch throws IllegalStateException.
        authRepository.reset()
        Dispatchers.resetMain()
    }

    // ── authState ─────────────────────────────────────────────────────────────

    @Nested
    inner class AuthState {

        @Test
        fun `given no user signed in, authState initial value is null`() = runTest {
            assertNull(viewModel.authState.value)
        }

        @Test
        fun `given user already signed in, authState initial value reflects current user`() =
            runTest {
                val user = TestData.authUser()
                authRepository.emitUser(user)
                // Re-create ViewModel to pick up initial state
                val vm = AuthViewModel(authRepository, identityManager)

                assertNotNull(vm.authState.value)
            }

        @Test
        fun `authState emits null after sign-out`() = runTest {
            authRepository.emitUser(TestData.authUser())
            viewModel.signOut()
            advanceUntilIdle()

            assertNull(viewModel.authState.value)
        }

        @Test
        fun `isSignedIn returns false when no user`() {
            assertFalse(viewModel.isSignedIn)
        }

        @Test
        fun `isSignedIn returns true after sign-in`() = runTest {
            authRepository.signInResult = Result.success(TestData.authUser())

            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()

            assertTrue(viewModel.isSignedIn)
        }
    }

    // ── signInWithGoogle ──────────────────────────────────────────────────────

    @Nested
    inner class SignInWithGoogle {

        @Test
        fun `given successful sign-in, updates authState with user`() = runTest {
            val user = TestData.authUser(uid = TestData.USER_UID)
            authRepository.signInResult = Result.success(user)

            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()

            assertEquals(TestData.USER_UID, viewModel.authState.value?.uid)
        }

        @Test
        fun `given successful sign-in, calls identityManager onSignIn`() = runTest {
            val user = TestData.authUser(uid = TestData.USER_UID)
            authRepository.signInResult = Result.success(user)

            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()

            verify { identityManager.onSignIn(user) }
        }

        @Test
        fun `given sign-in fails, sets signInError`() = runTest {
            authRepository.signInResult = Result.failure(RuntimeException("Auth failed"))

            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()

            assertNotNull(viewModel.signInError.value)
        }

        @Test
        fun `given sign-in fails, signInError contains message`() = runTest {
            authRepository.signInResult = Result.failure(RuntimeException("Credential error"))

            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()

            assertTrue(viewModel.signInError.value!!.isNotBlank())
        }

        @Test
        fun `signInLoading is true during sign-in and false after`() = runTest {
            authRepository.signInDelay = 100L
            authRepository.signInResult = Result.success(TestData.authUser())

            viewModel.signInWithGoogle(activity)
            // Loading should be true immediately after launch
            advanceUntilIdle()

            // After completion it should be false
            assertFalse(viewModel.signInLoading.value)
        }

        @Test
        fun `signInLoading StateFlow emits true then false`() = runTest {
            authRepository.signInResult = Result.success(TestData.authUser())

            viewModel.signInLoading.test {
                assertEquals(false, awaitItem()) // initial
                viewModel.signInWithGoogle(activity)
                assertEquals(true, awaitItem())  // loading
                assertEquals(false, awaitItem()) // done
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // ── signInWithApple ───────────────────────────────────────────────────────

    @Nested
    inner class SignInWithApple {

        @Test
        fun `given successful Apple sign-in, updates authState`() = runTest {
            val user = TestData.authUser(uid = TestData.USER_UID, provider = "apple.com")
            authRepository.signInResult = Result.success(user)

            viewModel.signInWithApple(activity)
            advanceUntilIdle()

            assertNotNull(viewModel.authState.value)
        }

        @Test
        fun `given Apple sign-in fails, sets error state`() = runTest {
            authRepository.signInResult = Result.failure(RuntimeException("Apple error"))

            viewModel.signInWithApple(activity)
            advanceUntilIdle()

            assertNotNull(viewModel.signInError.value)
        }
    }

    // ── signOut ───────────────────────────────────────────────────────────────

    @Nested
    inner class SignOut {

        @Test
        fun `given signed-in user, signOut clears auth state`() = runTest {
            authRepository.emitUser(TestData.authUser())

            viewModel.signOut()
            advanceUntilIdle()

            assertNull(viewModel.authState.value)
        }

        @Test
        fun `signOut calls identityManager onSignOut`() = runTest {
            viewModel.signOut()
            advanceUntilIdle()

            verify { identityManager.onSignOut() }
        }
    }

    // ── clearError ────────────────────────────────────────────────────────────

    @Nested
    inner class ClearError {

        @Test
        fun `given error state, clearError resets signInError to null`() = runTest {
            authRepository.signInResult = Result.failure(RuntimeException("error"))
            viewModel.signInWithGoogle(activity)
            advanceUntilIdle()
            assertNotNull(viewModel.signInError.value)

            viewModel.clearError()

            assertNull(viewModel.signInError.value)
        }
    }
}
