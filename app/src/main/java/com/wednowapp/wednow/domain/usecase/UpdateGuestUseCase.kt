package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.repository.GuestRepository
import javax.inject.Inject

class UpdateGuestUseCase @Inject constructor(
    private val repository: GuestRepository
) {
    suspend operator fun invoke(weddingId: String, guest: Guest): Result<Unit> =
        repository.updateGuest(weddingId, guest)
}
