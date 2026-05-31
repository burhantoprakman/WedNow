package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.WeddingRepository
import javax.inject.Inject

private val SHORT_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no confusable 0/1/I/O

private fun generateShortCode(): String =
    (1..6).map { SHORT_CODE_CHARS.random() }.joinToString("")

class CreateWeddingUseCase @Inject constructor(
    private val weddingRepository: WeddingRepository,
    private val guestRepository: GuestRepository,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase
) {
    suspend operator fun invoke(
        name: String,
        date: String,
        location: String,
        adminGuestId: String,
        coverImageUrl: String = "",
        menu: List<com.wednowapp.wednow.domain.model.MenuCourseData> = emptyList(),
        dressCode: com.wednowapp.wednow.domain.model.DressCodeData = com.wednowapp.wednow.domain.model.DressCodeData(),
        timeline: List<com.wednowapp.wednow.domain.model.TimelineEventData> = emptyList(),
    ): Result<String> {
        val weddingId = weddingRepository.createWedding(
            Wedding(
                shortCode = generateShortCode(),
                name = name,
                date = date,
                location = location,
                adminGuestId = adminGuestId,
                createdAt = System.currentTimeMillis(),
                coverImageUrl = coverImageUrl,
                menu = menu,
                dressCode = dressCode,
                timeline = timeline,
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
