package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.repository.PhotoRepository
import javax.inject.Inject

class ToggleLikeUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit> = repository.toggleLike(weddingId, photoId, guestId, isCurrentlyLiked)
}
