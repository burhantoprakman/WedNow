package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.repository.PhotoRepository
import javax.inject.Inject

class UpdatePhotoCaptionUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(weddingId: String, photoId: String, caption: String): Result<Unit> =
        repository.updateCaption(weddingId, photoId, caption)
}
