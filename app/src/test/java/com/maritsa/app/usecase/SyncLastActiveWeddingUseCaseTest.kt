package com.maritsa.app.usecase

import android.content.Context
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.UserPreferences
import com.maritsa.app.domain.usecase.SyncLastActiveWeddingUseCase
import com.maritsa.app.fake.FakeUserPreferencesRepository
import com.maritsa.app.fake.TestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SyncLastActiveWeddingUseCaseTest {

    private lateinit var context: Context
    private lateinit var identityManager: IdentityManager
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var useCase: SyncLastActiveWeddingUseCase

    @BeforeEach
    fun setUp() {
        mockkObject(WeddingSessionManager)
        context = mockk(relaxed = true)
        identityManager = mockk(relaxed = true)
        userPreferencesRepository = FakeUserPreferencesRepository()

        useCase = SyncLastActiveWeddingUseCase(
            context = context,
            identityManager = identityManager,
            userPreferencesRepository = userPreferencesRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        userPreferencesRepository.reset()
    }

    // ── Local-first strategy ──────────────────────────────────────────────────

    @Nested
    inner class LocalFirstStrategy {

        @Test
        fun `given local weddingId exists, returns it immediately`() = runTest {
            every { WeddingSessionManager.getWeddingId(context) } returns TestData.WEDDING_A_ID

            val result = useCase()

            assertEquals(TestData.WEDDING_A_ID, result)
        }

        @Test
        fun `given local weddingId exists, does not call Firestore`() = runTest {
            every { WeddingSessionManager.getWeddingId(context) } returns TestData.WEDDING_A_ID

            useCase()

            // userPreferencesRepository should never be queried
            assertEquals(0, userPreferencesRepository.fetchCount)
        }
    }

    // ── Firestore restore (USER identity, new device) ─────────────────────────

    @Nested
    inner class FirestoreRestore {

        @BeforeEach
        fun givenNoLocalWeddingId() {
            every { WeddingSessionManager.getWeddingId(context) } returns null
            every { WeddingSessionManager.saveWeddingId(context, any()) } returns Unit
        }

        @Test
        fun `given USER identity and preferences store has weddingId, returns it`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
            every { identityManager.currentIdentity } returns userIdentity
            every { identityManager.currentIdentityId } returns TestData.USER_UID
            userPreferencesRepository.seedPreferences(
                UserPreferences(
                    identityId = TestData.USER_UID,
                    lastActiveWeddingId = TestData.WEDDING_B_ID,
                )
            )

            val result = useCase()

            assertEquals(TestData.WEDDING_B_ID, result)
        }

        @Test
        fun `given USER identity and preferences store returns weddingId, saves locally`() =
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

                useCase()

                verify { WeddingSessionManager.saveWeddingId(context, TestData.WEDDING_B_ID) }
            }

        @Test
        fun `given USER identity but no stored pref, returns null`() = runTest {
            val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
            every { identityManager.currentIdentity } returns userIdentity
            every { identityManager.currentIdentityId } returns TestData.USER_UID
            // No preferences seeded

            val result = useCase()

            assertNull(result)
        }

        @Test
        fun `given GUEST identity, returns null without querying preferences`() = runTest {
            val guestIdentity = TestData.guestIdentity()
            every { identityManager.currentIdentity } returns guestIdentity
            every { identityManager.currentIdentityId } returns guestIdentity.identityId

            val result = useCase()

            assertNull(result)
            assertEquals(0, userPreferencesRepository.fetchCount)
        }

        @Test
        fun `given USER identity and prefs have null lastActiveWeddingId, returns null`() =
            runTest {
                val userIdentity = TestData.userIdentity(uid = TestData.USER_UID)
                every { identityManager.currentIdentity } returns userIdentity
                every { identityManager.currentIdentityId } returns TestData.USER_UID
                userPreferencesRepository.seedPreferences(
                    UserPreferences(identityId = TestData.USER_UID, lastActiveWeddingId = null)
                )

                val result = useCase()

                assertNull(result)
            }
    }
}
