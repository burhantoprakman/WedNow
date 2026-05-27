package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.WeddingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class JoinWeddingUseCase @Inject constructor(
    private val weddingRepository: WeddingRepository,
    private val guestRepository: GuestRepository,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String, guestName: String?): Result<String> {
        val wedding = weddingRepository.getWeddingById(weddingId)
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(Exception("No wedding found for that code. Check the code and try again."))

        val guestId = GuestSessionManager.getGuestId(context)

        val role = if (wedding.adminGuestId == guestId) GuestRole.ADMIN else GuestRole.GUEST

        guestRepository.addGuest(
            wedding.id,
            Guest(id = guestId, name = guestName.orEmpty(), role = role)
        ).getOrElse { return Result.failure(it) }

        WeddingSessionManager.saveWeddingId(context, wedding.id)
        // Persist the display name locally so photo uploads can tag themselves
        // without an extra Firestore round-trip.
        GuestSessionManager.saveGuestName(context, guestName.orEmpty())
        saveFcmTokenUseCase(wedding.id) // best-effort; ignore failure

        return Result.success(wedding.id)
    }
}
