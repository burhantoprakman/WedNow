package com.maritsa.app.domain.model

/**
 * Cross-device preferences for any identity.
 *
 * GUEST identities — stored locally in SharedPreferences only.
 * USER identities  — mirrored to Firestore under  userPreferences/{identityId}
 *                    so the data survives a fresh install and is restored after
 *                    the first sign-in on a new device.
 */
data class UserPreferences(
    val identityId: String = "",
    /** The wedding the user was last viewing; used for auto-open on launch. */
    val lastActiveWeddingId: String? = null,
    val updatedAt: Long = 0L,
)
