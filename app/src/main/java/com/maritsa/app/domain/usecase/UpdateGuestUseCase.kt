package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import javax.inject.Inject

class UpdateGuestUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    suspend operator fun invoke(weddingId: String, guest: Guest): Result<Unit> =
        repository.updateGuest(weddingId, guest)
}
