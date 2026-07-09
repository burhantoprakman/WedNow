package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentGuestUseCase @Inject constructor(
    private val repository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(weddingId: String): Flow<Guest?> {
        val guestId = GuestSessionManager.getGuestId(context)
        return repository.getGuestById(weddingId, guestId)
    }
}
