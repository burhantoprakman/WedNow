package com.maritsa.app.domain.model

/**
 * Records that a specific [Identity] belongs to a wedding with a given [role].
 *
 * Stored in Firestore at  memberships/{identityId}/weddings/{weddingId}
 *
 * This top-level index (separate from the per-wedding guests sub-collection)
 * enables an authenticated user to list all their weddings on a new device
 * immediately after sign-in — without iterating every wedding document.
 *
 * The per-wedding  weddings/{id}/guests/{guestId}  collection is unchanged and
 * continues to drive all in-wedding functionality.
 */
data class WeddingMembership(
    val weddingId: String = "",
    val identityId: String = "",
    val role: String = GuestRole.GUEST,
    val joinedAt: Long = 0L,
)
