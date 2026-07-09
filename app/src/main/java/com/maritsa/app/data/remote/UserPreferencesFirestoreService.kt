package com.maritsa.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maritsa.app.domain.model.UserPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists cross-device preferences under  userPreferences/{identityId}.
 *
 * Only USER (authenticated) identities write to Firestore — guest preferences
 * are kept in [WeddingSessionManager] (local SharedPreferences).
 *
 * Document schema:
 * {
 *   lastActiveWeddingId : String?  — wedding to open on next launch
 *   updatedAt           : Long
 * }
 */
@Singleton
class UserPreferencesFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    private val col = firestore.collection("userPreferences")

    /** Returns the stored preferences for [identityId], or null if none exist. */
    suspend fun getPreferences(identityId: String): Result<UserPreferences?> = runCatching {
        val doc = col.document(identityId).get().await()
        if (!doc.exists()) null
        else UserPreferences(
            identityId = identityId,
            lastActiveWeddingId = doc.getString("lastActiveWeddingId"),
            updatedAt = doc.getLong("updatedAt") ?: 0L,
        )
    }

    /** Writes (or merges) the lastActiveWeddingId for [identityId]. */
    suspend fun updateLastActiveWedding(
        identityId: String,
        weddingId: String,
    ): Result<Unit> = runCatching {
        col.document(identityId).set(
            mapOf(
                "lastActiveWeddingId" to weddingId,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }
}
