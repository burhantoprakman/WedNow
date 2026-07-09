package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.repository.GuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UpdateRsvpUseCase @Inject constructor(
    private val repository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String, status: String): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        return repository.updateRsvp(weddingId, guestId, status)
    }
}
