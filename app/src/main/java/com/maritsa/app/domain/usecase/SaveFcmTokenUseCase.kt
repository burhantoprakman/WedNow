package com.maritsa.app.domain.usecase

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.data.remote.FcmTokenService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fetches the current FCM token from Firebase and persists it to the guest document
 * in Firestore so Cloud Functions can deliver push notifications.
 *
 * Both [guestId] (the Firestore document ID, which may be a UUID) and [identityId]
 * (the canonical identity — Firebase UID after sign-in) are stored so Cloud Functions
 * can find the token via either a direct doc-ID lookup or the fallback
 * `where("identityId", "==", …)` query.
 */
class SaveFcmTokenUseCase @Inject constructor(
    private val fcmTokenService: FcmTokenService,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val identityId = identityManager.currentIdentityId
        val token = runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrElse { return Result.failure(it) }
        return fcmTokenService.saveToken(weddingId, guestId, identityId, token)
    }
}
