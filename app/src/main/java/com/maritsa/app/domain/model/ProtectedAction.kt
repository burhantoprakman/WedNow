package com.maritsa.app.domain.model

/**
 * An action that requires a USER identity (Firebase authentication).
 *
 * When an anonymous guest attempts one of these actions the app:
 *  1. Records the intent in [IdentityViewModel.pendingAction].
 *  2. Shows the sign-in sheet with a contextual reason string.
 *  3. On successful sign-in, resumes the original action automatically via
 *     [IdentityViewModel.consumePendingAction].
 *
 * This covers every "protected" gate listed in the product spec.
 */
enum class ProtectedAction {
    CREATE_WEDDING,
    UPLOAD_PHOTO,
    CREATE_GUESTBOOK_ENTRY,
    ADMIN_ACTION,
    COADMIN_ACTION,
}

/** Human-readable reason string shown in the sign-in bottom sheet. */
fun ProtectedAction.signInReason(): String = when (this) {
    ProtectedAction.CREATE_WEDDING -> "Sign in to create your wedding."
    ProtectedAction.UPLOAD_PHOTO -> "Sign in to share photos."
    ProtectedAction.CREATE_GUESTBOOK_ENTRY -> "Sign in to leave a guestbook message."
    ProtectedAction.ADMIN_ACTION -> "Sign in to perform admin actions."
    ProtectedAction.COADMIN_ACTION -> "Sign in to use co-admin features."
}
