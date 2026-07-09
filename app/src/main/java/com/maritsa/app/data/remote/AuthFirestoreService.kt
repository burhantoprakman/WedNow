package com.maritsa.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maritsa.app.domain.model.AuthUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the `users/{uid}` Firestore collection used to link a Firebase Auth UID
 * to the app's anonymous guestId (UUID stored in SharedPreferences).
 *
 * This enables cross-device continuity: a user who joins a wedding on one device
 * and later installs the app on a second device will recover the same guestId
 * (and therefore their existing RSVP, messages, photos) by signing in.
 */
@Singleton
class AuthFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Returns the guestId previously linked to [uid], or null if this is the
     * user's first sign-in on any device.
     */
    suspend fun getGuestIdForUid(uid: String): String? = runCatching {
        usersCollection.document(uid).get().await().getString("guestId")
    }.getOrNull()

    /**
     * Writes (or merges) a uid → guestId mapping together with the user's
     * display name and provider for reference.  Idempotent.
     */
    suspend fun linkUidToGuestId(uid: String, guestId: String, user: AuthUser) {
        runCatching {
            usersCollection.document(uid).set(
                mapOf(
                    "guestId" to guestId,
                    "displayName" to (user.displayName ?: ""),
                    "email" to (user.email ?: ""),
                    "provider" to user.provider,
                    "linkedAt" to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            ).await()
        }
        // best-effort; never surface to UI
    }
}
