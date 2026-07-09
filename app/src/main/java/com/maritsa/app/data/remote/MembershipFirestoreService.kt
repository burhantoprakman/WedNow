package com.maritsa.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maritsa.app.domain.model.WeddingMembership
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the  memberships/{identityId}/weddings/{weddingId}  sub-collection.
 *
 * This top-level index allows authenticated users to enumerate all their
 * weddings on any device, immediately after sign-in, without iterating every
 * wedding document in the database.
 *
 * Path:   memberships / {identityId} / weddings / {weddingId}
 *
 * Document schema:
 * {
 *   weddingId  : String
 *   identityId : String
 *   role       : String  — "guest" | "admin" | "coadmin"
 *   joinedAt   : Long    — epoch millis
 * }
 */
@Singleton
class MembershipFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    private fun weddingsRef(identityId: String) =
        firestore.collection("memberships").document(identityId).collection("weddings")

    /** Inserts or updates the membership record (merge so extra fields are preserved). */
    suspend fun addMembership(membership: WeddingMembership): Result<Unit> = runCatching {
        weddingsRef(membership.identityId)
            .document(membership.weddingId)
            .set(
                mapOf(
                    "weddingId" to membership.weddingId,
                    "identityId" to membership.identityId,
                    "role" to membership.role,
                    "joinedAt" to membership.joinedAt,
                ),
                SetOptions.merge(),
            ).await()
    }

    /** Returns all wedding memberships for [identityId]. Empty list on failure. */
    suspend fun getMemberships(identityId: String): Result<List<WeddingMembership>> =
        runCatching {
            weddingsRef(identityId).get().await().documents.mapNotNull { doc ->
                runCatching {
                    WeddingMembership(
                        weddingId = doc.getString("weddingId") ?: doc.id,
                        identityId = doc.getString("identityId") ?: identityId,
                        role = doc.getString("role") ?: "guest",
                        joinedAt = doc.getLong("joinedAt") ?: 0L,
                    )
                }.getOrNull()
            }
        }

    /** Removes the membership entry when a user leaves a wedding. */
    suspend fun removeMembership(identityId: String, weddingId: String): Result<Unit> =
        runCatching {
            weddingsRef(identityId).document(weddingId).delete().await()
        }

    /** Updates the role on an existing entry (e.g. GUEST → COADMIN). */
    suspend fun updateRole(
        identityId: String,
        weddingId: String,
        newRole: String,
    ): Result<Unit> = runCatching {
        weddingsRef(identityId).document(weddingId).update("role", newRole).await()
    }
}
