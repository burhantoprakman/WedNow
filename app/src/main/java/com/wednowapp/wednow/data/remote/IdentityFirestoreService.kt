package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wednowapp.wednow.domain.model.AuthProvider
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.model.IdentityType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the  identities/{identityId}  collection in Firestore.
 *
 * This collection is separate from (and complementary to) the legacy
 * users/{uid} collection that maps Firebase Auth UIDs to guestIds.
 * Both collections coexist during the transition period.
 *
 * Document schema:
 * {
 *   identityId    : String   — same as doc ID
 *   type          : String   — "GUEST" | "USER"
 *   provider      : String   — "NONE" | "GOOGLE" | "APPLE"
 *   displayName   : String
 *   email         : String
 *   photoUrl      : String
 *   createdAt     : Long
 *   linkedGuestId : String   — non-empty for USER docs; the UUID this was migrated from
 *   migratedTo    : String   — non-empty for GUEST docs; the UID it was merged into
 *   migratedAt    : Long
 * }
 */
@Singleton
class IdentityFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    private val col = firestore.collection("identities")

    suspend fun saveIdentity(identity: Identity): Result<Unit> = runCatching {
        col.document(identity.identityId).set(
            mapOf(
                "identityId" to identity.identityId,
                "type" to identity.type.name,
                "provider" to identity.provider.name,
                "displayName" to (identity.displayName ?: ""),
                "email" to (identity.email ?: ""),
                "photoUrl" to (identity.photoUrl ?: ""),
                "createdAt" to identity.createdAt,
                "linkedGuestId" to (identity.linkedGuestId ?: ""),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun getIdentity(identityId: String): Result<Identity?> = runCatching {
        val doc = col.document(identityId).get().await()
        if (!doc.exists()) return@runCatching null
        Identity(
            identityId = doc.getString("identityId") ?: identityId,
            type = IdentityType.valueOf(
                doc.getString("type") ?: IdentityType.GUEST.name
            ),
            provider = AuthProvider.valueOf(
                doc.getString("provider") ?: AuthProvider.NONE.name
            ),
            displayName = doc.getString("displayName"),
            email = doc.getString("email"),
            photoUrl = doc.getString("photoUrl"),
            createdAt = doc.getLong("createdAt") ?: 0L,
            linkedGuestId = doc.getString("linkedGuestId"),
        )
    }

    /** Marks a GUEST identity document as having been merged into [userIdentityId]. */
    suspend fun markMigrated(guestId: String, userIdentityId: String) {
        runCatching {
            col.document(guestId).set(
                mapOf(
                    "migratedTo" to userIdentityId,
                    "migratedAt" to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            ).await()
        } // best-effort; never surface to UI
    }
}
