package com.wednowapp.wednow.domain.model

/**
 * Represents an authenticated user returned by Firebase Auth.
 * [provider] is the Firebase provider ID string: "google.com" or "apple.com".
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val provider: String,
)
