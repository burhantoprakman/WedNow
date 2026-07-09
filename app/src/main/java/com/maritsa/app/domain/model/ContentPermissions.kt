package com.maritsa.app.domain.model

/**
 * Stateless permission helpers for user-created content.
 *
 * The preferred API uses [ownerIdentityId] which works for both guest
 * (UUID) and authenticated (Firebase UID) users.  The legacy methods
 * that accept [ownerUserId] / [currentUserId] are preserved for backward
 * compatibility with code that has not yet been migrated.
 *
 * Migration path: replace
 *   canEditPhoto(photo.ownerUserId, currentUserId)
 * with
 *   canEdit(photo.effectiveOwnerId, identityManager.currentIdentityId)
 */
object ContentPermissions {

    // ── New unified API (uses ownerIdentityId) ────────────────────────────────

    /**
     * True if [currentIdentityId] (guest UUID or Firebase UID) matches [ownerIdentityId].
     *
     * Works for both anonymous guests and authenticated users because every new
     * content write includes [ownerIdentityId] = the creator's current identity ID.
     */
    fun canEdit(ownerIdentityId: String, currentIdentityId: String): Boolean =
        ownerIdentityId.isNotBlank() && ownerIdentityId == currentIdentityId

    /**
     * True if the caller owns the content OR holds an elevated role in the wedding.
     *
     * [legacyOwnerId] provides a fallback for old documents where [ownerIdentityId]
     * is still blank (pre-migration records).  Pass [WeddingPhoto.uploadedBy] or
     * [GuestbookPost.guestId] as the legacy value.
     */
    fun canDelete(
        ownerIdentityId: String,
        currentIdentityId: String,
        role: String?,
        legacyOwnerId: String = "",
    ): Boolean {
        if (role == GuestRole.ADMIN || role == GuestRole.COADMIN) return true
        val effective = ownerIdentityId.ifBlank { legacyOwnerId }
        return effective.isNotBlank() && effective == currentIdentityId
    }

    /**
     * Returns the canonical owner ID for a piece of content, preferring the new
     * [ownerIdentityId] over the legacy [ownerUserId].
     *
     * Use this when you need a single identifier to pass to [canEdit] / [canDelete].
     */
    fun resolveOwnerId(ownerIdentityId: String, ownerUserId: String): String =
        ownerIdentityId.ifBlank { ownerUserId }

    // ── Legacy API (uses Firebase UID only) ───────────────────────────────────
    //
    // These are kept for backward compatibility.  New code should use the
    // unified API above.

    fun canEditPhoto(ownerUserId: String, currentUserId: String?): Boolean =
        !currentUserId.isNullOrBlank() && ownerUserId == currentUserId

    fun canDeletePhoto(
        ownerUserId: String,
        currentUserId: String?,
        guestRole: String?,
    ): Boolean = !currentUserId.isNullOrBlank() &&
            (ownerUserId == currentUserId ||
                    guestRole == GuestRole.ADMIN ||
                    guestRole == GuestRole.COADMIN)

    fun canEditPost(ownerUserId: String, currentUserId: String?): Boolean =
        !currentUserId.isNullOrBlank() && ownerUserId == currentUserId

    fun canDeletePost(
        ownerUserId: String,
        currentUserId: String?,
        guestRole: String?,
    ): Boolean = !currentUserId.isNullOrBlank() &&
            (ownerUserId == currentUserId ||
                    guestRole == GuestRole.ADMIN ||
                    guestRole == GuestRole.COADMIN)
}
