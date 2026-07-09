package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestsUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    operator fun invoke(weddingId: String): Flow<List<Guest>> =
        repository.getGuests(weddingId)
}
