package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.repository.PhotoRepository
import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(weddingId: String, photoId: String): Result<Unit> =
        repository.deletePhoto(weddingId, photoId)
}
