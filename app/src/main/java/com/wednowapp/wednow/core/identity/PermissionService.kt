package com.wednowapp.wednow.core.identity

import com.wednowapp.wednow.domain.model.ContentPermissions
import com.wednowapp.wednow.domain.model.GuestRole
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises all permission decisions that involve the current [Identity].
 *
 * Inject this service into ViewModels instead of calling [ContentPermissions]
 * directly so callers do not need to read [IdentityManager.currentIdentityId]
 * themselves.
 *
 * ── Protected-action gate ─────────────────────────────────────────────────
 * [requiresAuth] returns true when the caller must sign in before proceeding.
 * Pair this with [IdentityViewModel.requestProtectedAction] for the full UX flow.
 */
@Singleton
class PermissionService @Inject constructor(
    private val identityManager: IdentityManager,
) {

    // ── Content-level permissions ─────────────────────────────────────────────

    /**
     * True if the current identity owns [ownerIdentityId].
     *
     * [legacyOwnerId] is a fallback for old documents where [ownerIdentityId]
     * is blank (written before the field existed).  Pass [WeddingPhoto.uploadedBy]
     * or [GuestbookPost.guestId] as appropriate.
     */
    fun canEdit(ownerIdentityId: String, legacyOwnerId: String = ""): Boolean {
        val effective = ownerIdentityId.ifBlank { legacyOwnerId }
        return ContentPermissions.canEdit(effective, identityManager.currentIdentityId)
    }

    /**
     * True if the current identity owns the content OR holds ADMIN / COADMIN role.
     */
    fun canDelete(
        ownerIdentityId: String,
        role: String?,
        legacyOwnerId: String = "",
    ): Boolean = ContentPermissions.canDelete(
        ownerIdentityId = ownerIdentityId,
        currentIdentityId = identityManager.currentIdentityId,
        role = role,
        legacyOwnerId = legacyOwnerId,
    )

    // ── Action-level (auth) gates ─────────────────────────────────────────────

    /** True when the caller must authenticate before the action is allowed. */
    val requiresAuth: Boolean get() = !identityManager.isAuthenticated

    fun canCreateWedding(): Boolean = identityManager.isAuthenticated
    fun canUploadPhoto(): Boolean = identityManager.isAuthenticated
    fun canWriteGuestbook(): Boolean = identityManager.isAuthenticated
    fun canAdminister(): Boolean = identityManager.isAuthenticated

    // ── Role-level gates (used inside a wedding) ──────────────────────────────

    fun isAdmin(role: String?): Boolean = role == GuestRole.ADMIN
    fun isCoAdmin(role: String?): Boolean = role == GuestRole.COADMIN || role == GuestRole.ADMIN
    fun isElevated(role: String?): Boolean = role == GuestRole.ADMIN || role == GuestRole.COADMIN
}
