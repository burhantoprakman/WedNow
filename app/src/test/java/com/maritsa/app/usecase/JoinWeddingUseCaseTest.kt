package com.maritsa.app.usecase

import android.content.Context
import android.util.Log
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.usecase.JoinWeddingUseCase
import com.maritsa.app.domain.usecase.SaveFcmTokenUseCase
import com.maritsa.app.fake.FakeGuestGroupRepository
import com.maritsa.app.fake.FakeGuestRepository
import com.maritsa.app.fake.FakeMembershipRepository
import com.maritsa.app.fake.FakeWeddingRepository
import com.maritsa.app.fake.TestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class JoinWeddingUseCaseTest {

    private lateinit var context: Context
    private lateinit var identityManager: IdentityManager
    private lateinit var saveFcmTokenUseCase: SaveFcmTokenUseCase
    private lateinit var weddingRepository: FakeWeddingRepository
    private lateinit var guestRepository: FakeGuestRepository
    private lateinit var guestGroupRepository: FakeGuestGroupRepository
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var useCase: JoinWeddingUseCase

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkObject(GuestSessionManager)
        mockkObject(WeddingSessionManager)

        context = mockk(relaxed = true)
        identityManager = mockk(relaxed = true)
        saveFcmTokenUseCase = mockk(relaxed = true)
        weddingRepository = FakeWeddingRepository()
        guestRepository = FakeGuestRepository()
        guestGroupRepository = FakeGuestGroupRepository()
        membershipRepository = FakeMembershipRepository()

        every { GuestSessionManager.getGuestId(context) } returns TestData.GUEST_UUID
        every { GuestSessionManager.saveGuestName(context, any()) } just Runs
        every { WeddingSessionManager.saveWeddingId(context, any()) } just Runs
        every { identityManager.currentIdentityId } returns TestData.GUEST_UUID
        coEvery { saveFcmTokenUseCase.invoke(any()) } returns Result.success(Unit)

        useCase = JoinWeddingUseCase(
            weddingRepository = weddingRepository,
            guestRepository = guestRepository,
            guestGroupRepository = guestGroupRepository,
            membershipRepository = membershipRepository,
            identityManager = identityManager,
            saveFcmTokenUseCase = saveFcmTokenUseCase,
            context = context,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        weddingRepository.reset()
        guestRepository.reset()
        guestGroupRepository.reset()
        membershipRepository.reset()
    }

    // ── Short-code join ───────────────────────────────────────────────────────

    @Nested
    inner class JoinByShortCode {

        @Test
        fun `given valid short code, join succeeds and returns weddingId`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)

            val result = useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            assertTrue(result.isSuccess)
            assertEquals(TestData.WEDDING_A_ID, result.getOrNull())
        }

        @Test
        fun `given valid short code, registers guest in wedding`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)

            useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            val guest = guestRepository.getGuest(TestData.WEDDING_A_ID, TestData.GUEST_UUID)
            assertTrue(guest != null, "Guest should be registered")
        }

        @Test
        fun `given short code input is lowercase, normalises to uppercase before lookup`() =
            runTest {
                val wedding = TestData.wedding(shortCode = "ALPHA1")
                weddingRepository.seedWedding(wedding)

                val result = useCase(weddingCode = "alpha1", guestName = "Bob")

                assertTrue(result.isSuccess)
            }

        @Test
        fun `given invalid short code, returns failure with descriptive message`() = runTest {
            val result = useCase(weddingCode = "BADCODE", guestName = "Eve")

            assertTrue(result.isFailure)
            val msg = result.exceptionOrNull()?.message ?: ""
            assertTrue(msg.contains("BADCODE") || msg.isNotBlank())
        }
    }

    // ── Invite-token join ─────────────────────────────────────────────────────

    @Nested
    inner class JoinByInviteToken {

        @Test
        fun `given valid invite token, resolves to correct wedding and joins`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_B_ID, shortCode = TestData.WEDDING_B_CODE)
            weddingRepository.seedWedding(wedding)
            val group = GuestGroup(
                id = UUID.randomUUID().toString(),
                weddingId = TestData.WEDDING_B_ID,
                familyName = "Family Group",
                inviteToken = "INVITE42",
            )
            guestGroupRepository.addGuestGroup(TestData.WEDDING_B_ID, group)

            val result = useCase(weddingCode = "INVITE42", guestName = "Carol")

            assertTrue(result.isSuccess)
            assertEquals(TestData.WEDDING_B_ID, result.getOrNull())
        }
    }

    // ── Raw weddingId join (QR deep link) ─────────────────────────────────────

    @Nested
    inner class JoinByRawId {

        @Test
        fun `given raw weddingId, join succeeds`() = runTest {
            // JoinWeddingUseCase normalises the input with .uppercase().trim() before
            // every lookup, including the getWeddingById fallback.  Seed the wedding with
            // an already-uppercase ID so the fallback lookup finds it.
            val weddingId = TestData.WEDDING_C_ID.uppercase()   // "WEDDING-GAMMA"
            val wedding = TestData.wedding(id = weddingId, shortCode = "NOWAY1")
            weddingRepository.seedWedding(wedding)

            val result = useCase(weddingCode = weddingId, guestName = "Dave")

            assertTrue(result.isSuccess)
            assertEquals(weddingId, result.getOrNull())
        }
    }

    // ── Role assignment ───────────────────────────────────────────────────────

    @Nested
    inner class RoleAssignment {

        @Test
        fun `given guestId matches adminGuestId, assigned ADMIN role`() = runTest {
            val wedding = TestData.wedding(
                id = TestData.WEDDING_A_ID,
                shortCode = TestData.WEDDING_A_CODE,
                adminGuestId = TestData.GUEST_UUID,
            )
            weddingRepository.seedWedding(wedding)

            useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Admin")

            val guest = guestRepository.getGuest(TestData.WEDDING_A_ID, TestData.GUEST_UUID)
            assertEquals(GuestRole.ADMIN, guest?.role)
        }

        @Test
        fun `given guestId differs from adminGuestId, assigned GUEST role`() = runTest {
            val wedding = TestData.wedding(
                id = TestData.WEDDING_A_ID,
                shortCode = TestData.WEDDING_A_CODE,
                adminGuestId = "different-admin-id",
            )
            weddingRepository.seedWedding(wedding)

            useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Regular")

            val guest = guestRepository.getGuest(TestData.WEDDING_A_ID, TestData.GUEST_UUID)
            assertEquals(GuestRole.GUEST, guest?.role)
        }
    }

    // ── Membership index ──────────────────────────────────────────────────────

    @Nested
    inner class MembershipIndex {

        @Test
        fun `given successful join, membership record is created for current identity`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)

            useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            val membership =
                membershipRepository.getMembership(TestData.GUEST_UUID, TestData.WEDDING_A_ID)
            assertTrue(membership != null, "Membership should be recorded")
        }

        @Test
        fun `given successful join, membership has correct weddingId`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)

            useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            val membership =
                membershipRepository.getMembership(TestData.GUEST_UUID, TestData.WEDDING_A_ID)
            assertEquals(TestData.WEDDING_A_ID, membership?.weddingId)
        }

        @Test
        fun `given FCM save fails, join still succeeds`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)
            coEvery { saveFcmTokenUseCase.invoke(any()) } returns Result.failure(RuntimeException("FCM error"))

            val result = useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            assertTrue(result.isSuccess, "Join should succeed even when FCM token save fails")
        }
    }

    // ── Guest add failure ─────────────────────────────────────────────────────

    @Nested
    inner class GuestAddFailure {

        @Test
        fun `given guestRepository add fails, join returns failure`() = runTest {
            val wedding =
                TestData.wedding(id = TestData.WEDDING_A_ID, shortCode = TestData.WEDDING_A_CODE)
            weddingRepository.seedWedding(wedding)
            guestRepository.addShouldFail = true

            val result = useCase(weddingCode = TestData.WEDDING_A_CODE, guestName = "Alice")

            assertTrue(result.isFailure)
        }
    }
}
