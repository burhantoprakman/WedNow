package com.maritsa.app.edge

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.identity.IdentityMigrationService
import com.maritsa.app.core.identity.IdentityPreferences
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.ContentPermissions
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.IdentityType
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EdgeCaseTest {

    private lateinit var context: Context
    private lateinit var weddingRepository: FakeWeddingRepository
    private lateinit var guestRepository: FakeGuestRepository
    private lateinit var guestGroupRepository: FakeGuestGroupRepository
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var saveFcmToken: SaveFcmTokenUseCase
    private lateinit var joinWeddingUseCase: JoinWeddingUseCase
    private lateinit var identityManager: IdentityManager

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        mockkObject(GuestSessionManager)
        mockkObject(WeddingSessionManager)
        mockkObject(IdentityPreferences)

        context = mockk(relaxed = true)
        weddingRepository = FakeWeddingRepository()
        guestRepository = FakeGuestRepository()
        guestGroupRepository = FakeGuestGroupRepository()
        membershipRepository = FakeMembershipRepository()
        saveFcmToken = mockk(relaxed = true)
        identityManager = mockk(relaxed = true)

        every { GuestSessionManager.getGuestId(context) } returns TestData.GUEST_UUID
        every { GuestSessionManager.saveGuestName(context, any()) } just Runs
        every { GuestSessionManager.saveGuestId(context, any()) } just Runs
        every { WeddingSessionManager.saveWeddingId(context, any()) } just Runs
        every { IdentityPreferences.load(context) } returns null
        every { IdentityPreferences.save(context, any()) } just Runs
        every { identityManager.currentIdentityId } returns TestData.GUEST_UUID
        coEvery { saveFcmToken.invoke(any()) } returns Result.success(Unit)

        joinWeddingUseCase = JoinWeddingUseCase(
            weddingRepository = weddingRepository,
            guestRepository = guestRepository,
            guestGroupRepository = guestGroupRepository,
            membershipRepository = membershipRepository,
            identityManager = identityManager,
            saveFcmTokenUseCase = saveFcmToken,
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

    // ── Duplicate join ────────────────────────────────────────────────────────

    @Nested
    inner class DuplicateJoin {

        @Test
        fun `given joining same wedding twice, second join overwrites guest record gracefully`() =
            runTest {
                val wedding = TestData.wedding(
                    id = TestData.WEDDING_A_ID,
                    shortCode = TestData.WEDDING_A_CODE,
                )
                weddingRepository.seedWedding(wedding)

                joinWeddingUseCase(TestData.WEDDING_A_CODE, "Alice")
                val result = joinWeddingUseCase(TestData.WEDDING_A_CODE, "Alice Updated")

                assertTrue(result.isSuccess)
            }

        @Test
        fun `given joining same wedding twice, only one membership record exists`() = runTest {
            val wedding = TestData.wedding(
                id = TestData.WEDDING_A_ID,
                shortCode = TestData.WEDDING_A_CODE,
            )
            weddingRepository.seedWedding(wedding)

            joinWeddingUseCase(TestData.WEDDING_A_CODE, "Alice")
            joinWeddingUseCase(TestData.WEDDING_A_CODE, "Alice")

            val memberships = membershipRepository.getMemberships(TestData.GUEST_UUID)
            assertEquals(1, memberships.size)
        }
    }

    // ── Invalid wedding code ──────────────────────────────────────────────────

    @Nested
    inner class InvalidWeddingCode {

        @Test
        fun `given completely invalid code, join returns failure`() = runTest {
            val result = joinWeddingUseCase("XXXXXX", "Guest")

            assertTrue(result.isFailure)
        }

        @Test
        fun `given empty string code, join returns failure`() = runTest {
            val result = joinWeddingUseCase("", "Guest")

            assertTrue(result.isFailure)
        }

        @Test
        fun `given whitespace-only code, join returns failure`() = runTest {
            val result = joinWeddingUseCase("   ", "Guest")

            assertTrue(result.isFailure)
        }

        @Test
        fun `given valid code after failed attempt, join succeeds on retry`() = runTest {
            // First attempt fails
            val failResult = joinWeddingUseCase("WRONGCODE", "Alice")
            assertTrue(failResult.isFailure)

            // Seed the real wedding
            val wedding = TestData.wedding(
                id = TestData.WEDDING_A_ID,
                shortCode = TestData.WEDDING_A_CODE,
            )
            weddingRepository.seedWedding(wedding)

            // Second attempt with correct code succeeds
            val successResult = joinWeddingUseCase(TestData.WEDDING_A_CODE, "Alice")
            assertTrue(successResult.isSuccess)
        }
    }

    // ── Content permissions edge cases ────────────────────────────────────────

    @Nested
    inner class ContentPermissionsEdgeCases {

        @Test
        fun `given content with blank ownerIdentityId AND blank ownerUserId, no one can edit`() {
            val canEdit = ContentPermissions.canEdit(
                ownerIdentityId = "",
                currentIdentityId = TestData.GUEST_UUID,
            )
            assertFalse(canEdit)
        }

        @Test
        fun `given legacy content where ownerIdentityId is blank, legacyOwnerId is used for delete check`() {
            val guestId = TestData.GUEST_UUID
            val canDelete = ContentPermissions.canDelete(
                ownerIdentityId = "",
                currentIdentityId = guestId,
                role = GuestRole.GUEST,
                legacyOwnerId = guestId,  // matches → owner can delete
            )
            assertTrue(canDelete)
        }

        @Test
        fun `given migrated content, new ownerIdentityId takes priority over legacy field`() {
            // After migration, ownerIdentityId == USER_UID
            // Legacy guestId still exists in uploadedBy/guestId field but should NOT grant access
            val canEdit = ContentPermissions.canEdit(
                ownerIdentityId = TestData.USER_UID,
                currentIdentityId = TestData.GUEST_UUID,  // guest tries to edit
            )
            assertFalse(canEdit)
        }
    }

    // ── Identity always available ─────────────────────────────────────────────

    @Nested
    inner class IdentityAlwaysAvailable {

        @Test
        fun `given fresh install, identity is immediately available as GUEST`() {
            val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
            val migrationService = mockk<IdentityMigrationService>(relaxed = true)
            every { firebaseAuth.addAuthStateListener(any()) } just Runs
            every { GuestSessionManager.getGuestId(context) } returns "fresh-device-uuid"

            val manager = IdentityManager(context, firebaseAuth, migrationService)

            assertNotNull(manager.currentIdentity)
            assertEquals(IdentityType.GUEST, manager.currentIdentity.type)
        }

        @Test
        fun `given rapid sign-in then sign-out, final state is consistent GUEST`() {
            val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
            val migrationService = mockk<IdentityMigrationService>(relaxed = true)
            every { firebaseAuth.addAuthStateListener(any()) } just Runs

            val manager = IdentityManager(context, firebaseAuth, migrationService)

            // Rapid sign-in → sign-out
            manager.onSignIn(TestData.authUser(uid = TestData.USER_UID))
            manager.onSignOut()

            assertEquals(IdentityType.GUEST, manager.currentIdentity.type)
            assertFalse(manager.isAuthenticated)
        }

        @Test
        fun `given sign-in twice with same UID, identity remains consistent`() {
            val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
            val migrationService = mockk<IdentityMigrationService>(relaxed = true)
            every { firebaseAuth.addAuthStateListener(any()) } just Runs
            every { IdentityPreferences.load(context) } returns null

            val manager = IdentityManager(context, firebaseAuth, migrationService)
            val authUser = TestData.authUser(uid = TestData.USER_UID)

            manager.onSignIn(authUser)
            val firstId = manager.currentIdentityId
            manager.onSignIn(authUser) // second call with same user — idempotent
            val secondId = manager.currentIdentityId

            assertEquals(firstId, secondId)
            assertEquals(TestData.USER_UID, secondId)
        }
    }

    // ── Migration with no content ─────────────────────────────────────────────

    @Nested
    inner class MigrationEdgeCases {

        @Test
        fun `given guest with no memberships joins and signs in, no error during migration`() =
            runTest {
                // Guest has no memberships in the index
                // membershipRepository.getMemberships returns empty list

                val fakeMigration: IdentityMigrationService = mockk {
                    coEvery { migrate(any(), any()) } returns Unit
                }
                val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
                every { firebaseAuth.addAuthStateListener(any()) } just Runs

                val manager = IdentityManager(context, firebaseAuth, fakeMigration)
                manager.onSignIn(TestData.authUser(uid = TestData.USER_UID))

                // Should not throw
                assertTrue(manager.isAuthenticated)
            }
    }

    // ── Role boundary cases ───────────────────────────────────────────────────

    @Nested
    inner class RoleBoundaryCases {

        @Test
        fun `given null role, no elevated permissions granted`() {
            val canDelete = ContentPermissions.canDelete(
                ownerIdentityId = "someone-else",
                currentIdentityId = TestData.GUEST_UUID,
                role = null,
            )
            assertFalse(canDelete)
        }

        @Test
        fun `given unknown role string, no elevated permissions granted`() {
            val canDelete = ContentPermissions.canDelete(
                ownerIdentityId = "someone-else",
                currentIdentityId = TestData.GUEST_UUID,
                role = "SUPERADMIN",  // unknown role
            )
            assertFalse(canDelete)
        }

        @Test
        fun `given ADMIN can delete any content regardless of ownerIdentityId`() {
            val canDelete = ContentPermissions.canDelete(
                ownerIdentityId = "random-user-id",
                currentIdentityId = TestData.USER_UID,
                role = GuestRole.ADMIN,
            )
            assertTrue(canDelete)
        }
    }
}
