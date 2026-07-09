package com.maritsa.app.domain.model

/** Distinguishes an anonymous installation from a signed-in account. */
enum class IdentityType { GUEST, USER }

/** Which OAuth provider backs a USER identity. */
enum class AuthProvider { NONE, GOOGLE, APPLE }

/**
 * The canonical, persistent identifier for a person using the app.
 *
 * Every installation starts with a GUEST identity whose [identityId] is a
 * device-local UUID.  When the person authenticates with Google or Apple the
 * identity is upgraded to USER and [identityId] becomes the Firebase Auth UID,
 * which is stable across all their devices.
 *
 * All Firestore documents that represent user-created content store
 * [identityId] in the [ownerIdentityId] field so ownership can be verified
 * without requiring authentication (guest content is owned by guestId).
 */
data class Identity(
    /** Stable, opaque identifier.  UUID for GUEST; Firebase UID for USER. */
    val identityId: String,
    val type: IdentityType,
    val provider: AuthProvider,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /**
     * For USER identities: the guest UUID that was active just before this
     * sign-in occurred.  [IdentityMigrationService] uses it to reattach content.
     */
    val linkedGuestId: String? = null,
) {
    val isAuthenticated: Boolean get() = type == IdentityType.USER
    val isGuest: Boolean get() = type == IdentityType.GUEST
}
