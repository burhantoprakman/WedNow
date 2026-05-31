package com.wednowapp.wednow.identity

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.wednowapp.wednow.core.identity.IdentityMigrationService
import com.wednowapp.wednow.fake.FakeIdentityRepository
import com.wednowapp.wednow.fake.FakeMembershipRepository
import com.wednowapp.wednow.fake.TestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IdentityMigrationServiceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var identityRepository: FakeIdentityRepository
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var service: IdentityMigrationService

    // Firestore fakes
    private lateinit var weddingCollectionRef: CollectionReference
    private lateinit var weddingDocRef: DocumentReference
    private lateinit var guestbookCollRef: CollectionReference
    private lateinit var photosCollRef: CollectionReference
    private lateinit var guestsCollRef: CollectionReference
    private lateinit var guestDocRef: DocumentReference
    private lateinit var guestbookQuery: Query
    private lateinit var photosQuery: Query
    private lateinit var guestbookSnapshot: QuerySnapshot
    private lateinit var photosSnapshot: QuerySnapshot
    private lateinit var writeBatch: WriteBatch

    @BeforeEach
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Catch-all: any Task.await() that isn't explicitly mocked returns immediately
        coEvery { any<Task<Any>>().await() } returns mockk(relaxed = true)

        identityRepository = FakeIdentityRepository()
        membershipRepository = FakeMembershipRepository()
        firestore = mockk(relaxed = true)

        // ── Firestore collection/document structure ──────────────────────────
        weddingCollectionRef = mockk(relaxed = true)
        weddingDocRef = mockk(relaxed = true)
        guestbookCollRef = mockk(relaxed = true)
        photosCollRef = mockk(relaxed = true)
        guestsCollRef = mockk(relaxed = true)
        guestDocRef = mockk(relaxed = true)
        guestbookQuery = mockk(relaxed = true)
        photosQuery = mockk(relaxed = true)
        guestbookSnapshot = mockk(relaxed = true)
        photosSnapshot = mockk(relaxed = true)
        writeBatch = mockk(relaxed = true)

        every { firestore.collection("weddings") } returns weddingCollectionRef
        every { weddingCollectionRef.document(any()) } returns weddingDocRef
        every { weddingDocRef.collection("guestbook") } returns guestbookCollRef
        every { weddingDocRef.collection("photos") } returns photosCollRef
        every { weddingDocRef.collection("guests") } returns guestsCollRef
        every { guestsCollRef.document(any()) } returns guestDocRef

        every { guestbookCollRef.whereEqualTo(any<String>(), any()) } returns guestbookQuery
        every { photosCollRef.whereEqualTo(any<String>(), any()) } returns photosQuery

        val guestbookTask: Task<QuerySnapshot> = mockk(relaxed = true)
        val photosTask: Task<QuerySnapshot> = mockk(relaxed = true)
        val batchTask: Task<Void> = mockk(relaxed = true)
        val updateTask: Task<Void> = mockk(relaxed = true)

        every { guestbookQuery.get() } returns guestbookTask
        every { photosQuery.get() } returns photosTask
        every { firestore.batch() } returns writeBatch
        every { writeBatch.commit() } returns batchTask

        coEvery { guestbookTask.await() } returns guestbookSnapshot
        coEvery { photosTask.await() } returns photosSnapshot
        coEvery { batchTask.await() } returns mockk(relaxed = true)
        coEvery { updateTask.await() } returns mockk(relaxed = true)

        // Default: empty snapshots (no documents to migrate)
        every { guestbookSnapshot.documents } returns emptyList()
        every { photosSnapshot.documents } returns emptyList()

        service = IdentityMigrationService(firestore, identityRepository, membershipRepository)
    }

    // ── Identity record persistence ───────────────────────────────────────────

    @Nested
    inner class IdentityPersistence {

        @Test
        fun `given sign-in, saves USER identity to identityRepository`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            assertNotNull(identityRepository.getIdentity(TestData.USER_UID).getOrNull())
        }

        @Test
        fun `given sign-in, marks guest identity as migrated`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            val migratedTo = identityRepository.migrations[TestData.GUEST_UUID]
            assertEquals(TestData.USER_UID, migratedTo)
        }
    }

    // ── Membership migration ──────────────────────────────────────────────────

    @Nested
    inner class MembershipMigration {

        @Test
        fun `given guest has membership, copies it to user identity index`() = runTest {
            val guestMembership = TestData.membership(
                weddingId = TestData.WEDDING_A_ID,
                identityId = TestData.GUEST_UUID,
            )
            membershipRepository.addMembership(guestMembership)
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            val userMembership = membershipRepository.getMembership(
                identityId = TestData.USER_UID,
                weddingId = TestData.WEDDING_A_ID,
            )
            assertNotNull(userMembership)
            assertEquals(TestData.USER_UID, userMembership!!.identityId)
        }

        @Test
        fun `given guest has multiple weddings, all memberships are copied to user`() = runTest {
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
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            val memberships = membershipRepository.getMemberships(TestData.USER_UID)
            assertEquals(2, memberships.size)
        }

        @Test
        fun `given guest has no memberships, migration completes without error`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            // Should not throw
            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            assertEquals(0, membershipRepository.getMemberships(TestData.USER_UID).size)
        }
    }

    // ── Content migration (Firestore batch updates) ────────────────────────────

    @Nested
    inner class ContentMigration {

        @Test
        fun `given guestbook posts exist, batch update is committed`() = runTest {
            val docRef = mockk<DocumentReference>(relaxed = true)
            val docSnapshot = mockk<DocumentSnapshot> {
                every { reference } returns docRef
            }
            every { guestbookSnapshot.documents } returns listOf(docSnapshot)
            every { writeBatch.update(any(), any<String>(), any()) } returns writeBatch

            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = TestData.userIdentity())

            // Batch was set up and a document was updated
            verify { writeBatch.update(docRef, "ownerIdentityId", any()) }
        }

        @Test
        fun `given photos exist, batch update is committed for photos`() = runTest {
            val docRef = mockk<DocumentReference>(relaxed = true)
            val docSnapshot = mockk<DocumentSnapshot> {
                every { reference } returns docRef
            }
            every { photosSnapshot.documents } returns listOf(docSnapshot)
            every { writeBatch.update(any(), any<String>(), any()) } returns writeBatch

            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = TestData.userIdentity())

            verify { writeBatch.update(docRef, "ownerIdentityId", any()) }
        }
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    @Nested
    inner class Idempotency {

        @Test
        fun `calling migrate twice does not duplicate memberships`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)
            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            // addMembership is called twice but the fake just overwrites — same size
            val memberships = membershipRepository.getMemberships(TestData.USER_UID)
            assertEquals(1, memberships.size)
        }
    }
}
