package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.GuestRepository
import com.maritsa.app.domain.repository.WeddingRepository
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
        date: Long,
        location: String,
        adminGuestId: String,
        coverImageUrl: String = "",
        menu: List<com.maritsa.app.domain.model.MenuCourseData> = emptyList(),
        dressCode: com.maritsa.app.domain.model.DressCodeData = com.maritsa.app.domain.model.DressCodeData(),
        timeline: List<com.maritsa.app.domain.model.TimelineEventData> = emptyList(),
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
