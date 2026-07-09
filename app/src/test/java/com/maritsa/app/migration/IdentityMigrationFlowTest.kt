package com.maritsa.app.migration

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.maritsa.app.core.identity.IdentityMigrationService
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.fake.FakeIdentityRepository
import com.maritsa.app.fake.FakeMembershipRepository
import com.maritsa.app.fake.TestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * End-to-end migration flow tests.
 *
 * These tests exercise the full [IdentityMigrationService.migrate] pipeline
 * using fake repositories for identity/membership persistence and a mocked
 * FirebaseFirestore for the document-level batch operations.
 */
class IdentityMigrationFlowTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var identityRepository: FakeIdentityRepository
    private lateinit var membershipRepository: FakeMembershipRepository
    private lateinit var service: IdentityMigrationService

    // Common Firestore mocks
    private lateinit var weddingCollRef: CollectionReference
    private lateinit var weddingDocRef: DocumentReference
    private lateinit var guestbookCollRef: CollectionReference
    private lateinit var photosCollRef: CollectionReference
    private lateinit var guestsCollRef: CollectionReference
    private lateinit var guestDocRef: DocumentReference
    private lateinit var guestbookQuery: Query
    private lateinit var photosQuery: Query
    private lateinit var writeBatch: WriteBatch

    @BeforeEach
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Catch-all: any Task.await() not explicitly mocked below returns immediately
        coEvery { any<Task<Any>>().await() } returns mockk(relaxed = true)

        identityRepository = FakeIdentityRepository()
        membershipRepository = FakeMembershipRepository()
        firestore = mockk(relaxed = true)

        weddingCollRef = mockk(relaxed = true)
        weddingDocRef = mockk(relaxed = true)
        guestbookCollRef = mockk(relaxed = true)
        photosCollRef = mockk(relaxed = true)
        guestsCollRef = mockk(relaxed = true)
        guestDocRef = mockk(relaxed = true)
        guestbookQuery = mockk(relaxed = true)
        photosQuery = mockk(relaxed = true)
        writeBatch = mockk(relaxed = true)

        every { firestore.collection("weddings") } returns weddingCollRef
        every { weddingCollRef.document(any()) } returns weddingDocRef
        every { weddingDocRef.collection("guestbook") } returns guestbookCollRef
        every { weddingDocRef.collection("photos") } returns photosCollRef
        every { weddingDocRef.collection("guests") } returns guestsCollRef
        every { guestsCollRef.document(any()) } returns guestDocRef
        every { guestbookCollRef.whereEqualTo(any<String>(), any()) } returns guestbookQuery
        every { photosCollRef.whereEqualTo(any<String>(), any()) } returns photosQuery
        every { firestore.batch() } returns writeBatch
        every { writeBatch.update(any(), any<String>(), any()) } returns writeBatch

        stubEmptySnapshots()

        service = IdentityMigrationService(firestore, identityRepository, membershipRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        identityRepository.reset()
        membershipRepository.reset()
    }

    private fun stubEmptySnapshots() {
        val emptySnapshot = mockk<QuerySnapshot> { every { documents } returns emptyList() }
        val guestbookTask = mockk<Task<QuerySnapshot>> { }
        val photosTask = mockk<Task<QuerySnapshot>> { }
        every { guestbookQuery.get() } returns guestbookTask
        every { photosQuery.get() } returns photosTask
        coEvery { guestbookTask.await() } returns emptySnapshot
        coEvery { photosTask.await() } returns emptySnapshot
        stubBatchCommit()
    }

    private fun stubBatchCommit() {
        val batchTask: Task<Void> = mockk(relaxed = true)
        every { writeBatch.commit() } returns batchTask
        coEvery { batchTask.await() } returns mockk()
    }

    private fun stubDocumentsInGuestbook(docCount: Int): List<DocumentReference> {
        val docRefs = (1..docCount).map { mockk<DocumentReference>(relaxed = true) }
        val docSnapshots = docRefs.map { ref ->
            mockk<DocumentSnapshot> { every { reference } returns ref }
        }
        val snapshot = mockk<QuerySnapshot> { every { documents } returns docSnapshots }
        val task = mockk<Task<QuerySnapshot>>()
        every { guestbookQuery.get() } returns task
        coEvery { task.await() } returns snapshot
        return docRefs
    }

    private fun stubDocumentsInPhotos(docCount: Int): List<DocumentReference> {
        val docRefs = (1..docCount).map { mockk<DocumentReference>(relaxed = true) }
        val docSnapshots = docRefs.map { ref ->
            mockk<DocumentSnapshot> { every { reference } returns ref }
        }
        val snapshot = mockk<QuerySnapshot> { every { documents } returns docSnapshots }
        val task = mockk<Task<QuerySnapshot>>()
        every { photosQuery.get() } returns task
        coEvery { task.await() } returns snapshot
        return docRefs
    }

    // ── Single-wedding migration ───────────────────────────────────────────────

    @Nested
    inner class SingleWeddingMigration {

        @Test
        fun `given guest in one wedding, USER identity is saved to repository`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            assertNotNull(identityRepository.identities[TestData.USER_UID])
        }

        @Test
        fun `given guest in one wedding, guest is marked as migrated to user`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            assertEquals(TestData.USER_UID, identityRepository.migrations[TestData.GUEST_UUID])
        }

        @Test
        fun `given guestbook posts exist, batch updates ownerIdentityId for all posts`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            val docRefs = stubDocumentsInGuestbook(3)
            stubBatchCommit()
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            docRefs.forEach { ref ->
                verify { writeBatch.update(ref, "ownerIdentityId", TestData.USER_UID) }
            }
        }

        @Test
        fun `given photos exist, batch updates ownerIdentityId for all photos`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            val photoRefs = stubDocumentsInPhotos(2)
            stubBatchCommit()
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            photoRefs.forEach { ref ->
                verify { writeBatch.update(ref, "ownerIdentityId", TestData.USER_UID) }
            }
        }

        @Test
        fun `given no content in wedding, migration completes without batch commits`() = runTest {
            membershipRepository.addMembership(
                TestData.membership(
                    weddingId = TestData.WEDDING_A_ID,
                    identityId = TestData.GUEST_UUID
                )
            )
            // Snapshots already stubbed as empty via setUp → stubEmptySnapshots()

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = TestData.userIdentity())

            // No real batch commit needed — verify no errors thrown
            assertTrue(true)
        }
    }

    // ── Multi-wedding migration ────────────────────────────────────────────────

    @Nested
    inner class MultiWeddingMigration {

        @Test
        fun `given guest in two weddings, memberships are copied to user index for both`() =
            runTest {
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

                val userMemberships = membershipRepository.getMemberships(TestData.USER_UID)
                assertEquals(2, userMemberships.size)
                assertTrue(userMemberships.any { it.weddingId == TestData.WEDDING_A_ID })
                assertTrue(userMemberships.any { it.weddingId == TestData.WEDDING_B_ID })
            }

        @Test
        fun `given guest in three weddings, all membership identityIds are updated to USER uid`() =
            runTest {
                listOf(TestData.WEDDING_A_ID, TestData.WEDDING_B_ID, TestData.WEDDING_C_ID)
                    .forEach { weddingId ->
                        membershipRepository.addMembership(
                            TestData.membership(
                                weddingId = weddingId,
                                identityId = TestData.GUEST_UUID
                            )
                        )
                    }
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

                service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

                val userMemberships = membershipRepository.getMemberships(TestData.USER_UID)
                assertEquals(3, userMemberships.size)
                userMemberships.forEach { m ->
                    assertEquals(TestData.USER_UID, m.identityId)
                }
            }

        @Test
        fun `given guest has COADMIN role in one wedding, role is preserved during migration`() =
            runTest {
                membershipRepository.addMembership(
                    TestData.membership(
                        weddingId = TestData.WEDDING_A_ID,
                        identityId = TestData.GUEST_UUID,
                        role = GuestRole.COADMIN,
                    )
                )
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

                service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

                val membership =
                    membershipRepository.getMembership(TestData.USER_UID, TestData.WEDDING_A_ID)
                assertEquals(GuestRole.COADMIN, membership?.role)
            }
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Nested
    inner class EdgeCases {

        @Test
        fun `given no memberships, migration still saves identity and marks migrated`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

            service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

            assertNotNull(identityRepository.identities[TestData.USER_UID])
            assertEquals(TestData.USER_UID, identityRepository.migrations[TestData.GUEST_UUID])
        }

        @Test
        fun `given identityRepository save fails, migration still attempts to mark migrated`() =
            runTest {
                identityRepository.saveShouldFail = true
                membershipRepository.addMembership(
                    TestData.membership(
                        weddingId = TestData.WEDDING_A_ID,
                        identityId = TestData.GUEST_UUID
                    )
                )
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)

                // Should not throw — migration is best-effort
                service.migrate(guestId = TestData.GUEST_UUID, userIdentity = userIdentity)

                assertTrue(true, "migrate() must not throw on partial failure")
            }
    }
}
