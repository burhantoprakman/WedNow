package com.wednowapp.wednow.domain.model

data class WeddingPhoto(
    val id: String = "",
    val imageUrl: String = "",
    /** Internal guestId (UUID) — used for identity/logic, never displayed in UI. */
    val uploadedBy: String = "",
    /** Human-readable display name shown in the gallery. Falls back to "" which the UI shows as nothing. */
    val senderName: String = "",
    val timestamp: Long = 0L,
    /** Cached Firestore count — kept in sync via FieldValue.increment. */
    val likeCount: Int = 0,
    /** List of guestIds who have liked this photo — drives isLiked state per guest. */
    val likedBy: List<String> = emptyList(),
)
