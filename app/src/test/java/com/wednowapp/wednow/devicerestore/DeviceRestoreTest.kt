package com.wednowapp.wednow.devicerestore

import android.content.Context
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.UserPreferences
import com.wednowapp.wednow.domain.usecase.GetWeddingMembershipsUseCase
import com.wednowapp.wednow.domain.usecase.SyncLastActiveWeddingUseCase
import com.wednowapp.wednow.fake.FakeMembershipRepository
import com.wednowapp.wednow.fake.FakeUserPreferencesRepository
import com.wednowapp.wednow.fake.TestData
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests that cover cross-device session restore scenarios:
 *
 *  1. USER signs in on a new device → last-active wedding ID restored from Firestore.
 *  2. USER signs in on a new device → all wedding memberships are available.
 *  3. GUEST on a new device → no cross-device restore attempted.
 */
class DeviceRestoreTest {

    private lateinit var context: Context
    private lateinit var identityManager: IdentityManager
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var syncLastActiveWeddingUseCase: SyncLastActiveWeddingUseCase
    private lateinit var getWeddingMembershipsUseCase: GetWeddingMembershipsUseCase

    @BeforeEach
    fun setUp() {
        mockkObject(WeddingSessionManager)

        context = mockk(relaxed = true)
        identityManager = mockk(relaxed = true)
        membershipRepository = FakeMembershipRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()

        every { WeddingSessionManager.getWeddingId(context) } returns null
        every { WeddingSessionManager.saveWeddingId(context, any()) } just Runs

        syncLastActiveWeddingUseCase = SyncLastActiveWeddingUseCase(
            context = context,
            identityManager = identityManager,
            userPreferencesRepository = userPreferencesRepository,
        )
        getWeddingMembershipsUseCase = GetWeddingMembershipsUseCase(
            membershipRepository = membershipRepository,
            identityManager = identityManager,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        membershipRepository.reset()
        userPreferencesRepository.reset()
    }

    // ── Last-active wedding restore ───────────────────────────────────────────

    @Nested
    inner class LastActiveWeddingRestore {

        @Test
        fun `given USER on new device, syncLastActiveWedding returns remote weddingId`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
            every { identityManager.currentIdentity } returns userIdentity
            every { identityManager.currentIdentityId } returns TestData.USER_UID
            userPreferencesRepository.seedPreferences(
                UserPreferences(
                    identityId = TestData.USER_UID,
                    lastActiveWeddingId = TestData.WEDDING_A_ID,
                )
            )

            val result = syncLastActiveWeddingUseCase()

            assertEquals(TestData.WEDDING_A_ID, result)
        }

        @Test
        fun `given USER on new device, remote weddingId is saved locally for future fast access`() =
            runTest {
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
                every { identityManager.currentIdentity } returns userIdentity
                every { identityManager.currentIdentityId } returns TestData.USER_UID
                userPreferencesRepository.seedPreferences(
                    UserPreferences(
                        identityId = TestData.USER_UID,
                        lastActiveWeddingId = TestData.WEDDING_B_ID,
                    )
                )

                syncLastActiveWeddingUseCase()

                verify { WeddingSessionManager.saveWeddingId(context, TestData.WEDDING_B_ID) }
            }

        @Test
        fun `given GUEST on new device, syncLastActiveWedding returns null`() = runTest {
            val guestIdentity = TestData.guestIdentity()
            every { identityManager.currentIdentity } returns guestIdentity
            every { identityManager.currentIdentityId } returns guestIdentity.identityId

            val result = syncLastActiveWeddingUseCase()

            assertNull(result)
        }

        @Test
        fun `given USER on device with local weddingId, returns local without hitting preferences`() =
            runTest {
                every { WeddingSessionManager.getWeddingId(context) } returns TestData.WEDDING_C_ID
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
                every { identityManager.currentIdentity } returns userIdentity
                every { identityManager.currentIdentityId } returns TestData.USER_UID

                val result = syncLastActiveWeddingUseCase()

                assertEquals(TestData.WEDDING_C_ID, result)
                assertEquals(0, userPreferencesRepository.fetchCount)
            }
    }

    // ── Membership restore ────────────────────────────────────────────────────

    @Nested
    inner class MembershipRestore {

        @Test
        fun `given USER signs in on new device, all memberships are accessible via membership index`() =
            runTest {
                every { identityManager.currentIdentityId } returns TestData.USER_UID
                // Simulate memberships that were migrated/recorded when user was on their original device
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

                val memberships = getWeddingMembershipsUseCase()

                assertEquals(2, memberships.size)
            }

        @Test
        fun `given USER with COADMIN role in one wedding, role is preserved on restore`() =
            runTest {
                every { identityManager.currentIdentityId } returns TestData.USER_UID
                membershipRepository.addMembership(
                    TestData.membership(
                        weddingId = TestData.WEDDING_A_ID,
                        identityId = TestData.USER_UID,
                        role = GuestRole.COADMIN,
                    )
                )

                val memberships = getWeddingMembershipsUseCase()
                val wedding = memberships.find { it.weddingId == TestData.WEDDING_A_ID }

                assertEquals(GuestRole.COADMIN, wedding?.role)
            }

        @Test
        fun `given GUEST on new device, getWeddingMemberships returns empty`() = runTest {
            every { identityManager.currentIdentityId } returns "fresh-guest-uuid"

            val memberships = getWeddingMembershipsUseCase()

            assertTrue(memberships.isEmpty())
        }
    }

    // ── Content permissions after restore ────────────────────────────────────

    @Nested
    inner class ContentPermissionsAfterRestore {

        @Test
        fun `given USER restored on new device, content ownership matches USER UID`() {
            // After migration, all content has ownerIdentityId = USER_UID
            // On new device with same sign-in, identity.currentIdentityId == USER_UID
            every { identityManager.currentIdentityId } returns TestData.USER_UID

            val photo = TestData.photo(
                ownerIdentityId = TestData.USER_UID,
                ownerUserId = TestData.USER_UID,
            )
            val canEdit = com.wednowapp.wednow.domain.model.ContentPermissions.canEdit(
                ownerIdentityId = photo.ownerIdentityId,
                currentIdentityId = identityManager.currentIdentityId,
            )

            assertTrue(canEdit)
        }

        @Test
        fun `given USER on new device editing own content, permission resolves correctly`() {
            every { identityManager.currentIdentityId } returns TestData.USER_UID

            // Content written on original device post-migration
            val post = TestData.guestbookPost(
                ownerIdentityId = TestData.USER_UID,
                ownerUserId = TestData.USER_UID,
            )

            val canDelete = com.wednowapp.wednow.domain.model.ContentPermissions.canDelete(
                ownerIdentityId = post.ownerIdentityId,
                currentIdentityId = identityManager.currentIdentityId,
                role = GuestRole.GUEST,
            )

            assertTrue(canDelete)
        }
    }
}
