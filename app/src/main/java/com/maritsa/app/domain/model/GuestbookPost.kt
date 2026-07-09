package com.maritsa.app.domain.model

data class GuestbookPost(
    val id: String = "",
    val guestId: String = "",
    val senderName: String = "",
    val message: String = "",
    val photoUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val ownerUserId: String = "",     // legacy: Firebase Auth UID (may be "" for anonymous)
    val updatedAt: Long = 0L,
    /**
     * New unified ownership field.  Set on every write:
     *  - Authenticated user  → Firebase UID  (same value as [ownerUserId])
     *  - Anonymous guest     → guestId UUID
     *
     * [ContentPermissions] checks this field first, falling back to [ownerUserId]
     * for documents created before this field was introduced.
     */
    val ownerIdentityId: String = "",
)
