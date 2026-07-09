package com.maritsa.app.fake

import com.maritsa.app.domain.model.AuthProvider
import com.maritsa.app.domain.model.AuthUser
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.GuestbookPost
import com.maritsa.app.domain.model.Identity
import com.maritsa.app.domain.model.IdentityType
import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.model.WeddingMembership
import com.maritsa.app.domain.model.WeddingPhoto
import java.util.UUID

/**
 * Centralized test data factory.  Every helper returns a fresh object with
 * sensible defaults that can be overridden via named parameters.
 */
object TestData {

    // ── Identities ────────────────────────────────────────────────────────────

    fun guestIdentity(
        id: String = "guest-uuid-${UUID.randomUUID()}",
    ) = Identity(
        identityId = id,
        type = IdentityType.GUEST,
        provider = AuthProvider.NONE,
    )

    fun userIdentity(
        uid: String = "firebase-uid-${UUID.randomUUID()}",
        provider: AuthProvider = AuthProvider.GOOGLE,
        displayName: String = "Test User",
        email: String = "test@example.com",
        linkedGuestId: String? = null,
    ) = Identity(
        identityId = uid,
        type = IdentityType.USER,
        provider = provider,
        displayName = displayName,
        email = email,
        linkedGuestId = linkedGuestId,
    )

    fun authUser(
        uid: String = "firebase-uid-${UUID.randomUUID()}",
        displayName: String = "Test User",
        email: String = "test@example.com",
        provider: String = "google.com",
    ) = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = null,
        provider = provider,
    )

    // ── Weddings ──────────────────────────────────────────────────────────────

    fun wedding(
        id: String = "wedding-${UUID.randomUUID()}",
        shortCode: String = "ABC123",
        name: String = "Test Wedding",
        adminGuestId: String = "admin-guest-id",
    ) = Wedding(
        id = id,
        shortCode = shortCode,
        name = name,
        date = 1781481600000L, // 2026-06-15 midnight UTC
        location = "Test Venue",
        adminGuestId = adminGuestId,
        createdAt = System.currentTimeMillis(),
    )

    // ── Memberships ───────────────────────────────────────────────────────────

    fun membership(
        weddingId: String = "wedding-id",
        identityId: String = "identity-id",
        role: String = GuestRole.GUEST,
    ) = WeddingMembership(
        weddingId = weddingId,
        identityId = identityId,
        role = role,
        joinedAt = System.currentTimeMillis(),
    )

    // ── Photos ────────────────────────────────────────────────────────────────

    fun photo(
        id: String = "photo-${UUID.randomUUID()}",
        uploadedBy: String = "guest-uuid",
        ownerUserId: String = "",
        ownerIdentityId: String = "",
        weddingId: String = "wedding-id",
    ) = WeddingPhoto(
        id = id,
        imageUrl = "https://storage.fake/$weddingId/$id.jpg",
        uploadedBy = uploadedBy,
        senderName = "Test Guest",
        timestamp = System.currentTimeMillis(),
        ownerUserId = ownerUserId,
        ownerIdentityId = ownerIdentityId,
    )

    // ── Guestbook posts ───────────────────────────────────────────────────────

    fun guestbookPost(
        id: String = "post-${UUID.randomUUID()}",
        guestId: String = "guest-uuid",
        ownerUserId: String = "",
        ownerIdentityId: String = "",
        message: String = "Congratulations!",
    ) = GuestbookPost(
        id = id,
        guestId = guestId,
        senderName = "Test Guest",
        message = message,
        photoUrls = emptyList(),
        timestamp = System.currentTimeMillis(),
        ownerUserId = ownerUserId,
        ownerIdentityId = ownerIdentityId,
    )

    // ── Constants ─────────────────────────────────────────────────────────────

    const val WEDDING_A_ID = "wedding-alpha"
    const val WEDDING_B_ID = "wedding-beta"
    const val WEDDING_C_ID = "wedding-gamma"
    const val WEDDING_A_CODE = "ALPHA1"
    const val WEDDING_B_CODE = "BETA22"
    const val WEDDING_C_CODE = "GAMM33"
    const val GUEST_UUID = "guest-device-uuid-fixed"
    const val USER_UID = "firebase-user-uid-fixed"
    const val ADMIN_GUEST_ID = "admin-guest-id-fixed"
}
