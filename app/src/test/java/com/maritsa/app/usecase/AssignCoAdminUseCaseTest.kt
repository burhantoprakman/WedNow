package com.maritsa.app.usecase

import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.usecase.AssignCoAdminUseCase
import com.maritsa.app.fake.FakeGuestRepository
import com.maritsa.app.fake.FakeMembershipRepository
import com.maritsa.app.fake.TestData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AssignCoAdminUseCaseTest {

    private lateinit var guestRepository: FakeGuestRepository
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var useCase: AssignCoAdminUseCase

    private val weddingId = TestData.WEDDING_A_ID
    private val guestId = TestData.GUEST_UUID
    private val identityId = TestData.USER_UID

    @BeforeEach
    fun setUp() {
        guestRepository = FakeGuestRepository()
        membershipRepository = FakeMembershipRepository()
        useCase = AssignCoAdminUseCase(guestRepository, membershipRepository)

        // Seed a guest to be promoted
        runTest {
            guestRepository.addGuest(
                weddingId,
                com.maritsa.app.domain.model.Guest(
                    id = guestId,
                    name = "Test Guest",
                    role = GuestRole.GUEST,
                )
            )
        }
    }

    // ── COADMIN assignment ────────────────────────────────────────────────────

    @Nested
    inner class AssignCoAdmin {

        @Test
        fun `given valid guest, assigns COADMIN role in guest document`() = runTest {
            val result = useCase(weddingId = weddingId, guestId = guestId)

            assertTrue(result.isSuccess)
            val guest = guestRepository.getGuest(weddingId, guestId)
            assertEquals(GuestRole.COADMIN, guest?.role)
        }

        @Test
        fun `given identityId provided, updates membership role to COADMIN`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = weddingId,
                    identityId = identityId,
                    role = GuestRole.GUEST,
                )
            )

            useCase(weddingId = weddingId, guestId = guestId, identityId = identityId)

            val membership = membershipRepository.getMembership(identityId, weddingId)
            assertEquals(GuestRole.COADMIN, membership?.role)
        }

        @Test
        fun `given blank identityId, only updates guest document`() = runTest {
            useCase(weddingId = weddingId, guestId = guestId, identityId = "")

            val guest = guestRepository.getGuest(weddingId, guestId)
            assertEquals(GuestRole.COADMIN, guest?.role)
            // No memberships should have been created
            assertEquals(0, membershipRepository.totalMemberships())
        }

        @Test
        fun `returns success result when assignment succeeds`() = runTest {
            val result = useCase(weddingId = weddingId, guestId = guestId)

            assertTrue(result.isSuccess)
        }
    }

    // ── Revocation ────────────────────────────────────────────────────────────

    @Nested
    inner class RevokeCoAdmin {

        @Test
        fun `given revoke=true, downgrades COADMIN back to GUEST`() = runTest {
            // First assign
            useCase(weddingId = weddingId, guestId = guestId)
            assertEquals(GuestRole.COADMIN, guestRepository.getGuest(weddingId, guestId)?.role)

            // Then revoke
            val result = useCase(weddingId = weddingId, guestId = guestId, revoke = true)

            assertTrue(result.isSuccess)
            assertEquals(GuestRole.GUEST, guestRepository.getGuest(weddingId, guestId)?.role)
        }

        @Test
        fun `given revoke=true with identityId, downgrades membership role to GUEST`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = weddingId,
                    identityId = identityId,
                    role = GuestRole.COADMIN,
                )
            )

            useCase(
                weddingId = weddingId,
                guestId = guestId,
                identityId = identityId,
                revoke = true
            )

            val membership = membershipRepository.getMembership(identityId, weddingId)
            assertEquals(GuestRole.GUEST, membership?.role)
        }
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Nested
    inner class ErrorHandling {

        @Test
        fun `given guest not found, returns failure`() = runTest {
            val result = useCase(weddingId = weddingId, guestId = "non-existent-guest")

            assertTrue(result.isFailure)
        }

        @Test
        fun `given guest not found, does not update membership`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(weddingId = weddingId, identityId = identityId)
            )

            useCase(weddingId = weddingId, guestId = "non-existent", identityId = identityId)

            // Role should be unchanged
            val membership = membershipRepository.getMembership(identityId, weddingId)
            assertEquals(GuestRole.GUEST, membership?.role)
        }
    }
}
