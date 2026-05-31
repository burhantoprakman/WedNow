package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the FCM push token for a guest.
 *
 * Uses SET + MERGE so the call is safe to make before the guest document is fully
 * created (e.g. token rotation fires before the join flow completes on a reinstall).
 *
 * Also writes [identityId] alongside [token] so Cloud Functions can look up the
 * correct guest document even after sign-in changes the identity from a UUID to a
 * Firebase UID:
 *
 *   guests/{guestId}.fcmToken    — used for FCM delivery
 *   guests/{guestId}.identityId  — used by Cloud Functions' fallback query
 *                                   `where("identityId", "==", recipientId)`
 */
@Singleton
class FcmTokenService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * @param weddingId  The wedding the guest belongs to.
     * @param guestId    Firestore document ID of the guest (UUID or Firebase UID).
     * @param identityId The current identity ID (UUID for anonymous, Firebase UID after sign-in).
     * @param token      The FCM registration token.
     */
    suspend fun saveToken(
        weddingId: String,
        guestId: String,
        identityId: String,
        token: String,
    ): Result<Unit> = runCatching {
        firestore.collection("weddings")
            .document(weddingId)
            .collection("guests")
            .document(guestId)
            .set(
                mapOf(
                    "fcmToken" to token,
                    "identityId" to identityId,
                ),
                SetOptions.merge(),
            )
            .await()
    }
}
