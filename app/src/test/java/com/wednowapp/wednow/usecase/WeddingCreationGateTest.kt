package com.wednowapp.wednow.usecase

import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.identity.PermissionService
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.ProtectedAction
import com.wednowapp.wednow.domain.usecase.GetWeddingMembershipsUseCase
import com.wednowapp.wednow.fake.TestData
import com.wednowapp.wednow.presentation.identity.IdentityViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests the gate that prevents anonymous guests from creating weddings.
 *
 * Covers both the [PermissionService] layer and the [IdentityViewModel]
 * protected-action gate used in the UI layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeddingCreationGateTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var identityManager: IdentityManager
    private lateinit var permissionService: PermissionService
    private lateinit var identityViewModel: IdentityViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        identityManager = mockk(relaxed = true)
        permissionService = PermissionService(identityManager)

        val getMemberships = mockk<GetWeddingMembershipsUseCase>(relaxed = true)
        every { identityManager.identity } returns MutableStateFlow(TestData.guestIdentity())
        every { identityManager.currentIdentity } returns TestData.guestIdentity()
        identityViewModel = IdentityViewModel(identityManager, getMemberships)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── PermissionService gate ────────────────────────────────────────────────

    @Nested
    inner class PermissionServiceGate {

        @Test
        fun `given GUEST identity, canCreateWedding returns false`() {
            every { identityManager.isAuthenticated } returns false

            assertFalse(permissionService.canCreateWedding())
        }

        @Test
        fun `given USER identity, canCreateWedding returns true`() {
            every { identityManager.isAuthenticated } returns true

            assertTrue(permissionService.canCreateWedding())
        }

        @Test
        fun `given GUEST identity, requiresAuth returns true`() {
            every { identityManager.isAuthenticated } returns false

            assertTrue(permissionService.requiresAuth)
        }

        @Test
        fun `given USER identity, requiresAuth returns false`() {
            every { identityManager.isAuthenticated } returns true

            assertFalse(permissionService.requiresAuth)
        }

        @Test
        fun `given GUEST identity, canUploadPhoto returns false`() {
            every { identityManager.isAuthenticated } returns false

            assertFalse(permissionService.canUploadPhoto())
        }

        @Test
        fun `given GUEST identity, canWriteGuestbook returns false`() {
            every { identityManager.isAuthenticated } returns false

            assertFalse(permissionService.canWriteGuestbook())
        }
    }

    // ── IdentityViewModel gate ────────────────────────────────────────────────

    @Nested
    inner class IdentityViewModelGate {

        @Test
        fun `given GUEST, requestProtectedAction stores CREATE_WEDDING and returns false`() {
            every { identityManager.isAuthenticated } returns false

            val canProceed =
                identityViewModel.requestProtectedAction(ProtectedAction.CREATE_WEDDING)

            assertFalse(canProceed)
            assert(identityViewModel.pendingAction.value == ProtectedAction.CREATE_WEDDING)
        }

        @Test
        fun `given USER, requestProtectedAction returns true without storing pending`() {
            every { identityManager.isAuthenticated } returns true

            val canProceed =
                identityViewModel.requestProtectedAction(ProtectedAction.CREATE_WEDDING)

            assertTrue(canProceed)
            assert(identityViewModel.pendingAction.value == null)
        }

        @Test
        fun `given pending CREATE_WEDDING after sign-in, consumePendingAction returns it`() {
            every { identityManager.isAuthenticated } returns false
            identityViewModel.requestProtectedAction(ProtectedAction.CREATE_WEDDING)

            // Simulate user signing in — now consume the pending action
            val consumed = identityViewModel.consumePendingAction()

            assert(consumed == ProtectedAction.CREATE_WEDDING)
            assert(identityViewModel.pendingAction.value == null)
        }
    }

    // ── Role-level gates ──────────────────────────────────────────────────────

    @Nested
    inner class RoleLevelGates {

        @Test
        fun `given ADMIN role, isAdmin returns true`() {
            assertTrue(permissionService.isAdmin(GuestRole.ADMIN))
        }

        @Test
        fun `given COADMIN role, isAdmin returns false`() {
            assertFalse(permissionService.isAdmin(GuestRole.COADMIN))
        }

        @Test
        fun `given ADMIN role, isElevated returns true`() {
            assertTrue(permissionService.isElevated(GuestRole.ADMIN))
        }

        @Test
        fun `given COADMIN role, isElevated returns true`() {
            assertTrue(permissionService.isElevated(GuestRole.COADMIN))
        }

        @Test
        fun `given GUEST role, isElevated returns false`() {
            assertFalse(permissionService.isElevated(GuestRole.GUEST))
        }
    }
}
