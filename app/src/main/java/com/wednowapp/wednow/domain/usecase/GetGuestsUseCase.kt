package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestsUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    operator fun invoke(weddingId: String): Flow<List<Guest>> =
        repository.getGuests(weddingId)
}
