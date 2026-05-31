package com.wednowapp.wednow.signout

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.identity.IdentityMigrationService
import com.wednowapp.wednow.core.identity.IdentityPreferences
import com.wednowapp.wednow.core.identity.PermissionService
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.IdentityType
import com.wednowapp.wednow.fake.FakeAuthRepository
import com.wednowapp.wednow.fake.TestData
import com.wednowapp.wednow.presentation.auth.AuthViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignOutTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    // Real IdentityManager with mocked dependencies
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var migrationService: IdentityMigrationService
    private lateinit var identityManager: IdentityManager
    private lateinit var permissionService: PermissionService
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var authViewModel: AuthViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(IdentityPreferences)
        mockkObject(GuestSessionManager)

        context = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        migrationService = mockk(relaxed = true)
        authRepository = FakeAuthRepository()

        // Stub auth state listener capture
        every { firebaseAuth.addAuthStateListener(any()) } just Runs

        // Start as USER identity
        val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
        every { IdentityPreferences.load(context) } returns userIdentity
        every { IdentityPreferences.save(context, any()) } just Runs
        every { GuestSessionManager.saveGuestId(context, any()) } just Runs
        every { GuestSessionManager.saveGuestName(context, any()) } just Runs
        every { GuestSessionManager.getGuestId(context) } returns TestData.GUEST_UUID

        identityManager = IdentityManager(context, firebaseAuth, migrationService)
        permissionService = PermissionService(identityManager)

        // Auth state starts as signed-in
        authRepository.emitUser(TestData.authUser(uid = TestData.USER_UID))
        authViewModel = AuthViewModel(authRepository, identityManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        authRepository.reset()
    }

    // ── Identity after sign-out ───────────────────────────────────────────────

    @Nested
    inner class IdentityAfterSignOut {

        @Test
        fun `given USER signs out, identity type becomes GUEST`() {
            identityManager.onSignOut()

            assertFalse(identityManager.isAuthenticated)
            assert(identityManager.currentIdentity.type == IdentityType.GUEST)
        }

        @Test
        fun `given USER signs out, new GUEST UUID is different from previous USER UID`() {
            identityManager.onSignOut()

            assertNotEquals(TestData.USER_UID, identityManager.currentIdentityId)
        }

        @Test
        fun `given USER signs out, new GUEST UUID is not blank`() {
            identityManager.onSignOut()

            assertTrue(identityManager.currentIdentityId.isNotBlank())
        }

        @Test
        fun `signing out twice creates a different GUEST UUID each time`() {
            identityManager.onSignOut()
            val firstGuestId = identityManager.currentIdentityId

            identityManager.onSignOut()
            val secondGuestId = identityManager.currentIdentityId

            assertNotEquals(firstGuestId, secondGuestId)
        }
    }

    // ── Auth state after sign-out ─────────────────────────────────────────────

    @Nested
    inner class AuthStateAfterSignOut {

        @Test
        fun `given signed-in user, AuthViewModel signOut clears authState`() = runTest {
            assertNotNull(authViewModel.authState.value) // sanity check

            authViewModel.signOut()
            advanceUntilIdle()

            assertNull(authViewModel.authState.value)
        }

        @Test
        fun `after sign-out, isSignedIn returns false`() = runTest {
            authViewModel.signOut()
            advanceUntilIdle()

            assertFalse(authViewModel.isSignedIn)
        }
    }

    // ── Permission gates after sign-out ───────────────────────────────────────

    @Nested
    inner class PermissionsAfterSignOut {

        @Test
        fun `after sign-out, canCreateWedding returns false`() {
            identityManager.onSignOut()

            assertFalse(permissionService.canCreateWedding())
        }

        @Test
        fun `after sign-out, canUploadPhoto returns false`() {
            identityManager.onSignOut()

            assertFalse(permissionService.canUploadPhoto())
        }

        @Test
        fun `after sign-out, canWriteGuestbook returns false`() {
            identityManager.onSignOut()

            assertFalse(permissionService.canWriteGuestbook())
        }

        @Test
        fun `after sign-out, requiresAuth returns true`() {
            identityManager.onSignOut()

            assertTrue(permissionService.requiresAuth)
        }
    }

    // ── Browsing after sign-out ───────────────────────────────────────────────

    @Nested
    inner class BrowsingAfterSignOut {

        @Test
        fun `after sign-out, identity is not null — browsing remains available`() {
            identityManager.onSignOut()

            // Identity is always non-null; guest can still browse
            assertNotNull(identityManager.currentIdentity)
            assert(identityManager.currentIdentity.type == IdentityType.GUEST)
        }

        @Test
        fun `after sign-out, guest can still read content`() {
            // Reading is not gated on authentication — identity always exists
            identityManager.onSignOut()

            val identity = identityManager.currentIdentity
            assertNotNull(identity)
            assertTrue(identity.isGuest)
        }
    }
}
