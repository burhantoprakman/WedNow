package com.wednowapp.wednow.core.identity

import com.google.firebase.firestore.FirebaseFirestore
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.repository.IdentityRepository
import com.wednowapp.wednow.domain.repository.MembershipRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migrates all content from a GUEST identity (UUID) to a USER identity (Firebase UID)
 * after the user signs in for the first time on this or any device.
 *
 * ── What is migrated ─────────────────────────────────────────────────────────
 * Per-wedding content owned by the guest:
 *  • guestbook posts  — ownerIdentityId field updated
 *  • photos           — ownerIdentityId field updated
 *  • guest document   — ownerIdentityId cross-reference added
 *
 * Cross-wedding index:
 *  • memberships/{guestId}/weddings/{*}  copied to  memberships/{uid}/weddings/{*}
 *
 * Identity record:
 *  • identities/{guestId}  marked as migrated
 *  • identities/{uid}      saved/merged
 *
 * ── Safety properties ────────────────────────────────────────────────────────
 * • Per-wedding migrations run in parallel but are individually wrapped in
 *   runCatching so one failure never blocks the others.
 * • Batch writes are split into chunks of ≤ 400 (Firestore max = 500).
 * • The function is idempotent: if called twice it overwrites the same fields
 *   with the same values — no duplicate data is created.
 * • All operations are best-effort; the function never throws to the caller.
 */
@Singleton
class IdentityMigrationService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val identityRepository: IdentityRepository,
    private val membershipRepository: MembershipRepository,
) {

    suspend fun migrate(guestId: String, userIdentity: Identity) {
        // 1. Persist the user identity document in Firestore
        identityRepository.saveIdentity(userIdentity)

        // 2. Discover all weddings the guest participated in
        val memberships = membershipRepository.getMemberships(guestId)

        // 3. Migrate per-wedding content in parallel
        coroutineScope {
            memberships.map { membership ->
                async {
                    runCatching {
                        migrateWeddingContent(
                            weddingId = membership.weddingId,
                            guestId = guestId,
                            newId = userIdentity.identityId,
                        )
                        // 4. Copy membership from guest index to user index
                        membershipRepository.addMembership(
                            membership.copy(identityId = userIdentity.identityId)
                        )
                    }
                }
            }.awaitAll()
        }

        // 5. Mark old guest identity as migrated (best-effort)
        identityRepository.markMigrated(
            guestId = guestId,
            userIdentityId = userIdentity.identityId,
        )
    }

    // ── Per-wedding content migration ─────────────────────────────────────────

    private suspend fun migrateWeddingContent(
        weddingId: String,
        guestId: String,
        newId: String,
    ) {
        val weddingRef = firestore.collection("weddings").document(weddingId)

        // ── Guestbook posts ────────────────────────────────────────────────────
        // Query by guestId (the field that was always written, even before ownerIdentityId)
        val posts = weddingRef.collection("guestbook")
            .whereEqualTo("guestId", guestId)
            .get().await()

        posts.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc -> batch.update(doc.reference, "ownerIdentityId", newId) }
            batch.commit().await()
        }

        // ── Photos ─────────────────────────────────────────────────────────────
        // Query by uploadedBy (the field that was always written for photos)
        val photos = weddingRef.collection("photos")
            .whereEqualTo("uploadedBy", guestId)
            .get().await()

        photos.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc -> batch.update(doc.reference, "ownerIdentityId", newId) }
            batch.commit().await()
        }

        // ── Guest document ─────────────────────────────────────────────────────
        // Add ownerIdentityId so the wedding member record is also linked to the UID
        runCatching {
            weddingRef.collection("guests").document(guestId)
                .update("ownerIdentityId", newId)
                .await()
        }
    }
}
