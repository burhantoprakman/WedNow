package com.wednowapp.wednow.multiwedding

import android.content.Context
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.domain.usecase.GetWeddingMembershipsUseCase
import com.wednowapp.wednow.domain.usecase.UpdateLastActiveWeddingUseCase
import com.wednowapp.wednow.fake.FakeMembershipRepository
import com.wednowapp.wednow.fake.FakeUserPreferencesRepository
import com.wednowapp.wednow.fake.FakeWeddingRepository
import com.wednowapp.wednow.fake.TestData
import com.wednowapp.wednow.presentation.identity.IdentityViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MultiWeddingTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var context: Context
    private lateinit var identityManager: IdentityManager
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var weddingRepository: FakeWeddingRepository
    private lateinit var identityViewModel: IdentityViewModel
    private lateinit var updateLastActiveWeddingUseCase: UpdateLastActiveWeddingUseCase
    private lateinit var getWeddingMembershipsUseCase: GetWeddingMembershipsUseCase

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(WeddingSessionManager)
        mockkObject(GuestSessionManager)

        context = mockk(relaxed = true)
        identityManager = mockk(relaxed = true)
        membershipRepository = FakeMembershipRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()
        weddingRepository = FakeWeddingRepository()

        every { WeddingSessionManager.saveWeddingId(context, any()) } just Runs
        every { WeddingSessionManager.getWeddingId(context) } returns null

        getWeddingMembershipsUseCase = GetWeddingMembershipsUseCase(
            membershipRepository = membershipRepository,
            identityManager = identityManager,
        )
        updateLastActiveWeddingUseCase = UpdateLastActiveWeddingUseCase(
            context = context,
            identityManager = identityManager,
            userPreferencesRepository = userPreferencesRepository,
        )

        every { identityManager.identity } returns MutableStateFlow(TestData.guestIdentity())
        every { identityManager.currentIdentity } returns TestData.guestIdentity()
        every { identityManager.currentIdentityId } returns TestData.GUEST_UUID

        identityViewModel = IdentityViewModel(identityManager, getWeddingMembershipsUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        membershipRepository.reset()
        userPreferencesRepository.reset()
        weddingRepository.reset()
    }

    // ── Multiple memberships ──────────────────────────────────────────────────

    @Nested
    inner class MultipleMemberships {

        @Test
        fun `given guest joined two weddings, getMemberships returns both`() = runTest {
            every { identityManager.currentIdentityId } returns TestData.GUEST_UUID
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_B_ID,
                    identityId = TestData.GUEST_UUID
                )
            )

            val memberships = getWeddingMembershipsUseCase()

            assertEquals(2, memberships.size)
        }

        @Test
        fun `given user joined three weddings, refreshMemberships in ViewModel reflects all three`() =
            runTest {
                every { identityManager.currentIdentityId } returns TestData.USER_UID
                listOf(TestData.WEDDING_A_ID, TestData.WEDDING_B_ID, TestData.WEDDING_C_ID)
                    .forEach { id ->
                        membershipRepository.addMembership(
                            TestData.membership(weddingId = id, identityId = TestData.USER_UID)
                        )
                    }

                identityViewModel.refreshMemberships()
                advanceUntilIdle()

                assertEquals(3, identityViewModel.memberships.value.size)
            }

        @Test
        fun `given identity with no memberships, getWeddingMemberships returns empty list`() =
            runTest {
                every { identityManager.currentIdentityId } returns "no-memberships-identity"

                val memberships = getWeddingMembershipsUseCase()

                assertTrue(memberships.isEmpty())
            }
    }

    // ── lastActiveWeddingId update ────────────────────────────────────────────

    @Nested
    inner class LastActiveWeddingUpdate {

        @Test
        fun `given GUEST identity, updateLastActiveWedding saves locally only`() = runTest {
            every { identityManager.currentIdentity } returns TestData.guestIdentity()

            updateLastActiveWeddingUseCase(TestData.WEDDING_A_ID)

            // Local save should happen
            io.mockk.verify { WeddingSessionManager.saveWeddingId(context, TestData.WEDDING_A_ID) }
            // Remote preference should NOT be updated for guests
            assertNull(userPreferencesRepository.getLastActiveWedding(TestData.GUEST_UUID))
        }

        @Test
        fun `given USER identity, updateLastActiveWedding saves locally AND to Firestore`() =
            runTest {
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
                every { identityManager.currentIdentity } returns userIdentity
                every { identityManager.currentIdentityId } returns TestData.USER_UID

                updateLastActiveWeddingUseCase(TestData.WEDDING_B_ID)

                io.mockk.verify {
                    WeddingSessionManager.saveWeddingId(
                        context,
                        TestData.WEDDING_B_ID
                    )
                }
                assertEquals(
                    TestData.WEDDING_B_ID,
                    userPreferencesRepository.getLastActiveWedding(TestData.USER_UID),
                )
            }

        @Test
        fun `given USER switches weddings, each switch updates lastActiveWeddingId`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
            every { identityManager.currentIdentity } returns userIdentity
            every { identityManager.currentIdentityId } returns TestData.USER_UID

            updateLastActiveWeddingUseCase(TestData.WEDDING_A_ID)
            updateLastActiveWeddingUseCase(TestData.WEDDING_B_ID)

            assertEquals(
                TestData.WEDDING_B_ID,
                userPreferencesRepository.getLastActiveWedding(TestData.USER_UID),
            )
        }
    }

    // ── Wedding switching ─────────────────────────────────────────────────────

    @Nested
    inner class WeddingSwitching {

        @Test
        fun `given user has joined weddings, ViewModel exposes them for switching`() = runTest {
            every { identityManager.currentIdentityId } returns TestData.USER_UID
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.USER_UID
                )
            )
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_B_ID,
                    identityId = TestData.USER_UID
                )
            )

            identityViewModel.refreshMemberships()
            advanceUntilIdle()

            val weddingIds = identityViewModel.memberships.value.map { it.weddingId }
            assertTrue(weddingIds.contains(TestData.WEDDING_A_ID))
            assertTrue(weddingIds.contains(TestData.WEDDING_B_ID))
        }

        @Test
        fun `given membership list, can find the correct membership by weddingId`() = runTest {
            every { identityManager.currentIdentityId } returns TestData.USER_UID
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.USER_UID,
                    role = "ADMIN",
                )
            )

            identityViewModel.refreshMemberships()
            advanceUntilIdle()

            val membership = identityViewModel.memberships.value
                .firstOrNull { it.weddingId == TestData.WEDDING_A_ID }
            assertNotNull(membership)
            assertEquals("ADMIN", membership!!.role)
        }
    }
}
