package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestByIdUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    operator fun invoke(weddingId: String, guestId: String): Flow<Guest?> =
        repository.getGuestById(weddingId, guestId)
}
