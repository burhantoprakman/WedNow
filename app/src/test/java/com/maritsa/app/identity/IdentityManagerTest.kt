package com.maritsa.app.identity

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.identity.IdentityMigrationService
import com.maritsa.app.core.identity.IdentityPreferences
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.model.AuthProvider
import com.maritsa.app.domain.model.Identity
import com.maritsa.app.domain.model.IdentityType
import com.maritsa.app.fake.TestData
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IdentityManagerTest {

    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var migrationService: IdentityMigrationService
    private val authListenerSlot = slot<FirebaseAuth.AuthStateListener>()

    // Captured from IdentityPreferences.save() calls
    private val savedIdentities = mutableListOf<Identity>()

    @BeforeEach
    fun setUp() {
        mockkObject(IdentityPreferences)
        mockkObject(GuestSessionManager)

        context = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        migrationService = mockk(relaxed = true)

        // Capture the auth listener registered in IdentityManager.init
        every { firebaseAuth.addAuthStateListener(capture(authListenerSlot)) } just Runs

        // Default: no stored identity; legacy guest ID available
        every { IdentityPreferences.load(context) } returns null
        every { IdentityPreferences.save(context, any()) } answers {
            savedIdentities.add(secondArg())
            Unit
        }
        every { IdentityPreferences.clear(context) } just Runs

        every { GuestSessionManager.getGuestId(context) } returns TestData.GUEST_UUID
        every { GuestSessionManager.saveGuestId(context, any()) } just Runs
        every { GuestSessionManager.saveGuestName(context, any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        savedIdentities.clear()
        unmockkAll()
    }

    private fun buildManager() = IdentityManager(context, firebaseAuth, migrationService)

    // ── Initial identity ───────────────────────────────────────────────────────

    @Nested
    inner class InitialIdentity {

        @Test
        fun `given no stored identity, creates GUEST from legacy guestId`() {
            val manager = buildManager()

            val identity = manager.currentIdentity

            assertEquals(IdentityType.GUEST, identity.type)
            assertEquals(TestData.GUEST_UUID, identity.identityId)
            assertEquals(AuthProvider.NONE, identity.provider)
        }

        @Test
        fun `given stored GUEST identity in IdentityPreferences, restores it`() {
            val stored = TestData.guestIdentity(id = "stored-guest-id")
            every { IdentityPreferences.load(context) } returns stored

            val manager = buildManager()

            assertEquals("stored-guest-id", manager.currentIdentityId)
            assertEquals(IdentityType.GUEST, manager.currentIdentity.type)
        }

        @Test
        fun `given stored USER identity in IdentityPreferences, restores it`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored

            val manager = buildManager()

            assertEquals(TestData.USER_UID, manager.currentIdentityId)
            assertEquals(IdentityType.USER, manager.currentIdentity.type)
            assertTrue(manager.isAuthenticated)
        }

        @Test
        fun `identity StateFlow always emits a non-null value`() = runTest {
            val manager = buildManager()

            val identity = manager.identity.first()

            assertNotNull(identity)
        }

        @Test
        fun `initial identity is never null`() {
            val manager = buildManager()

            assertNotNull(manager.currentIdentity)
        }
    }

    // ── onSignIn ──────────────────────────────────────────────────────────────

    @Nested
    inner class OnSignIn {

        @Test
        fun `given GUEST identity, onSignIn upgrades to USER`() {
            val manager = buildManager()
            val authUser = TestData.authUser(uid = TestData.USER_UID)

            manager.onSignIn(authUser)

            assertEquals(IdentityType.USER, manager.currentIdentity.type)
            assertEquals(TestData.USER_UID, manager.currentIdentityId)
            assertTrue(manager.isAuthenticated)
        }

        @Test
        fun `given GUEST identity, onSignIn preserves provider as GOOGLE`() {
            val manager = buildManager()
            val authUser = TestData.authUser(uid = TestData.USER_UID, provider = "google.com")

            manager.onSignIn(authUser)

            assertEquals(AuthProvider.GOOGLE, manager.currentIdentity.provider)
        }

        @Test
        fun `given GUEST identity, onSignIn preserves display name`() {
            val manager = buildManager()
            val authUser = TestData.authUser(displayName = "Jane Doe")

            manager.onSignIn(authUser)

            assertEquals("Jane Doe", manager.currentIdentity.displayName)
        }

        @Test
        fun `given GUEST identity, onSignIn links guestId as linkedGuestId`() {
            val manager = buildManager()
            val authUser = TestData.authUser(uid = TestData.USER_UID)

            manager.onSignIn(authUser)

            assertEquals(TestData.GUEST_UUID, manager.currentIdentity.linkedGuestId)
        }

        @Test
        fun `given already USER with same UID, onSignIn is idempotent`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()
            savedIdentities.clear() // reset tracking after init

            val authUser = TestData.authUser(uid = TestData.USER_UID)
            manager.onSignIn(authUser)

            // Should not emit a new save for an identical identity
            assertTrue(savedIdentities.isEmpty(), "No save expected for idempotent sign-in")
        }

        @Test
        fun `onSignIn saves new USER identity to IdentityPreferences`() {
            val manager = buildManager()
            savedIdentities.clear()

            manager.onSignIn(TestData.authUser(uid = TestData.USER_UID))

            val saved = savedIdentities.firstOrNull()
            assertNotNull(saved)
            assertEquals(IdentityType.USER, saved!!.type)
        }

        @Test
        fun `onSignIn triggers migration in background`() = runTest {
            val manager = buildManager()
            val authUser = TestData.authUser(uid = TestData.USER_UID)

            manager.onSignIn(authUser)

            // Allow background IO coroutine to execute
            delay(200)
            coVerify(timeout = 2000) {
                migrationService.migrate(guestId = TestData.GUEST_UUID, userIdentity = any())
            }
        }
    }

    // ── onSignOut ─────────────────────────────────────────────────────────────

    @Nested
    inner class OnSignOut {

        @Test
        fun `given USER identity, onSignOut creates a fresh GUEST`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()

            manager.onSignOut()

            assertEquals(IdentityType.GUEST, manager.currentIdentity.type)
            assertFalse(manager.isAuthenticated)
        }

        @Test
        fun `given USER identity, onSignOut UUID differs from previous identityId`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()

            manager.onSignOut()

            assertNotEquals(TestData.USER_UID, manager.currentIdentityId)
        }

        @Test
        fun `given GUEST identity, onSignOut still creates a brand-new GUEST UUID`() {
            val manager = buildManager()
            val firstGuestId = manager.currentIdentityId

            manager.onSignOut()

            assertNotEquals(firstGuestId, manager.currentIdentityId)
        }

        @Test
        fun `after sign-out, isAuthenticated is false`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()

            manager.onSignOut()

            assertFalse(manager.isAuthenticated)
        }

        @Test
        fun `after sign-out, new guest ID is saved to GuestSessionManager`() {
            val manager = buildManager()

            manager.onSignOut()

            verify { GuestSessionManager.saveGuestId(context, neq(TestData.GUEST_UUID)) }
        }
    }

    // ── Firebase AuthStateListener ────────────────────────────────────────────

    @Nested
    inner class FirebaseAuthStateListener {

        private fun buildFirebaseUser(
            uid: String = TestData.USER_UID,
            displayName: String? = "Test User",
            email: String? = "test@example.com",
        ): FirebaseUser {
            val user = mockk<FirebaseUser>(relaxed = true)
            every { user.uid } returns uid
            every { user.displayName } returns displayName
            every { user.email } returns email
            every { user.photoUrl } returns null
            every { user.providerData } returns emptyList()
            return user
        }

        @Test
        fun `given Firebase reports sign-in, upgrades GUEST to USER`() {
            val manager = buildManager()
            val firebaseUser = buildFirebaseUser()
            every { firebaseAuth.currentUser } returns firebaseUser

            authListenerSlot.captured.onAuthStateChanged(firebaseAuth)

            assertEquals(IdentityType.USER, manager.currentIdentity.type)
            assertEquals(TestData.USER_UID, manager.currentIdentityId)
        }

        @Test
        fun `given Firebase reports sign-out, creates new GUEST`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()
            every { firebaseAuth.currentUser } returns null

            authListenerSlot.captured.onAuthStateChanged(firebaseAuth)

            assertEquals(IdentityType.GUEST, manager.currentIdentity.type)
        }

        @Test
        fun `given already USER with same UID in Firebase, listener is a no-op`() {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()
            savedIdentities.clear()

            val firebaseUser = buildFirebaseUser(uid = TestData.USER_UID)
            every { firebaseAuth.currentUser } returns firebaseUser

            authListenerSlot.captured.onAuthStateChanged(firebaseAuth)

            assertTrue(savedIdentities.isEmpty(), "No save expected when identity already matches")
        }
    }

    // ── StateFlow ─────────────────────────────────────────────────────────────

    @Nested
    inner class IdentityStateFlow {

        @Test
        fun `identity StateFlow emits updated value after sign-in`() = runTest {
            val manager = buildManager()

            manager.onSignIn(TestData.authUser(uid = TestData.USER_UID))

            val emitted = manager.identity.first()
            assertEquals(IdentityType.USER, emitted.type)
        }

        @Test
        fun `identity StateFlow emits updated value after sign-out`() = runTest {
            val stored = TestData.userIdentity(uid = TestData.USER_UID)
            every { IdentityPreferences.load(context) } returns stored
            val manager = buildManager()

            manager.onSignOut()

            val emitted = manager.identity.first()
            assertEquals(IdentityType.GUEST, emitted.type)
        }
    }
}
