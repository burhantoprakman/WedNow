package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.WeddingRepository
import javax.inject.Inject

class CreateWeddingUseCase @Inject constructor(
    private val weddingRepository: WeddingRepository,
    private val guestRepository: GuestRepository,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase
) {
    suspend operator fun invoke(
        name: String,
        date: String,
        location: String,
        adminGuestId: String
    ): Result<String> {
        val weddingId = weddingRepository.createWedding(
            Wedding(
                name = name,
                date = date,
                location = location,
                adminGuestId = adminGuestId,
                createdAt = System.currentTimeMillis()
            )
        ).getOrElse { return Result.failure(it) }

        // Creator is always the admin
        guestRepository.addGuest(
            weddingId,
            Guest(id = adminGuestId, role = GuestRole.ADMIN)
        ).getOrElse { return Result.failure(it) }

        saveFcmTokenUseCase(weddingId) // best-effort; ignore failure

        return Result.success(weddingId)
    }
}
