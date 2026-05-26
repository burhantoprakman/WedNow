package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestByIdUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    operator fun invoke(weddingId: String, guestId: String): Flow<Guest?> =
        repository.getGuestById(weddingId, guestId)
}
