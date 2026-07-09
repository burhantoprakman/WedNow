package com.maritsa.app.core.identity

import com.google.firebase.firestore.FirebaseFirestore
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.Identity
import com.maritsa.app.domain.repository.IdentityRepository
import com.maritsa.app.domain.repository.MembershipRepository
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
 *  • guest document   — ownerIdentityId cross-reference added; role promoted to
 *                       ADMIN if this user is determined to be the wedding creator
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
                        val promotedToAdmin = migrateWeddingContent(
                            weddingId = membership.weddingId,
                            guestId = guestId,
                            newId = userIdentity.identityId,
                        )
                        // 4. Copy membership from guest index to user index,
                        //    preserving the promoted role if applicable.
                        val newRole = if (promotedToAdmin) GuestRole.ADMIN else membership.role
                        membershipRepository.addMembership(
                            membership.copy(
                                identityId = userIdentity.identityId,
                                role = newRole,
                            )
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

    // Returns true if this user was determined to be the wedding admin, so the
    // caller can also promote the membership role in the cross-wedding index.
    private suspend fun migrateWeddingContent(
        weddingId: String,
        guestId: String,
        newId: String,
    ): Boolean {
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
        // Link the guest document to the Firebase UID. Also promotes role → ADMIN
        // when this user is the wedding creator. Two cases:
        //   (a) Same device: adminGuestId == guestId (UUID created this wedding here)
        //   (b) Cross-device: the original admin UUID's guest doc already has
        //       ownerIdentityId == newId (admin signed in on another device first)
        return runCatching {
            val guestRef = weddingRef.collection("guests").document(guestId)
            val weddingDoc = weddingRef.get().await()
            val adminGuestId = weddingDoc.getString("adminGuestId") ?: ""

            val isAdmin = when {
                adminGuestId == guestId -> true
                adminGuestId.isNotEmpty() -> {
                    val adminGuestDoc = weddingRef.collection("guests")
                        .document(adminGuestId).get().await()
                    adminGuestDoc.getString("ownerIdentityId") == newId
                }

                else -> false
            }

            val updateMap = mutableMapOf<String, Any?>("ownerIdentityId" to newId)
            if (isAdmin) updateMap["role"] = GuestRole.ADMIN

            guestRef.update(updateMap).await()
            isAdmin
        }.getOrDefault(false)
    }
}
