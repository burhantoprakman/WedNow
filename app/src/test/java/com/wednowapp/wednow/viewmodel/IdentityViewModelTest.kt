package com.wednowapp.wednow.viewmodel

import app.cash.turbine.test
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.model.IdentityType
import com.wednowapp.wednow.domain.model.ProtectedAction
import com.wednowapp.wednow.domain.usecase.GetWeddingMembershipsUseCase
import com.wednowapp.wednow.fake.TestData
import com.wednowapp.wednow.presentation.identity.IdentityViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdentityViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var identityManager: IdentityManager
    private lateinit var getWeddingMembershipsUseCase: GetWeddingMembershipsUseCase
    private val identityFlow = MutableStateFlow<Identity>(TestData.guestIdentity())
    private lateinit var viewModel: IdentityViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        identityManager = mockk(relaxed = true)
        getWeddingMembershipsUseCase = mockk(relaxed = true)

        every { identityManager.identity } returns identityFlow
        every { identityManager.currentIdentity } returns identityFlow.value
        every { identityManager.isAuthenticated } returns (identityFlow.value.type == IdentityType.USER)
        every { identityManager.currentIdentityId } returns identityFlow.value.identityId
        coEvery { getWeddingMembershipsUseCase() } returns emptyList()

        viewModel = IdentityViewModel(identityManager, getWeddingMembershipsUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Identity StateFlow ────────────────────────────────────────────────────

    @Nested
    inner class IdentityState {

        @Test
        fun `given GUEST identity, identity StateFlow emits GUEST`() = runTest {
            assertEquals(IdentityType.GUEST, viewModel.identity.value.type)
        }

        @Test
        fun `given identity upgrades to USER, StateFlow emits updated identity`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            viewModel.identity.test {
                awaitItem() // consume initial GUEST
                identityFlow.value = userIdentity
                val updated = awaitItem()
                assertEquals(IdentityType.USER, updated.type)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `isAuthenticated reflects identityManager state`() {
            every { identityManager.isAuthenticated } returns false
            assertFalse(viewModel.isAuthenticated)

            every { identityManager.isAuthenticated } returns true
            assertTrue(viewModel.isAuthenticated)
        }

        @Test
        fun `currentIdentityId delegates to identityManager`() {
            every { identityManager.currentIdentityId } returns TestData.GUEST_UUID
            assertEquals(TestData.GUEST_UUID, viewModel.currentIdentityId)
        }
    }

    // ── Protected-action gate ─────────────────────────────────────────────────

    @Nested
    inner class ProtectedActionGate {

        @Test
        fun `given USER identity, requestProtectedAction returns true`() {
            every { identityManager.isAuthenticated } returns true

            val result = viewModel.requestProtectedAction(ProtectedAction.UPLOAD_PHOTO)

            assertTrue(result)
        }

        @Test
        fun `given USER identity, no pending action is stored`() {
            every { identityManager.isAuthenticated } returns true

            viewModel.requestProtectedAction(ProtectedAction.UPLOAD_PHOTO)

            assertNull(viewModel.pendingAction.value)
        }

        @Test
        fun `given GUEST identity, requestProtectedAction returns false`() {
            every { identityManager.isAuthenticated } returns false

            val result = viewModel.requestProtectedAction(ProtectedAction.CREATE_WEDDING)

            assertFalse(result)
        }

        @Test
        fun `given GUEST identity, requested action is stored as pending`() {
            every { identityManager.isAuthenticated } returns false

            viewModel.requestProtectedAction(ProtectedAction.CREATE_GUESTBOOK_ENTRY)

            assertEquals(ProtectedAction.CREATE_GUESTBOOK_ENTRY, viewModel.pendingAction.value)
        }

        @Test
        fun `pendingAction StateFlow emits stored action`() = runTest {
            every { identityManager.isAuthenticated } returns false

            viewModel.pendingAction.test {
                awaitItem() // null initial
                viewModel.requestProtectedAction(ProtectedAction.UPLOAD_PHOTO)
                val pending = awaitItem()
                assertEquals(ProtectedAction.UPLOAD_PHOTO, pending)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `given pending action exists, consumePendingAction returns it and clears`() {
            every { identityManager.isAuthenticated } returns false
            viewModel.requestProtectedAction(ProtectedAction.CREATE_WEDDING)

            val consumed = viewModel.consumePendingAction()

            assertEquals(ProtectedAction.CREATE_WEDDING, consumed)
            assertNull(viewModel.pendingAction.value)
        }

        @Test
        fun `given no pending action, consumePendingAction returns null`() {
            val consumed = viewModel.consumePendingAction()

            assertNull(consumed)
        }

        @Test
        fun `calling requestProtectedAction twice stores the latest action`() {
            every { identityManager.isAuthenticated } returns false

            viewModel.requestProtectedAction(ProtectedAction.UPLOAD_PHOTO)
            viewModel.requestProtectedAction(ProtectedAction.ADMIN_ACTION)

            assertEquals(ProtectedAction.ADMIN_ACTION, viewModel.pendingAction.value)
        }
    }

    // ── Memberships ───────────────────────────────────────────────────────────

    @Nested
    inner class Memberships {

        @Test
        fun `given no weddings, memberships list is empty`() = runTest {
            coEvery { getWeddingMembershipsUseCase() } returns emptyList()

            viewModel.refreshMemberships()
            advanceUntilIdle()

            assertTrue(viewModel.memberships.value.isEmpty())
        }

        @Test
        fun `given two wedding memberships, refreshMemberships populates list`() = runTest {
            val memberships = listOf(
                TestData.membership(weddingId = TestData.WEDDING_A_ID),
                TestData.membership(weddingId = TestData.WEDDING_B_ID),
            )
            coEvery { getWeddingMembershipsUseCase() } returns memberships

            viewModel.refreshMemberships()
            advanceUntilIdle()

            assertEquals(2, viewModel.memberships.value.size)
        }

        @Test
        fun `refreshMemberships emits updated list via StateFlow`() = runTest {
            val memberships = listOf(TestData.membership(weddingId = TestData.WEDDING_A_ID))
            coEvery { getWeddingMembershipsUseCase() } returns memberships

            viewModel.memberships.test {
                awaitItem() // empty initial
                viewModel.refreshMemberships()
                advanceUntilIdle()
                val updated = awaitItem()
                assertEquals(1, updated.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
