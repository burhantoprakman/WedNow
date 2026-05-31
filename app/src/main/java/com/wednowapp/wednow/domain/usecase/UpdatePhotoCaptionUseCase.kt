package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.repository.PhotoRepository
import javax.inject.Inject

class UpdatePhotoCaptionUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(weddingId: String, photoId: String, caption: String): Result<Unit> =
        repository.updateCaption(weddingId, photoId, caption)
}
