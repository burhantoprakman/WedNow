package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.data.remote.AuthFirestoreService
import com.maritsa.app.domain.model.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Called immediately after a successful sign-in.
 *
 * Strategy:
 * 1. Look up whether this Firebase UID was previously linked to a guestId (cross-device recovery).
 * 2a. If yes → adopt that guestId so the user's existing RSVP / photos / messages are preserved.
 * 2b. If no  → link the current device's anonymous guestId to this UID (first sign-in).
 * 3. Persist the resolved guestId + display name locally for offline use.
 */
class LinkGuestToAuthUseCase @Inject constructor(
    private val authFirestoreService: AuthFirestoreService,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(user: AuthUser) {
        val deviceGuestId = GuestSessionManager.getGuestId(context)

        // Check for an existing guestId linked to this Firebase UID (another device)
        val existingGuestId = authFirestoreService.getGuestIdForUid(user.uid)
        val resolvedGuestId = existingGuestId ?: deviceGuestId

        // If a different guestId was found for this UID, adopt it on this device
        if (existingGuestId != null && existingGuestId != deviceGuestId) {
            GuestSessionManager.saveGuestId(context, existingGuestId)
        }

        // Persist uid → guestId mapping in Firestore (idempotent / merge)
        authFirestoreService.linkUidToGuestId(user.uid, resolvedGuestId, user)

        // Update local display name so photo/guestbook uploads show the correct name
        if (!user.displayName.isNullOrBlank()) {
            GuestSessionManager.saveGuestName(context, user.displayName)
        }
    }
}
